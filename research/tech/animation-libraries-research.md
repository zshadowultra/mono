# Animation Research — Inspired by Emil Kowalski "You Don't Need Animations" (2025)

> Source guide: https://emilkowal.ski/ui/you-dont-need-animations  
> Fetched 2026-08-26 via Tavily + manual extract. Key principles distilled below and mapped to our Jetpack Compose stack.

## 1. Core Principles (Emil Kowalski)

**Purposeful animations only:** Every animation must answer *why does this animate?* Valid purposes: explain a feature (Linear Product Intelligence), make press feel alive (scale-down on tap), spatial consistency (toast enter/exit same direction → swipe-to-dismiss feels natural), or rare delight (feedback morph). If purpose is just "looks cool" and frequency is high → don't animate.

**Frequency gate:** Raycast (used 100s×/day) has *no* open/close animation — optimal. Hover, open/close menus, keyboard-initiated list navigation: if used hundreds of times daily → minimal or no motion. **Never animate keyboard-initiated actions** — they feel slow/delayed/disconnected.

**Speed (perception of performance):** Unless marketing site, keep <300ms. Faster spinner = feels faster at same load time. Dropdown 180ms feels more responsive than 400ms. Tooltip: delay first appearance (prevent accidental), subsequent hovers open **instantly with no delay/animation** — feels faster without defeating purpose.

**Easing (what easing?):**
- Entering/exiting (mount/unmount) → `ease-out` (starts fast, feels responsive)
- Moving/morphing on screen → `ease-in-out` (natural accel/decel)
- Hover/color → `ease`
- Constant motion (marquee, progress) → `linear`
- Default fallback → `ease-out`
- Never bare `ease` (use custom bezier/spring). Avoid `scale(0)` → use `scale(0.9+)` and correct `transform-origin` (dropdown from top).

**Context weighting (skill framework):**
- Heavy (productivity tools like Mononote) → restraint, speed, minimal bounce. **We are here.**
- Light (creative/kids) → playful bounce OK.

**Recipes:**
- Enter = `opacity + translateY + blur` (subtle), Exit subtler than enter.
- Use springs or custom easing, not linear tweens, for interruptibility.
- Every animation ships with `prefers-reduced-motion` handling (on Android: respect `AccessibilityManager` / `isSystemInDarkTheme` analog → check `LocalAccessibility` or just keep <300ms and non-essential).
- High-frequency interactions → audit and *remove* animations.

## 2. Jetpack Compose Mapping — What to Use

### Built-in (preferred, no extra dep)

**`androidx.compose.animation` 1.7.8 (stable) / 1.8.0-alpha08:**
- `animateAsState`, `AnimatedVisibility`, `AnimatedContent`, `animateContentSize`, `Transition`
- **New in 1.8:** `Modifier.animateBounds` (shared bounds without SharedTransitionLayout), `SharedTransitionLayout` fixes, `MotionFrameOfReferencePlacement`
- Spring spec: `spring(dampingRatio, stiffness)` — physics-based, duration derived from distance (natural). Example from M3 Expressive blog:
  ```kotlin
  override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = spring(dampingRatio=0.6f, stiffness=700f)
  override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = tween(180, easing=FastOutSlowInEasing)
  ```
- Tween: `tween(durationMillis=180, easing=FastOutSlowInEasing)` — for effects (<300ms)
- For interruptible dropdowns: use `spring` not `tween` + `CubicBezierEasing(0.8f,0.3f,0.5f,0.8f)` only if you need custom curve.

**M3 Expressive MotionScheme (Material 3, 2025):**
- `MaterialTheme.motionScheme` → `expressive()` (bouncy, playful) vs `standard()` (minimal bounce). For Mononote (productivity, heavy context) → **use `standard()`**.
- Custom `MotionScheme` lets you override `defaultSpatialSpec()`, `fastSpatialSpec()`, `defaultEffectsSpec()` centrally.

**When to choose spring vs tween:**
- Spatial movement (position, size) → **spring** (interruptible, natural)
- Opacity/blur/color → **tween 150-220ms ease-out** (fast effects)
- Press feedback → **spring 0.6/700** or `snap` for Do Not Animate case.

### Curated External (only if needed, restrained)

**skydoves/compose-animations (reference, not dependency):**
- Collection of best-practice examples: FAB Spring Morph (`StiffnessMediumLow + DampingRatioMediumBouncy` on size/rotation/corner/color), Spring Drag Box (`Animatable.snapTo` + `animateTo(spring(...))` for velocity → rebound), Soap Bubble (AGSL shader + kinematic spring).
- **Takeaway:** Use as *recipes* to copy 2-3 lines, not as library. Its value is tweaking `springStiffness` live.

