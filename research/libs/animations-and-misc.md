# Libraries: iOS-feel motion, liquid-glass alternatives & platform equivalents

Researched 2026-08-23 (Maven metadata pulled live). Primary glass lib = **Kyant0 Backdrop** — see `backdrop.md`.

## Comparison table

| Library | Coordinates | Latest verified | minSdk | What it gives us | Verdict |
|---|---|---|---|---|---|
| **Backdrop (AndroidLiquidGlass)** | `io.github.kyant0:backdrop-android` | **2.0.0** | 21 (effects need 31/33) | True iOS-26 liquid glass: backdrop sampling, vibrancy, blur, lens refraction, gamma, tint. KMP but pure-Android fine. Apache-2.0, ~3.3k★, active | ✅ **USE (primary)** |
| Shapes (squircles) | `io.github.kyant0:shapes-android` | **1.2.0** | 21 | G2-continuous rounded rects/capsules (`RoundedRectangle(dp, Continuous)`, `Capsule`) = Apple corner feel. Companion of Kyant's Capsule repo (repo renamed to Shapes; `Kyant0/Capsule` no longer separate) | ✅ USE |
| Haze | `dev.chrisbanes.haze:haze` + `haze-materials` | **1.7.2** stable (2.0.0-beta01 out) | 21+ (blur 31+) | Battle-tested backdrop blur + built-in "glassmorphism" material presets; simpler API than Backdrop for plain frosted panels; great fallback tier | ✅ USE as fallback layer / MAYBE |
| Cloudy | `com.github.skydoves:cloudy` | 0.7.1 (Jul 2026); Maven Central `<latest>` shows 1.0.0-alpha01 KMP rewrite | CPU fallback below 31 | KMP blur + `Modifier.liquidGlass()` lens w/ refraction, dispersion, glow params | 🟡 MAYBE (alt lens impl if Backdrop's lens disappoints) |
| PrismalAGSL | `com.github.styropyr0:PrismalAGSL` (JitPack) | tag-released 2026 | 25 | Full AGSL pipeline + ready-made glass components (buttons, toggles, sliders, iOS-style bottom tabs w/ draggable droplet), 3-tier degradation | 🟡 MAYBE (component design reference) |
| LiquidGlass (Abdullajon1881) | not yet on Central (`publishToMavenLocal`) | Jun 2026 | 21 | SDF lens + **shape merging** (`GlassEffectContainer` analog) + gel-press interaction; RN module too | 🔍 REFERENCE (merging/morph ideas) |
| KMPLiquidGlass (Kashif-E) | fork of Kyant0 | — | — | KMP port incl. skiaMain targets | SKIP (we're Android-only) |
| Mortd3kay liquid-glass-android | experimental release zip | 0.1.0 exp | 21 | AGSL blur/distortion/tint demo w/ live param tuning | 🔍 REFERENCE only |
| AppleLiquidGlassForAndroid (SidZadaun02) | source-only | Dec 2025 | — | View-based downsampling blur+magnification+vibrancy | SKIP |
| ardakazanci/LiquidGlass-JetpackCompose | gist/demo | Sep 2025 | 33 | Bitmap→RuntimeShader liquid animation demo | 🔍 REFERENCE |

## iOS-native-feel motion on Android (Compose built-ins first)

Verified against Compose BOM `2026.08.00`:

1. **Springs that match UIKit**: `androidx.compose.animation.core.spring(dampingRatio, stiffness)` — use `dampingRatioNoBouncy`/`NoHaptic`-style critically-damped springs with low stiffness for sheet/nav transitions; `dampingRatioMediumBouncy` for the widget/live-chip pop-in. Never tween.
2. **Shared element transitions**: `SharedTransitionLayout` + `Modifier.sharedElement()/sharedBounds()` (stable since Compose 1.7) — replicate Mononote's note-card ↔ lock-chip morph in-app and in widget previews.
3. **LookaheadScope + animateContentSize()** for the card growing while typing (iOS textview autosize feel).
4. **Predictive back → dismiss gesture**: activity `1.13.0`, `enableOnBackInvokedCallback`, plus custom horizontal drag-to-dismiss on the note card using `AnchoredDraggable` (iOS sheet physics).
5. **Material 3 Expressive motion** (in current M3 under BOM 2026.08.00): `MaterialTheme.motionScheme` expressive springs, spacing-driven layout morphs — cheap way to get Google's tuned "bouncy but premium" curves.
6. **Rubber-band overscroll**: Android 12+ default is stretch; for true iOS bounce on lists use `overscrollEffect` customization (low priority).
7. **Haptics**: `LocalHapticFeedback` + `HapticFeedbackConstants` (e.g., `CONFIRM`, `GESTURE_TICK` on API 30+) wired to Done/archive/Go Live taps.

## Lock-screen / Dynamic Island / widgets — Android equivalents

| iOS feature | Android implementation |
|---|---|
| Home Screen widget | **Glance** `androidx.glance:glance-appwidget:1.1.1` (+`glance-material3`), sizes: small/medium/large via `sizeMode.Responsive`; deep-link tap → app editor |
| Lock Screen Live Activity | **Ongoing notification** with `NotificationCompat.DecoratedCustomViewStyle` + `setOngoing(true)`; on Android 15+ mark *promoted ongoing*; on **Android 16 (API 36)** use the **Live Updates** rich-notification style (`ProgressStyle` etc.) so it renders like a proper live card on the lock screen |
| Dynamic Island | No public API on any OEM. Closest: Live Updates chip on Pixel/16+, Samsung Now Bar on One UI. In-app we can fake the expanded island as a top overlay during "Go Live" state (pure Compose, Backdrop glass) |
| Widget/Live update cadence | Push widget updates via `GlanceAppWidget.update()` from a `DataStore` change listener — instant, no polling |

## Typography (free SF-family stand-ins, bundle via Google Fonts)

| Role (Mononote) | Recommended | Why |
|---|---|---|
| Default (SF Pro) | **Inter** (or Roboto Flex for optical sizing) | Closest free grotesque metrics |
| Serif (New York) | **Source Serif 4** or Lora | NY-style transitional serif |
| Mono (SF Mono) | **JetBrains Mono** or Roboto Mono | Clean terminal mono |

## Core stack pins (verified 2026-08-23)

```
Kotlin 2.4.10 (stable; stdlib ≥2.3.21 required by Backdrop 2.0.0)
AGP 9.3.1 · Gradle wrapper 9.7.1 · JDK 21
Compose BOM 2026.08.00 · compileSdk 37 · targetSdk 37
minSdk: 31 (blur tier) — full AGSL lens auto-enables on 33+
Room 2.8.4 · DataStore Preferences 1.2.1 · androidx.activity:activity-compose 1.13.0
Navigation: androidx.navigation3:navigation3-runtime/-ui 1.1.6 (or navigation-compose if simpler)
Glance 1.1.1 · lifecycle 2.9.x
```
