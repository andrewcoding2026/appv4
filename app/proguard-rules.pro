# ---------- Room ----------
# Entity/DAO classes use reflection for column names and type converters.
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# ---------- WorkManager ----------
# Worker classes are instantiated by class name from the manifest.
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ---------- Security / Crypto ----------
-keep class androidx.security.crypto.** { *; }
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ---------- Kotlin coroutines ----------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ---------- Crash-reporting symbols ----------
# Retains line numbers in stack traces while still obfuscating class names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
