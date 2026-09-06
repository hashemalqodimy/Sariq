if [ ! -f "upload-keystore.jks" ]; then
    keytool -genkey -v -keystore upload-keystore.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Aman, OU=Yemen, O=Dev, L=Sanaa, S=Sanaa, C=YE" -storepass android -keypass android
fi
