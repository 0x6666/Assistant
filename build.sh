#!/bin/bash

#  passwd: 123456

BUILD_TOOL=/Users/Shared/Android/sdk/build-tools/31.0.0

./gradlew aRelease

KEY_PATH=./key/assistant.key
APP_PATH_UNSIGNED=./app/build/outputs/apk/release/app-release-unsigned.apk
${BUILD_TOOL}/apksigner sign --ks ${KEY_PATH} --ks-key-alias assistant_sign --out Assistant-signed.apk ${APP_PATH_UNSIGNED} 
