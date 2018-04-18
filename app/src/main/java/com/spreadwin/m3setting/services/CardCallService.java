package com.spreadwin.m3setting.services;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;

import com.spreadwin.m3setting.view.InitSettingShortcut;

public class CardCallService extends Service {
	public String TAG = "BroadcastReceiver";
	private WindowManager wm;
	public Context context;

	private InitSettingShortcut dialog;


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (dialog==null){
			dialog = InitSettingShortcut.getInstance(context);

			dialog.registReceiver();
		}
		if (dialog.isAttach()){
			dialog.dismissDialog();
		}else {
			dialog.initCusDialog();
			dialog.attachDialog();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (dialog!=null)
		dialog.unRegistReceiver();
	}
}
