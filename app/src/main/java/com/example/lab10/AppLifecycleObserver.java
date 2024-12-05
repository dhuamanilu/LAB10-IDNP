package com.example.lab10;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class AppLifecycleObserver implements LifecycleObserver {

    private final AudioPlayerService audioPlayerService;

    public AppLifecycleObserver(AudioPlayerService service) {
        this.audioPlayerService = service;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        // La aplicación ha pasado a segundo plano
        audioPlayerService.showNotification();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        // La aplicación ha pasado a primer plano
        audioPlayerService.hideNotification();
    }
}
