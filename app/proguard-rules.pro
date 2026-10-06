# Add project specific ProGuard rules here.
# Keep model data classes from obfuscation to protect JSON serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# Keep Kotlinx Serialization generated serializer classes
-keep class *$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
}

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep CameraX
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.camera2.** { *; }

# Keep Accessibility & Service declarations
-keep class * extends android.accessibilityservice.AccessibilityService { *; }
-keep class * extends android.app.Service { *; }

# Keep Data Models
-keep class com.heymahesh.agent.core.models.** { *; }
-keep class com.heymahesh.agent.core.state.** { *; }
