

# 📚 TUT Labs Management System

## Overview

TUT Labs is a cross-platform mobile application designed to help manage and monitor university computer labs. It provides **lab tutors** and **administrators** with tools to manage schedules, update lab statuses, track attendance, and streamline communication.

This project consists of **two apps**:

* 📱 **Android app** built with **Kotlin + Jetpack Compose + Firebase**
* 🍏 **iOS app** built with **SwiftUI + Firebase**

Both apps are synced in real time through Firebase, ensuring lab data is consistent across platforms.

---

## ✨ Features

### ✅ Admin Features

* Assign lab shifts to tutors.
* Send lab announcements (e.g., open/closed updates).
* View tutor activity and schedules.

### 🎓 Tutor Features

* Mark attendance when on duty.
* Open/close labs in real time (status synced for all students).
* View assigned shifts in a weekly schedule.

### 👩‍🎓 Student View (optional extension)

* See which labs are open/closed in real time.
* Access lab announcements.

### 🌍 Location-Based Security

* Tutors can only open/close labs when within **200m of the lab location** (using GPS geofencing).

---

## 🛠️ Tech Stack

### Android (Kotlin)

* Jetpack Compose
* Firebase Authentication & Firestore
* MVVM Architecture
* StateFlow for UI state management

### iOS (Swift)

* SwiftUI
* Firebase Authentication & Firestore
* MVVM Pattern
* CoreLocation for geofencing

---

## 🚀 Installation

### Prerequisites

* Xcode 15+ (for iOS app)
* Android Studio Ladybug+ (for Android app)
* Firebase Project set up with Authentication + Firestore
* Apple Developer account (for iOS testing on devices)

---

## 📍 Geofencing Setup

The lab is located at:
**Latitude:** -25.5391879
**Longitude:** 28.0955252
**Radius:** 200m

Tutors must be within this radius to update lab status.

---

## 📜 License

This project is proprietary and developed for **Tshwane University of Technology (TUT)**.
All rights reserved © 2025.

---
