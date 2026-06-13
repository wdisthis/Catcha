# Catcha - A Fun Decision-Making Assistant

Catcha is an interactive Android application built using Jetpack Compose designed to help resolve everyday dilemmas in a playful and engaging way. Featuring a hand-drawn school notebook theme with crayon aesthetics, the app turns mundane decisions into satisfying, tactile, and visual experiences.

---
## Star History

<p align="center">
  <a href="https://www.star-history.com/?repos=wdisthis%2FCatcha&type=date&legend=top-left">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=wdisthis/Catcha&type=date&theme=dark&legend=top-left" />
      <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=wdisthis/Catcha&type=date&legend=top-left" />
      <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=wdisthis/Catcha&type=date&legend=top-left" />
    </picture>
  </a>
</p>


## Core Features Summary

Below is an overview of the decision-making tools included in Catcha:

| Feature Name | Input Method | Customization Options | Primary Visual Style | Haptic & Sound Effects |
| --- | --- | --- | --- | --- |
| **Finger Chooser** | Multi-touch (Fingers) | Number of winners (1 to 4), Minimum fingers (2 to 6) | Expanding ripples, shrinking countdown arc, and a spring-physics spotlight reveal | Pop sound effects, countdown tick vibrations, and a strong winner haptic pulse |
| **Finger Grouper** | Multi-touch (Fingers) | Number of groups (2 to 4), Minimum fingers (3 to 6) | Sketchy hand-drawn crayon connector lines and handwritten floating group bubbles | Pop sound effects, countdown haptic ticks, and a final grouping vibration |
| **Finger Order** | Multi-touch (Fingers) | Minimum fingers (2 to 10 via a dropdown) | Numbers inside circles, floating ordinal bubble labels, and sketched dashed sequence paths | Pop sound effects, countdown haptic ticks, sequential order reveal vibes |

---

## Feature Details

### 1. Finger Chooser
* **Real-time Multi-touch Pointer Tracking**: Place multiple fingers on the screen simultaneously. Each finger is tracked with a sketchy gray core, double outlines, and expanding animated ripple waves.
* **Customizable Winner Count**: Choose how many players to pick randomly (from 1 to 4) using the floating control panel at the top.
* **Visual Shrinking Countdown**: A thick border shrinks around each finger during a 4-second countdown, indicating when the decision will trigger.
* **Spotlight Celebration Reveal**: Selected winning fingers remain highlighted with a spring-physics scaling pulse (beating scale), while the rest of the screen is overlaid with the winner's harmonic crayon tint, hiding non-selected areas.

### 2. Finger Grouper
* **Balanced Team Division**: Automatically divide active players evenly into balanced teams (supporting up to 4 teams).
* **Hand-Drawn Connecting Paths**: Players in the same team are connected by hand-drawn sketchy crayon lines, rendering multiple jittery strokes for a real penciled look.
* **Handwritten Balloon Labels**: Each player's finger displays a floating handwritten box indicating their team (e.g., "GROUP 1", "GROUP 2").
* **Flexible Configuration**: Choose your desired team count (2 to 4) and minimum participants instantly before starting.

### 3. Finger Order
* **Random Turn Sequence**: Touch together to generate a random turn order from 1 to N players.
* **Sequential Path Connection**: Draws sketched black dashed connecting arrows between the fingers following the generated order (e.g., 1 -> 2 -> 3).
* **Ordinal Balloon Labels**: Floating handwritten bubbles appear above each finger showing their exact sequence position (e.g., "1st", "2nd", "3rd").
* **Compact Dropdown Configuration**: Choose the minimum required fingers (from 2 to 10) easily using a compact dropdown menu at the top.
* **Persisted Finger Colors**: Once the order is determined, circles maintain their distinct assigned crayon colors even if fingers are lifted from the screen.

### 4. Splash Screen
* **Polished Entrance Animation**: The Catcha logo enters the screen with a combined fade-in and scale-bounce animation.
* **Doodle Styling**: Features a hand-drawn double-bordered box around the logo and a crayon-red zigzag accent under the title text.

---

## Aesthetics and Styling System

