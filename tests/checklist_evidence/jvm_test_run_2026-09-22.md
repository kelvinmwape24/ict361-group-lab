# JVM unit test run — 22 September 2026

Run by Chibesa Mumbi (202001699) on his own machine, from the git clone, not
from a copy of the source. Verbatim output below; nothing summarised upward.

## Command

```
cd android
JAVA_HOME=~/.jdks/jbr-21.0.11
./gradlew test --no-daemon --console=plain
```

## Result

```
BUILD SUCCESSFUL in 28m 25s
48 actionable tasks: 48 executed
```

The 28 minutes is almost entirely download: this was a cold cache, so the run
included fetching the Gradle 8.13 distribution (131 MB), AGP and every library,
and installing two missing SDK packages. A warm re-run of `assembleDebug`
afterwards took 1m 27s.

| Variant | Class | Tests | Failures | Errors | Skipped | Time |
|---|---|---|---|---|---|---|
| testDebugUnitTest | `zm.mu.ict361lab.RosterViewModelTest` | 11 | 0 | 0 | 0 | 0.081s |
| testDebugUnitTest | `zm.mu.ict361lab.ValidatorsTest` | 6 | 0 | 0 | 0 | 0.007s |
| testReleaseUnitTest | `zm.mu.ict361lab.RosterViewModelTest` | 11 | 0 | 0 | 0 | 0.108s |
| testReleaseUnitTest | `zm.mu.ict361lab.ValidatorsTest` | 6 | 0 | 0 | 0 | 0.009s |

**17 distinct tests, 0 failures.** `test` runs the debug and release variants,
which is why the totals show 34 executions of the same 17 tests.

## What this settles

Two tasks in the middle of that list are the ones worth pointing at:

```
> Task :app:processDebugResources
> Task :app:compileDebugJavaWithJavac
```

Both passed. Before this commit the manifest named `@mipmap/ic_launcher` and no
icon resource existed, so `aapt` failed at `processDebugResources` and no Java
was ever compiled — the project could not build at all, on any machine. A green
`compileDebugJavaWithJavac` is the evidence that it now does.

## APK

```
./gradlew assembleDebug
BUILD SUCCESSFUL in 1m 27s
app/build/outputs/apk/debug/app-debug.apk   7.87 MB
```

## Environment, for the record

The brief asks for dependency versions to be recorded. These are the ones this
run actually used, read off the build files rather than remembered.

| | |
|---|---|
| Commit | `90ea3b3b69c061977d4a1fdb1f8b83720985c15a` on `chibesa-arch` |
| Gradle | 8.13 (wrapper) |
| Android Gradle Plugin | 8.11.2 |
| JDK | JetBrains Runtime 21.0.11+1-b1163.116 |
| `compileSdk` / `targetSdk` / `minSdk` | 36 / 36 / 24 |
| Build-Tools installed during the run | 35.0.0, and SDK Platform 36 rev 2 |
| Java source/target | 11 |
| Room | 2.6.1 |
| Retrofit / Gson converter | 2.9.0 |
| OkHttp / logging-interceptor | 4.12.0 |
| WorkManager | 2.9.1 |
| Lifecycle (ViewModel, LiveData, SavedState) | 2.8.7 |
| AppCompat | 1.7.1 |
| Material | 1.12.0 |
| JUnit | 4.13.2 |
| AndroidX Test ext-junit / Espresso | 1.2.1 / 3.6.1 |

## Not yet run

`connectedDebugAndroidTest` — the 8 instrumented tests (4 roster rotation,
3 student-edit rotation, 1 Room migration). They need a device or an emulator.
No AVD exists on this machine and an emulator system image is roughly 1.2 GB,
which is not a sensible download on this connection. Two routes, either
acceptable as evidence:

1. A real Android phone over USB with developer mode on. Preferred — it is also
   what the six-screen screenshots should come from.
2. The `instrumented` job in `.github/workflows/android.yml`, which boots an
   API 34 emulator on a GitHub runner. Start it from the Actions tab
   (`workflow_dispatch`); it uploads the report as an artifact.
