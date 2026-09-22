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

Empty. 26 tests exist and pass under static verification; none of the runs are
recorded yet, and `../test_plan.md` still has placeholder slots where results
go.
