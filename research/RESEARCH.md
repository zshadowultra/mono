# Mononote for Android — Master Research

Date: 2026-08-23 · Target: Android-only, Jetpack Compose, pixel-perfect clone of **Mononote: One Note** (iOS)
Repo: https://github.com/zshadowultra/mono

## 1. The original app

- **Mononote: One Note** by The Digital Minimalist Pte. Ltd. — Jason Chin (Easlo, @heyeaslo), Singapore. Launched ~Jul 22 2026. Free, iOS 17+, iPhone/iPad/Mac (Mac added Aug 15 2026). 4.9★ (96 ratings).
- Positioning: "One note at a time." A minimalist thought-catcher: write one note, it follows you across Home Screen (widget), Lock Screen + Dynamic Island (Live Activity, "Go Live"), archive/delete when done → fresh blank note. Archived notes restorable.
- Press: featured in The Verge *Installer* (David Pierce, Jul 25 2026: "sending myself lockscreen notes with the new Mononote app"), FLIP.de writeup (Jul 30), startupcorners product digest (Aug 9), sir.studio "thought catchers" roundup.

### Feature inventory (from App Store listing, dev blog, reviews, screenshots)

| # | Feature | Evidence |
|---|---|---|
| 1 | Single active note, autosaves as you type | listing + shots 1/2 |
| 2 | Placeholder `Start typing...`, caret visible | shot 2 |
| 3 | `•••` menu button (white circle, top-right) | shots 1/2 |
| 4 | **Done** black pill button commits/leaves edit | shots 1/2 |
| 5 | Small ring/circle affordance bottom-right of card ("Go Live"?) | shots 1/2 — confirm via video |
| 6 | **Go Live** → Live Activity on Lock Screen + Dynamic Island | listing + reviews ("I love the Go Live feature") |
| 7 | Home Screen widget (medium/large; `Mononote` caption) | shot 4, iPad shot 4 |
| 8 | Archive or Delete current note → blank slate | listing |
| 9 | Archived notes list + restore | listing + review ("learning curve: where to find Archived notes") |
| 10 | Fonts: Default / Monospace / Serif; smaller text size option | v1.x release notes |
| 11 | Share or copy current note text | v1.x release notes |
| 12 | Monochrome design, continuous-corner (squircle) shapes | all screenshots |

## 2. Pixel-perfect UI spec

See `assets/MANIFEST.md` (17 downloaded assets incl. 1024px icon, all 4 iPhone shots @1290px, iPad/mac variants, Dynamic Island close-up) for the full visual breakdown. Key numbers:

- bg `#F2F2F7`, card `#FBFBFD`, radius 20–24dp continuous, black pill button h≈50dp, title 17sp semibold centered, note text 17sp regular, ring button ≈28dp.
- Lock-screen live card: dark frosted glass, radius ≈24dp, white 15–16sp text, 2 lines.
- Dynamic Island expanded: black, doc glyph + `Note` header, white 17sp body.
- Widget: `#F2F2F7` card, black 16sp text top-left, caption bottom-center.

## 3. Android stack (all versions verified live)

| Concern | Choice |
|---|---|
| UI | Jetpack Compose, BOM `2026.08.00`, Material 3 (Expressive motion scheme) |
| **Liquid glass** | **`io.github.kyant0:backdrop-android:2.0.0`** (+ `io.github.kyant0:shapes-android:1.2.0` squircles) — recipe: `vibrancy(); blur(4dp); lens(16dp,32dp)` + 50% white veil. Details & code: `libs/backdrop.md` |
| Blur fallback | `dev.chrisbanes.haze:haze:1.7.2` (pre-33 tiers) |
| Widgets | `androidx.glance:glance-appwidget:1.1.1` |
| Lock-screen live note | Ongoing notification (promoted-ongoing on 15+, **Live Updates** style on API 36+) |
| Dynamic Island | No API — in-app glass overlay + Live Updates chip on Pixels |
| Persistence | Room `2.8.4` (notes: active/archived/deleted) + DataStore `1.2.1` (font/size prefs) |
| Motion | Compose springs, `SharedTransitionLayout`, `AnchoredDraggable`, predictive back (`activity-compose 1.13.0`) |
| Fonts | Inter / Source Serif 4 / JetBrains Mono (bundled) |
| Toolchain | Kotlin `2.4.10`, AGP `9.3.1`, Gradle `9.7.1`, JDK 21, compileSdk 37, minSdk 31 |
| CI | GitHub Actions only — see `ci/github-actions-android.md` |

Alternatives considered & references: `libs/animations-and-misc.md` (Cloudy, PrismalAGSL, shape-merging SDF libs from r/androiddev & X).

## 4. Build plan (phases)

1. **Scaffold**: Gradle 9.7.1 wrapper, AGP 9.3.1, single `:app` module, Compose, CI green (`ci.yml`).
2. **Core editor**: one-note Room entity, autosave-debounce, Done pill, `•••` menu, archive/delete/restore, blank-state.
3. **Glass pass**: Backdrop backdrop + glass card, glass `•••` button, squircles, dark mode.
4. **Widget**: Glance small/medium/large, tap-through to editor, instant updates on note change.
5. **Live note**: ongoing notification (lock screen), Live Updates on 36+, "Go Live" ring button state machine, in-app island-style overlay.
6. **Polish**: fonts/size settings, share/copy, archived list UI, haptics, springs/shared-element morphs.
7. **Release**: signing secrets, `release.yml`, tag `v0.1.0`.

## 5. Open questions (need motion reference)

- Exact `•••` menu items & order; archived-list screen design (no static capture exists).
- Ring-button behavior (Go Live vs progress). → pull frames from YT Short `uYD8jruOjlE` / IG reels `DbHvwW7TPlQ`, `DbIv3eTzy8x` (auth-walled to curl; manual capture needed).

## 6. Legal note

UI concept/branding belong to The Digital Minimalist. This is an engineering study/clone for personal use — ship under our own name/icon, don't reuse their marketing assets in a published product.
