sed -i 's/base64 -d debug.keystore.base64 > debug.keystore/echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > debug.keystore/g' .github/workflows/build.yml
sed -i '/run: gradle assembleRelease/i\        env:\n          KEYSTORE_PATH: "debug.keystore"\n          KEY_PASSWORD: "${{ secrets.KEY_PASSWORD }}"\n          STORE_PASSWORD: "${{ secrets.STORE_PASSWORD }}"' .github/workflows/build.yml
