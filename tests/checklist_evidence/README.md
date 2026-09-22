# Demonstration checklist evidence

The eight checklist items, each needing evidence. `../test_plan.md` has the
steps; this folder holds what came out of running them.

## What goes here

| | |
|---|---|
| Screenshots of the two HTML test reports | `gradlew test` and `gradlew connectedDebugAndroidTest` |
| Screen recordings of the four lifecycle demonstrations | rotation, process death, half-typed input, dialog survival |
| The Challenge 1 run table | from `../challenge_1_evidence/run_20.ps1` |
| Screenshots of each of the six screens on a real device or emulator | |

## What does not go here

The HTML reports themselves. They live under `android/app/build/reports/`,
which is generated and git-ignored — as it should be. Commit the screenshot,
not the artefact.

## Report paths after a run

```
android/app/build/reports/tests/testDebugUnitTest/index.html
android/app/build/reports/androidTests/connected/debug/index.html
```

Both pull in a `css/` and `js/` folder beside them, so if you copy them
anywhere, copy the whole `reports` directory or they open unstyled.

## Current state

**JVM tests: run and recorded.** See `jvm_test_run_2026-09-22.md` — 17 tests,
0 failures, on commit `90ea3b3`, with the full dependency version table the
brief asks for.

Still outstanding:

| | |
|---|---|
| The 8 instrumented tests | need a phone over USB, or the `instrumented` job in `.github/workflows/android.yml` |
| The four lifecycle screen recordings | rotation, process death, half-typed input, dialog survival |
| The Challenge 1 run table | `../challenge_1_evidence/` records 1 run; the brief asks for 20 |
| Screenshots of the six screens | from a real device |
| `../test_plan.md` | still has placeholder slots where results go |
