package com.example.pieter.auth0test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Pieter on 22/11/2015.
 */
public class SampleAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        SampleAccountAuthenticator authenticator = new SampleAccountAuthenticator(this);
        return authenticator.getIBinder();
    }
}
