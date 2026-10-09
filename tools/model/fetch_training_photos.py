"""Downloads freely licensed iNaturalist photos to train the species recognition model.

For every species in species.json it takes research-grade observations whose photos carry a
Creative Commons licence (most iNaturalist photos are CC BY-NC; the photos are only used for
training and never shipped in the app), at most one photo per observation (so one animal is not counted
many times), and stores them as <out>/<species-id>/<observation>.jpg at 320 px.

Usage: python3 tools/model/fetch_training_photos.py <species.json> <out-dir> [photos-per-species]
"""
import concurrent.futures
import io
import json
import sys
import time
import urllib.parse
import urllib.request
from pathlib import Path

from PIL import Image

API = "https://api.inaturalist.org/v1"
HEADERS = {"User-Agent": "Nudidex/0.2 (github.com/aouarius/nudibranch)"}
SIZE = 320
LICENSES = ("cc0", "cc-by", "cc-by-sa", "cc-by-nc", "cc-by-nc-sa")


def get_json(path, **params):
    url = f"{API}/{path}?{urllib.parse.urlencode(params)}"
    for attempt in range(4):
        time.sleep(1.1)  # iNaturalist asks for at most ~1 request per second
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers=HEADERS), timeout=60) as r:
                return json.load(r)
        except Exception as error:
            print(f"retry {url}: {error}", file=sys.stderr)
            time.sleep(5 * (attempt + 1))
    raise RuntimeError(f"giving up on {url}")


def taxon_id(latin_name):
    results = get_json("taxa", q=latin_name, rank="species", per_page=10)["results"]
    exact = [t for t in results if t["name"].lower() == latin_name.lower()]
    return exact[0]["id"] if exact else None


def photo_urls(taxon, wanted):
    """(observation id, photo url) pairs, newest observations first."""
    found, id_below = [], None
    while len(found) < wanted:
        params = dict(taxon_id=taxon, photo_license=",".join(LICENSES), quality_grade="research",
                      per_page=200, order_by="id", order="desc")
        if id_below:
            params["id_below"] = id_below
        results = get_json("observations", **params)["results"]
        if not results:
            break
        for observation in results:
            photos = [p for p in observation.get("photos", [])
                      if (p.get("license_code") or "").lower() in LICENSES]
            if photos:
                found.append((observation["id"], photos[0]["url"].replace("/square.", "/medium.")))
        id_below = results[-1]["id"]
    return found[:wanted]


def download(url, target):
    if target.exists():
        return True
    try:
        with urllib.request.urlopen(urllib.request.Request(url, headers=HEADERS), timeout=60) as r:
            image = Image.open(io.BytesIO(r.read())).convert("RGB")
        image.thumbnail((SIZE, SIZE))
        image.save(target, quality=88)
        return True
    except Exception as error:
        print(f"{url}: {error}", file=sys.stderr)
        return False


def main():
    species_file, out = Path(sys.argv[1]), Path(sys.argv[2])
    wanted = int(sys.argv[3]) if len(sys.argv) > 3 else 300
    report = {}
    with concurrent.futures.ThreadPoolExecutor(8) as pool:
        for species in json.loads(species_file.read_text()):
            sid = species["id"]
            taxon = taxon_id(species["latinName"])
            if taxon is None:
                print(f"{sid}: taxon not found", file=sys.stderr)
                report[sid] = 0
                continue
            folder = out / sid
            folder.mkdir(parents=True, exist_ok=True)
            urls = photo_urls(taxon, wanted)
            ok = sum(pool.map(lambda pair: download(pair[1], folder / f"{pair[0]}.jpg"), urls))
            report[sid] = ok
            print(f"{sid}: {ok} photos")
    (out / "counts.json").write_text(json.dumps(report, indent=1))


if __name__ == "__main__":
    main()
