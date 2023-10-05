#!/usr/bin/env python3
# -*- coding:utf-8 -*-

import io
import re
import argparse

def _replace_file_content(file, pattern, new_str):
    done = False
    res = []

    with io.open(file, "r", encoding="utf-8") as f1:
        lines = f1.readlines()
        for line in lines:
            newLine = re.sub(pattern, new_str, line)
            res.append(newLine)
            if newLine != line:
                done = True

    if done:
        with io.open(file, "w", encoding="utf-8") as f1:
            f1.writelines(res)

    return done

if __name__ == '__main__':
    parser = argparse.ArgumentParser(formatter_class=argparse.RawTextHelpFormatter)
    parser.add_argument("-c", "--code")
    parser.add_argument("-n", "--name")
    args = parser.parse_args()

    code = args.code
    name = args.name

    _replace_file_content('app/src/main/AndroidManifest.xml', r'android:versionName="[\d.]+"', f'android:versionName="{name}"')
    _replace_file_content('app/src/main/AndroidManifest.xml', r'android:versionCode="[\d]+"', f'android:versionCode="{code}"')
    _replace_file_content('app/build.gradle', r'versionCode [\d]+', f'versionCode {code}')
    _replace_file_content('app/build.gradle', r'versionName "[\d.]+"', f'versionName "{name}"')

