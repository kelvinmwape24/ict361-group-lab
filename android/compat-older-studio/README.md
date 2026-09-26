# Compatibility variant — only if the main project will not sync

The main project targets **AGP 8.11.2, Gradle 8.14.3, SDK 36**. On an Android
Studio older than Narwhal (2025.1) you may see one of these:

- *"Your project is using an incompatible version of the Android Gradle Plugin"*
- *"Failed to find target with hash string 'android-36'"* with no offer to install it
- *"Minimum supported Gradle version is 8.13"* going the other way

Two ways out. Try the first one first.

## Option 1 — install what is missing (preferred, keeps the repo honest)

Android Studio usually offers this itself as a blue quick-fix link in the sync
error. Otherwise: **Tools → SDK Manager → SDK Platforms**, tick *Android 16
(API 36)*, apply. Then **File → Sync Project with Gradle Files**.

This is the better option because the repository and the machine then agree, and
you are not committing a downgrade that Kelvin has to reconcile later.

## Option 2 — downgrade this checkout (last resort, do not commit it)

Three files, and **all three must move together** or the build breaks in a more
confusing way than it started:

```cmd
copy /Y compat-older-studio\libs.versions.toml         ..\gradle\libs.versions.toml
copy /Y compat-older-studio\gradle-wrapper.properties  ..\gradle\wrapper\gradle-wrapper.properties
copy /Y compat-older-studio\app-build.gradle.kts       ..\app\build.gradle.kts
```

Run those from the `android` folder. Then **File → Sync Project with Gradle Files**.

| | Main | This variant |
|---|---|---|
| AGP | 8.11.2 | 8.5.2 |
| Gradle | 8.14.3 | 8.7 |
| compileSdk / targetSdk | 36 | 34 |
| minSdk | 24 | 24 (unchanged) |
| JDK | 17+ | 17+ |

Nothing in the app source uses an API above 24, so SDK 34 loses no
functionality — every dependency in `app/build.gradle.kts` supports
compileSdk 34. The only real cost is that `targetSdk = 34` opts out of the
Android 15 and 16 behaviour changes, which matters for a Play Store release and
not for a lab demonstration.

**Do not commit this variant to the repository.** It is a local workaround for
one machine. If you end up needing it on your own laptop too, raise it with
Kelvin as a deliberate decision rather than pushing it quietly — a version
downgrade that appears in a diff with no explanation reads as an accident.
