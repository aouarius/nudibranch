"""Download freely licensed iNaturalist photos for every species in species.json.

For each species it saves up to N candidate photos (CC0, CC BY or CC BY-SA only)
plus a candidates.json with the credit line and source page of each photo.
Pick one per species, copy it to app/src/main/assets/species_photos/<id>.jpg and
put its credit into species.json.

Usage: python3 tools/fetch_species_photos.py <species.json> <out-dir> [candidates-per-species]
"""
import json
import sys
import time
import urllib.parse
import urllib.request
from pathlib import Path

API = "https://api.inaturalist.org/v1"
FREE_LICENSES = {"cc0", "cc-by", "cc-by-sa"}
HEADERS = {"User-Agent": "Nudidex/0.1 (github.com/aouarius/nudibranch)"}


def get_json(path, **params):
    url = f"{API}/{path}?{urllib.parse.urlencode(params)}"
    time.sleep(1.1)  # iNaturalist asks for at most ~1 request per second
    with urllib.request.urlopen(urllib.request.Request(url, headers=HEADERS), timeout=60) as r:
        return json.load(r)


def download(url, target):
    with urllib.request.urlopen(urllib.request.Request(url, headers=HEADERS), timeout=60) as r:
        target.write_bytes(r.read())


def find_taxon(latin_name):
    results = get_json("taxa", q=latin_name, rank="species", is_active="true", per_page=10)["results"]
    exact = [t for t in results if t["name"].lower() == latin_name.lower()]
    if not exact:
        return None
    return get_json(f"taxa/{exact[0]['id']}")["results"][0]


def candidate_photos(taxon):
    """Photos curated on the taxon page first, then well-voted research-grade observations."""
    for entry in taxon.get("taxon_photos", []):
        yield entry["photo"]
    observations = get_json(
        "observations", taxon_id=taxon["id"], photo_license="cc0,cc-by,cc-by-sa",
        quality_grade="research", order_by="votes", per_page=20,
    )["results"]
    for observation in observations:
        for photo in observation.get("photos", [])[:1]:
            yield photo


def main():
    species_file, out_dir = Path(sys.argv[1]), Path(sys.argv[2])
    per_species = int(sys.argv[3]) if len(sys.argv) > 3 else 3
    out_dir.mkdir(parents=True, exist_ok=True)
    report = {}
    for species in json.loads(species_file.read_text()):
        sid, latin = species["id"], species["latinName"]
        taxon = find_taxon(latin)
        if taxon is None:
            print(f"{sid}: no taxon found", file=sys.stderr)
            report[sid] = {"error": "taxon not found"}
            continue
        picked, seen = [], set()
        for photo in candidate_photos(taxon):
            if photo["id"] in seen or (photo.get("license_code") or "").lower() not in FREE_LICENSES:
                continue
            seen.add(photo["id"])
            url = photo.get("medium_url") or photo["url"].replace("/square.", "/medium.")
            target = out_dir / f"{sid}-{len(picked) + 1}.jpg"
            try:
                download(url, target)
            except Exception as error:  # keep going, one broken photo is not fatal
                print(f"{sid}: {url}: {error}", file=sys.stderr)
                continue
            picked.append({
                "file": target.name,
                "photoId": photo["id"],
                "license": photo["license_code"].lower(),
                "attribution": photo.get("attribution"),
                "sourceUrl": f"https://www.inaturalist.org/photos/{photo['id']}",
            })
            if len(picked) >= per_species:
                break
        print(f"{sid}: taxon {taxon['id']} ({taxon['name']}), {len(picked)} photos")
        report[sid] = {"taxonId": taxon["id"], "taxonName": taxon["name"], "candidates": picked}
    (out_dir / "candidates.json").write_text(json.dumps(report, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
