from PIL import Image, ImageDraw
import os
import math

BASE_DIR = r"D:\AllCodes\WriteDone\app\src\main\res"

SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

def rounded_rect(draw, xy, r, **kwargs):
    x1, y1, x2, y2 = xy
    draw.pieslice([x1, y1, x1 + 2 * r, y1 + 2 * r], 180, 270, **kwargs)
    draw.pieslice([x2 - 2 * r, y1, x2, y1 + 2 * r], 270, 360, **kwargs)
    draw.pieslice([x2 - 2 * r, y2 - 2 * r, x2, y2], 0, 90, **kwargs)
    draw.pieslice([x1, y2 - 2 * r, x1 + 2 * r, y2], 90, 180, **kwargs)
    draw.rectangle([x1 + r, y1, x2 - r + 1, y2], **kwargs)
    draw.rectangle([x1, y1 + r, x2, y2 - r + 1], **kwargs)

def draw_thick_line(draw, points, fill, width):
    for i in range(len(points) - 1):
        x1, y1 = points[i]
        x2, y2 = points[i + 1]
        draw.line([(x1, y1), (x2, y2)], fill=fill, width=width)
    # round caps at endpoints
    x1, y1 = points[0]
    draw.ellipse([x1 - width // 2, y1 - width // 2, x1 + width // 2, y1 + width // 2], fill=fill)
    x2, y2 = points[-1]
    draw.ellipse([x2 - width // 2, y2 - width // 2, x2 + width // 2, y2 + width // 2], fill=fill)
    # round joints at intermediate points
    for i in range(1, len(points) - 1):
        x, y = points[i]
        draw.ellipse([x - width // 2, y - width // 2, x + width // 2, y + width // 2], fill=fill)

def create_icon(size):
    canvas = Image.new("RGBA", (108, 108), (0, 0, 0, 0))
    draw = ImageDraw.Draw(canvas)

    # Background: #C49A8C rounded rect rx=24
    rounded_rect(draw, (0, 0, 108, 108), 24, fill=(196, 154, 140))

    # Sticky note card with rotation -6 deg around center (54,54)
    note = Image.new("RGBA", (108, 108), (0, 0, 0, 0))
    ndraw = ImageDraw.Draw(note)

    # White sticky note body at (14,14)-(94,94) rx=8
    rounded_rect(ndraw, (14, 14, 94, 94), 8, fill=(255, 252, 248))

    # Card edge stroke #E0D8CE width=1
    rounded_rect(ndraw, (14, 14, 94, 94), 8, outline=(224, 216, 206), width=1)

    # Checkmark #3C3530 width=8
    check_points = [(32, 54), (46, 68), (76, 38)]
    draw_thick_line(ndraw, check_points, fill=(60, 53, 48), width=8)

    # Rotate around center (54,54) by -6 degrees
    note_rotated = note.rotate(-6, center=(54, 54), expand=False, fillcolor=None)
    canvas.paste(note_rotated, (0, 0), note_rotated)

    # Scale to target size
    return canvas.resize((size, size), Image.LANCZOS)

for mipmap_dir, size in SIZES.items():
    for suffix in ["ic_launcher", "ic_launcher_round"]:
        icon = create_icon(size)
        webp_path = os.path.join(BASE_DIR, mipmap_dir, f"{suffix}.webp")
        png_path = os.path.join(BASE_DIR, mipmap_dir, f"{suffix}.png")
        # Save as WebP (lossless) for Android compatibility
        icon.save(webp_path, "WEBP", lossless=True)
        # Also save PNG for reference
        icon.save(png_path, "PNG")
        print(f"Generated {webp_path} ({size}x{size})")
