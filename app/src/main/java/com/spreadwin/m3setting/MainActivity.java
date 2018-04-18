package com.spreadwin.m3setting;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;

import com.spreadwin.m3setting.adapter.GridViewAdapter;
import com.spreadwin.m3setting.bean.SettingsItemInfo;
//import com.spreadwin.x3settings.services.CardCallService;
import com.spreadwin.m3setting.utils.OpenUtils;
import com.spreadwin.m3setting.view.TitleBar;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    TitleBar titleBar;

    GridView gridView;

    public static List<SettingsItemInfo> data = new ArrayList<SettingsItemInfo>();
    public static GridViewAdapter adapter;
//	NetTypeReceiver mNetworkReceiver;

    public static boolean IS_WIFI_OPEN;
    public static boolean IS_WIFI_AP_OPEN;

    public OpenUtils openUtils;

    HomeReceiver homeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.layout_settings);
        initListData();
        initView();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        homeReceiver=new HomeReceiver();
        registerReceiver(homeReceiver,filter);
    }

    public void initView() {
        titleBar =(TitleBar) findViewById(R.id.custom_title_bar);
        titleBar.initView(getApplicationContext());
        titleBar.setIvtitleBackground(R.mipmap.settings1);
        titleBar.setTvtitleContent(getString(R.string.settings));
        titleBar.setTvBackVisibility(View.GONE);
        titleBar.setVisibility(View.GONE);
        gridView =(GridView) findViewById(R.id.gv_setting);
        adapter=new GridViewAdapter(getApplicationContext());
        adapter.initData(data);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);

    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        switch (keyCode){
//            case KeyEvent.KEYCODE_HOME:
//                Log.i("MainActivity","KEYCODE_HOME");
//                Toast.makeText(this,"KEYCODE_HOME",Toast.LENGTH_LONG).show();
//                break;
//            case KeyEvent.KEYCODE_BACK:
//                Log.i("MainActivity","KEYCODE_BACK");
//                Toast.makeText(this,"KEYCODE_BACK",Toast.LENGTH_LONG).show();
//                break;
//
//
//        }
//        return super.onKeyDown(keyCode,event);
//    }


    public class  HomeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MainAcitivity","intent.action="+intent.getAction());
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    public void initListData() {
        openUtils=new OpenUtils(this);
        data.clear();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            data.add(new SettingsItemInfo(getString(R.string.wifi), R.drawable.wifi_open_selector));
            IS_WIFI_OPEN=true;
        } else {
            data.add(new SettingsItemInfo(getString(R.string.wifi), R.drawable.wifi_selector));
            IS_WIFI_OPEN=false;
        }
        if (getWifiApState(getApplicationContext()) == 12 || getWifiApState(getApplicationContext()) == 13) {
            data.add(new SettingsItemInfo(getString(R.string.wifi_wireless_hotspot),
                    R.drawable.wifi_hotspot_open_selector));
            IS_WIFI_AP_OPEN=true;
        } else {
            data.add(new SettingsItemInfo(getString(R.string.wifi_wireless_hotspot), R.drawable.wifi_hotspot_selector));
            IS_WIFI_AP_OPEN=false;
        }
        boolean isNetworkEnable =openUtils.getMobileDataStatus(this);
        if (isNetworkEnable) {
            data.add(new SettingsItemInfo(getString(R.string.mobile_network), R.drawable.mobile_network_open_selector));
        } else {
            data.add(new SettingsItemInfo(getString(R.string.mobile_network), R.drawable.mobile_network_selector));
        }

        data.add(new SettingsItemInfo(getString(R.string.application_settings),
                R.drawable.application_settings_selector));
        data.add(new SettingsItemInfo(getString(R.string.bluetooth_settings), R.drawable.bluetooth_settings_selector));
        data.add(new SettingsItemInfo(getString(R.string.mode_change), R.drawable.mode_change_selector));
        data.add(new SettingsItemInfo(getString(R.string.about_device), R.drawable.about_device_selector));

        data.add(new SettingsItemInfo(getString(R.string.backup_reset), R.drawable.backup_settings_selector));

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                Intent wifiIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                wifiIntent.setComponent(new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings"));
                startActivity(wifiIntent);
                break;
            case 1:
                Intent wifiHotspotIntent = new Intent("/");
                ComponentName cm = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
                wifiHotspotIntent.setComponent(cm);
                wifiHotspotIntent.setAction("android.intent.action.VIEW");
                startActivity(wifiHotspotIntent);
                break;
            case 2:
                Intent mobileNetworkIntent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                startActivity(mobileNetworkIntent);
                break;
            case 3:
                Intent applicationSettingIntent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);

                startActivity(applicationSettingIntent);
                break;
            case 4:
//                Intent adjustSettingsIntent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
//
//                startActivity(adjustSettingsIntent);
                Intent intent =  new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
                break;
            case 5:
//                Intent storageCapacityIntent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
//                startActivity(storageCapacityIntent);

                sendBroadcast(new Intent("com.spreadwin.ACTION_REBOOT"));
                break;
            case 6:
                Intent deviceIntent = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);

                startActivity(deviceIntent);
                break;
            case 7:
                Intent backupResetIntent = new Intent(Settings.ACTION_PRIVACY_SETTINGS);

                startActivity(backupResetIntent);
                break;
            default:
                break;
        }

    }


//    @SuppressWarnings("deprecation")
//    public boolean getMobileDataStatus(Context context, String getMobileDataEnabled)
//
//    {
//
//        ConnectivityManager cm;
//
//        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        Class cmClass = cm.getClass();
//        Class[] argClasses = null;
//        Object[] argObject = null;
//        Boolean isOpen = false;
//        try {
//
//            Method method = cmClass.getMethod(getMobileDataEnabled, argClasses);
//            cm.setNetworkPreference(1);
//            isOpen = (Boolean) method.invoke(cm, argObject);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return isOpen;
//
//    }

    public int getWifiApState(Context mContext) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApState");
            int i = (Integer) method.invoke(wifiManager);
            Log.i("Settings", "wifi state:  " + i);
            return i;
        } catch (Exception e) {
            Log.e("Settings", "Cannot get WiFi AP state" + e);
            return 14;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 6:
//                     changeLancher();
//                sendBroadcast(new Intent("com.spreadwin.ACTION_REBOOT"));


                break;
        }
        return false;
    }

//    private void changeLancher(){
//        int luncher = Settings.Global.getInt(getContentResolver(),"launcher", 0) ;
//        Settings.Global.putInt(getContentResolver(),"launcher", 1-luncher);
//        sendBroadcast(new Intent("ACTION_SPREADWIN_NORMAL_REBOOT"));
//    }


    @Override
    protected void onDestroy() {

        unregisterReceiver(homeReceiver);
        super.onDestroy();
    }
}
