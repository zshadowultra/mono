# Video frame-by-frame analysis — Mononote (youtube.com/watch?v=bP_QrTwV700)

Frames extracted at native 30fps into `video-frames/` (blob sequence: `blob/f001–f090`, 1:31.5–1:34.5).

## The glass popover blob (Appearance → System/Light/Dark), 30fps

| Frame | Time | What happens |
|---|---|---|
| f016–f039 | 1:32.03–1:32.80 | Closed. Appearance row shows `Light ›` value |
| f040–f041 | 1:32.83–87 | Tap. **The row's value text fades out first** (~2 frames, ≈66ms) |
| f042 | 1:32.93 | Blob emerges **from the value-text anchor** (right side of the row): tiny ≈60×30dp translucent glass nub |
| f043 | 1:32.97 | **Vertical growth leads** — ≈90×70dp, elongating downward; first item ("System") ghosting in |
| f044 | 1:33.00 | ≈full width ≈150dp, ≈80% height; items fade in **staggered top→bottom** (System opaque, Dark still faint) |
| f045 | 1:33.03 | Full size ≈150×110dp; panel floats over the card and **overhangs the card's right edge** onto the bg |
| f047 | 1:33.10 | Settled; ✓ checkmark on selected "Light"; full opacity |
| f050 | 1:33.20 | Identical — no visible overshoot; settle is near-critically damped |

### Extracted animation spec
- **Duration**: ≈130–200ms total (4–6 frames) — much faster than a typical menu spring
- **Anchor**: grows out of the control that triggered it (the value text area), right-anchored
- **Growth order**: height leads, width follows; corners stay large throughout (no capsule→square morph visible at this size)
- **Easing**: fast-out, near-critically-damped settle (tiny/no bounce)
- **Content**: items fade in staggered top→bottom during final ~60% of growth
- **Pre-step**: the triggering row's value text fades out before the blob appears
- **Glass**: real backdrop blur — card edge visibly refracts through the panel
- **Selected state**: ✓ checkmark on current value

## Full app structure confirmed by frames + transcript

**Editor** (0:00, 0:58, 1:02, 1:04): top bar = archive icon LEFT (only when a note exists) / "Mononote" / ••• right. **Compact centered card** (~45% width, squarish, radius ~22–24, soft shadow), not full-width. Bottom bar = trash icon left, **"◯ Go Live" pill center**, second bin-style icon right. Done button only exists while the keyboard is up. Blank state (1:00): archive icon disappears.

**••• menu** (1:03): small glass panel — "Archived Notes" + "Settings" (font/size live in Settings, NOT the menu).

**Archived Notes** (1:06): full sheet — ✕ left / "Archived Notes" / "Select" right; plain rows (no card bg): content + tiny timestamp + **restore ↪ arrow right** (no per-row delete); floating **search bar bottom-center**; Select mode for multi-delete.

**Settings** (1:33, 1:38): sheet with ✕/title. Grouped iOS-style cards: [Appearance (value + glass popover: System/Light/Dark), Note Text ›] / [Widget ›, Live Activity ›] / [Give Feedback, Rate Mononote, About Mononote ›]. Footer: glyph + "Mononote Version 1.0 (8)" + "Focus on one note at a time".

**Note Text** (transcript 1:38–1:43): font picker + "smaller text" toggle.

**Live Activity** (1:58): back chevron / title / lock-screen preview card / explanation / **Appearance: Default ✓ / Clear** radio rows + caption.

**Transcript quotes**: "On the bottom left... you can archive a note" (0:57); "go on the top right, click on the three dots, click archived notes" (1:02); "there's a dark mode" (1:33); "in note text, you can change the font and you can use a smaller text" (1:38); "widgets... just the text against the backdrop" (1:46); "different settings on the view whether it's clear or default" (2:03).
