"""Trains the on-device species recognition model from the photos fetch_training_photos.py saved.

Transfer learning on MobileNetV3 (ImageNet weights): the network turns each photo into a
feature vector once (several augmented views per training photo, some tinted blue-green like
real dive photos), a small classifier learns the species from those vectors, and the two
are joined into one LiteRT model for the app.

Writes <out>/species_model.tflite, <out>/species_labels.txt and <out>/report.json.

Usage: python3 tools/model/train.py <photo-dir> <out-dir>
"""
import json
import sys
import zlib
from pathlib import Path

import numpy as np
import tensorflow as tf
import keras

SIZE = 224
TRAIN_VIEWS = 4  # one plain, three augmented views per training photo
VAL_SHARE = 0.15
SEED = 7


def split(photos):
    """Stable split by file name, so a photo never moves between training and validation."""
    train, val = [], []
    for photo in photos:
        (val if zlib.crc32(photo.name.encode()) % 1000 < VAL_SHARE * 1000 else train).append(photo)
    return train, val


def load(path):
    image = tf.io.decode_jpeg(tf.io.read_file(path), channels=3)
    return tf.cast(image, tf.float32)


def center(image):
    """Shorter side to SIZE, then the middle square: what the app does with a photo."""
    shape = tf.cast(tf.shape(image)[:2], tf.float32)
    scale = SIZE / tf.reduce_min(shape)
    resized = tf.image.resize(image, tf.cast(tf.round(shape * scale), tf.int32))
    return tf.image.resize_with_crop_or_pad(resized, SIZE, SIZE)


def augment(image, seed):
    """Random crop, flip, brightness and contrast, and sometimes the colour cast of deep water."""
    shape = tf.shape(image)
    side = tf.cast(tf.cast(tf.reduce_min(shape[:2]), tf.float32) * tf.random.stateless_uniform([], seed, 0.6, 1.0), tf.int32)
    image = tf.image.stateless_random_crop(image, [side, side, 3], seed)
    image = tf.image.resize(image, [SIZE, SIZE])
    image = tf.image.stateless_random_flip_left_right(image, seed)
    image = tf.image.stateless_random_brightness(image, 25.0, seed + 1)
    image = tf.image.stateless_random_contrast(image, 0.75, 1.25, seed + 2)
    tint = tf.random.stateless_uniform([], seed + 3, 0.0, 1.0)
    # Red fades first under water: weaken red and lift blue-green in about a third of the views.
    cast = tf.constant([0.65, 1.0, 1.12]) * tf.cast(tint < 0.35, tf.float32) + tf.cast(tint >= 0.35, tf.float32)
    return tf.clip_by_value(image * cast, 0.0, 255.0)


def features(base, paths, views):
    ds = tf.data.Dataset.from_tensor_slices([str(p) for p in paths]).map(load, num_parallel_calls=tf.data.AUTOTUNE)
    out = []
    for view in range(views):
        if view == 0:
            mapped = ds.map(center, num_parallel_calls=tf.data.AUTOTUNE)
        else:
            indexed = ds.enumerate()
            mapped = indexed.map(
                lambda i, img, v=view: augment(img, tf.stack([tf.cast(i, tf.int32), tf.constant(v * 1000 + SEED)])),
                num_parallel_calls=tf.data.AUTOTUNE,
            )
        out.append(base.predict(mapped.batch(64).prefetch(2), verbose=0))
        print(f"  view {view + 1}/{views}: {len(paths)} photos", flush=True)
    return np.concatenate(out)


def top_k(probabilities, labels, k):
    best = np.argsort(-probabilities, axis=1)[:, :k]
    return float(np.mean([label in row for label, row in zip(labels, best)]))


