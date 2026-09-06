sed -i 's/gradle assembleDebug --stacktrace --no-daemon --no-configuration-cache/gradle assembleRelease --stacktrace --no-daemon --no-configuration-cache/g' .github/workflows/build.yml
sed -i 's/app\/build\/outputs\/apk\/debug\/app-debug.apk/app\/build\/outputs\/apk\/release\/app-release.apk/g' .github/workflows/build.yml
sed -i 's/Build Debug APK/Build Release APK/g' .github/workflows/build.yml
sed -i 's/aman-phone-debug.apk/aman-phone-release.apk/g' .github/workflows/build.yml
