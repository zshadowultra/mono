# Backdrop (Kyant0/AndroidLiquidGlass) — Library Research

> Research date: 2026-08-23. Sources: GitHub repo (default branch `kmp`), docs at
> https://kyant.gitbook.io/backdrop (llms.txt index), Maven Central POMs, raw library sources.
> Purpose: primary visual library for the Mononote Android clone (Jetpack Compose, Android-only).

---

## 1. Overview

**Backdrop** is a Compose Multiplatform "Liquid Glass" library by Kyant (Kyant0). It draws a copy of
the **background (backdrop)** under a foreground composable and applies GPU effects to it —
blur, saturation ("vibrancy"), edge refraction/lensing, specular highlight, inner shadow —
recreating the iOS 26-style liquid glass look. It works with **any** composable content as backdrop
source (NavHost content, images, colored tracks), not just bitmaps.

- Repo: https://github.com/Kyant0/AndroidLiquidGlass (default branch: `kmp`)
- Docs: https://kyant.gitbook.io/backdrop (every page also available as `.../<page>.md`; index at `/llms.txt`)
- License: **Apache-2.0**
- Stars / forks: **~3,332 ★ / 280 forks**, created 2025-06, last push 2026-07 → very active
- Language: Kotlin; Compose Multiplatform (Android/desktop/js/wasm/iOS targets)
- The library ships **no high-level components** — you build your own (`LiquidButton`,
  `LiquidToggle`, `LiquidSlider`, `LiquidBottomTabs` exist only as sample code in
  `app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/`). This is good for a clone:
  we copy patterns, we don't fight a widget API.

History: v1.x was Android-only (`master` branch). **v2.0.0 (2026-05-28)** became KMP; it removed
Android-only effect APIs in favor of common ones and added a common `RuntimeShader` interface +
`BackdropEffectScope.runtimeShaderEffect`.

---

## 2. Exact Gradle setup (pure Android Jetpack Compose app)

### Artifacts on Maven Central (verified from repo1.maven.org metadata)

| Artifact | Latest | Notes |
|---|---|---|
| `io.github.kyant0:backdrop` | **2.0.0** | KMP root artifact. Use this coordinate in an Android app; Gradle module metadata resolves the `-android` variant automatically. Also contains 1.0.0–1.0.6 (legacy Android-only line). |
| `io.github.kyant0:backdrop-android` | 2.0.0 | The Android target AAR (exists only for 2.0.0-alpha01+). Don't reference directly unless you want to pin the platform artifact. |
| `io.github.kyant0:shapes` | 1.2.0 | iOS-like smooth-corner shapes (companion library). Pulled in transitively by backdrop (as `shapes-android`) — add explicitly if you use its shapes directly. |

