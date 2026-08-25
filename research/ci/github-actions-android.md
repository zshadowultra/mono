# GitHub Actions CI/CD for the mono Android app

All versions verified live on 2026-08-23 (Gradle/AGP/Kotlin metadata endpoints + GitHub releases API).

## Verified toolchain

| Tool | Version |
|---|---|
| Gradle | **9.7.1** (current) |
| Android Gradle Plugin | **9.3.1** (latest stable; 9.x line requires Gradle 9 + JDK 21) |
| Kotlin | **2.4.10** stable (Compose compiler bundled since 2.0) |
| JDK | **21** (temurin) |
| compileSdk / targetSdk | **37** / 37 |
| minSdk | **31** (RenderEffect blur tier; AGSL lens auto-on at 33+) |
| Compose BOM | **2026.08.00** |

Actions majors (latest releases today): checkout `v7`, setup-java `v5`, gradle/actions `v6` (setup-gradle & wrapper-validation both live under this repo now), upload-artifact `v7`, softprops/action-gh-release `v3`.

## `.github/workflows/ci.yml`

```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:

concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v7

      - name: Validate Gradle wrapper
        uses: gradle/actions/wrapper-validation@v6

      - name: Set up JDK 21
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '21'

      - name: Setup Gradle (cached)
        uses: gradle/actions/setup-gradle@v6

      - name: Build debug APK + lint
        run: ./gradlew assembleDebug lint --stacktrace

      - name: Upload debug APK
        uses: actions/upload-artifact@v7
        with:
          name: mononote-debug
          path: app/build/outputs/apk/debug/*.apk
```

Notes:
- Free tier: 2,000 min/month private repos, unlimited for public — this repo is fine either way; typical run ≈ 4–8 min with cache warm.
- `setup-gradle@v6` caches `~/.gradle` automatically; no manual cache step needed.
- Emulator/instrumented tests intentionally omitted for v1; add `ReactiveCircus/android-emulator-runner` later if needed.

## `.github/workflows/release.yml`

```yaml
name: Release

on:
  push:
    tags: ['v*']

concurrency:
  group: release-${{ github.ref }}
  cancel-in-progress: false

jobs:
  release:
    runs-on: ubuntu-latest
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v7

      - name: Decode signing keystore
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
        run: echo "$KEYSTORE_BASE64" | base64 -d > "$RUNNER_TEMP/keystore.jks"

      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '21'

      - uses: gradle/actions/setup-gradle@v6

      - name: Build signed release APK + AAB
        env:
          KEYSTORE_FILE: ${{ runner.temp }}/keystore.jks
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew assembleRelease bundleRelease --stacktrace

      - name: Release
        uses: softprops/action-gh-release@v3
        with:
          generate_release_notes: true
          files: |
            app/build/outputs/apk/release/*.apk
            app/build/outputs/bundle/release/*.aab
```

Required repo secrets: `KEYSTORE_BASE64` (base64 of jks), `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. App module reads them from env in `signingConfigs` (never commit the keystore).

## Local dev caveat (Termux)

We author code locally on-device but never build locally — all compilation happens in Actions. Keep `gradlew` wrapper committed so CI controls the exact Gradle version.
