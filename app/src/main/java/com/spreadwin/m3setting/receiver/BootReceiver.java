package com.spreadwin.m3setting.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.spreadwin.m3setting.services.CardCallService;

public class BootReceiver extends BroadcastReceiver{
    private static final String ACTION_START = "com.spreadwin.action.shortcut.show";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub  
//    	List<SettingsItemInfo> data=activity.getData();
        Log.i("BootReceiver","action="+intent.getAction());
        if(intent.getAction().equals(ACTION_START)){
            Intent service = new Intent(context, CardCallService.class);
            context.startService(service);
        }


    }
}