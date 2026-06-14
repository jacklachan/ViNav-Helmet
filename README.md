# ViNav Helmet - Smart Navigation & Safety System

[![FAR AWAY 2026](https://img.shields.io/badge/Competition-FAR_AWAY_2026-blue)](https://zuup.club/faraway)
[![Theme](https://img.shields.io/badge/Theme-Embodied_Intelligence-orange)](#)

## 🚀 Overview
**ViNav Helmet** is an innovative smart helmet system designed to eliminate navigation distractions and enhance rider safety. By bridging the gap between mobile navigation and hardware-based feedback (Embodied Intelligence), ViNav provides a Head-Up Display (HUD) for turn-by-turn navigation and an automated SOS emergency system.

This project was built for the **FAR AWAY 2026** competition, focusing on solving critical friction points in road safety.

## 💡 The Problem
Riders often struggle with checking navigation on their phones mounted on handlebars or by stopping repeatedly. This distraction is a major cause of road accidents. Additionally, in case of an accident, notifying emergency contacts with precise location data is often delayed.

## ✨ Features
- **HUD Navigation**: Turn-by-turn directions displayed directly inside the helmet on a SH1106 OLED screen.
- **Hands-Free Operation**: Bluetooth integration ensures the phone stays safely in the pocket.
- **Emergency SOS**: A dedicated SOS trigger that sends live location maps links to emergency contacts via SMS, WhatsApp, and automated calls.
- **Smart Integration**: Android app built with modern Jetpack Compose architecture.

## 🛠️ Tech Stack
### **Mobile (Android)**
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Local Storage**: Room & DataStore
- **APIs**: Google Maps SDK, Directions API, Places API

### **Hardware (Firmware)**
- **Microcontroller**: ESP32
- **Display**: SH1106 1.3" OLED (I2C)
- **Communication**: Bluetooth Serial (SPP)
- **Language**: C++/Arduino

## 📂 Project Structure
```text
.
├── app/                  # Android Application (Kotlin/Compose)
├── esp32_helmet/         # ESP32 Firmware (Arduino/C++)
├── documentation/        # PCB/CAD Files (If applicable)
└── README.md             # Project Documentation
```

## 🚀 Getting Started
### **1. Firmware Setup**
1. Open `esp32_helmet/esp32_helmet.ino` in Arduino IDE.
2. Install `U8g2` and `BluetoothSerial` libraries.
3. Select **ESP32 Dev Module** and upload to your hardware.

### **2. Android App Setup**
1. Clone the repository.
2. Add your `MAPS_API_KEY` to `local.properties`.
3. Build and run the `app` module in Android Studio.

## 📺 Demo Video
[Link to your submission video here]

---
**Built with ❤️ for FAR AWAY 2026**
