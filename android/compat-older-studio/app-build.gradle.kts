plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "zm.mu.ict361lab"
    compileSdk = 34

    defaultConfig {
        applicationId = "zm.mu.ict361lab"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // The emulator reaches the host machine at 10.0.2.2, never localhost.
        // A physical device needs your machine's LAN address here instead.
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    packaging {
        resources.excludes += setOf("META-INF/LICENSE.md", "META-INF/LICENSE-notice.md")
    }
}

dependencies {
    // UI
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Lifecycle — ViewModel + LiveData (Activity B)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.7")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.8.7")
    // SavedStateHandle — a ViewModel alone does not survive process death.
    // This is what carries screen state through a low-memory kill.
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.7")

    // Fragment — DialogFragment, so a dialog survives rotation instead of
    // vanishing, and setFragmentResult, so its callback cannot leak an Activity.
    implementation("androidx.fragment:fragment:1.8.5")

    // Room — durable local records and the pending-operation queue (Activity E)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Retrofit + OkHttp — the API client. Android never talks to MySQL directly.
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // WorkManager — persistent sync with constraints and retry backoff (Activity E)
    implementation("androidx.work:work-runtime:2.9.1")

    // Unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.7")

    // Instrumented tests
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    // ActivityScenario.recreate() — the rotation tests
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.fragment:fragment-testing:1.8.5")
}
