package ru.yandex.qatools.wifitool;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = IntentModule.class)
interface Injector {
    void inject(WifiIntentService context);
}