def main():
    photo_dir, out = Path(sys.argv[1]), Path(sys.argv[2])
    out.mkdir(parents=True, exist_ok=True)
    species = sorted(d.name for d in photo_dir.iterdir() if d.is_dir() and any(d.glob("*.jpg")))
    train_paths, train_labels, val_paths, val_labels = [], [], [], []
    for index, sid in enumerate(species):
        train, val = split(sorted((photo_dir / sid).glob("*.jpg")))
        if not val:  # very rare species: keep at least one photo for checking
            val, train = train[-1:], train[:-1]
        train_paths += train
        train_labels += [index] * len(train)
        val_paths += val
        val_labels += [index] * len(val)
    print(f"{len(species)} species, {len(train_paths)} training and {len(val_paths)} validation photos", flush=True)

    base = keras.applications.MobileNetV3Large(
        input_shape=(SIZE, SIZE, 3), include_top=False, pooling="avg", weights="imagenet", include_preprocessing=True,
    )
    base.trainable = False
    print("Extracting training features", flush=True)
    x_train = features(base, train_paths, TRAIN_VIEWS)
    y_train = np.tile(np.array(train_labels), TRAIN_VIEWS)
    print("Extracting validation features", flush=True)
    x_val = features(base, val_paths, 1)
    y_val = np.array(val_labels)

    counts = np.bincount(y_train, minlength=len(species))
    class_weight = {i: float(len(y_train) / (len(species) * max(c, 1))) for i, c in enumerate(counts)}
    head = keras.Sequential([
        keras.Input(shape=(x_train.shape[1],)),
        keras.layers.Dropout(0.4),
        keras.layers.Dense(len(species), activation="softmax", kernel_regularizer=keras.regularizers.l2(1e-4)),
    ])
    head.compile(optimizer=keras.optimizers.Adam(1e-3), loss="sparse_categorical_crossentropy", metrics=["accuracy"])
    head.fit(
        x_train, y_train, validation_data=(x_val, y_val), epochs=60, batch_size=128, class_weight=class_weight,
        callbacks=[keras.callbacks.EarlyStopping(patience=8, restore_best_weights=True)], verbose=2,
    )
    probabilities = head.predict(x_val, verbose=0)
    report = {
        "species": len(species),
        "trainPhotos": len(train_paths),
        "valPhotos": len(val_paths),
        "top1": top_k(probabilities, y_val, 1),
        "top3": top_k(probabilities, y_val, 3),
        "perSpecies": {
            sid: {
                "photos": int(np.sum(np.array(train_labels) == i) + np.sum(y_val == i)),
                "top3": top_k(probabilities[y_val == i], y_val[y_val == i], 3) if np.any(y_val == i) else None,
            }
            for i, sid in enumerate(species)
        },
    }
    print(f"Validation: top-1 {report['top1']:.3f}, top-3 {report['top3']:.3f}", flush=True)

    # One model for the app: photo in (224 x 224 RGB, values 0-255), probabilities out.
    image = keras.Input(shape=(SIZE, SIZE, 3), name="image")
    model = keras.Model(image, head(base(image), training=False))
    saved = out / "saved_model"
    model.export(str(saved), format="tf_saved_model")
    converter = tf.lite.TFLiteConverter.from_saved_model(str(saved))
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite = converter.convert()
    (out / "species_model.tflite").write_bytes(tflite)
    (out / "species_labels.txt").write_text("\n".join(species) + "\n")

    # The converted model must agree with the Keras one.
    interpreter = tf.lite.Interpreter(model_content=tflite)
    interpreter.allocate_tensors()
    inp, outp = interpreter.get_input_details()[0], interpreter.get_output_details()[0]
    agree = 0
    checks = val_paths[:: max(1, len(val_paths) // 50)]
    for path in checks:
        x = center(load(str(path)))[None].numpy()
        interpreter.set_tensor(inp["index"], x)
        interpreter.invoke()
        agree += int(np.argmax(interpreter.get_tensor(outp["index"])) == np.argmax(model.predict(x, verbose=0)))
    report["tfliteAgreement"] = agree / len(checks)
    report["modelBytes"] = len(tflite)
    (out / "report.json").write_text(json.dumps(report, indent=1))
    print(f"LiteRT agrees on {agree}/{len(checks)}, model {len(tflite) / 1e6:.1f} MB", flush=True)


if __name__ == "__main__":
    main()
