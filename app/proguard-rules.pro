# Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep model classes
-keep class com.example.model.** { *; }
-keep class com.example.engine.** { *; }

# Keep Kotlin serialization
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Moshi
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}

# Firebase
-keep class com.google.firebase.** { *; }

# General
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
