
# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>(...);
}
-keep class com.expensetracker.data.local.entity.** { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# Lottie
-keep class com.airbnb.lottie.** { *; }

# Prevent shrinking of these classes
-keep class com.expensetracker.presentation.** { *; }
-keep class com.expensetracker.domain.model.** { *; }