Catcha is meticulously styled to look and feel like an interactive, handwritten school notebook:
* **Textured Notebook Paper Background**: A warm creamy paper color (`ObsidianBg`: `0xFFFCFAF5`) layered with faint blue-gray grid lines and a pink/red left margin line.
* **Background Sketch Doodles**: Subtle, low-opacity drawings (a smiling cloud, a scribble star, paper binding loops, and a happy face) are scattered in the background.
* **Thick Charcoal Outlines**: Interactive buttons, cards, text inputs, and dialogs are bordered with thick, ink-black lines (`BorderColor`: `0xFF121212`) representing charcoal pen strokes.
* **3D Solid Shadows**: Cards and buttons utilize flat, solid-black offsets to the bottom-right for a playful, cartoon/comic book depth.
* **Bubble SFX Engine**: Interacting with buttons, cards, and starting games triggers playful, satisfying bubble-pop audio effects.
* **Handwritten Typography System**: 
  - **Cabin Sketch (Bold)**: Used for all large title components, headers, countdown numbers, and status states, giving them a rich hand-shaded hatching look.
  - **Cabin (Medium/SemiBold/Bold)**: Used for secondary text, descriptions, lists, and dialog bodies, ensuring maximum readability while maintaining a friendly, rounded handwritten feel.
  - **Font Weight Safeguard**: All font weights are mapped correctly in the Compose typography system to prevent fallback to generic system fonts (like Roboto).

---

## Technical Specifications

| Component | Technology / Detail |
| --- | --- |
| Programming Language | Kotlin (1.9.22) |
| UI Framework | Jetpack Compose with Material 3 |
| Design Theme | School Notebook Paper & Playful Crayon Doodle Art Theme |
| Typography System | Cabin Sketch (Titles) & Cabin (Clean text) - Bundled Offline |
| Sound Effects Engine | Bubble Sound Player (Android MediaPlayer API) |
| Haptic Feedback | Android OS Vibrator API |
| Dependency Management | Gradle Version Catalog (libs.versions.toml) |
| Build Parameters | Compile SDK 35, Min SDK 26 (Android 8.0) |

---

## How to Build and Run

This project is built using Gradle and can be opened in Android Studio or compiled directly via the terminal.

### Prerequisites:
* Java Development Kit (JDK) 17 or newer.
* Android SDK Platform API 34 or 35.

### Compiling via Terminal:
1. Open your terminal or PowerShell in the root directory of the project, for example: `d:\Project\app\Catcha`.
2. Run the following Gradle command to compile a debug APK:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
3. Once the build completes, the compiled APK will be located at:
   ```path
   app/build/outputs/apk/debug/app-debug.apk
   ```
4. Copy the `app-debug.apk` file directly to your physical Android device to install and run it.

### Opening in Android Studio:
1. Launch Android Studio, select **Open Project**.
2. Navigate to the directory `d:\Project\app\Catcha`.
3. Wait for the Gradle sync to complete successfully.
4. Connect a physical Android device (with USB debugging enabled) or launch an Emulator.
5. Click the green **Run** button to install and launch the application.

---

## Project Structure

```path
Catcha/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── kotlin/org/example/
│   │       │   ├── MainActivity.kt        # State navigation and core app entry point
│   │       │   ├── audio/
│   │       │   │   └── BubbleSoundPlayer.kt # Bubble-pop sound effects manager
│   │       │   ├── components/
│   │       │   │   ├── DoodleComponents.kt # Handwritten & school notebook UI components
│   │       │   │   └── SparkleConfetti.kt # Canvas-based particle confetti system
│   │       │   ├── data/
│   │       │   │   └── AppSettings.kt     # App state storage for audio/haptics settings
│   │       │   ├── screens/
│   │       │   │   ├── SplashScreen.kt    # Animated entrance splash screen
│   │       │   │   ├── MainMenuScreen.kt  # Notebook-themed home dashboard
│   │       │   │   ├── FingerChooserScreen.kt # Multi-touch pointer random chooser screen
│   │       │   │   ├── FingerGrouperScreen.kt # Multi-touch finger grouping screen
│   │       │   │   └── FingerOrderScreen.kt # Multi-touch random turn order screen
│   │       │   └── theme/
│   │       │       ├── Color.kt           # Color definitions for Crayon & Notebook theme
│   │       │       ├── Theme.kt           # Material 3 setup and Edge-to-edge configurations
│   │       │       └── Type.kt            # Typography configurations and custom font families
│   │       └── res/
│   │           ├── values/
│   │           │   ├── strings.xml        # Localization text keys
│   │           │   └── themes.xml         # Native Android base themes configuration
│   │           ├── drawable/
│   │           │   ├── catcha_ico.png     # Application logo icon
│   │           │   ├── ic_launcher_background.xml
│   │           │   └── ic_launcher_foreground.xml
│   │           └── font/
│   │               ├── cabin_bold.ttf           # Clean handwriting bold
│   │               ├── cabin_medium.ttf         # Clean handwriting medium (default normal weight)
│   │               ├── cabin_regular.ttf        # Clean handwriting regular
│   │               ├── cabin_semibold.ttf       # Clean handwriting semibold
│   │               └── cabin_sketch_bold.ttf    # Sketchy hand-shaded bold (default sketch weight)
│   └── build.gradle.kts                   # Android module build and dependencies configuration
├── gradle/
│   └── libs.versions.toml                 # Centralized dependency Version Catalog
└── settings.gradle.kts                    # Gradle settings and module inclusions
```
