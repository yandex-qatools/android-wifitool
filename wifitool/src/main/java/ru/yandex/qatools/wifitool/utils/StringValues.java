package ru.yandex.qatools.wifitool.utils;

import javax.annotation.Nullable;

public class StringValues {
    private StringValues() {}

    @Nullable
    public static String enquote(@Nullable String value) {
        return value == null ? null : '\"' + value + '\"';
    }
}
