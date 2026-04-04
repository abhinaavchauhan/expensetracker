# 💰 ExpenseTracker - Premium Android Expense Tracker

A production-ready, premium fintech-level Android application built with **Java**, **Groovy DSL**, and **Clean Architecture (MVVM)**. Features smooth animations, Material Design UI, MPAndroidChart visualizations, and zero-crash stability.

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java |
| Build | Gradle (Groovy DSL) |
| Architecture | Clean Architecture + MVVM |
| Database | Room (SQLite) |
| UI | XML + Material Design 3 |
| Charts | MPAndroidChart |
| Animations | ViewPropertyAnimator + ValueAnimator |
| Min SDK | 24 |
| Target SDK | 34 |

## 📁 Project Structure

```
com.expensetracker/
├── presentation/          # UI Layer
│   ├── dashboard/         # Dashboard with balance, charts, recent transactions
│   ├── addexpense/        # Add expense/income with categories
│   ├── transactions/      # Transaction list with search, filter, swipe-delete
│   ├── analytics/         # Charts and insights (weekly/monthly/yearly)
│   ├── settings/          # Dark mode, budget, export
│   └── splash/            # Animated splash screen
├── domain/                # Business Logic
│   ├── model/             # Expense, Category models
│   └── usecase/           # AddExpense, GetExpenses, DeleteExpense
├── data/                  # Data Layer
│   ├── local/
│   │   ├── db/            # Room Database (Singleton)
│   │   ├── dao/           # Data Access Objects
│   │   └── entity/        # Room Entities
│   └── repository/        # Repository pattern
└── core/                  # Utilities
    ├── utils/             # Date, Currency, Preferences
    ├── animations/        # Custom animations
    └── constants/         # Category definitions
```

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest stable)
- JDK 17
- Android device or emulator (API 24+)

### Setup
1. Open the project in Android Studio
2. Let Gradle sync complete
3. Connect your Android device via USB (enable USB debugging)
4. Click Run ▶

## ✨ Features

- **Dashboard** - Animated balance counter, pie chart, recent transactions
- **Add Expense** - Category grid, date picker, income/expense toggle
- **Transactions** - Search, filter chips, swipe-to-delete with confirmation
- **Analytics** - Bar/pie charts, weekly/monthly/yearly views, insight cards
- **Settings** - Dark/light mode, budget limits, CSV export
- **Animations** - Counter animations, slide transitions, press effects

## 🎨 Design System

- **Dark-first theme** (#0F1115 background)
- **Primary**: #6C63FF | **Accent**: #00C9A7
- **Material Design 3** components
- **16-24dp** rounded corners
- **Premium gradient** cards

## 🏗️ Architecture

```
UI (Fragment) → ViewModel → UseCase → Repository → Room DB
Room DB → LiveData → ViewModel → UI (Observer)
```

All database operations run on background threads via `ExecutorService`.