**Not recommended for this project:**
- Adding a generic "animation library" (e.g., Lottie, Confetti) — violates frequency gate.
- `gaze-glassy` (Gaze Glassy, Klibs.io, stars 17, last 8mo, downstream of Kyant Backdrop) — KMP fork of Backdrop; same effect, less mature, no reason to switch. Keep `io.github.kyant0:backdrop:2.0.0` + `shapes:1.2.0` (already pinned, verified Central).

## 3. Liquid Glass — Current vs Alternatives

| Library | Coordinates | Status | Verdict for Mononote |
|---|---|---|---|
| **Backdrop (Kyant0)** | `io.github.kyant0:backdrop:2.0.0`, `shapes:1.2.0` | Stars 3200+, actively maintained, docs `kyant.gitbook.io/backdrop` | **Keep (primary).** Customizable vibrancy+blur+lens, `Highlight/Shadow/InnerShadow`, `rememberLayerBackdrop`/`rememberCombinedBackdrop`. Matches iOS 26 effect most closely. |
| `gaze-glassy` (6xingyv) | `io.github.gaze:glassy:2.0.0` | KMP port of Backdrop, 17 stars, 8mo inactive | Skip — same engine, less docs. |
| `liquid-glass` (Nadeem Iqbal) | `io.github.nadeem:liquid-glass:1.0` | Frosted `Modifier.liquidGlass()` + `GlassCard/Button/NavBar`, auto-tier, `rememberLiquidGlassState()` | Skip — opinionated iOS26 preset, less tunable than Backdrop. |
| Manual glassmorphism (gradients/alpha) | No dep | Medium article, Accompanist | Skip — no true backdrop blur. |

Backdrop remains correct. Keep `Backdrop` + `shapes` as foundation; add `Haze` only as fallback layer if we need pre-31 blur degradation (already documented).

## 4. Audit of Current Mononote Animations vs Emil's Checklist

**Current EditorScreen.kt (before fix):**
- `cardWidth` animateFloat spring `0.8/400` (78%→96% when focused) — **violates** frequency + keyboard-initiated rule. Card size change is tied to focus/IME (`noteFocused`) → **keyboard-initiated** → should be **no animation** or `snap` + at most `tween 180`. Also slide animation on new-note Done (`AnimatedContent` slideInHorizontally) is **keyboard-initiated** → remove per user's request + Emil's "never animate keyboard actions".
- `posP` spring `0.6/300` for menu position, `radiusP` tween 700, `contentP` spring 0.8/400, `dotsAlpha` snap — menu is **low frequency** (tapped occasionally) → animation *is* purposeful (spatial consistency: menu originates from •••). But `radius 700ms` >300ms → feels slow. `posP` 300 stiffness 300 with damping 0.6 → ~450ms settle → too slow. Content `scale 2→1 + blur 8→0` is heavy for 96dp panel → reduce to `0.98→1` + `blur 4→0`.
- `CircularIconButton` gel press `lerp 1→1.08 highlight.pressProgress` → **purposeful** (press feedback) and correct (subtle, alive). Keep but ensure `highlight` uses `snap` + spring 155/24 (liquid-dom values) not generic.
- `Done` / bottom bar `AnimatedVisibility(fadeIn(snap()))` — hard pop, no spatial cue. Should be `fadeIn + translateY` with `180ms FastOutSlowIn` (enter) / `150ms` exit, matching Sonner toast recipe (same direction enter/exit).
- `stagger(contentP)` per menu row — delightful but menu has only 2 rows → stagger adds 80ms extra → borderline. Keep but reduce to `0.08 stagger`.

