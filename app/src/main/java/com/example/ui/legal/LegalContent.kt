package com.example.ui.legal

import com.example.data.model.LegalDocumentType

object LegalContent {

    fun titleFor(type: LegalDocumentType): String = when (type) {
        LegalDocumentType.PRIVACY_POLICY -> "Privacy Policy"
        LegalDocumentType.TERMS_AND_CONDITIONS -> "Terms and Conditions"
        LegalDocumentType.DISCLAIMER -> "Disclaimer"
    }

    fun bodyFor(type: LegalDocumentType): String = when (type) {
        LegalDocumentType.PRIVACY_POLICY -> """
            SnapTube Privacy Policy
            Last updated: 2026

            1. Zero-Backend, On-Device Architecture
            SnapTube operates 100% locally on your Android device. It does not require an account, does not connect to any proprietary SnapTube backend server, and does not collect, sell, or transmit your personal data or download history.

            2. Local Data Storage
            Your download queue, completed file history, theme choice, language preference, and folder selection are stored exclusively on your device using Android Room and DataStore Preferences. You can clear your download history at any time from Settings.

            3. Network Requests
            Internet connectivity is used solely to probe media metadata and download files directly from the URLs you explicitly enter or share into the application.

            4. Permissions
            SnapTube requests only the minimum permissions necessary for core functionality:
            • Internet & Network State: Required to analyze links and verify Wi-Fi-only rules.
            • Foreground Service (Data Sync): Keeps your active downloads reliable in the background.
            • Notifications (Android 13+): Displays live download progress, completion alerts, and optional shared-link alerts.
            • Storage Access Framework: Optional user-selected folder access without requesting broad storage permissions.

            5. Third-Party Websites
            When you inspect or download from an external media URL, your device connects directly to that host subject to the host's own privacy terms.
        """.trimIndent()

        LegalDocumentType.TERMS_AND_CONDITIONS -> """
            SnapTube Terms and Conditions
            Last updated: 2026

            1. Authorized Use Only
            SnapTube is a local media utility designed solely for downloading content that you own, have explicit permission to save, or that is made available under open licenses (such as Creative Commons or Public Domain).

            2. Respect for Copyright & DRM
            You agree not to use SnapTube to infringe copyright, circumvent Digital Rights Management (DRM), bypass authentication walls, or violate the Terms of Service of any third-party platform.

            3. No Warranty
            This application is provided "as is" without warranties of any kind. Availability, speed, and resumability depend on the remote media host and your network connection.

            4. User Responsibility
            You are solely responsible for all URLs you analyze and files you store on your device.
        """.trimIndent()

        LegalDocumentType.DISCLAIMER -> """
            SnapTube Disclaimer

            1. Platform Independence
            SnapTube is an independent Android application and is not affiliated with, endorsed by, or sponsored by YouTube, Facebook, Instagram, TikTok, X (Twitter), Pinterest, Reddit, Vimeo, or Dailymotion.

            2. Platform Restrictions
            Third-party platforms that prohibit unauthenticated media downloading or employ DRM protection are respected. SnapTube does not bypass platform security controls and will inform you when a link requires an authorized source.

            3. Trademark Notice
            All third-party product names and brand references belong to their respective owners. Before public store distribution, verify that the application name and branding are legally available in your jurisdiction.
        """.trimIndent()
    }
}
