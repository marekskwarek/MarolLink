# Keep FileProvider & Android platform hooks intact
-keepclassmembers class * extends android.content.ContentProvider {
    public *;
}
