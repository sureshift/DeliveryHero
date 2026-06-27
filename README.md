# 🛵 DeliveryMitra

A free, open-source Android app for last-mile delivery partners in India.

## Features
- 📊 **Earnings Tracker** — Track earnings from Swiggy, Shadowfax, Rapido, Loadshare, MagicFleet, Ola, Shiprocket
- 🔔 **Auto-capture** — Reads delivery app notifications automatically
- 🗺️ **Delivery Map** — See where your deliveries are concentrated
- 🎯 **Goals** — Set daily/weekly/monthly earning targets
- 💰 **Expenses** — Track fuel, maintenance costs, net earnings
- 🤖 **Mitra AI** — Ask questions about your earnings in plain language

## How to Get the APK

### Step 1 — Fork & Upload to GitHub
1. Go to [github.com](https://github.com) and create a free account
2. Click **New Repository** → name it `deliverymitra` → click **Create**
3. Upload all these files to the repository

### Step 2 — GitHub Builds it Automatically
- Within ~5 minutes, GitHub Actions will compile the APK
- Go to **Actions** tab in your repo → click the latest run → download APK

### Step 3 — Install on Phone
1. Download the `DeliveryMitra-Debug-APK` artifact
2. On your Android: **Settings → Security → Install unknown apps** → Allow
3. Open the `.apk` file → Install

## Enable Notification Capture
1. Open the app
2. Go to phone **Settings → Notification Access**
3. Enable **DeliveryMitra**
4. Now earnings are captured automatically from your delivery apps!

## Tech Stack
- Kotlin + Jetpack Compose
- Room Database (SQLite)
- OSMDroid (OpenStreetMap)
- 100% free & open source
