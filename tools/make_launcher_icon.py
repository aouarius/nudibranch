"""Draws the Nudidex launcher icon as Android vector drawables (plus an SVG preview).

Usage: python3 tools/make_launcher_icon.py <repo-root> <preview-dir>
"""
import sys, os
repo, out = sys.argv[1], sys.argv[2]
BG = "#12324A"
def circle(cx, cy, r):
    return f"M{cx-r:.1f},{cy:.1f} a{r},{r} 0 1,0 {2*r:.1f},0 a{r},{r} 0 1,0 {-2*r:.1f},0 Z"
shapes = []  # (kind, color, path, width)
import math
def f(v): return f"{v:.1f}"
def teardrop(bx, by, angle_deg, length, width, color, tip):
    a = math.radians(angle_deg)
    dx, dy = math.sin(a), -math.cos(a)
    nx, ny = -dy, dx
    x1, y1 = bx - nx * width / 2, by - ny * width / 2
    x2, y2 = bx + nx * width / 2, by + ny * width / 2
    tx, ty = bx + dx * length, by + dy * length
    k = 1.15
    shapes.append(("fill", color,
        f"M{f(x1)},{f(y1)} Q{f(x1 + dx*length*k)},{f(y1 + dy*length*k)} {f(tx)},{f(ty)} "
        f"Q{f(x2 + dx*length*k)},{f(y2 + dy*length*k)} {f(x2)},{f(y2)} Z", 0))
    shapes.append(("fill", tip, circle(round(tx, 1), round(ty + 0.8, 1), 2.6), 0))
# cerata fan out over the back, back row darker
for i, (x, ang, ln) in enumerate([(26, -50, 17), (33, -32, 22), (41, -16, 25), (49, -3, 26), (57, 10, 25), (64, 22, 21)]):
    teardrop(x + 2, 60, ang - 6, ln - 3, 6.5, "#B84DE0", "#FF8A2A")
for x, ang, ln in [(27, -45, 15), (35, -26, 20), (43, -10, 23), (51, 3, 23), (59, 15, 21), (66, 27, 17)]:
    teardrop(x, 62, ang, ln, 7, "#E36BFF", "#FFA53D")
# rhinophores
shapes.append(("stroke", "#FFA53D", "M78,58 Q75,49 73,42", 3.6))
shapes.append(("stroke", "#FFA53D", "M83,58 Q85,49 88,43", 3.6))
# body
shapes.append(("fill", "#9B6BFF", "M14,74 C24,64 42,59 60,58 C72,57.5 82,55 88,60 C93,65 89,71 82,72 C66,75.5 40,77 14,74 Z", 0))
shapes.append(("fill", "#C9B2FF", "M30,66 C42,62.5 56,61.5 70,61.5 C58,63.5 44,65 30,66 Z", 0))
# white foot line
shapes.append(("stroke", "#FFFFFF", "M22,73.4 C42,75.6 64,74.6 82,71.2", 1.8))

def svg():
    parts = [f'<svg xmlns="http://www.w3.org/2000/svg" width="432" height="432" viewBox="0 0 108 108">',
             f'<rect width="108" height="108" fill="{BG}"/>', '<g transform="translate(0,-4)">']
    for kind, color, d, w in shapes:
        if kind == "fill":
            parts.append(f'<path d="{d}" fill="{color}"/>')
        else:
            parts.append(f'<path d="{d}" fill="none" stroke="{color}" stroke-width="{w}" stroke-linecap="round"/>')
    parts.append("</g></svg>")
    return "\n".join(parts)

def vector(mono=False):
    lines = ['<?xml version="1.0" encoding="utf-8"?>',
             '<!-- Nudidex launcher icon: a stylised nudibranch. Generated, edit freely or replace. -->',
             '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
             '    android:width="108dp"', '    android:height="108dp"',
             '    android:viewportWidth="108"', '    android:viewportHeight="108">',
             '  <group android:translateY="-4">']
    for kind, color, d, w in shapes:
        if mono and color in ("#FFFFFF", "#C9B2FF"):
            continue
        c = "#FFFFFFFF" if mono else color
        if kind == "fill":
            lines.append(f'    <path android:fillColor="{c}" android:pathData="{d}" />')
        else:
            lines.append(f'    <path android:strokeColor="{c}" android:strokeWidth="{w}" android:strokeLineCap="round" android:pathData="{d}" />')
    lines.append("  </group>")
    lines.append("</vector>")
    return "\n".join(lines) + "\n"

open(f"{out}/icon.svg", "w").write(svg())
res = f"{repo}/app/src/main/res"
os.makedirs(f"{res}/drawable", exist_ok=True)
os.makedirs(f"{res}/mipmap-anydpi-v26", exist_ok=True)
open(f"{res}/drawable/ic_launcher_foreground.xml", "w").write(vector())
open(f"{res}/drawable/ic_launcher_monochrome.xml", "w").write(vector(mono=True))
open(f"{res}/drawable/ic_launcher_background.xml", "w").write(
    '<?xml version="1.0" encoding="utf-8"?>\n<shape xmlns:android="http://schemas.android.com/apk/res/android">\n'
    f'    <solid android:color="{BG}" />\n</shape>\n')
adaptive = ('<?xml version="1.0" encoding="utf-8"?>\n'
    '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
    '    <background android:drawable="@drawable/ic_launcher_background" />\n'
    '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
    '    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n'
    '</adaptive-icon>\n')
for name in ("ic_launcher", "ic_launcher_round"):
    open(f"{res}/mipmap-anydpi-v26/{name}.xml", "w").write(adaptive)