### Dependency snippet

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("io.github.kyant0:backdrop:2.0.0")

    // optional but recommended: G2-continuous (squircle) shapes: Capsule(), RoundedRectangle(...)
    implementation("io.github.kyant0:shapes:1.2.0")
}
```

### What 2.0.0 pulls in (verified from published POM)

- `org.jetbrains.kotlin:kotlin-stdlib:2.3.21`
- `org.jetbrains.compose.foundation:foundation:1.11.0`, `org.jetbrains.compose.ui:ui:1.11.0`,
  `org.jetbrains.compose.ui:ui-graphics:1.11.0` (runtime scope). On Android these JB CMP artifacts
  delegate to the matching `androidx.compose.*` artifacts, so they coexist fine with a normal
  androidx-compose-only app — just keep your Compose reasonably current.
- `io.github.kyant0:shapes-android:1.2.0`
- Packaging `aar`, namespace `com.kyant.backdrop`

### Version compatibility matrix

| Component | Library build config / dependency | Recommended for our app |
|---|---|---|
| minSdk (declared) | **21** (from `backdrop/build.gradle.kts`) | **31 minimum** for any glass effect; **33+ recommended** (full lens/refraction). See §3. |
| compileSdk (library built against) | 37 | compileSdk 36+ in app |
| Kotlin stdlib dep | 2.3.21 | Kotlin plugin **2.3.21 or newer** (repo itself now builds with Kotlin 2.4.10 + AGP 9.3.0 + CMP 1.11.1) |
| Compose Multiplatform dep | JB CMP foundation/ui 1.11.0 | androidx.compose BOM ≥ 2025.xx equivalent; CMP 1.10+/1.11 line |
| JVM target | JVM_11 | default modern AGP ok |
| shapes | 1.2.0 (minimum since backdrop 1.0.5 / 2.0.0-alpha03) | 1.2.0 |

Legacy note: if ever needed, the old Android-only line is `io.github.kyant0:backdrop:1.0.6`
(built against CMP 1.10.0 / Kotlin 2.3.0); API differs slightly (e.g. `HighlightStyle.Default(...)`).

---

## 3. API level gating & fallback behavior (critical)

Verified from docs + `androidMain` sources:

- **All effects are `RenderEffect`s → require Android 12 / API 31+.** In
  `DrawBackdropNode.updateEffects()` the code does `if (!isRenderEffectSupported()) return` —
  below API 31 the modifier silently draws **content with no glass** (no crash): your surface
  tint drawn via `onDrawSurface` still renders, so the UI stays readable.
- Effects involving **AGSL `RuntimeShader` require Android 13 / API 33+**:
  - `lens(...)` — the signature refraction/edge-distortion effect (the whole point!)
  - `gammaAdjustment(...)`
  - custom `runtimeShaderEffect(...)`
  - Source proof: `android.graphics.RenderEffect.createRuntimeShaderEffect` wrapped in
    `@RequiresApi(Build.VERSION_CODES.TIRAMISU)`.
- `lens()` additionally requires the shape to be a `CornerBasedShape`.
- FAQ-level gotcha: nothing renders? You forgot `Modifier.layerBackdrop(backdrop)` on the source.

**Decision for Mononote:** set `minSdk = 33` (or 31 with an `Build.VERSION.SDK_INT >= 33` gate that
swaps `lens()` for `vibrancy() + blur() + white overlay`). Below 33 there is no refraction fallback.

---

## 4. Core API cheat-sheet

Package root: `com.kyant.backdrop`. Imports seen in real usage:

```kotlin
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop          // Modifier
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.effects.blur                     // effects are extensions of
import com.kyant.backdrop.effects.lens                     //   BackdropEffectScope
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
```

### 4.1 Declaring a backdrop source

From `api/backdrops.md`:

| Function | What it does |
|---|---|
| `rememberLayerBackdrop { drawRect(bg); drawContent() }` | Layer capture of a composable's rendered content. **Coordinate-dependent.** The lambda lets you paint background color behind the captured content (needed so transparent areas aren't refracted). |
| `Modifier.layerBackdrop(backdrop)` | Marks that node's drawing as the backdrop's content. |
| `rememberBackdrop { }` | Backdrop from arbitrary custom draw commands. |
| `rememberCombinedBackdrop(a, b, …)` | Merges several backdrops into one — key for tabs/sliders where a thumb must refract both page content *and* a local track. |
| `rememberCanvasBackdrop { }` | Draw custom content onto an empty backdrop (coordinate-independent). |
| `emptyBackdrop` | Draws nothing (placeholder). |

Canonical wiring (glass bottom bar over a NavHost):

```kotlin
Box(Modifier.fillMaxSize()) {
    val backgroundColor = Color.White
    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)   // draw what's OUTSIDE the nav host too!
        drawContent()
    }

    MainNavHost(modifier = Modifier.layerBackdrop(backdrop))

    Box(
        Modifier
            .safeContentPadding()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(16f.dp.toPx(), 32f.dp.toPx())
                },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) } // readability
            )
            .height(64f.dp)
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    )
}
```

### 4.2 `Modifier.drawBackdrop` — full parameter list (verified from `DrawBackdropModifier.kt`)

```kotlin
fun Modifier.drawBackdrop(
    backdrop: Backdrop,                                   // required
    shape: () -> Shape,                                   // lambda! e.g. { Capsule() }
    effects: BackdropEffectScope.() -> Unit,              // required (may be empty)
    highlight: (() -> Highlight?)? = DefaultHighlight,    // specular sheen, on by default
    shadow: (() -> Shadow?)? = DefaultShadow,
    innerShadow: (() -> InnerShadow?)? = null,
    layerBlock: (GraphicsLayerScope.() -> Unit)? = null,  // press/scale/translate animation hook;
                                                          //   ALSO transforms sampled backdrop
    exportedBackdrop: LayerBackdrop? = null,              // re-publish this node as a backdrop
    onDrawBehind: (DrawScope.() -> Unit)? = null,
    onDrawBackdrop: DrawScope.(drawBackdrop: DrawScope.() -> Unit) -> Unit = Default,
    onDrawSurface: (DrawScope.() -> Unit)? = null,        // tint / white veil goes here
    onDrawFront: (DrawScope.() -> Unit)? = null
)
```

Plus a lighter `drawPlainBackdrop(...)` without highlight/shadow/innerShadow layers.

Param notes:

- `shape` is a **lambda returning Shape** — cheap per-draw shape changes, animate radius freely.
- Draw order inside the node: `onDrawBehind` → backdrop-with-effects → `onDrawSurface` → content → `onDrawFront`.
- `layerBlock` drives both the node's `graphicsLayer` and how the backdrop is sampled → drag/squish
  animations distort the glass realistically (see LiquidButton below).
- Tint recipe (docs-recommended): use `BlendMode.Hue` so backdrop hues adapt to the tint:
  ```kotlin
  onDrawSurface = {
      val tint = Color(0xFF0088FF)
      drawRect(tint, blendMode = BlendMode.Hue)
      drawRect(tint.copy(alpha = 0.75f))
  }
  ```

### 4.3 Effects (`com.kyant.backdrop.effects`) — order matters!

Docs rule: apply as **color filter ⇒ blur ⇒ lens**.

| Effect | Signature | API |
|---|---|---|
| Custom color filter | `colorFilter(colorFilter: ColorFilter)` | 31+ |
| Opacity | `opacity(alpha: Float)` | 31+ |
| Color controls | `colorControls(brightness = 0f, contrast = 1f, saturation = 1f)` | 31+ |
| Vibrancy | `vibrancy()` == `colorControls(saturation = 1.5f)` | 31+ |
| Exposure | `exposureAdjustment(ev: Float)` | 31+ |
| Gamma | `gammaAdjustment(power: Float)` | **33+** |
| Blur | `blur(radius: Float, edgeTreatment: TileMode = TileMode.Clamp)` | 31+ |
| Lens (refraction) | `lens(refractionHeight: Float, refractionAmount: Float = height, depthEffect: Boolean = false, chromaticAberration: Boolean = false)` | **33+**, shape must be `CornerBasedShape` |
| Custom RenderEffect | `effect(effect: RenderEffect)` | 31+ |
| Custom shader | `runtimeShaderEffect(key, shaderString, uniformShaderName) { ... }` | 33+ |

Constraints documented for `lens`:
- `refractionHeight` ∈ [0, `shape.minCornerRadius`] (exceeding causes corner discontinuities — tolerated).
- `refractionAmount` ∈ [0, `size.minDimension`].
- `depthEffect = true` adds the thicker-glass depth shading; `chromaticAberration = true` adds RGB fringe (great on small thumbs/highlights).

Typical "iOS-ish" recipe used across all official examples:

```kotlin
effects = {
    vibrancy()
    blur(4f.dp.toPx())                       // frosted body
    lens(16f.dp.toPx(), 32f.dp.toPx())       // height ≤ corner radius, amount ~2× height
}
onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) }
```

### 4.4 Real component code (quoted from catalog sources)

**LiquidButton** (`catalog/components/LiquidButton.kt`, abridged verbatim):

```kotlin
Row(
    modifier
        .drawBackdrop(
            backdrop = backdrop,
            shape = { Capsule() },
            effects = {
                vibrancy()
                blur(2f.dp.toPx())
                lens(12f.dp.toPx(), 24f.dp.toPx())
            },
            layerBlock = {
                val progress = interactiveHighlight.pressProgress
                val scale = lerp(1f, 1f + 4f.dp.toPx() / size.height, progress)
                translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)
                // + directional scaleX/scaleY stretch toward the finger
            },
            onDrawSurface = {
                if (tint.isSpecified) {
                    drawRect(tint, blendMode = BlendMode.Hue)
                    drawRect(tint.copy(alpha = 0.75f))
                }
                if (surfaceColor.isSpecified) drawRect(surfaceColor)
            }
        )
        .clickable(interactionSource = null, indication = null, role = Role.Button, onClick = onClick)
        .height(48f.dp),
    ...
)
```

**LiquidBottomTabs** pattern (verbatim essentials):

```kotlin
val tabsBackdrop = rememberLayerBackdrop()

