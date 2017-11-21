package com.example.admed.sharelocation.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CapturaLocalizacaoService extends Service {

    public CapturaLocalizacaoService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO -  fica capiturando a posição;

        return START_NOT_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
