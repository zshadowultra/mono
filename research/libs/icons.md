# Icon strategy — import from libraries, don't hand-draw

Verified on Maven Central 2026-08-23.

## Chosen library

**`com.composables:icons-lucide-cmp-android:2.2.1`** (KMP "cmp" artifact, plain Android usage fine)

```kotlin
dependencies {
    implementation("com.composables:icons-lucide-cmp-android:2.2.1")
}
```

- Lucide = the de-facto SF-Symbols-lookalike: 24dp grid, 2px rounded strokes, monochrome — matches Mononote's minimal black-on-gray aesthetic almost 1:1.
- Usage: `Icon(Lucide.Ellipsis, null)` (icons are `ImageVector` objects under `com.composables.icons.lucide.*`).
- License: ISC (Lucide) / library Apache-2.0 — safe to ship.
- Runner-up packs in the same family if we ever need filled style: `icons-tabler-outline-cmp-android`, `icons-material-symbols-rounded-cmp-android` (same `com.composables` group).

Fallback: `androidx.compose.material:material-icons-extended` is **frozen** (no BOM inclusion post-1.7; the old `1.7.8` central artifact path even 404s now) — do not build on it.

## Glyph mapping (screenshot → SF Symbol → our import)

| Where in Mononote | Glyph seen | iOS SF Symbol | Our Lucide import | Notes |
|---|---|---|---|---|
| Top-right `•••` in white circle | horizontal ellipsis | `ellipsis` | `Lucide.Ellipsis` | inside 44dp white circle w/ shadow |
| Card bottom-right ring button | thin ring w/ dot/gap | `circle.dashed` / `record.circle` | `Lucide.Circle` + `Lucide.Dot` overlay, or `Lucide.CircleDot` | "Go Live" candidate — confirm via video frame |
| Dynamic Island header | lined document | `note.text` / `doc.richtext` | `Lucide.FileText` (or `Lucide.StickyNote`) | white, next to label `Note` |
| `•••` menu (not screenshotted — inferred) | archive | `archivebox` | `Lucide.Archive` | |
| `•••` menu | delete | `trash` | `Lucide.Trash2` | |
| `•••` menu | share | `square.and.arrow.up` | `Lucide.Share` | |
| `•••` menu | copy | `doc.on.doc` | `Lucide.Copy` | |
| `•••` menu | Go Live | `dot.circle`/`livephoto` | `Lucide.Radio` or `Lucide.CircleDot` | pick after confirming menu contents |
| `•••` menu | font/style picker | `textformat` | `Lucide.Type` | Default/Serif/Mono switcher |
| Archived list (inferred) | back / restore | `chevron.left`, `arrow.uturn.backward` | `Lucide.ChevronLeft`, `Lucide.Undo2` | |
| Empty archive state | — | `tray` | `Lucide.Tray`/`Lucide.Inbox` | |
| App launcher icon | folded black note sheet | custom artwork | **not from a library** — recreate as vector from `assets/icon-1024.png` (simple two-path drawable: dark rounded square + silver dog-ear) | |

## Rules
- Stroke width 2dp, `Icon` tinted black (light) / white (dark) — never multicolor; matches the monochrome spec.
- Sizes: 20dp (menu rows), 24dp (default), 28dp ring button.
- If a needed glyph is missing from Lucide (rare), pull the single SVG from lucide.dev and add as `ImageVector` via the Studio vector import path — still "imported, not hand-drawn".
