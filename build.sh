#!/bin/bash

#  passwd: 123456

./gradlew aRelease

KEY_PATH=./key/assistant.key
APP_PATH_UNSIGNED=./app/build/outputs/apk/release/app-release-unsigned.apk
# keytool -genkey -keystore ${KEY_PATH} -keyalg RSA -validity 10000 -alias assistant_sign
jarsigner -verbose -keystore ${KEY_PATH} -signedjar signed.apk ${APP_PATH_UNSIGNED} assistant_sign

/Users/Shared/Android/sdk/build-tools/31.0.0/zipalign -v 4 signed.apk signed-align.apk

