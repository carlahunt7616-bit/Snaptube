# Privacy Policy — SnapTube

**Last Updated:** 2026

1. **Zero-Backend, Local-Only Architecture**  
   SnapTube operates entirely on your Android device. It does not require user accounts, does not transmit telemetry or download history to any SnapTube server, and does not include third-party advertising or analytics SDKs.

2. **Data Stored Locally on Device**  
   Download queue entries, completed file history, appearance settings, language selection, and Storage Access Framework folder preferences are stored locally using Android Room and Jetpack DataStore Preferences. You may clear history at any time from Settings.

3. **Network Connectivity**  
   Internet access is used solely to inspect media headers/metadata and download media directly from the URLs you paste or share into SnapTube.

4. **Permissions Used**
   - `INTERNET` & `ACCESS_NETWORK_STATE`: Analyze links, download files, and enforce Wi-Fi-only preferences.
   - `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_DATA_SYNC`: Keep background downloads reliable.
   - `POST_NOTIFICATIONS` (Android 13+): Optional alerts for shared links, active download progress, and completed downloads.
   - **Storage Access Framework (Zero Broad Permissions)**: Used only if you select a custom download folder.
