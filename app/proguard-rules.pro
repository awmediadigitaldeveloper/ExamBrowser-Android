# Add project specific ProGuard rules here.
-keep class com.exambrowser.** { *; }
-keep class androidx.webkit.** { *; }
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-dontwarn okhttp3.**
