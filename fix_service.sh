sed -i '/AmanForegroundSyncService/d' app/src/main/AndroidManifest.xml
sed -i '/android:foregroundServiceType="dataSync"/d' app/src/main/AndroidManifest.xml
sed -i '/android:exported="false"/d' app/src/main/AndroidManifest.xml
