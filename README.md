# Giggz — Smart Job Marketplace & Local Community Trade

Giggz is a hyper-local, high-fidelity Android mobile application built with **Kotlin** and **Jetpack Compose**. It serves as a decentralized connection platform for skilled workers, local employers, and neighborhood community traders. 

This app is architected as a **fully functional offline-first demo**. It operates with an embedded local **Room Database** pre-seeded with realistic mock data, meaning the entire app can be built, run, tested, and explored right out of the box **without requiring any external API keys, backends, servers, or cloud setup**.

---

## 🎨 Major Visual & Functional Core Modules

1. **SplashScreen & Auth Flow**:
   - Welcome onboarding screen featuring high-contrast illustration cards and direct buttons to log in or register.
   - **Developer Fast-Testing Panel**: A single-tap login board that lets you instantly authenticate as any preset user role (Worker, Employer, Admin) to explore their distinct workspace experiences.

2. **Worker Dashboard (`Worker` Role)**:
   - Browse recommended contracts matching user skills.
   - Live Search, filter by budget ranges, and location fields.
   - View detailed Job Spec cards, apply with CVs, and track Application Statuses.

3. **Employer Dashboard (`Employer` Role)**:
   - Post new jobs or contract requirements with specific budgets, categories, deadlines, and images.
   - View and manage incoming job applications from available local workers.
   - Accept, complete, or reject applicant submissions with live status synchronization.

4. **Ama Sampo (The Local Marketplace)**:
   - A community trade bazaar featuring categories like Phones, Electronics, Services, and Furniture.
   - **Book/Rent Flow (Latest)**: Custom-tailored category screen for renting equipment or booking hourly/daily services, complete with a duration stepper, unit selector, dynamic cost calculation, additional notes, and instant booking submission.
   - Express interest inside any listing to instantly spin up an encrypted-style local message chat thread with the seller.

5. **Local Chat & Direct Messaging Engine**:
   - Realistic two-way instant messaging thread dashboard.
   - Auto-categorized inbox separating worker threads from retail buyer/seller threads.
   - Interactive chat rooms that support sending text messages, loading simulated image files, or attaching PDFs.

6. **Admin Panel Screen (`Admin` Role)**:
   - Full statistics overview (active users, listings, jobs, application metrics).
   - Content Moderation panel to review user-submitted reports for listings or users, with resolve/suspend functionality.
   - Platform-wide push announcements board to broad-cast news across the ecosystem.

---

## 🏗️ Technical Stack & Architecture

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose (using Material Design 3 guidelines)
- **Local Persistence**: SQLite under Jetpack Room Database (DAOs, Entities, Flows)
- **Architecture**: MVVM (Model-View-ViewModel) with StateFlow
- **Dependency Management**: Gradle Version Catalog (`libs.versions.toml`)
- **Image Loading**: Coil Compose for async image streaming

---

## 🚀 How to Export/Download the ZIP from AI Studio

To test this app on your local machine or load it in your own IDE:
1. Locate the **Settings Menu (Gear Icon)** or the project download/export buttons in the top-right toolbar of the **Google AI Studio Build** browser workspace.
2. Click **Export project as ZIP** or **Push to GitHub**.
3. Save the ZIP file and extract it on your local system.

---

## 💻 How to Compile and Run Locally

### Prerequisites
- **Android Studio** (Koala | 2024.1 or newer recommended)
- **Java SE Development Kit (JDK)**: JDK 17 or JDK 21 (configured in Android Studio under Gradle Settings)
- An Android Emulator (with API Level 30+) or a physical Android device connected via USB with USB Debugging enabled.

### Import and Build
1. Open Android Studio.
2. Select **File -> New -> Import Project...** (or **Open**) and choose the root directory where you extracted the ZIP.
3. Allow Gradle to sync dependencies automatically. Since the project uses a Version Catalog (`libs.versions.toml`), all library versions are strictly tied and resolved on load.
4. Click the green **Run (Play button)** at the top or use the shortcut `Shift + F10` to deploy Giggz to your device.

### Manual CLI Compilation
If you prefer compiling via the terminal, navigate to the project directory and execute:
```bash
# Clean the project
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Run unit / Robolectric tests
./gradlew :app:testDebugUnitTest
```
The compiled APK will be located inside the folder:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔑 Default Preset Accounts for Fast Testing

When you open the app, you can use our single-tap buttons on the **SplashScreen** to log in, or type in the credentials manually on the **LoginScreen**:

| Role | Username (Email) | Password | Purpose |
| :--- | :--- | :--- | :--- |
| **Worker (Artisan)** | `john.carpenter@giggz.com` | `password` | Search & apply for woodworking jobs, check application status, view incoming messages, list items on Ama Sampo. |
| **Employer** | `buildtech@giggz.com` | `password` | Post jobs/gigs, review worker applications, message workers, accept/hire people, and view contract stats. |
| **Administrator** | `admin@giggz.com` | `admin123` | Moderate reports, broadcast news notifications, check general platform analytics. |

---

## 📂 Codebase File Structure

```
├── app/src/main/java/com/example/
│   ├── MainActivity.kt               # Entrypoint, configures edge-to-edge & loads ViewModel
│   ├── data/
│   │   ├── GiggzDatabase.kt          # Room DB declaration, Schemas, DAOs & Entities
│   │   └── GiggzRepository.kt        # Repository bridging DAOs and pre-seeding mock data
│   ├── ui/
│   │   ├── GiggzApp.kt               # App Scaffold, Main Navigation, & TopBar controller
│   │   ├── GiggzViewModel.kt         # Master state machine handling all business logic
│   │   ├── components/
│   │   │   └── CommonComponents.kt   # High-quality dynamic buttons, star rating, avatar cards
│   │   ├── screens/
│   │   │   ├── AuthScreens.kt        # Splash, Login, Register, Worker onboarding steps
│   │   │   ├── WorkerDashboardScreen.kt # Job search, filter chips, and apply flows
│   │   │   ├── MarketplaceScreen.kt  # Ama Sampo listings, Book/Rent sub-dialog, add listing form
│   │   │   ├── AdminPanelScreen.kt   # Admin dashboard, announcements, moderation
│   │   │   └── OtherSections.kt      # Direct messaging threads, chat room, user profile details
│   │   └── theme/
│   │       ├── Color.kt              # M3 Giggz palette colors
│   │       └── Theme.kt              # Dark/Light theme setup and typeface pairing
```

Have fun exploring the **Giggz** ecosystem! If you have any questions or want to request custom features, just ask the AI agent to implement them directly.
