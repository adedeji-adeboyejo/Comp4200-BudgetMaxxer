# Comp4200-BudgetMaxxer

BudgetMaxxer is an offline Android expense tracking app built in Java for COMP 4200(Mobile App Development) project.

Log daily expenses, organize them by category, browse your full spending history, and view monthly breakdowns; all stored locally on your device with no account or internet required.

---

## Demo

https://uwin365-my.sharepoint.com/:v:/g/personal/marcusa_uwindsor_ca/IQDL8UB6dc-kQ7EcmNp0eId_AZaSDxQxLnZifdIKqLewi00?nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJPbmVEcml2ZUZvckJ1c2luZXNzIiwicmVmZXJyYWxBcHBQbGF0Zm9ybSI6IldlYiIsInJlZmVycmFsTW9kZSI6InZpZXciLCJyZWZlcnJhbFZpZXciOiJNeUZpbGVzTGlua0NvcHkifX0&e=FMw2hh

---

## Features

- **Dashboard** - displays the current month total, last 5 expenses, month-to-month navigation arrows
- **Add Expenses** - log the amount, category, description, and date picker for an expense
- **Edit & Delete** - tap any expense to edit or delete it
- **History** - full scrollable list with dynamic month filter and swipe-to-delete (with Undo)
- **Summary** - monthly category breakdown with totals, percentages, and progress bars
- **Fully offline** - no account, no internet, no cloud

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |
| Architecture | MVVM |
| Database | Room (SQLite) |
| UI | Material Design 3 |
| Async | LiveData + ExecutorService |
| Build | Gradle (Kotlin DSL) |

---

## Architecture

```
Activities + Adapters       ← UI layer, observes LiveData
        ↕
ExpenseViewModel            ← survives rotation, switchMap for month filtering
        ↕
ExpenseRepository           ← thread-safe, wraps writes in ExecutorService
        ↕
ExpenseDao / CategoryDao    ← Room-generated SQL
        ↕
AppDatabase (SQLite)        ← singleton, seeds 6 categories on first install
```

---

## Getting Started

### Prerequisites

- Android Studio (Ladybug or newer)
- Java 11+
- Android device or emulator running Android 7.0+ (API 24+)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/adedeji-adeboyejo/Comp4200-BudgetMaxxer.git
   ```

2. Open the project in Android Studio:

3. Wait for Gradle to sync (dependencies download automatically)

4. Run the app:
   - Start an Android Virtual Device (AVD) from the Device Manager
   - Press **Run** or `Shift + F10`

The app installs and launches automatically. On first launch, 6 default categories are seeded into the database.

---

## Default Categories

| Emoji | Category |
|---|---|
| 🍔 | Food | `#F59E0B` |
| 🚌 | Transport | `#3B82F6` |
| 💡 | Bills | `#8B5CF6` |
| 🛍️ | Shopping | `#EC4899` |
| 💊 | Health | `#10B981` |
| 📦 | Other | `#9CA3AF` |

---

## Project Structure

```
app/src/main/java/com/group22/budgetmaxxer/
│
├── database/
│   ├── AppDatabase.java        # Room singleton, seeds categories on first launch
│   ├── Expense.java            # @Entity — expenses table
│   ├── Category.java           # @Entity — categories table
│   ├── CategoryTotal.java      # Projection class for summary query
│   ├── ExpenseDao.java         # All expense queries
│   └── CategoryDao.java        # All category queries
│
├── repository/
│   └── ExpenseRepository.java  # Thread-safe DB access layer
│
├── viewmodel/
│   └── ExpenseViewModel.java   # LiveData streams + switchMap month filter
│
├── ui/
│   ├── ExpenseAdapter.java     # RecyclerView adapter (Dashboard + History)
│   └── SummaryAdapter.java     # RecyclerView adapter (Summary screen)
│
├── SplashActivity.java         # Entry point, 1.5s logo, navigates to Dashboard
├── DashboardActivity.java      # Home screen
├── AddExpenseActivity.java     # Add + edit + delete expenses
├── HistoryActivity.java        # Full list with filter and swipe-to-delete
└── SummaryActivity.java        # Monthly category breakdown
```

---

## Division of Work

| Member | Role & Contributions |
|---|---|
| Member 1 | Database & History | 
| Member 2 | Input & Navigation |
| Member 3 | Dashboard & Summary |

---

## Course

**COMP 4200 — Mobile Application Development** · Group 22
