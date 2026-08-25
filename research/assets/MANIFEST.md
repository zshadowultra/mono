# Mononote — Asset Manifest & UI Observations

Collected 2026-08-23. All assets are from public App Store listing / developer site / social thumbnails.
App: **Mononote: One Note** · Developer: The Digital Minimalist Pte. Ltd. (Jason Chin / Easlo, @heyeaslo) · Free · iOS 17+

## Files

| File | Size | Source | Shows |
|---|---|---|---|
| `icon-1024.png` | 102 KB | [App Store icon CDN](https://is1-ssl.mzstatic.com/image/thumb/PurpleSource211/v4/10/62/66/1062663f-c80e-3c54-ef2c-0bef5f959669/Placeholder.mill/1024x1024bb.png) | App icon |
| `iphone-screenshot-1.jpg` (+`-hires`) | 1290x2796 avail | [Shot 1 @3x](https://is1-ssl.mzstatic.com/image/thumb/PurpleSource221/v4/fa/78/9b/fa789b7e-856b-02a8-5b82-88e41cfee37d/iPhone_Screenshot_1@3x.jpg/{w}x{h}{c}.{f}) | Editor screen, marketing headline "One Note At A Time." |
| `iphone-screenshot-2.jpg` | 600x1300 | [Shot 2 @3x](https://is1-ssl.mzstatic.com/image/thumb/PurpleSource221/v4/09/46/32/094632c1-7e8d-f14f-d771-d6c1fc61cded/iPhone_Screenshot_2@3x.jpg/{w}x{h}{c}.{f}) | Empty-state editor, "Start typing..." |
| `iphone-screenshot-3.jpg` | 600x1300 | [Shot 3 @3x](https://is1-ssl.mzstatic.com/image/thumb/PurpleSource221/v4/0c/e3/16/0ce316be-71cb-8576-deb2-e92fb0c22f5f/iPhone_Screenshot_3@3x.jpg/{w}x{h}{c}.{f}) | Lock Screen Live Activity |
| `iphone-screenshot-4.jpg` | 600x1300 | [Shot 4 @3x](https://is1-ssl.mzstatic.com/image/thumb/PurpleSource221/v4/92/10/3e/92103efe-317b-d709-8aad-5a4379da4cbc/iPhone_Screenshot_4@3x.jpg/{w}x{h}{c}.{f}) | Home Screen widget (large) |
| `ipad-screenshot-1..4.png` | 1200px | App Store CDN | iPad variants of same 4 scenes |
| `macos-screenshot-2.png`, `-5.png` | 1200px | App Store CDN | Mac desktop-widget variants |
| `macos-screenshot-1.png` | ⚠️ 217 B stub | CDN (403 at some sizes) | re-fetch attempt needed |
| `promo-ipad-wide.png` | 1200x630 | App Store promo tile | iPad hero shot |
| `feature-tile.png` | 512x512 | App Store featured-artwork CDN | Editorial tile |
| `blog-hero.png` | — | [Introducing Mononote post](https://www.digitalminimalist.com/blog/introducing-mononote) | Hero banner |
| `blog-app-ui.png` | 1800px wide | same | Close-up editor screenshot (cleanest UI ref) |
| `blog-widget-lockisland-1.png` | — | same | Widget/Dynamic Island frame |
| `blog-widget-lockisland-2.png` | 1800px | same | Lock Screen live-activity close-up |
| `blog-widget-lockisland-3.png` | 1800px | same | **Dynamic Island expanded** close-up |
| `yt-short-thumb.jpg` | maxres | youtube.com/shorts/uYD8jruOjlE | Video frame (launch clip) |

Blocked sources: TikTok (`@heyeaslo/video/7665566249111358741`) and Instagram reels return auth-walls to curl; Threads posts likewise. YouTube short thumbnail was the only social frame captured. For more motion reference: manually grab frames from the YT Short and the IG reels (`DbIv3eTzy8x`, `DbHvwW7TPlQ`).

## UI Observations (for pixel-perfect cloning)

### Global
- **Palette is strictly monochrome**: near-white surfaces, pure-black accents, grays between. No brand color anywhere.
- Marketing captions use an uppercase letter-spaced gray eyebrow (`WIDGETS`, `LIVE ACTIVITY`, `MINIMALIST`) + huge SF-Pro-Display-Bold headline.
- Corners are continuous-curvature (squircle) everywhere — replicate with `io.github.kyant0:shapes` `RoundedRectangle(..., Continuous)` / `Capsule`.

### App icon
- White squircle canvas. Centered black rounded-square "sheet" with a subtle top-black → bottom-dark-gray vertical gradient and a thin lighter rim. Bottom-right corner folds up into a silver/white gradient dog-ear. No glyph, no text.

### Main editor screen (shots 1, 2, blog-app-ui)
- Background: iOS systemGray6 ≈ `#F2F2F7`.
- Nav bar: **no back button**, centered title `Mononote` (semibold, ~17pt, black). Top-right: **white filled circle** (~44pt, faint shadow) containing a black `•••` ellipsis glyph.
- Body: one large **near-white card** (`#FBFBFD`-ish) filling width minus ~16-20pt margins, corner radius ≈ 20–24pt, very low elevation. Inside:
  - Note text top-left, padding ≈ 16pt, SF Pro regular ~17pt, black, multiline.
  - Placeholder state: gray `Start typing...` with visible caret.
  - **Bottom-right inside the card**: a small (~28pt) thin gray **ring/circle button** — reads as the "Go Live"/status affordance (ring with a gap/dot). Exact function needs motion-reference confirmation.
- Below card: full-width **black pill "Done" button** (~50pt tall, fully rounded, white ~17pt semibold label). It sits directly above the keyboard.
- Keyboard shown is stock iOS QWERTY — on Android we get the system IME; layout parity not required.

### Lock Screen Live Activity (shot 3, blog-widget-lockisland-2)
- Frosted **dark translucent rounded rectangle** (radius ≈ 24pt, dark blur over wallpaper), pinned just above the flashlight/camera buttons.
- Content: white ~15–16pt regular text, max 2 lines, no title row, no app icon on lock screen variant.

### Dynamic Island expanded (blog-widget-lockisland-3)
- Black expanded capsule spanning most of the top. Header row: small white **doc/list glyph** + word `Note` (white, semibold). Below: note text in white ~17pt, wraps to 2 lines.

### Home Screen widget (shot 4, ipad-screenshot-4)
- Large/medium rectangular widget: light-gray (`#F2F2F7`) rounded card, note text black ~16pt aligned top-left with ~16pt padding. Medium-size iPhone variant shows tiny centered caption `Mononote` at the bottom. iPad variant is square-ish with no caption visible.
- Widget background matches app background tone (blends with light homescreen wallpapers).

### Unknown / TODO for clone fidelity
- Contents of the `•••` menu (Archive, Delete, Go Live, Share/Copy, Font picker?, Sizes?) — not in any static screenshot. Extract frames from the YouTube Short / IG reels to confirm.
- Archived-notes list screen design (not publicly screenshotted; reviews say you find it by clicking around — likely inside the `•••` menu).
- Font picker UI (app supports Default / Monospace / Serif + smaller size option per v1.x release notes).
- The exact behavior/label of the small ring button inside the card.
