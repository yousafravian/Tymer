package com.developer.tymer.services;

import android.app.IntentService;
import android.content.Intent;

public class ServerCallingIntentService extends IntentService {
    public ServerCallingIntentService() {
        super("ServerCallingIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        BackgroundService.executeTask(this,BackgroundService.SERVER_CALL_ACTION);
    }

}
