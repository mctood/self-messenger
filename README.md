# Samopisets

`Samopisets` is a local-first Android app for sending messages to yourself, organizing private notes into chats and folders, and turning conversations into lightweight task lists.

It is designed as a personal space rather than a networked messenger: there is no backend, no account system, and no external sync. Everything lives on the device, which makes the app fast, simple, and intentionally private.

## What The App Does

The app treats self-messaging as a flexible productivity tool:

- create personal chats for notes, reminders, drafts, or quick thoughts
- create to-do chats with checkable task items
- group chats into folders
- search across both chat titles and message contents
- customize chats with avatars and background images
- send text into the app directly from Android's share sheet
- jump to matching messages from search results

In short, it combines the familiarity of a messenger UI with the utility of a private notebook and a lightweight task manager.

## Highlights

- Local-first architecture with on-device storage
- Built fully with Jetpack Compose and Material 3
- Two chat modes: classic chat and to-do list
- Folder-based organization with custom icons
- Search across chats and messages
- Per-chat avatar and background customization
- Share intent support for saving text from other apps
- Message editing, deletion, and clipboard copy actions
- Animated navigation and folder swipe transitions
- Dedicated debug build configuration

## Why This Project Is Interesting

Most note-taking apps are form-based, and most messengers are network-first. `Samopisets` explores a middle ground:

- the UX feels like chatting, which lowers friction for quick capture
- the data model is simple enough to stay local and portable
- the same interface works for notes, journaling, inbox-style capture, and personal to-do lists

That makes it a good example of product thinking as much as mobile engineering.

## Tech Stack

- Kotlin
- Android SDK
- Jetpack Compose
- Material 3
- Android Navigation Compose
- Kotlin Coroutines
- Gson
- Gradle Kotlin DSL

## Main Features

### 1. Local personal chats

Users can create named chats and write messages to themselves in a familiar messenger-style interface.

### 2. To-do chats

Chats can also be created as task lists. Each entry can be marked as done, edited, or removed, and the app shows aggregate completion stats.

### 3. Folder organization

Chats can be assigned to folders, and folders can be created and deleted directly from the app. The UI supports folder switching and swipe navigation.

### 4. Search

The app can search both:

- chat names
- individual messages

Search results can open the matching chat and automatically scroll to the selected message.

### 5. Visual customization

Each chat can have:

- a custom avatar
- a custom background image

Images are selected from device media and stored locally inside the app sandbox.

### 6. Android share integration

The app supports `ACTION_SEND` for plain text, so text from other apps can be forwarded directly into one of your private chats.

## Architecture Overview

The project is structured as a single Android app module with a Compose-based UI and simple local persistence.

### Navigation screens

- `MainScreen` - chat list, folders, and top-level app actions
- `DetailsScreen` - create a new chat and choose its type/folder
- `ChatScreen` - view and edit messages inside a chat
- `SearchScreen` - search across chats and messages

### Core storage model

The app stores its data locally as JSON files in internal app storage:

- `chats.json`
- `folders.json`
- `settings.json`

This keeps the persistence layer lightweight and easy to reason about for a single-user offline app.

### Data concepts

- `Chat` - a conversation-like container
- `Message` - a single note or task item
- `Folder` - an organizational grouping for chats
- `Settings` - app-level UI preferences

## Project Structure

```text
app/
  src/main/java/com/rogatka/introgram/
    MainActivity.kt        App entrypoint and navigation host
    Storage.kt             Local models, JSON persistence, helpers
    Components.kt          Shared Compose UI components
    nav/                   Main screens
    modals/                Dialogs and modal flows
    ui/theme/              Theme, colors, typography
  src/main/AndroidManifest.xml
  build.gradle.kts
gradle/
build.gradle.kts
settings.gradle.kts
```

## Requirements

- Android Studio
- Android SDK 36
- JDK 11
- Android device or emulator running Android 8.0+ (`minSdk 26`)

## Build And Run

1. Open the project in Android Studio.
2. Let Gradle sync dependencies.
3. Run the `app` configuration on an emulator or physical device.

You can also build from the command line:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Current App Version

From the Gradle configuration:

- `versionName`: `1.6`
- `versionCode`: `7`
- `minSdk`: `26`
- `targetSdk`: `36`

## Privacy Model

This project is intentionally local-first:

- no backend
- no user accounts
- no cloud sync
- no remote message delivery

All chats, folders, settings, and selected images are stored on the device.

## Potential Future Improvements

- add export and import for backups
- add encryption for local data
- support image and file messages
- add tags or pinned chats
- add widgets and richer shortcuts
- improve automated testing coverage

## License

No license file is currently included in the repository.
