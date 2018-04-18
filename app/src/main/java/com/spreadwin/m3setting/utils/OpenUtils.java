package com.spreadwin.m3setting.utils;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.lang.reflect.Method;

public class OpenUtils {

    public static OpenUtils mOpenUtils;

    private WifiManager wfManager;
    // private WifiInfo wfInfo;
    private Context context;
    private static String mTAG = "OpenUtils";
    private boolean bnCbWlanHot;
    private AudioManager mAudioManager;

    private boolean mWifiChanging = false;

    public OpenUtils(Context context) {
        this.context = context;
        wfManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);// 取得WifiManager对象
        // wfInfo = wfManager.getConnectionInfo();// 取得WifiInfo对象
        mAudioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
    }

    public static OpenUtils getInstance(Context context) {
        if (mOpenUtils == null) {
            mOpenUtils = new OpenUtils(context);
        }
        return mOpenUtils;
    }

    /**
     * 判断wifi是否打开
     */
    public boolean isWifiOpen() {
        return wfManager.isWifiEnabled();
    }

    /**
     * 判断wifi是否打开
     */
    public boolean canChangeWifi(boolean toggle) {
        if (toggle
                && wfManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            return true;
        } else if (!toggle
                && wfManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            return true;
        }
        return false;
    }

    /**
     * 判断wifi是否打开
     */
    public boolean canChangeWifiStatu() {
        if (wfManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            return true;
            // }else if ( wfManager.getWifiState() ==
            // WifiManager.WIFI_STATE_ENABLED) {
        } else if (wfManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            return true;
        }
        return false;
    }

    /**
     * 判断wifi是否打开
     */
    public boolean canChangeWifiApStatu() {
        if (getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED ||
                getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_DISABLED) {
            return true;
        }
        return false;
    }

    /**
     * 打开或关闭WIFI
     */
    public void setWifiToggle(boolean toggle) {
        wfManager.setWifiEnabled(toggle);
    }

    public enum WIFI_AP_STATE {
        WIFI_AP_STATE_DISABLING, WIFI_AP_STATE_DISABLED, WIFI_AP_STATE_ENABLING, WIFI_AP_STATE_ENABLED, WIFI_AP_STATE_FAILED
    }

    /**
     * 判断热点开启状态 true为开启，false为关闭
     */
    public boolean getWifiApEnabled() {
        return getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
    }

    public WIFI_AP_STATE getWifiApState() {
        int tmp;
        try {
            Method method = wfManager.getClass().getMethod("getWifiApState");
            tmp = ((Integer) method.invoke(wfManager));
            // Fix for Android 4
            if (tmp > 10) {
                tmp = tmp - 10;
            }
            return WIFI_AP_STATE.class.getEnumConstants()[tmp];
        } catch (Exception e) {
            e.printStackTrace();
            return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
        }
    }

    /**
     * 打开或者关闭wifi热点
     */
    public boolean openOrCloseWifiAp_1(boolean onOff) {
        // 通过反射调用设置热点
        Method method = null;
        try {
            method = wfManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, Boolean.TYPE);
            // 返回热点打开状态
            return (Boolean) method.invoke(wfManager, null, onOff);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(mTAG, e.toString());

        }
        return false;
    }

    /**
     * 设置移动网络状态
     *
     * @param context
     * @param isOpen
     */
    public void setMobileDataStatus(Context context, boolean isOpen) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.setMobileDataEnabled(isOpen);

    }

    /**
     * 获取移动网络状态
     *
     * @param context
     * @return
     */
    public boolean getMobileDataStatus(Context context)

    {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isOpen = cm.getMobileDataEnabled();
        return isOpen;

    }

    /**
     * 打开或关闭FM发射
     */
    public void openFMShot() {
        context.sendBroadcast(new Intent("com.spreadwin.fm.open"));
    }

    /**
     * 得到屏幕亮度
     *
     * @return
     */
    public int getScreenBrightness() {
        int screenBright = 255;
        try {
            screenBright = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
            Log.e(mTAG, e.toString());
        }
        return screenBright;
    }

    /**
     * 设置系统屏幕亮度
     *
     * @param paramInt
     */
    public void setSysScreenBrightness(int paramInt) {
        try {

            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, paramInt);
        } catch (Exception e) {
            Log.e(mTAG, e.toString());
        }
    }

    /**
     * 获取屏幕亮度的模式
     *
     * @return
     */
    public int getScreenMode() {
        try {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
            Log.e(mTAG, e.toString());
        }
        return 0;
    }

    /**
     * 设置亮度模式
     *
     * @param mode SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
     *             SCREEN_BRIGHTNESS_MODE_MANUAL=0 为手动调节屏幕亮度
     */
    public void setScreenMode(int mode) {
        try {
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
        } catch (Exception e) {
            Log.e(mTAG, e.toString());
        }
    }

    /**
     * 获得休眠时间 毫秒
     */
    public int getScreenOffTime() {
        int screenOffTime = 0;
        try {
            screenOffTime = Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Exception localException) {

        }
        return screenOffTime;
    }

    /**
     * 设置休眠时间 毫秒
     */
    public void setScreenOffTime(int paramInt) {
        try {
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, paramInt);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(mTAG, e.toString());
        }
    }

    /**
     * 设置音量
     *
     * @param voice
     */
    private void setVoice(int voice) {
        Log.d("voice", "set stream music voice==" + voice);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, voice,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        // int max = mAudioManager.getStreamMaxVolume(
        // AudioManager.STREAM_SYSTEM );//7
        // int current = mAudioManager.getStreamVolume(
        // AudioManager.STREAM_SYSTEM );//当前音量
    }

    /**
     * 获取最大音量值
     */
    private int getMaxVoice() {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * @return 当前音量
     */
    private String getVoice() {
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return current == 0 ? "0" : (current < 10 ? "0" + current : current
                + "");
    }

    /**
     * 设置静音或正常 AudioManager.RINGER_MODE_SILENT静音0
     * AudioManager.RINGER_MODE_VIBRATE 静音,但有振动1
     * AudioManager.RINGER_MODE_NORMAL正常声音,振动开关由setVibrateSetting决定2
     */
    private void setRingerMode(boolean mode) {
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, mode);
    }

    // public void setRingerMode(int mode) {
    // if (mode == AudioManager.RINGER_MODE_NORMAL) {
    // mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
    // }else{
    // mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
    // }
    // mAudioManager.setRingerMode(mode);
    // }

    /**
     * 返回音量模式 AudioManager.RINGER_MODE_SILENT静音0
     * AudioManager.RINGER_MODE_VIBRATE 静音,但有振动1
     * AudioManager.RINGER_MODE_NORMAL正常声音,振动开关由setVibrateSetting决定2
     */
    // public int getRingerMode() {
    // return mAudioManager.getRingerMode();
    // }