Row(Modifier                              // visible panel
    .drawBackdrop(
        backdrop = backdrop,
        shape = { Capsule() },
        effects = { vibrancy(); blur(8f.dp.toPx()); lens(24f.dp.toPx(), 24f.dp.toPx()) },
        layerBlock = { /* scale on press */ },
        onDrawSurface = { drawRect(containerColor) })   // Color(0xFFFAFAFA).copy(0.4f) light
    ...
)

Row(Modifier                              // hidden accent-tinted copy, re-captured
    .alpha(0f)
    .layerBackdrop(tabsBackdrop)
    .drawBackdrop(
        backdrop = backdrop,
        shape = { Capsule() },
        effects = { vibrancy(); blur(8f.dp.toPx()); lens(24.dp*progress, 24.dp*progress) },
        highlight = { Highlight.Default.copy(alpha = progress) },
        onDrawSurface = { drawRect(containerColor) })
    .graphicsLayer(colorFilter = ColorFilter.tint(accentColor))
    ...
)

Box(                                      // moving indicator pill
    Modifier
        .graphicsLayer { translationX = dampedDragAnimation.value * tabWidth }
        .drawBackdrop(
            backdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop),  // refracts BOTH
            shape = { Capsule() },
            effects = { lens(10.dp*progress, 14.dp*progress, chromaticAberration = true) },
            highlight = { Highlight.Default.copy(alpha = progress) },
            shadow = { Shadow(alpha = progress) },
            innerShadow = { InnerShadow(radius = 8.dp*progress, alpha = progress) },
            layerBlock = { scaleX = anim.scaleX; scaleY = anim.scaleY /* velocity squish */ },
            onDrawSurface = { /* fade static veil out with press */ })
        ...)