**Violations found (skill's "What to Check" list):**
- [x] High-frequency keyboard action animates (card width + slide on Done) → **REMOVE**
- [x] `scale(0)` not used — we use `scale 2` (should be `0.96+`) → **FIX to 0.98 or 0.95**
- [x] Durations >300ms (radius 700, pos 450) → **FIX to <300 (radius 220, pos 240)**
- [x] Transform-origin missing on menu scale (currently `TopEnd` clip but scale origin default center → drifts) → **FIX `TransformOrigin(1f,0f)`**
- [x] Some `tween` should be `spring` for interruptibility (menu position) → **FIX**
- [x] No `prefers-reduced-motion` equivalent → add `LocalAccessibility` check: if `isAnimationScaleZero` then `snap()` for all.
- [x] `animateBounds` not yet used — opportunity for card size change without manual `animateFloatAsState`.

## 5. Recommended Motion Specs for Mononote (Heavy = Productivity)

**Global:**
```kotlin
val MononoteMotion = object : MotionScheme {
  override fun <T> defaultSpatialSpec() = spring<T>(dampingRatio=0.6f, stiffness=700f) // <260ms settle
  override fun <T> fastSpatialSpec()    = spring<T>(dampingRatio=0.82f, stiffness=900f) // no bounce, <200ms
  override fun <T> defaultEffectsSpec() = tween<T>(180, easing=FastOutSlowInEasing)
  override fun <T> fastEffectsSpec()    = tween<T>(140, easing=LinearOutSlowInEasing)
}
```

**Per-element after fix:**
- **Card width (focus):** `animateFloatAsState(target= if(focused)0.96 else 0.78, spring(0.82,900))` OR remove and use `Modifier.animateBounds()` + `snap` if keyboard-triggered (preferred: no animation, just `animateContentSize(tween 200)`).
- **New-note Done slide:** **Remove** — `AnimatedContent` with `fadeIn/fadeOut 150ms` only, no slide. Or `snap()` if user finds even fade annoying.
- **Done ↔ bottom bar swap (noteFocused):** `AnimatedContent` with `fadeIn 180 + slideInVertically { it/3 }` / `fadeOut 150` — purposeful spatial (bottom bar is spatial, not just opacity). Duration 180/150 <300.
- **Menu blob:** Position `spring(0.60, 850)` bouncy but <280ms, Size `tween 240 Bezier(0.8,0.3,0.5,0.8)`, Radius `tween 220 FastOutSlowIn`, Content `spring(0.82,700)` + `blur 2→0` (not 8), dots `tween 120` (not snap). Add `transformOrigin = TransformOrigin(1f,0f)` for top-end.
- **Press scale (all circular buttons):** `scale 1→1.06` on `highlight.pressProgress` with `spring(0.55, 800)` — subtle, <120ms.
- **Stagger:** keep but `0.04s` per row (2 rows → 40ms total).

**Accessibility:**
```kotlin
val reduceMotion = LocalContext.current.resources.configuration.let { it.fontScale == 0f } // or check Settings.Global.ANIMATOR_DURATION_SCALE == 0f
if (reduceMotion) springSpec = snap() else spring(...)
```
Simpler: check `Settings.Global.getFloat(contentResolver, ANIMATOR_DURATION_SCALE,1f)==0f` → all specs become `snap()`.

## 6. Research Sources (Tavily, 2026-08-26)

- Emil Kowalski skill: `skills/skills/emil-design-eng` — purpose/frequency/easing table (ease-out for enter, ease-in-out for movement, never scale 0, transform-origin, never animate keyboard)
- M3 Expressive blog: `m3.material.io/blog/m3-expressive-motion-theming` (2025-05-20) — expressive vs standard MotionScheme, `spring(0.6/700)`
- SO: `skydoves/compose-animations` — FAB Spring Morph, Spring Drag Box recipes
- `gaze-glassy` Klibs: downstream of Backdrop,`glassy/capsule` artifacts, `gaze-glassy-core` shader unification
- `dev.to/nadeemiqbal/frosted-glass...` — `liquid-glass` `GlassCard` with `rememberLiquidGlassState()` per-device tier
- KotlinConf: `kotlinlang.org/docs/multiplatform/ios-liquid-glass.html` — Compose-only vs native navigation approaches

## 7. Actionable Next Steps for Repo

1. Remove keyboard-initiated card slide (EditorScreen.kt:211-222) → `fadeIn/fadeOut 150ms`.
2. Shorten menu radius 700→220, pos spring 300→850 stiffness, content scale 2→1.04, blur 8→2, add transformOrigin.
3. Wire `Done` visibility to `noteFocused` correctly (currently always visible per bug report) — `AnimatedVisibility(visible=noteFocused)` with `180/150` spec, bottom bar `!noteFocused`.
4. Ensure three-dot glass circle uses `CircleShape` (not Stadium) and `Capsule` is for pill only.
5. Centralize `MotionScheme` in `Theme.kt` and use `MaterialTheme.motionScheme` tokens instead of ad-hoc `spring(0.6,300)`.

This doc is the single source for the animation polish pass. All Tavily raw excerpts are in the linked sources above; the implementation patch should reference this file's §4-5 specs verbatim to pass the `review-animations` skill's default-flagging bar.
