# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# WorkManager (pulled in transitively by Glance, for app widget update scheduling) bundles an old
# androidx.room:room-runtime:2.2.5 for its internal WorkDatabase. That version's own consumer
# proguard rule ("-keep class * extends androidx.room.RoomDatabase", no member body) doesn't
# protect the no-arg constructor Room instantiates reflectively, so R8 can strip it and crash
# release builds with "Failed to create an instance of androidx.work.impl.WorkDatabase". Room3
# (this app's own Room usage) already ships the stronger version of this rule - mirrored here for
# the older androidx.room package.
-keep class * extends androidx.room.RoomDatabase { void <init>(); }