```

**Glass slider** (tutorial): track `Box.layerBackdrop(trackBackdrop).background(accent, CircleShape)`;
thumb uses `backdrop = rememberCombinedBackdrop(pageBackdrop, trackBackdrop)` with
`lens(12dp, 16dp, chromaticAberration = true)`.

**Glass bottom sheet — "glass on glass"** (tutorial + FAQ crash fix):
To place a glass button *on top of* a glass sheet, do NOT stack `layerBackdrop` inside a
`drawBackdrop` node (infinite loop → `Fatal signal 11 SIGSEGV` in RenderThread). Instead give the
sheet an `exportedBackdrop`:

```kotlin
val sheetBackdrop = rememberLayerBackdrop()
Column(
    Modifier.drawBackdrop(
        backdrop = pageBackdrop,
        exportedBackdrop = sheetBackdrop,     // publishes this node's output, skips self-loop
        ...
    )
) {
    Box(Modifier.drawBackdrop(backdrop = sheetBackdrop, ...))   // glass button on the sheet
}
```

---

## 5. Copy-paste starter: Mononote glass note card + FAB-style button

Adapted from the above patterns for a one-note minimalist app:

```kotlin
@Composable
fun GlassNoteCard(
    backdrop: Backdrop,
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(28f.dp) },          // G2-continuous squircle (Shapes lib)
                effects = {
                    vibrancy()                                 // saturate what's behind
                    blur(4f.dp.toPx())
                    lens(20f.dp.toPx(), 40f.dp.toPx(),         // height ≤ corner radius!
                         depthEffect = true)
                },
                onDrawSurface = {
                    drawRect(Color.White.copy(alpha = 0.45f))  // readability veil
                }
            )
            .padding(20f.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6f.dp))
        Text(body, style = MaterialTheme.typography.bodySmall, maxLines = 3)
    }
}

