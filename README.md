# TimeCalc

Minimal calculator that accumulates numbers with automatic sum after 800ms of inactivity.

- 🎨 GitHub Dark theme
- 🧮 Precise `BigDecimal` math
- ⏱️ Auto-commit timer (800ms debounce)
- 📝 Expression history
- 📱 No external dependencies — pure Android SDK

## Screenshots

| Dark theme | 
|---|
| *(add screenshot here)* |

## Build

```bash
git clone https://github.com/andrejpedin-wq/TimeCalc
cd TimeCalc
./gradlew assembleRelease
```

## F-Droid

This app is ready for F-Droid submission. Metadata:

```yaml
Categories:Time,Calculator
License:Apache-2.0
SourceCode:https://github.com/andrejpedin-wq/TimeCalc
IssueTracker:https://github.com/andrejpedin-wq/TimeCalc/issues

AutoName:TimeCalc
Summary:Minimal sum accumulator calculator
Description:|
    A minimal calculator that accumulates numbers with
    auto-commit after 800ms. GitHub Dark theme, precise
    BigDecimal math, expression history.

Build:4.0,4
    commit=v4.0
    subdir=app
    gradle=yes

AutoUpdateMode:Version
UpdateCheckMode:Tags
CurrentVersion:4.0
CurrentVersionCode:4
```

## License

Apache 2.0 — see [LICENSE](LICENSE).
