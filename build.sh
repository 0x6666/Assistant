#!/bin/bash

#  passwd: 123456

ahost=https://assistant.nas.ysong.cc:5001
# ahost=http://127.0.0.1:2002

code=$(python3 ch-ver.py lastVersion -u ${ahost})
code=$((code + 1))
name=1.0.${code}

python3 ch-ver.py changeVersion -c ${code} -n ${name}

BUILD_TOOL=/Users/Shared/Android/sdk/build-tools/31.0.0

./gradlew aRelease

KEY_PATH=./key/assistant.key
APP_PATH_UNSIGNED=./app/build/outputs/apk/release/app-release-unsigned.apk
${BUILD_TOOL}/apksigner sign --ks ${KEY_PATH} --ks-key-alias assistant_sign --out Assistant-signed.apk ${APP_PATH_UNSIGNED} 

curl -X POST ${ahost}/syno/api/app/upload \
  -F "file=@/Users/ys/work/song/Assistant/Assistant-signed.apk" \
  -F "name=Assistant" \
  -F ver=${name} -F code=${code} \
  -H "Content-Type: multipart/form-data"