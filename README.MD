┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION START                        │
│                    (Main.java)                              │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
         ┌───────────────────────────────┐
         │  1. Initialize UI Framework   │
         │     - JavaFX initialization   │
         │     - Window setup            │
         └───────────┬───────────────────┘
                     │
                     ▼
         ┌───────────────────────────────┐
         │  2. Load Login Screen         │
         │     - Display login form      │
         │     - NO database needed      │
         └───────────┬───────────────────┘
                     │
                     ▼
         ┌───────────────────────────────┐
         │  3. User Enters Credentials   │
         │     Username: admin           │
         │     Password: admin123        │
         └───────────┬───────────────────┘
                     │
                     ▼
         ┌───────────────────────────────┐
         │  4. Authenticate User         │
         │     - In-memory validation    │
         │     - Fast, no DB needed      │
         └───────────┬───────────────────┘
                     │
           ┌─────────┴─────────┐
           │                   │
        [FAIL]              [SUCCESS]
           │                   │
           ▼                   ▼
    ┌──────────────┐   ┌──────────────────────┐
    │ Show Error   │   │  5. Show Loading     │
    │ Try Again    │   │     "Initializing    │
    └──────────────┘   │      database..."    │
                       └──────────┬───────────┘
                                  │
                                  ▼
                       ┌──────────────────────┐
                       │  6. Initialize DB    │
                       │     (Background)     │
                       │  - Connect to DB     │
                       │  - Create tables     │
                       │  - Init services     │
                       └──────────┬───────────┘
                                  │
                       ┌──────────┴──────────┐
                       │                     │
                    [FAIL]               [SUCCESS]
                       │                     │
                       ▼                     ▼
            ┌──────────────────┐   ┌────────────────────┐
            │ Show Warning:    │   │  7. Load Main App  │
            │ "Continue        │   │     - Dashboard    │
            │  without DB?"    │   │     - Full access  │
            └──────────────────┘   └────────────────────┘
                       │
            ┌──────────┴──────────┐
            │                     │
         [YES]                  [NO]
            │                     │
            ▼                     ▼
    ┌──────────────────┐  ┌──────────────┐
    │ Load Main App    │  │ Stay on      │
    │ Limited Mode:    │  │ Login Screen │
    │ - No DB logging  │  └──────────────┘
    │ - No history     │
    │ - Live only      │
    └──────────────────┘
