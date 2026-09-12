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

# Same failure mode, a different WorkManager-reflected type: the work-runtime version actually
# resolved here weakened its own consumer rule for ListenableWorker/InputMerger subclasses from an
# unconditional "-keep" (still the app's declared io.sentry-adjacent 2.7.1 baseline via Glance) to
# "-keepnames" + a conditional "-keepclassmembers ... if class is kept" - which only blocks
# renaming, not removal, and doesn't guarantee the no-arg constructor a reflective
# newInstance()/WorkerFactory call needs. Confirmed via logcat on a real release build:
# "WM-InputMerger: java.lang.InstantiationException: androidx.work.OverwritingInputMerger has no
# zero argument constructor", thrown while WorkManager was running the Glance widget-composition
# work, which is what the launcher surfaces as "Couldn't add widget". Restore the stronger keep for
# any WorkManager-instantiated worker/input-merger.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
-keep class * extends androidx.work.InputMerger {
    void <init>();
}

# Third instance of the same failure category: when a widget checkbox/row is tapped, Glance
# reconstructs the ActionCallback (here, ToggleTodoDoneAction) by reflection from the class name it
# stashed in the click broadcast - there's no direct code reference to `new ToggleTodoDoneAction()`
# anywhere in app code. glance-appwidget's own consumer rule ("-keep public class * extends
# androidx.glance.appwidget.action.ActionCallback", no member body) only protects the class from
# removal/renaming, not its no-arg constructor from being stripped. Confirmed via logcat on a real
# release build: "GlanceAppWidget: java.lang.NoSuchMethodException:
# com.artemonre.onemoretodolist.widget.ToggleTodoDoneAction.<init> []", thrown when tapping a todo
# in the widget - i.e. completing a todo from the widget silently did nothing.
-keep class * extends androidx.glance.appwidget.action.ActionCallback {
    public <init>();
}