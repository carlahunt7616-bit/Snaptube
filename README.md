# SnapTube — Lightweight Android Video & Audio Downloader

**Tagline:** *Download Anything, Anywhere.*

SnapTube is a clean, ultra-lightweight native Android video and audio downloader built with **Kotlin**, **Jetpack Compose**, and **Material 3**. It works **100% locally** with zero custom backend, zero Firebase, and zero cloud tracking.

## Key Features

- **Original Visual Identity**: Custom Play + Download Arrow emblem in a Royal Purple → Electric Blue → Luminous Cyan gradient, adaptive launcher icon, monochrome notification icon, and animated Splash Screen.
- **Share-to-SnapTube (`ACTION_SEND`)**: Share supported video or audio links directly from external Android apps into SnapTube to trigger link analysis and the Download Options Bottom Sheet automatically.
- **Download Options Bottom Sheet**: Rounded modal popup featuring media preview, platform badge, **Music** and **Video** sections, radio quality selection, estimated file sizes, and expandable **More Formats**.
- **Real Background Download Engine**: Coroutine + OkHttp streaming engine with `dataSync` Foreground Service, live progress/speed/ETA tracking, HTTP `Range` pause & resume, concurrency limits (1–4), Wi-Fi-only guard, duplicate prevention, and app-restart recovery.
- **Android Notifications**: Dedicated channels for Shared Link Detection (*Open Download* / *Dismiss*), Ongoing Download Progress (*Pause* / *Cancel*), and Download Completed alerts.
- **Completed Library**: Videos & Audio tabs, search, sort (Newest / Name / Size), external playback (`ACTION_VIEW`), sharing, renaming, and safe deletion.
- **Storage Access Framework & MediaStore**: Zero broad storage permissions; saves to `Downloads/SnapTube` via MediaStore (API 29+) or any user-selected SAF folder.
- **Appearance & Bilingual Support**: System Default, Clean White Light Mode, Deep Charcoal Dark Mode, and live **English / বাংলা (Bengali)** switching.

## Platform Compliance

SnapTube implements a modular platform recognition architecture for YouTube, Facebook, Instagram, TikTok, X / Twitter, Pinterest, Reddit, Vimeo, Dailymotion, and Direct Media URLs. Direct HTTP/HTTPS media streams and authorized open-access pages download natively. DRM-protected or auth-restricted platforms display a clear compliance notice and are never bypassed.

## Release Build & Signing

Release builds enable **R8 code shrinking**, **resource shrinking**, and PNG crunching.

Configure your production upload keystore via environment variables (never commit keystores or passwords):

```bash
export KEYSTORE_PATH="/path/to/your-release-keystore.jks"
export STORE_PASSWORD="your-store-password"
export KEY_PASSWORD="your-key-password"

gradle :app:assembleRelease
gradle :app:bundleRelease
```

Outputs:
- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`

## Legal & Trademark Notice

See [PRIVACY_POLICY.md](PRIVACY_POLICY.md), [TERMS_AND_CONDITIONS.md](TERMS_AND_CONDITIONS.md), and [DISCLAIMER.md](DISCLAIMER.md). Before public store publication, verify that the SnapTube name and branding are legally available in your target jurisdiction.