//	@RequiresApi(api = Build.VERSION_CODES.M)
//	private boolean getRingerMode() {
//		return mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC);
//	}

    /**
     * 模拟按键
     *
     * @param KeyCode
     */
    public static synchronized void onSimulateKey(final int KeyCode) {
        new Thread() {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyCode);
                } catch (Exception e) {
                    Log.e(mTAG, e.toString());
                }
            }
        }.start();
    }


    @SuppressLint("WrongConstant")
    public void onOpenWifi() {
        Log.d(mTAG, "getWifiState =" + wfManager.getWifiState());
        if (isWifiOpen()) {
            setWifiToggle(false);
        } else {
            if (getWifiApEnabled()) {
                openOrCloseWifiAp_1(false);
            }
            setWifiToggle(true);
        }
    }

    public void closeScreen() {
        try {
            PowerManager pm = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);
			pm.goToSleep(SystemClock.uptimeMillis());
//            final PowerManager.WakeLock wakeLock =
//                    pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "TAG");
//            wakeLock.acquire();
//            mTimeHandler.postDelayed(new Runnable() {
//                public void run() {
//                    wakeLock.release();
//                }
//            }, 5 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Handler mTimeHandler = new Handler();

    private void showToast(String str) {
        Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }


    public  void showNotification(Context context,int icon)
    {
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        //int icon = R.drawable.ic_cloud_video;
        //CharSequence tickerText = context.getResources().getString(R.string.recorder_switch);
        long when = java.lang.System.currentTimeMillis();
        Notification notification = new Notification(icon, "", when);

        //define the notification's expand message and intent
        Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, null, null, contentIntent);
        nm.notify(icon, notification);
    }

    public void cancelNotification(Context context,int id){
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(id);
    }
}
