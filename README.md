# Samopisets

`Samopisets` is a small Android app for writing messages to yourself.

You can use it as a private notebook, a list of tasks, or just a place to quickly save thoughts. The app works locally on the device and does not need a backend.

## What it can do

- create regular chats with yourself
- create to-do chats with checkboxes
- group chats into folders
- search by chat name and message text
- edit and delete messages
- copy message text
- set custom chat avatars
- set custom chat backgrounds
- receive shared text from other apps

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Coroutines
- Gson
- Android SDK

## How it stores data

The app stores everything locally in internal storage.

Main files:

- `chats.json`
- `folders.json`
- `settings.json`

There is no server, no login, and no sync.

## Project structure

```text
app/
  src/main/java/com/rogatka/introgram/
    MainActivity.kt
    Storage.kt
    Components.kt
    nav/
    modals/
    ui/theme/
```

## Run

Open the project in Android Studio and run the `app` configuration.

You can also build from the command line:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Requirements

- Android Studio
- JDK 11
- Android SDK 36
- Android 8.0+ (`minSdk 26`)

## Version

- `versionName`: `1.6`
- `versionCode`: `7`

## Notes

This is a local-first project. It is closer to a personal messenger for yourself than to a classic chat app.
