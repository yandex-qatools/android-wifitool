# Enable WiFi and connect to WiFi network from adb

## Build
1. clone current repository to your local computer
2. run "./gradlew build"

## Install
1. adb install app-debug.apk

## Usage
As soon as WiFi network is connected and IP is obtained, `WifiTool:Success` is logged

When connection fails, `WifiTool:Fail` is logged.

Possible failure reasons: WiFi can not be enabled, WiFi network can not be connected, IP can not be obtained.

Usage:

    adb shell am broadcast
    -n ru.yandex.qatools.wifitool/.Connect
    -e ssid SSID
    -e securityString [WEP|WPA]
    -e pass password
    -e retry_count number of connection retries. Default is 0
    -e retry_delay retry delay in milliseconds. Default is 10000
Examples:

    adb shell am broadcast  -n ru.yandex.qatools.wifitool/.Connect -e ssid SecureNet -e securityString WPA -e pass 123456 -e retry_count 3 -e retry_delay 5
    adb shell am broadcast  -n ru.yandex.qatools.wifitool/.Connect -e ssid UnsecureNet
