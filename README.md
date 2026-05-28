# Catcha - Decision Maker
---

## Key Features

### 1. Finger Chooser
*   **Real-time Multi-touch**: Place multiple fingers on the screen. Each finger is detected dynamically, drawing a unique glowing neon circle with expanding animated ripples.
*   **Customizable Winners**: Choose how many players to pick randomly (from 1 to 4) using the glassmorphic floating control panel at the top.
*   **Visual Countdown**: A white outer boundary shrinks around each finger during a 3-second countdown, accompanied by tactile haptic ticks at each second.
*   **Winner Celebration**: The selected winning fingers burst into colorful neon particle sparkles, while the non-selected fingers gracefully fade out.

### 2. Custom Roulette
*   **Canvas-drawn Spin Wheel**: Wheel sectors are drawn with mathematical precision on a Compose Canvas, featuring neatly rotated text labels inside each segment.
*   **Dynamic Option Manager**: Add, edit, or delete custom options easily (supporting up to 12 choices). Includes automatic assignment from a premium, harmonious color palette.
*   **Haptic Ticking Physics**: The wheel decelerates using a realistic Cubic Bezier curve, triggering light haptic vibrations as division boundaries pass the indicator arrow.
*   **Instant Presets**: Load quick-decision templates including "What to Eat", "Who Cleans Dishes", "Truth or Dare", and "Yes or No".
*   **Victory Dialog Overlay**: Displays the final outcome in a futuristic neon card with an explosion of colorful confetti.

### 3. 3D Coin Flipper
*   **3D Flight Simulation**: The coin flies upwards, rotates on its X-axis, zooms in, and lands back down with a decelerating spring-physics animation.
*   **Custom Side Labels**: Easily customize the text on both sides of the coin (e.g., "BUY" vs "SAVE", "GO" vs "STAY").
*   **Bouncing Catch**: Strong tactile vibration feedback when the coin lands to announce the final decision.

---

## Tech Stack

| Component | Technology / Details |
| --- | --- |
| Language | Kotlin (1.9.22) |
| UI Framework | Jetpack Compose with Material 3 |
| Styling | Obsidian Cyber Dark Theme (Obsidian Background, Glowing Cyan, Neon Violet, and Pink accents) |
| Animations | Compose Animation Core (Animatable, CubicBezierEasing, InfiniteTransition) |
| Haptics | Android OS Vibrator API |
| Dependency Management | Gradle Version Catalog (libs.versions.toml) |
| Build Target | Compile SDK 35, Min SDK 26 (Android 8.0) |

---

## How to Build and Run

This project is ready to be opened in Android Studio or compiled directly via the command-line interface.

### Prerequisites:
*   Java Development Kit (JDK) 17 or newer.
*   Android SDK Platform API 34 or 35.

### Building via Terminal:
1.  Open your terminal or PowerShell in the root directory of the project: `d:\Project\app\Catcha`.
2.  Run the following Gradle command to compile a debug APK:
    ```powershell
    .\gradlew assembleDebug
    ```
3.  Once the build completes, the compiled APK will be located at:
    ```path
    app/build/outputs/apk/debug/app-debug.apk
    ```
4.  You can copy the `app-debug.apk` file directly to your Android device to install and run it.

### Opening in Android Studio:
1.  Open Android Studio, select **Open Project**.
2.  Navigate to the directory `d:\Project\app\Catcha`.
3.  Wait for the Gradle sync to complete, then connect your physical Android device or launch an Emulator.
4.  Click the green **Run** button to install and launch the application.

---

## Project Structure
```path
Catcha/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── kotlin/org/example/
│   │       │   ├── MainActivity.kt        # State navigation and core app entry point
│   │       │   ├── theme/
│   │       │   │   ├── Color.kt           # Color definitions for the Obsidian theme
│   │       │   │   └── Theme.kt           # Material 3 setup and Edge-to-edge drawing
│   │       │   ├── components/
│   │       │   │   └── SparkleConfetti.kt # Canvas-based particle confetti system
│   │       │   └── screens/
│   │       │       ├── MainMenuScreen.kt  # Glassmorphic landing dashboard
│   │       │       ├── FingerChooserScreen.kt # Multi-touch pointer tracker
│   │       │       ├── RouletteScreen.kt  # Custom spin wheel and ticking haptics
│   │       │       └── CoinFlipScreen.kt  # Interactive 3D coin flipper
│   │       └── res/
│   │           ├── values/
│   │           │   ├── strings.xml        # Localization text keys
│   │           │   └── themes.xml         # Android system themes style
│   │           └── drawable/
│   │               ├── ic_launcher_background.xml
│   │               └── ic_launcher_foreground.xml
│   └── build.gradle.kts                   # Android build and dependency configurations
├── gradle/
│   └── libs.versions.toml                 # Centralized dependency Version Catalog
└── settings.gradle.kts                    # Gradle settings and module inclusions
```

---

*Made for endless fun with friends. Enjoy choosing!*