# Vendored reference repos (local clones for component copy-paste)

Cloned `--depth 1` on 2026-08-23 into `/data/data/com.termux/files/usr/tmp/opencode/repos/` (outside the repo, not committed). Re-clone with the same command if the temp dir gets wiped:

```bash
cd /data/data/com.termux/files/usr/tmp/opencode/repos
for r in Kyant0/AndroidLiquidGlass chrisbanes/haze skydoves/Cloudy \
         styropyr0/PrismalAGSL ardakazanci/LiquidGlass-JetpackCompose \
         Abdullajon1881/LiquidGlass Mortd3kay/liquid-glass-android; do
  git clone --depth 1 --single-branch "https://github.com/$r.git" "$(basename $r)"
done
```

## Copy-paste map

### Kyant0/AndroidLiquidGlass (Apache-2.0) — PRIMARY
| Steal this | Path (under `AndroidLiquidGlass/`) |
|---|---|
| `LiquidButton.kt` | `app/src/commonMain/kotlin/com/kyant/backdrop/catalog/components/` |
| `LiquidToggle.kt` | same dir |
| `LiquidSlider.kt` | same dir |
| `LiquidBottomTab(s).kt` | same dir |
| Core modifiers/effects | `backdrop/src/commonMain/kotlin/com/kyant/backdrop/` (`DrawBackdropModifier.kt`, `backdrops/LayerBackdrop*.kt`, `effects/{Blur,ColorFilter,Lens,RenderEffect}.kt`) |
| AGSL shaders (android) | `backdrop/src/androidMain/kotlin/com/kyant/backdrop/` (`RuntimeShader.kt`, `internal/RenderEffect.kt`) |
| Catalog demo app | `androidApp/` (+ prebuilt `androidApp/release/androidApp-release.apk` — sideload to see the effect live on-device) |

Note: catalog components are KMP (`commonMain`) — they compile as plain Android Compose code with zero changes as long as we're on JB Compose-aligned deps (BOM 2026.08.00 is fine).

### chrisbanes/haze (Apache-2.0) — fallback/frosted tier
- **2.0.0 line now has `haze-glass` + `haze-glass-material3` modules** — a first-party glass material (HazeMaterials → glass styles). Check `haze-glass/src/...` when picking the sub-33 fallback recipe.
- Usage samples: `sample/app/src/main/java/dev/chrisbanes/haze/sample/`

### skydoves/Cloudy (Apache-2.0) — alt lens implementation
- `Modifier.liquidGlass` impl: `cloudy/src/androidMain/kotlin/com/skydoves/cloudy/LiquidGlass.android.kt` + shader source `LiquidGlassShaderSource.kt`
- Live demo screen: `app/src/commonMain/kotlin/demo/screen/LiquidGlassDemoScreen.kt`

### styropyr0/PrismalAGSL (MIT) — component design reference
- Ready-made glass UI: `Prismal/src/main/java/com/styropyr0/prismal/components/` — `PrismalGlassButton`, `PrismalGlassToggle`, `PrismalGlassSlider`, `PrismalGlassBottomTabs` (+draggable droplet tab), `PrismalGlassProgressBar`, selectors, gradient panel. Good patterns for press/spring interactions even if we render with Backdrop.

### Abdullajon1881/LiquidGlass (Apache-2.0) — shape-merging ideas
- SDF smooth-min "melting shapes" container (GlassEffectContainer analog) in `liquidglass-compose/` — reference if we ever cluster glass buttons.

### ardakazanci/LiquidGlass-JetpackCompose — shader demo only
### Mortd3kay/liquid-glass-android — **license is custom/unclear (NOASSERTION)** → reference concepts only, do NOT copy code into our repo.

## License hygiene
- Copying code from Apache-2.0/MIT repos into our app is fine (keep attribution in NOTICE or file headers).
- Mortd3kay repo: no clean license → ideas only.
