"""Turns Alex's icon picture (tools/launcher_icon_source.jpg) into the Android launcher icon.

The picture shows the icon as a blue rounded square on a dark backdrop. The script cuts
the nudibranch and the "NUDIDEX" lettering out of the square's blue gradient, so Android
can put them on its own icon shape (circle, squircle ...) over the same gradient.

Usage: python3 tools/import_launcher_icon.py <repo-root> [<preview.png>]
"""
import os
import sys

from PIL import Image, ImageDraw

ROOT = sys.argv[1]
SOURCE = os.path.join(ROOT, "tools", "launcher_icon_source.jpg")
RES = os.path.join(ROOT, "app", "src", "main", "res")

# Part of the square that holds the nudibranch and the lettering; the rim and the
# glossy highlight around it stay out.
INNER = (560, 265, 840, 535)
# Columns at the left and right of the square that hold only the gradient.
EDGE_COLUMNS = list(range(520, 560)) + list(range(850, 890))
LAYER = 432  # 108 dp at xxxhdpi
CONTENT_WIDTH = 0.52  # share of the layer the artwork spans; keeps it inside every launcher mask
TOP, BOTTOM = (52, 117, 150), (17, 45, 67)


def median(values):
    values = sorted(values)
    return values[len(values) // 2]


def cut_out(img):
    """Artwork with transparent background, colors freed from the blue that shone through."""
    left, top, right, bottom = INNER
    out = Image.new("RGBA", (right - left, bottom - top))
    for y in range(top, bottom):
        row = [img.getpixel((x, y)) for x in EDGE_COLUMNS]
        bg = tuple(median([p[c] for p in row]) for c in range(3))
        for x in range(left, right):
            p = img.getpixel((x, y))
            diff = max(abs(p[c] - bg[c]) for c in range(3))
            a = max(0.0, min(1.0, (diff - 30) / 60))
            if a == 0:
                continue
            color = tuple(max(0, min(255, round((p[c] - (1 - a) * bg[c]) / a))) for c in range(3))
            out.putpixel((x - left, y - top), color + (round(a * 255),))
    return out.crop(out.getbbox())


def main():
    img = Image.open(SOURCE).convert("RGB")
    art = cut_out(img)
    width = round(LAYER * CONTENT_WIDTH)
    art = art.resize((width, round(art.height * width / art.width)), Image.LANCZOS)

    foreground = Image.new("RGBA", (LAYER, LAYER))
    foreground.alpha_composite(art, ((LAYER - art.width) // 2, (LAYER - art.height) // 2))
    nodpi = os.path.join(RES, "drawable-nodpi")
    os.makedirs(nodpi, exist_ok=True)
    foreground.save(os.path.join(nodpi, "ic_launcher_foreground.png"), optimize=True)

    # Themed (monochrome) icon: Android tints the shape itself, so only the outline of the artwork counts.
    white = Image.new("RGBA", (LAYER, LAYER), (255, 255, 255, 255))
    white.putalpha(foreground.getchannel("A"))
    white.save(os.path.join(nodpi, "ic_launcher_monochrome.png"), optimize=True)

    if len(sys.argv) > 2:
        preview = Image.new("RGBA", (LAYER, LAYER))
        for y in range(LAYER):
            t = y / (LAYER - 1)
            color = tuple(round(TOP[c] + (BOTTOM[c] - TOP[c]) * t) for c in range(3)) + (255,)
            for x in range(LAYER):
                preview.putpixel((x, y), color)
        preview.alpha_composite(foreground)
        mask = Image.new("L", (LAYER, LAYER))
        ImageDraw.Draw(mask).rounded_rectangle((36, 36, LAYER - 37, LAYER - 37), radius=80, fill=255)
        shown = Image.new("RGBA", (LAYER, LAYER), (22, 24, 29, 255))
        shown.paste(preview, (0, 0), mask)
        circle = Image.new("L", (LAYER, LAYER))
        ImageDraw.Draw(circle).ellipse((36, 36, LAYER - 37, LAYER - 37), fill=255)
        round_icon = Image.new("RGBA", (LAYER, LAYER), (22, 24, 29, 255))
        round_icon.paste(preview, (0, 0), circle)
        sheet = Image.new("RGBA", (LAYER * 2, LAYER), (22, 24, 29, 255))
        sheet.paste(shown, (0, 0))
        sheet.paste(round_icon, (LAYER, 0))
        sheet.save(sys.argv[2])


main()
