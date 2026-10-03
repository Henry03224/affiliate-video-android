# AI Affiliate Video

Android app. Pumili ng video, preview, edit caption + hashtags, copy, then share/open TikTok.

## Android Studio

1. Clone ang repo.
2. Android Studio → Open → piliin ang folder.
3. Hintayin mag-sync ang Gradle.
4. Ikonekta ang phone o emulator, tapos Run.

Min SDK 24. Package: `com.henryshop.affiliatevideo`.

## Flow

- SELECT VIDEO — gallery picker (`video/*`)
- Preview — ExoPlayer
- Caption at hashtags — editable
- COPY CAPTION — clipboard
- OPEN / SHARE TIKTOK — share intent kung naka-install ang TikTok. Caption auto-copy. I-paste sa TikTok composer. Hindi auto-post.

Ang `standalone/` folder ay Java source ng naka-install na debug APK (VideoView, walang AndroidX).
