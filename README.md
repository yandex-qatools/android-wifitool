# Enable WiFi and connect to WiFi network from adb

[![Travis](https://img.shields.io/travis/yandex-qatools/android-wifitool.svg)](https://travis-ci.org/yandex-qatools/android-wifitool)
[![SonarQube Coverage](https://img.shields.io/sonar/https/sonarqube.com/android-wifitool/coverage.svg)](https://sonarqube.com/dashboard?id=android-wifitool)

## Build
1. clone current repository to your local computer
2. run "./gradlew build"

There is a prebuilt binary available:
https://github.com/yandex-qatools/android-wifitool/releases

## Install
1. adb install wifitool/build/outputs/apk/wifitool-debug.apk

## Usage
    adb shell am broadcast
    -n ru.yandex.qatools.wifitool/.Connect
    -e ssid SSID
    -e security [WEP|WPA]
    -e pass password
    -e retry_count number of connection retries. Default is 0
    -e retry_delay retry delay in milliseconds. Default is 10000
Examples:

    adb shell am broadcast  -n ru.yandex.qatools.wifitool/.Connect -e ssid SecureNet -e security WPA -e pass 123456 -e retry_count 3 -e retry_delay 5
    adb shell am broadcast  -n ru.yandex.qatools.wifitool/.Connect -e ssid UnsecureNet

## Output
As soon as WiFi network is connected and IP is obtained, `WifiTool:Success` is logged

When connection fails, `WifiTool:Fail` is logged.

Possible failure reasons: WiFi can not be enabled, WiFi network can not be connected, IP can not be obtained.

Short: look for `I/WifiTool:Success` or `I/WifiTool:Fail`

    08-23 09:52:31.648 5565-5565/ru.yandex.qatools.wifitool I/WifiTool:Success: Connected

Long: `D/WifiTool` exposes extra info on what's going on

    08-23 09:52:26.978 5565-5584/ru.yandex.qatools.wifitool D/WifiTool: Start connection
    08-23 09:52:26.988 5565-5584/ru.yandex.qatools.wifitool D/WifiTool: Attempt 0
    08-23 09:52:26.998 5565-5584/ru.yandex.qatools.wifitool D/WifiTool: WIFI_STATE_DISABLED
    08-23 09:52:26.998 5565-5584/ru.yandex.qatools.wifitool D/WifiTool: Setting WiFi enabled
    08-23 09:52:28.038 5565-5593/ru.yandex.qatools.wifitool D/WifiTool: WIFI_STATE_ENABLED
    08-23 09:52:28.038 5565-5593/ru.yandex.qatools.wifitool D/WifiTool: Get connected network id...
    08-23 09:52:28.048 5565-5593/ru.yandex.qatools.wifitool D/WifiTool: Configured network not found
    08-23 09:52:28.048 5565-5593/ru.yandex.qatools.wifitool D/WifiTool: Add network
    08-23 09:52:28.138 5565-5593/ru.yandex.qatools.wifitool D/WifiTool: Network added
    08-23 09:52:28.178 5565-5593/ru.yandex.qatools.wifitool D/WifiTool: Register network status receiver
    08-23 09:52:31.628 5565-5565/ru.yandex.qatools.wifitool D/WifiTool: Wifi supplicant state: COMPLETED
    08-23 09:52:31.628 5565-5565/ru.yandex.qatools.wifitool D/WifiTool: Network has been connected
    08-23 09:52:31.628 5565-5565/ru.yandex.qatools.wifitool D/WifiTool: Unregister network status receiver
    08-23 09:52:31.648 5565-5565/ru.yandex.qatools.wifitool I/WifiTool:Success: Connected
