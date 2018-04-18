package com.spreadwin.m3setting.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.spreadwin.m3setting.MainActivity;
import com.spreadwin.m3setting.R;
import com.spreadwin.m3setting.bean.SettingsItemInfo;

public class NetworkReceiver extends BroadcastReceiver{
    MainActivity activity=new MainActivity();
    public NetworkReceiver() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub  
//    	List<SettingsItemInfo> data=activity.getData();
        Log.i("WifiReceiver","action="+intent.getAction());
        if (MainActivity.data==null||MainActivity.data.size()==0||MainActivity.adapter==null) {
            return;
        }
        if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){//wifi连接上与否
            System.out.println("网络状态改变");
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info.getState().equals(NetworkInfo.State.DISCONNECTED)){
                System.out.println("wifi网络连接断开");
            }
            else if(info.getState().equals(NetworkInfo.State.CONNECTED)){

                WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                //获取当前wifi名称
                System.out.println("连接到网络 " + wifiInfo.getSSID());

            }

        }else if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){//wifi打开与否
            int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);

            if(wifistate == WifiManager.WIFI_STATE_DISABLED){
                System.out.println("系统关闭wifi");
//                Toast.makeText(context, "wifistate="+"系统关闭wifi", Toast.LENGTH_LONG).show();
                MainActivity.data.set(0, new SettingsItemInfo(context.getString(R.string.wifi), R.drawable.wifi_selector));
//               MainActivity.IS_WIFI_OPEN=false;
            }else if(wifistate == WifiManager.WIFI_STATE_ENABLED){
//                System.out.println("系统开启wifi");
//                Toast.makeText(context, "wifistate="+"系统开启wifi", Toast.LENGTH_LONG).show();
                MainActivity.data.set(0, new SettingsItemInfo(context.getString(R.string.wifi), R.drawable.wifi_open_selector));
//                MainActivity.IS_WIFI_OPEN=true;
            }
        }else if(intent.getAction().equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
            int wifiApState=intent.getIntExtra("wifi_state", 14);
            if (wifiApState==12||wifiApState==13) {
//        		 Toast.makeText(context, "wifiApState="+wifiApState, Toast.LENGTH_LONG).show();

                MainActivity.data.set(1,new SettingsItemInfo(context.getString(R.string.wifi_wireless_hotspot),
                        R.drawable.wifi_hotspot_open_selector));
//        		 MainActivity.IS_WIFI_AP_OPEN=true;
            }else{
                MainActivity. data.set(1,new SettingsItemInfo(context.getString(R.string.wifi_wireless_hotspot),
                        R.drawable.wifi_hotspot_selector));
//				 MainActivity.IS_WIFI_AP_OPEN=false;
            }
        }else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
//        	 Toast.makeText(context, "connectivity", Toast.LENGTH_LONG).show();
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
//            if (connectivityManager.getMobileDataEnabled()) {
//                MainActivity.data.set(2,new SettingsItemInfo(context.getString(R.string.mobile_network), R.drawable.mobile_network_open_selector));
//            } else {
//                MainActivity.data.set(2,new SettingsItemInfo(context.getString(R.string.mobile_network), R.drawable.mobile_network_selector));
//            }

        }
//       activity.changeData(activity);

        MainActivity.adapter.initData(MainActivity.data);

    }
}