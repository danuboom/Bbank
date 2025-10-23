# Bbank

Bbank is a simple mobile banking application for Android that demonstrates modern Android app development practices. It allows users to manage their bank accounts, view transactions, and transfer funds.

## Key Features

*   **User Authentication:** Secure login and logout functionality.
*   **Account Management:** Create and view bank accounts.
*   **Transaction History:** View a detailed list of all past transactions.
*   **Fund Transfers:** Transfer money between accounts.
*   **Transaction Receipts:** View details of a specific transaction.
*   **Auto-generated Account Numbers:** Account numbers are automatically generated for new accounts.
*   **Owner Display Names:** Transaction details show the display names of the sender and receiver.

## Tech Stack & Architecture

This project utilizes a modern Android tech stack and follows a clean architecture pattern.

*   **UI:** Built entirely with [Jetpack Compose](https://developer.android.com/jetpack/compose) for a declarative and modern UI.
*   **Architecture:** Follows a typical MVVM (Model-View-ViewModel) architecture.
    *   **ViewModel:** [Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) is used to manage UI-related data in a lifecycle-conscious way.
    *   **Repository:** The repository pattern is used to abstract the data sources.
*   **Navigation:** [Jetpack Navigation](https://developer.android.com/guide/navigation) for Compose is used to handle in-app navigation.
*   **Dependency Injection:** [Hilt](https://dagger.dev/hilt/) is used for dependency injection to manage dependencies and improve modularity.
*   **Database:** [Room](https://developer.android.com/training/data-storage/room) is used for local data persistence.
*   **Language:** The entire project is written in [Kotlin](https://kotlinlang.org/).

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
