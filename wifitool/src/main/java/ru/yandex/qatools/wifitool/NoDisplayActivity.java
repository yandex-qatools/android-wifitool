package ru.yandex.qatools.wifitool;

import android.app.Activity;
import android.os.Bundle;

public class NoDisplayActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }
}
