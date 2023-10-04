#!/usr/bin/env python3
# -*- coding:utf-8 -*-

import io
import argparse

def _replace_file_content(file, old_str, new_str):
    done = False
    res = []

    f1 = io.open(file, "r", encoding="utf-8")
    lines = f1.readlines()
    for line in lines:
        newLine = line.replace(old_str, new_str)
        res.append(newLine)
        if newLine != line:
            done = True
    f1.close()

    if done:
        f1 = io.open(file, "w", encoding="utf-8")
        f1.writelines(res)
    return done

if __name__ == '__main__':
    parser = argparse.ArgumentParser(formatter_class=argparse.RawTextHelpFormatter)
    parser.add_argument("-c", "--code")
    parser.add_argument("-n", "--name")
    args = parser.parse_args()

    code = args.code
    name = args.name

    _replace_file_content('app/build.gradle', 'versionCode 4', f'versionCode {code}')
    _replace_file_content('app/build.gradle', 'versionName "1.0.4"', f'versionName "{name}"')
    _replace_file_content('app/src/main/AndroidManifest.xml', 'android:versionCode="4"', f'android:versionCode="{code}"')
    _replace_file_content('app/src/main/AndroidManifest.xml', 'android:versionName="1.0.4"', f'android:versionName="{name}"')
