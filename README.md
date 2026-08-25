# mono — Mononote for Android

Pixel-faithful Android clone of the iOS app **Mononote: One Note** (one note at a time, pinned to widget + lock screen), built with Jetpack Compose + [Kyant0 Backdrop](https://github.com/Kyant0/AndroidLiquidGlass) liquid glass.

- **Builds happen entirely on GitHub Actions** — `ci.yml` on every push/PR (debug APK artifact), `release.yml` on `v*` tags (signed APK/AAB).
- Toolchain: Gradle 9.7.1 · AGP 9.3.1 (built-in Kotlin) · Compose plugin 2.4.10 · BOM 2026.08.00 · JDK 21 · minSdk 31 / target 37.
- Full research & UI spec: [`research/RESEARCH.md`](research/RESEARCH.md).
