# Release shrinking is off (isMinifyEnabled = false), so these rules are not
# applied today. They are here because build.gradle.kts names this file, and
# because turning minification on later must not silently break Room, Retrofit
# and Gson, all three of which are reflective.

# Retrofit interfaces are called through a Proxy; keep their signatures.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Gson maps JSON onto these by field name, so the names must survive.
-keep class zm.mu.ict361lab.data.remote.Dtos$** { *; }

# Room generates implementations that are looked up by name.
-keep class zm.mu.ict361lab.data.local.** { *; }

# OkHttp ships references to optional platform classes.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
