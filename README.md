# RentIt - Android App

A basic Android application built with Kotlin that displays centered text on the main screen.

## Project Structure

This is a standard Android project with the following structure:

```
rentIt/
├── app/
│   ├── build.gradle                    # App-level build configuration
│   ├── proguard-rules.pro              # ProGuard rules
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml     # App manifest
│           ├── java/com/rentit/app/
│           │   └── MainActivity.kt     # Main Activity
│           └── res/
│               ├── layout/
│               │   └── activity_main.xml   # Main layout with centered text
│               ├── values/
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               └── mipmap-anydpi-v26/
│                   ├── ic_launcher.xml
│                   └── ic_launcher_round.xml
├── build.gradle                        # Project-level build configuration
├── settings.gradle                     # Project settings
├── gradle.properties                   # Gradle properties
└── local.properties                    # Local SDK path (you'll need to configure this)
```

## Requirements

- Android Studio (Arctic Fox or newer recommended)
- JDK 17 or higher
- Android SDK with API Level 34 (Android 14)
- Minimum Android API Level 24 (Android 7.0)

## How to Open and Run in Android Studio

1. **Open the Project:**
   - Launch Android Studio
   - Click on "Open" or "File" → "Open"
   - Navigate to the `rentIt` folder and select it
   - Click "OK"

2. **Wait for Gradle Sync:**
   - Android Studio will automatically sync the Gradle files
   - This may take a few minutes on the first run
   - You'll see a progress bar at the bottom of the screen

3. **Configure SDK Path (if needed):**
   - If you see an error about SDK location, Android Studio will usually configure it automatically
   - Alternatively, you can manually edit `local.properties` and set:
     ```
     sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
     ```
   - Replace `YourUsername` with your actual Windows username

4. **Add Launcher Icon Images (Optional):**
   - The project uses adaptive icons defined in XML
   - For production, you should add actual launcher icon images using:
     - Right-click on `res` folder → New → Image Asset
     - Follow the wizard to create launcher icons

5. **Run the App:**
   - Connect an Android device via USB (with USB debugging enabled) OR
   - Start an Android Virtual Device (AVD) emulator:
     - Tools → Device Manager → Create Device
   - Click the green "Run" button (▶) in the toolbar
   - Select your device/emulator
   - The app will build and launch

## What the App Does

The app displays a simple screen with the text **"Welcome to RentIt!"** centered in the middle of the page.

## Key Files

- **MainActivity.kt**: The main activity class that uses ViewBinding to inflate the layout
- **activity_main.xml**: ConstraintLayout with a TextView centered both horizontally and vertically
- **strings.xml**: Contains the welcome text string resource

## Tech Stack

- **Language**: Kotlin 1.9.0
- **Build System**: Gradle 8.0
- **Android Gradle Plugin**: 8.1.0
- **Compile SDK**: 34 (Android 14)
- **Target SDK**: 34
- **Min SDK**: 24 (Android 7.0)
- **Dependencies**:
  - AndroidX Core KTX
  - AppCompat
  - Material Components
  - ConstraintLayout
  - ViewBinding (enabled)

## Next Steps

You can now start building your app! Some suggestions:

- Modify the text in `res/values/strings.xml`
- Update the layout in `activity_main.xml`
- Add more activities or fragments
- Implement your app's specific features

## Troubleshooting

- **Gradle sync failed**: Make sure you have an internet connection for downloading dependencies
- **SDK not found**: Configure the SDK path in `local.properties`
- **Build errors**: Try "Build" → "Clean Project" then "Build" → "Rebuild Project"
- **Emulator slow**: Consider using a physical device or enabling hardware acceleration for the emulator

Happy coding! 🚀