@Composable
fun GlassActionButton(onClick: () -> Unit, backdrop: Backdrop, icon: ImageVector) {
    Box(
        Modifier
            .size(56f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                effects = { vibrancy(); blur(2f.dp.toPx()); lens(12f.dp.toPx(), 24f.dp.toPx()) },
                onDrawSurface = {
                    drawRect(Color(0xFF0088FF), blendMode = BlendMode.Hue)  // tinted glass
                    drawRect(Color(0xFF0088FF).copy(alpha = 0.75f))
                }
            )
            .clickable(interactionSource = null, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.85f))
    }
}
```

Screen scaffold (root of each Mononote screen):

```kotlin
Box(Modifier.fillMaxSize().background(AppBackground)) {
    val backdrop = rememberLayerBackdrop {
        drawRect(AppBackground)
        drawContent()
    }
    NotesList(Modifier.layerBackdrop(backdrop))   // scrolling notes = living backdrop
    GlassBottomBar(backdrop)                      // tabs / actions
}
```

---

## 6. Smooth corners via Shapes ("Capsule")

The user-facing repo for iOS-style squircles is **Kyant0/Shapes** ("iOS-like shapes for Compose
Multiplatform", branch `master`, Apache-2.0, small/new repo ~34★). Note: there is no separate
"Kyant0/Capsule" repo anymore — `Capsule` is a class inside Shapes. It's already a transitive
dependency of backdrop (`shapes-android:1.2.0`).

Maven: `io.github.kyant0:shapes:1.2.0`

API (verified from source):

```kotlin
package com.kyant.shapes

enum class RoundedCornerStyle { Circular, Continuous }

class Capsule : RoundedRectangularShape                 // stadium/pill, usage: Capsule()

class RoundedRectangle(
    val cornerRadius: Dp,
    override val style: RoundedCornerStyle = RoundedCornerStyle.Continuous  // G2 ≈ G3 squircle
) : RoundedRectangularShape

// also: UnevenRoundedRectangle(cornerRadii...), lerp/copy helpers
```

Usage anywhere a `Shape` is expected (they implement Compose `Shape` via `createOutline`):

```kotlin
.shape(Capsule())
shape = { RoundedRectangle(24f.dp) }                    // in drawBackdrop
shape = { UnevenRoundedRectangle(topStart = 24.dp, ...) }
```

The docs' "Smoother rounded corners" tutorial shows G2-continuous corners visibly reduce the
"flat arc" look vs `RoundedCornerShape` — use them for cards/bars/buttons to hit the iOS feel.

---

## 7. Performance & gotchas summary

1. **API gating**: no RenderEffect support below API 31 → effects skipped silently (plain surface);
   `lens`/`gammaAdjustment`/custom shaders need **API 33+**. Gate or set minSdk 33.
2. **Never loop backdrops**: `layerBackdrop` inside a node that `drawBackdrop`s from the same
   backdrop → SIGSEGV crash in RenderThread. Use `exportedBackdrop` for glass-on-glass.
3. **Include everything behind the glass**: only nodes marked `layerBackdrop` (plus the
   `rememberLayerBackdrop {}` preamble) get refracted; window background must be drawn into the
   backdrop manually.
4. **Effect order**: color filter ⇒ blur ⇒ lens. Wrong order looks wrong.
5. **Lens constraints**: `height` ≤ corner radius, `amount` ≤ min dimension; requires
   `CornerBasedShape`.
6. Each glass node records the backdrop region into a **GraphicsLayer** and applies a RenderEffect
   chain on the RenderThread — keep the count of simultaneous glass nodes modest (a bar + a few
   pills is fine; avoid dozens of large blurring surfaces). Prefer animating through `layerBlock`
   (GraphicsLayerScope) instead of recomposition-driven parameters; the samples drive all press
   feedback via Animatable + graphicsLayer.
7. `shape` param is a lambda — build shapes inline; combine with Shapes-lib squircles for free
   smoothness. Changing shape invalidates redraws, not layout.
8. `Highlight.Default`, `Shadow.Default` are on by default in `drawBackdrop` (specular sheen +
   drop shadow); pass `highlight = null` / `shadow = null` to disable (samples pass `shadow = null`
   for inner glass buttons).
9. Coordinate-dependent backdrops track position via `GlobalPositionAwareModifierNode` — moving
   elements update automatically; fixed rc01 leak of LayoutCoordinates references (fixed in 2.0.0-rc01).

## 8. License

Apache License 2.0 (both AndroidLiquidGlass and Shapes). Commercial-friendly; keep NOTICE/attribution.
