# Bbank

Bbank is a simple mobile banking application for Android that demonstrates modern Android app development practices. It allows users to manage their bank accounts, view transactions, and transfer funds.

## Key Features

*   **User Authentication:** Secure registration, login, and logout functionality.
*   **Account Management:** Full CRUD (Create, Read, Update, Delete) functionality for bank accounts.
*   **Dashboard:** A central hub to view all accounts and their balances.
*   **Fund Transfers:** A comprehensive transfer screen with real-time form validation to prevent errors.
*   **Transaction History:** A detailed list of all past transactions, grouped by date with sticky headers for easy navigation.
*   **Transaction Receipts:** Generates a shareable e-Receipt after each successful transaction.
*   **Auto-generated Account Numbers:** Account numbers are automatically generated for new accounts.
*   **Owner Display Names:** Transaction details show the display names of the sender and receiver.

## Tech Stack & Architecture

This project utilizes a modern Android tech stack and follows a clean architecture pattern.

*   **UI:** Built entirely with **Jetpack Compose** and **Material 3** for a declarative, modern, and beautiful UI.
*   **Architecture:** Follows a robust **MVVM (Model-View-ViewModel)** architecture.
    *   **State Management:** Uses **StateFlow** for reactive state management.
    *   **ViewModel:** [Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) is used to manage UI-related data in a lifecycle-conscious way.
    *   **Repository:** The repository pattern is used to abstract the data sources.
*   **Navigation:** [Jetpack Navigation](https://developer.android.com/guide/navigation) for Compose is used to handle in-app navigation.
*   **Dependency Injection:** [Hilt](https://dagger.dev/hilt/) is used for dependency injection to manage dependencies and improve modularity.
*   **Database:** [Room](https://developer.android.com/training/data-storage/room) is used for local data persistence.
*   **Language:** The entire project is written in **Kotlin**.

## Roadmap to Production

The goal is to evolve Bbank from a prototype to a production-ready application. The plan involves developing a secure API and backend, implementing a comprehensive testing strategy (Unit, Integration, and UI tests), and establishing a CI/CD pipeline for automated builds and deployments. This will ensure Bbank is a reliable, secure, and scalable application ready for the Google Play Store.

## Future Features

*   **Biometric Authentication:** Login with fingerprint or face unlock.
*   **QR Code Payments:** Scan QR codes for quick and easy payments.
*   **Transaction Search & Filtering:** Search and filter transaction history.
*   **Push Notifications:** Real-time alerts for transactions.
*   **Dark Mode:** A dark theme for comfortable viewing in low-light environments.
*   **Financial Overview:** Dashboard charts to visualize income and expenses.

## Getting Started

To get the project up and running, follow these simple steps:

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    ```
2.  **Open in Android Studio:**
    Open the cloned project in Android Studio.
3.  **Gradle Sync:**
    Wait for Android Studio to download and sync all the project dependencies.
4.  **Run the app:**
    Run the application on an Android emulator or a physical device.
