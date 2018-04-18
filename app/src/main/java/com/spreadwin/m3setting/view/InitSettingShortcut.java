package com.spreadwin.m3setting.view;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.spreadwin.m3setting.MainActivity;
import com.spreadwin.m3setting.R;
import com.spreadwin.m3setting.utils.MuteControl;
import com.spreadwin.m3setting.utils.OpenUtils;

import java.util.List;

public class InitSettingShortcut implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener,
        SeekBar.OnSeekBarChangeListener {
    private String TAG = "InitSetting";
    public View mView;
    public Context mContext;
    private WindowManager wm;
    private static InitSettingShortcut mInitSettingShortcut;

    private AlertDialog.Builder builder;

//    private ImageView ivCloseDialog;
    private ImageView ivWlan, ivWlanHot, ivYJLY,ivYDSJ,ivEdog,ivBattery,ivPM;
    private TextView tvWlan, tvWlanHot, tvYJLY,tvYDSJ,tvEdog,tvBattery,tvPM;
    private LinearLayout llYjly,llEdog,llYDSJ,llWlan,llWlanHot,llPower,llPM;
    private LinearLayout upContainer,downContainer;

    // 亮度
    private SeekBar sbLight;
    // 声音
    private SeekBar sbVoice;

    private MuteControl mMuteControl;
    private ConnectivityManager mConnService;
    private OpenUtils openUtils;

    private boolean mobileDataStatus=false;// 移动数据状态
    private boolean wlanStatus;// wlan状态
    private boolean wlanHot;// wlan热点状态
    private boolean fmShotStatus;// FM发射状态
    private boolean screenMode;// 调整屏幕亮度模式,true为自动,false为手动
    private int initProgress;
    private boolean screenType;
    private final int WIFI_STATUS_NORMAL = 0;
    private final int WIFI_STATUS_CHANGING = 1;
    private final int WIFIAP_STATUS_CHANGING = 2;
    private int mWifiStatus = WIFI_STATUS_NORMAL;

    private boolean firstShowDialog = true;// 判断是否第一次初始化
    /**
     * 返回true为不休眠，false有休眠时间
     */
    private boolean screenSaver;// 关闭或者打开休眠
    private boolean mMute;// 是否静音
    private boolean isAttach;

    private View cameraViewDialog;
    private Dialog cameraDialog;
    private Button btnConfirm;
    private int cameraNum;
    private LayoutInflater inflater;
    private static boolean isYJLYOpen;
    private static boolean isYJLYStatusChange;// 云记录仪状态是否在改变

    private String security_mode = "persist.sys.security_mode";
    private String security_enable = "persist.sys.enable_security";
    /**
     * SYSTEMUI控制云记录仪
     */
    public static final String ACTION_OUT_VIDEO_SYSTEMUI = "com.spreadwin.camera.outvideo.systemui";
    public static final String ACTION_OUT_REPLY_SYSTEMUI = "com.spreadwin.camera.outreply.systemui";

    public static final String PREFERENCE_NAME = "camera_status";
    private int mCameraAutoCloseTime = 5;// 5s无动作自动消失
    private final int MSG_YJLY_STATUS_CHANGE = 5;
    private final int MSG_CAMERA_AUTO_CLOSE_TIME = 2;
    private static final String ACTION_SAY_SOMETHING = "ACTION_SAY_SOMETHING";
    private static final String EXTRA_SAY_SOMETHING = "EXTRA_SAY_SOMETHING";

    private final String FMACTION = "MYACTION_RETURN_STATE";
    private InitSettingReceiver receiver = new InitSettingReceiver();
    private CarmerReceiver carmerReceiver=new CarmerReceiver();
    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String ACTION_TETHER_STATE_CHANGED = ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED;
    private final int mAutoCloseTime = 8 * 1000;//5s无动作自动消失
    private final int MSG_AUTO_CLOSE_TIME = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUTO_CLOSE_TIME:
//                    dismissDialog();
                    break;
                case MSG_CAMERA_AUTO_CLOSE_TIME:
                    // Log.d(TAG, "MSG_AUTO_SCREEN_BRIGHTNESS");
                    mCameraAutoCloseTime -= 1;
//                    btnConfirm.setText(mContext.getString(R.string.confirm, mCameraAutoCloseTime));
                    if (mCameraAutoCloseTime > 0) {
                        // sendEmptyMessageDelayed(MSG_CAMERA_AUTO_CLOSE_TIME,
                        // 1000);
//                        if (cameraDialog.isShowing()) {
                            mHandler.post(runnable);
//                        }
                    } else if (mCameraAutoCloseTime == 0) {
//                        cameraDialog.dismiss();
                        removeCallbacks(runnable);
                        openYjly();
                        ivYJLY.setImageResource(R.drawable.ico_anim);
//					ivYJLY.setClickable(false);
//					tvYJLY.setClickable(false);
                        isYJLYStatusChange = true;
                        sendEmptyMessageDelayed(MSG_YJLY_STATUS_CHANGE, 8000);
                    } else {
//                        cameraDialog.dismiss();
                        removeCallbacks(runnable);
                    }
                    break;
                case MSG_YJLY_STATUS_CHANGE:
                    Log.i(TAG, "handleMessage isYJLYStatusChange=" + isYJLYStatusChange + ",isYJLYOpen=" + isYJLYOpen);
                    if (isYJLYStatusChange) {
                        if (isYJLYOpen) {
                            ivYJLY.setBackgroundResource(R.mipmap.ico_yjly_blue);
                            tvYJLY.setTextColor(
                                    mContext.getResources().getColor(R.color.shortcut_setting_text_checked_color));
                        } else {
                            ivYJLY.setBackgroundResource(R.mipmap.ico_yjly_white);
                            tvYJLY.setTextColor(
                                    mContext.getResources().getColor(R.color.shortcut_setting_text_normal_color));
                        }
//					ivYJLY.setClickable(true);
//					tvYJLY.setClickable(true);
                        ivYJLY.setImageResource(0);
                    }
                    break;
            }
        }
    };

    public static InitSettingShortcut getInstance(Context context) {
        if (mInitSettingShortcut == null) {
            mInitSettingShortcut = new InitSettingShortcut(context);
        }

        return mInitSettingShortcut;
    }

    public InitSettingShortcut(Context context) {
        this.mContext = context;
        mView = LayoutInflater.from(context).inflate(R.layout.shortcut_setting, null);
        initView();
    }

    private void initView() {
/**
 * 初始化dialog中各个控件及添加点击事件
 */
        upContainer = (LinearLayout) mView
                .findViewById(R.id.up_container);
        downContainer = (LinearLayout) mView
                .findViewById(R.id.down_container);


        sbLight = (SeekBar) mView.findViewById(R.id.sb_light_setting);
        sbVoice = (SeekBar) mView.findViewById(R.id.sb_voice_setting);

        llYjly=(LinearLayout) mView.findViewById(R.id.ll_yjly);
        llYDSJ=(LinearLayout)mView.findViewById(R.id.ll_ydsj);
        llPM=(LinearLayout)mView.findViewById(R.id.ll_prevention_mode);
        llPower=(LinearLayout)mView.findViewById(R.id.ll_power);
        llWlan=(LinearLayout)mView.findViewById(R.id.ll_wlan);
        llWlanHot=(LinearLayout)mView.findViewById(R.id.ll_wlan_hot);
        llEdog=(LinearLayout)mView.findViewById(R.id.ll_edog);

        ivYDSJ = (ImageView) mView.findViewById(R.id.iv_ydsj);
        tvYDSJ =(TextView) mView.findViewById(R.id.tv_ydsj);
        ivPM = (ImageView) (ImageView)mView.findViewById(R.id.iv_prevention_mode);
        tvPM =(TextView) mView.findViewById(R.id.tv_prevention_mode);
        ivBattery = (ImageView)mView.findViewById(R.id.iv_power);
        tvBattery =(TextView) mView.findViewById(R.id.tv_power);
        ivWlan =(ImageView) mView.findViewById(R.id.iv_wlan);
        tvWlan =(TextView) mView.findViewById(R.id.tv_wlan);
        ivWlanHot = (ImageView)mView.findViewById(R.id.iv_wlan_hot);
        tvWlanHot = (TextView)mView.findViewById(R.id.tv_wlan_hot);
        ivYJLY =(ImageView) mView.findViewById(R.id.iv_yjly);
        tvYJLY = (TextView)mView.findViewById(R.id.tv_yjly);
        llYjly.setOnClickListener(this);
        llYDSJ.setOnClickListener(this);
        llPM.setOnClickListener(this);
        llPower.setOnClickListener(this);
        llWlan.setOnClickListener(this);
        llWlanHot.setOnClickListener(this);
        llEdog.setOnClickListener(this);



        sbLight.setOnSeekBarChangeListener(this);
        sbVoice.setOnSeekBarChangeListener(this);


        upContainer.setOnClickListener(this);
        downContainer.setOnClickListener(this);


    }

    public void initCusDialog() {
        getShortcutStatus();

        setStatus();// 设置checkbox是否被选中以及SeekBar的值
//        initDialog();
    }

    private void getShortcutStatus() {
        openUtils = new OpenUtils(mContext);
        mMuteControl = new MuteControl(mContext);
        mConnService = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mobileDataStatus = openUtils.getMobileDataStatus(mContext);
        wlanStatus = openUtils.isWifiOpen();
        screenMode = openUtils.getScreenMode() == 1 ? true : false;
        wlanHot = openUtils.getWifiApEnabled();
        screenSaver = openUtils.getScreenOffTime() > -1 ? false : true;
        //mMute = openUtils.getRingerMode();
        mMute = mMuteControl.getStreamMute();
        cameraDialog = new Dialog(mContext, R.style.Dialog);
        Log.i(TAG, "isStreamMute == " + mMute);
    }

    private void setStatus() {
        if(mobileDataStatus){
            ivYDSJ.setBackgroundResource(R.mipmap.ico_mobiledata_blue);
            tvYDSJ.setTextColor(mContext.getResources().getColor(R.color.shortcut_setting_text_checked_color));
        }else{
            ivYDSJ.setBackgroundResource(R.mipmap.ico_mobiledata_white);
            tvYDSJ.setTextColor(mContext.getResources().getColor(R.color.shortcut_setting_text_normal_color));
        }
        int screenBrightness = (int) Math
                .round((openUtils.getScreenBrightness() - 50) / 2.05);
        if (screenBrightness < 0)
            screenBrightness = 0;
        sbLight.setProgress(screenBrightness);

        sbVoice.setMax(mMuteControl.getMaxVolume());
        sbVoice.setProgress(mMuteControl.getStreamVolume());
        Log.i(TAG, "Max Voice==" + mMuteControl.getMaxVolume() + ";current Voice=="
                + mMuteControl.getStreamVolume());
        if (SystemProperties.getBoolean(security_enable, false)) {
            ivPM.setEnabled(true);
            if (SystemProperties.getBoolean(security_mode, false)) {
                ivPM.setBackgroundResource(R.mipmap.ico_fd_d);
                tvPM.setTextColor(mContext.getResources().getColor(R.color.shortcut_setting_text_checked_color));
            } else {
                ivPM.setBackgroundResource(R.mipmap.ico_fd_u);
                tvPM.setTextColor(mContext.getResources().getColor(R.color.shortcut_setting_text_normal_color));
            }
        } else {
            ivPM.setEnabled(false);
        }

    }


    public View getView() {
        return mView;
    }

    public void dismissDialog() {
        try {
            if (isAttach) {
                isAttach = false;
                wm.removeViewImmediate(mView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void attachDialog() {
        if (!isAttach) {
            wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
//            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
//            params.type = WindowManager.LayoutParams.TYPE_PHONE;
            params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
//            params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            params.format = PixelFormat.RGBA_8888;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            params.gravity = Gravity.CENTER;
            params.x = 0;
            params.y =0;
            InitSettingShortcut dialog = InitSettingShortcut.getInstance(mContext);
            wm.addView(dialog.getView(), params);

            isAttach = true;
        }
    }

    @Override
    public void onClick(View v) {
        resetAutoTime();
        switch (v.getId()) {
            case R.id.up_container:// 关闭dialog
                dismissDialog();
                break;
            case R.id.down_container:// 关闭dialog
                dismissDialog();
                break;
            case R.id.ll_wlan:// iv Wlan
                onChangeWifiOrAp(true);
                break;
            case R.id.ll_wlan_hot:// iv WlanHot
                onChangeWifiOrAp(false);
                break;
            case R.id.ll_yjly:
                operateYJLY();
                break;
            case R.id.ll_edog:
//                  changeSystem();
//                mContext.sendBroadcast(new Intent("ACTION_SPREADWIN_NORMAL_REBOOT"));
                break;
            case R.id.ll_power:
               // 息屏
                openUtils.closeScreen();
                dismissDialog();
                break;
            case R.id.ll_ydsj:
                operateMobileData();// 打开或关闭移动数据
                break;
            case R.id.ll_prevention_mode:
                 operatePM();
                break;

            default:
                break;
        }
    }

//    private void changeSystem(){
//        int value=android.os.SystemProperties.getInt("persist.sys.luncher", 0);
//        SystemProperties.set("persist.sys.luncher",""+(1-value));
//        CommandExecution.execRootCmd("stop");
//        CommandExecution.execRootCmd("start");
//    }



    private void operateMobileData(){
        boolean isMobileStatus=openUtils.getMobileDataStatus(mContext);
        Log.i(TAG,"isMobileStatus="+isMobileStatus);
        if(isMobileStatus){
            openUtils.setMobileDataStatus(mContext, false);// 打开或关闭移动数据
            ivYDSJ.setBackgroundResource(R.mipmap.ico_mobiledata_white);
            tvYDSJ.setTextColor(mContext.getResources().getColor(R.color.shortcut_setting_text_normal_color));
        }else{
            openUtils.setMobileDataStatus(mContext, true);// 打开或关闭移动数据
            ivYDSJ.setBackgroundResource(R.mipmap.ico_mobiledata_blue);
            tvYDSJ.setTextColor(mContext.getResources().getColor(R.color.shortcut_setting_text_checked_color));
        }
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)// checkout状态改变事件
    {
        resetAutoTime();
    }

    private void operatePM(){
        boolean mode = SystemProperties.getBoolean(security_mode, false);
        Log.i(TAG,"iv_prevention_mode mode ==" + mode);
        if (mode) {
            ivPM.setBackgroundResource(R.mipmap.ico_fd_u);
            tvPM.setTextColor(mContext.getResources().getColor(R.color.shortcut_setting_text_normal_color));
            sendProperty(security_mode, false);
        } else {
            sendProperty(security_mode, true);
            ivPM.setBackgroundResource(R.mipmap.ico_fd_d);
            tvPM.setTextColor(mContext.getResources().getColor(R.color.shortcut_setting_text_checked_color));
        }
    }
    private void sendProperty(String mode, boolean status) {
        Intent intent = new Intent("ACTION_SPREADWIN_SECURITY_MODE");
        intent.putExtra("status", status);
        mContext.sendBroadcast(intent);
    }
    private void startLiveControlServer() {
        Intent i = new Intent();
        i.setComponent(new ComponentName("com.spreadwin.cameralivecontroller",
                "com.spreadwin.cameralivecontroller.service.ControllerService"));
        mContext.startService(i);
    }

    private void operateYJLY() {
        if (!isAppRunning()) {
            Log.i(TAG, "isAppRunning false");
            startLiveControlServer();
            Intent i = new Intent(ACTION_SAY_SOMETHING);
            i.putExtra(EXTRA_SAY_SOMETHING, "直播控制器还未启动，请稍后重试");
            mContext.sendBroadcast(i);
            return;
        }

        boolean status = getBoolean(mContext, "status", false);
        Log.i(TAG, "cbYJLY isYJLYOpen=" + isYJLYOpen + ",status=" + status);
        if (!isYJLYOpen) {
//            showCameraSelectDialog();
            mCameraAutoCloseTime = 1;
            cameraNum=1;
            mHandler.removeMessages(MSG_CAMERA_AUTO_CLOSE_TIME);
            new Thread(runnable).start();
        } else {
            Intent it = new Intent(ACTION_OUT_VIDEO_SYSTEMUI);
            it.putExtra("cameraid", Camera.CameraInfo.CAMERA_FACING_BACK);
            it.putExtra("is_start", !status);
            it.putExtra("out_mode", 1);
            mContext.sendBroadcast(it);
            Intent it1 = new Intent(ACTION_OUT_VIDEO_SYSTEMUI);
            it1.putExtra("cameraid", Camera.CameraInfo.CAMERA_FACING_FRONT);
            it1.putExtra("is_start", !status);
            it1.putExtra("out_mode", 1);
            mContext.sendBroadcast(it1);
            // ivYJLY.setBackgroundResource(R.drawable.ico_cloud_video_white);
            // tvYJLY.setTextColor(context.getResources().getColor(R.color.shortcut_setting_text_normal_color));
            // Log.i(TAG, "onCheckedChanged putBoolean
            // status="+cbYJLY.isChecked());
            // putBoolean(context,"status",cbYJLY.isChecked());
            mHandler.sendEmptyMessageDelayed(MSG_YJLY_STATUS_CHANGE, 8000);
            ivYJLY.setImageResource(R.drawable.ico_anim);
//			ivYJLY.setClickable(false);
//			tvYJLY.setClickable(false);
            isYJLYStatusChange = true;
        }
    }
    private final class CarmerReceiver extends BroadcastReceiver {
        // 当扫描结束后将会触发该方法
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "CameraReceiver action=" + intent.getAction());
            if (intent.getAction().equals(ACTION_OUT_REPLY_SYSTEMUI)) {
                int num = intent.getIntExtra("status", 1);
                int error_code = intent.getIntExtra("error", -1);
                Log.i(TAG, "CameraReceiver status=" + num + ",error_code=" + error_code);

                if (error_code == 0) {
                    if (num == 6 || num == 7) {
                        ivYJLY.setBackgroundResource(R.mipmap.ico_yjly_blue);
                        tvYJLY.setTextColor(
                                context.getResources().getColor(R.color.shortcut_setting_text_checked_color));
                        Log.i(TAG, "CameraReceiver putBoolean status=true");
                        putBoolean(context, "status", true);
                        isYJLYOpen = true;
                        openUtils.showNotification(mContext,R.mipmap.stat_sys_data_fully_cloud_recorder);
                    } else {
                        ivYJLY.setBackgroundResource(R.mipmap.ico_yjly_white);
                        tvYJLY.setTextColor(
                                context.getResources().getColor(R.color.shortcut_setting_text_normal_color));
                        Log.i(TAG, "CameraReceiver putBoolean status=false");
                        putBoolean(context, "status", false);
                        isYJLYOpen = false;
                        openUtils.cancelNotification(mContext,R.mipmap.stat_sys_data_fully_cloud_recorder);
                    }
                } else {
                    if (error_code == -23 && num == 1) {
                        Intent i = new Intent(ACTION_SAY_SOMETHING);
                        i.putExtra(EXTRA_SAY_SOMETHING, "时光流视频未打开，请先打开");
                        context.sendBroadcast(i);
                    }
                    ivYJLY.setBackgroundResource(R.mipmap.ico_yjly_white);
                    tvYJLY.setTextColor(context.getResources().getColor(R.color.shortcut_setting_text_normal_color));
                    Log.i(TAG, "CameraReceiver putBoolean status=false");
                    putBoolean(context, "status", false);
                    isYJLYOpen = false;
                    openUtils.cancelNotification(mContext,R.mipmap.stat_sys_data_fully_cloud_recorder);
                }
                isYJLYStatusChange = false;
                ivYJLY.setClickable(true);
                tvYJLY.setClickable(true);
                ivYJLY.setImageResource(0);
            }

        }
    }
    private boolean isAppRunning() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> list = am.getRunningServices(100);
        boolean isAppRunning = false;
        final String MY_PKG_NAME = "com.spreadwin.cameralivecontroller";
        for (ActivityManager.RunningServiceInfo info : list) {
            if (info.service.getPackageName().equals(MY_PKG_NAME)) {
                isAppRunning = true;
                break;
            }
        }
        return isAppRunning;
    }

    public boolean putBoolean(Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public boolean putInt(Context context, String key, int camera) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, camera);
        return editor.commit();
    }

    public boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_MULTI_PROCESS);
        return settings.getBoolean(key, defaultValue);
    }

    private void showCameraSelectDialog() {
        inflater = LayoutInflater.from(mContext);
        cameraViewDialog = inflater.inflate(R.layout.camera_selection, null);
        final RadioGroup rgCameraSelected = (RadioGroup) cameraViewDialog.findViewById(R.id.camera_selected);
        btnConfirm = (Button) cameraViewDialog.findViewById(R.id.btn_confirm);
        Button btnConcel = (Button) cameraViewDialog.findViewById(R.id.btn_concel);
        rgCameraSelected.check(rgCameraSelected.getChildAt(0).getId());
        cameraNum = 1;
        rgCameraSelected.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                Log.i(TAG, "CameraReceiver checkedId=" + checkedId);
                if (rgCameraSelected.getChildAt(0).getId() == checkedId) {
                    cameraNum = 1;
                } else if (rgCameraSelected.getChildAt(1).getId() == checkedId) {
                    cameraNum = 0;
                } else if (rgCameraSelected.getChildAt(2).getId() == checkedId) {
                    cameraNum = 2;
                }
            }
        });
        btnConfirm.setText(mContext.getString(R.string.confirm, 5));
        btnConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                boolean status = getBoolean(mContext, "status", false);
                mCameraAutoCloseTime = 1;
                // mHandler.removeCallbacks(runnable);
                // openYjly();
                // cameraDialog.dismiss();

            }
        });
        btnConcel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mCameraAutoCloseTime = -1;
                mHandler.removeCallbacks(runnable);
                cameraDialog.dismiss();

            }
        });
        cameraDialog.setContentView(cameraViewDialog);
        cameraDialog.setCanceledOnTouchOutside(true);
        Window window = cameraDialog.getWindow();
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();// 获取屏幕宽高用
        WindowManager.LayoutParams layoutParams = window.getAttributes();// 获取对话框当前的参数
        layoutParams.gravity = Gravity.CENTER;
        window.setAttributes(layoutParams);
        cameraDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        if (!cameraDialog.isShowing()) {
            cameraDialog.show();
            dismissDialog();
        }
    }

    private void openYjly() {
        putInt(mContext, "cameraNum", cameraNum);
        if (cameraNum == 1) {
            Log.i(TAG, "CameraReceiver getChildAt(0)");
            Intent it1 = new Intent(ACTION_OUT_VIDEO_SYSTEMUI);
            it1.putExtra("cameraid", Camera.CameraInfo.CAMERA_FACING_FRONT);
            it1.putExtra("is_start", true);
            it1.putExtra("out_mode", 1);
            mContext.sendBroadcast(it1);

        } else if (cameraNum == 0) {
            Log.i(TAG, "CameraReceiver getChildAt(1)");
            Intent it = new Intent(ACTION_OUT_VIDEO_SYSTEMUI);
            it.putExtra("cameraid", Camera.CameraInfo.CAMERA_FACING_BACK);
            it.putExtra("is_start", true);
            it.putExtra("out_mode", 1);
            mContext.sendBroadcast(it);
        } else if (cameraNum == 2) {
            Log.i(TAG, "CameraReceiver getChildAt(2)");
            Intent it = new Intent(ACTION_OUT_VIDEO_SYSTEMUI);
            it.putExtra("cameraid", Camera.CameraInfo.CAMERA_FACING_BACK);
            it.putExtra("is_start", true);
            it.putExtra("out_mode", 1);
            mContext.sendBroadcast(it);
            Intent it1 = new Intent(ACTION_OUT_VIDEO_SYSTEMUI);
            it1.putExtra("cameraid", Camera.CameraInfo.CAMERA_FACING_FRONT);
            it1.putExtra("is_start", true);
            it1.putExtra("out_mode", 1);
            mContext.sendBroadcast(it1);
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                mHandler.sendEmptyMessageDelayed(MSG_CAMERA_AUTO_CLOSE_TIME, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void setIsMute(boolean isChecked) {
        if (isChecked) {
            if (!mMute) {
                sbVoice.setEnabled(false);
                mMute = true;
                //openUtils.setRingerMode(true);
                mMuteControl.setChangeStreamMode();
            }
        } else {
            if (mMute) {
                sbVoice.setEnabled(true);
                mMute = false;
                sbVoice.setProgress(Integer.valueOf(mMuteControl.getStreamVolume()));
                //openUtils.setRingerMode(false);
                mMuteControl.setChangeStreamMode();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        resetAutoTime();

        if (seekBar == sbLight)// 亮度
        {
            openUtils.setSysScreenBrightness((int) Math.round((progress * 2.05) + 50));
        } else if (seekBar == sbVoice)// 设置音量
        {
            int voice = seekBar.getProgress();
            if (firstShowDialog) {
                firstShowDialog = false;
            } else {
                mMuteControl.setStreamVolume(voice);
            }
            Log.i(TAG, "current voice==" + mMuteControl.getStreamVolume());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)// 开始点击SeekBar时触发
    {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)// 结束点击SeekBar时触发
    {
        // if (seekBar == sbScreensaver)
        // tvScreensaver.setText(getSetStr(sbScreensaver));

    }


    public boolean isAttach() {
        return isAttach;
    }

    /**
     * 重置消失时间
     */
    private void resetAutoTime() {
        Log.i(TAG, "resetAutoTime");
        mHandler.removeMessages(MSG_AUTO_CLOSE_TIME);
        mHandler.sendEmptyMessageDelayed(MSG_AUTO_CLOSE_TIME, mAutoCloseTime);
    }

    /**
     * 改变wifi或者wifi热点的状态
     *
     * @param isWifi true:wifi,false:wifiAp
     */
    private void onChangeWifiOrAp(boolean isWifi) {
        Log.i(TAG, "onChangeWifiOrAp isWifi=" + isWifi);
        if (mWifiStatus != WIFI_STATUS_NORMAL) {
            Log.i(TAG, "Wifi is switching");
            return;
        }
        if (isWifi) {
            Log.i(TAG, "get hotSpotSttu canChangeWifiStatu=" + openUtils.canChangeWifiStatu());
            if (openUtils.canChangeWifiStatu()) {
                mWifiStatus = WIFI_STATUS_CHANGING;
                onSetWifiClickable(false, true);
                setWlanHotBackground(false);
                openUtils.onOpenWifi();
            }
        } else {
            Log.i(TAG, "get hotSpotSttu canChangeWifiApStatu=" + openUtils.canChangeWifiApStatu());
            if (openUtils.canChangeWifiApStatu()) {
                boolean hotSpotSttu = openUtils.getWifiApEnabled();
                Log.i(TAG, "get hotSpotSttu statu=" + hotSpotSttu);
                mWifiStatus = WIFIAP_STATUS_CHANGING;
                onSetWifiClickable(false, false);
                if (hotSpotSttu) {
                    Log.i(TAG, "set wlan hot off");
                    openUtils.openOrCloseWifiAp_1(false);
                    setWlanHotBackground(false);
                } else {
                    if (openUtils.isWifiOpen()) {
                        openUtils.setWifiToggle(false);
                    }
                    boolean hotspot = openUtils.openOrCloseWifiAp_1(true);
                    Log.i(TAG, "set wlan hot true ,hotspot statu=" + hotspot);
                    setWlanHotBackground(hotspot);
                }

            }


        }

    }

    class InitSettingReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifiState = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, 0);
                Log.i(TAG, "wifiState" + wifiState);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLING:
                    case WifiManager.WIFI_STATE_ENABLING:
                        onSetWifiClickable(false, true);
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        onSetWifiClickable(true, true);
                        ivWlan.setBackgroundResource(R.mipmap.ico_wlan_white);
                        tvWlan.setTextColor(context.getResources().getColor(R.color.shortcut_setting_text_normal_color));
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        onSetWifiClickable(true, true);
                        ivWlanHot
                                .setBackgroundResource(R.mipmap.ico_wlanap_white);
                        tvWlanHot.setTextColor(context.getResources().getColor(R.color.shortcut_setting_text_normal_color));
                        ivWlan.setBackgroundResource(R.mipmap.ico_wlan_blue);
                        tvWlan.setTextColor(context.getResources().getColor(R.color.shortcut_setting_text_checked_color));
                        break;
                    default:
                        onSetWifiClickable(true, true);
                        break;
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent
                    .getAction())) {
                Parcelable parcelableExtra = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;// 当然，这边可以更精确的确定状态
                    Log.i(TAG, "isConnected" + isConnected);
                    if (isConnected) {
                    } else {

                    }
                }
            } else if (action.equals(CONNECTIVITY_CHANGE_ACTION)) {
                if (TextUtils.equals(action, CONNECTIVITY_CHANGE_ACTION)) { // 网络变化的时候会发送通知
                    Log.i(TAG, "CONNECTIVITY_CHANGE_ACTION");
                }
            } else if (action.equals(ACTION_TETHER_STATE_CHANGED)) {
                Log.i(TAG, "ACTION_TETHER_STATE_CHANGED");

            } else if (intent.getAction().equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                int wifiApState = intent.getIntExtra("wifi_state", 14);
                Log.i(TAG, "onSetWifiClickable wifiApState ==" + wifiApState + "; wlanHot ==" + wlanHot);
                switch (wifiApState) {
                    case 13:
                        setWlanHotBackground(true);
                        onSetWifiClickable(true, false);
                        break;
                    case 11:
                        setWlanHotBackground(false);
                        onSetWifiClickable(true, false);
                        break;
                    case 10:
                    case 12:
                        onSetWifiClickable(false, false);
                        break;
                    default:
                        onSetWifiClickable(false, false);
                        break;
                }
            } else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int current = intent.getExtras().getInt("level");// 获得当前电量
                int total = intent.getExtras().getInt("scale");// 获得总电量
                int percent = current * 100 / total;
            }

        }
    }

    public void onSetWifiClickable(boolean clickable, boolean isWifi) {
        Log.i(TAG, "onSetWifiClickable clickable ==" + clickable + "; isWifi ==" + isWifi + "; mWifiStatus==" + mWifiStatus);
        switch (mWifiStatus) {
            case WIFI_STATUS_NORMAL:
                if (isWifi) {
                    ivWlan.setImageResource(clickable ? 0 : R.drawable.ico_anim);
                    ivWlan.setClickable(clickable);
                } else {
                    ivWlanHot.setImageResource(clickable ? 0 : R.drawable.ico_anim);
                    ivWlanHot.setClickable(clickable);
                }
                break;
            case WIFI_STATUS_CHANGING:
                if (isWifi) {
                    ivWlan.setImageResource(clickable ? 0 : R.drawable.ico_anim);
                    ivWlanHot.setImageResource(clickable ? 0 : R.drawable.ico_anim);
                    ivWlan.setClickable(clickable);
                    ivWlanHot.setClickable(clickable);
                    if (clickable) {
                        mWifiStatus = WIFI_STATUS_NORMAL;
                    }
                }
                break;
            case WIFIAP_STATUS_CHANGING:
                if (!isWifi) {
                    ivWlan.setImageResource(clickable ? 0 : R.drawable.ico_anim);
                    ivWlanHot.setImageResource(clickable ? 0 : R.drawable.ico_anim);
                    ivWlan.setClickable(clickable);
                    ivWlanHot.setClickable(clickable);
                    if (clickable) {
                        mWifiStatus = WIFI_STATUS_NORMAL;
                    }
                }
                break;
        }
    }

    private void setWlanHotBackground(boolean open) {// set hotspot background
        if (open) {
            ivWlanHot.setBackgroundResource(R.mipmap.ico_wlanap_blue);
            tvWlanHot.setTextColor(mContext.getResources().getColor(R.color.shortcut_setting_text_checked_color));
        } else {
            ivWlanHot.setBackgroundResource(R.mipmap.ico_wlanap_white);
            tvWlanHot.setTextColor(mContext.getResources().getColor(R.color.shortcut_setting_text_normal_color));
        }
    }

    private void setFMOnOff(boolean isChecked) {
        if (isChecked) {
            if (!fmShotStatus) {
                fmShotStatus = true;
                openUtils.openFMShot();
            }
        } else {
            if (fmShotStatus) {
                fmShotStatus = false;
                openUtils.openFMShot();
            }
        }
    }



    /**
     * 注册广播
     */
    public void registReceiver() {
        Log.i(TAG, "registReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(FMACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction(CONNECTIVITY_CHANGE_ACTION);
        filter.addAction(ACTION_TETHER_STATE_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(receiver, filter);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_OUT_REPLY_SYSTEMUI);
        mContext.registerReceiver(carmerReceiver, intentFilter);
    }

    /**
     * 销毁广播
     */
    public void unRegistReceiver() {
        if (receiver != null) {
            Log.i(TAG, "unRegistReceiver");
            mContext.unregisterReceiver(receiver);
        }
        if (carmerReceiver!=null){
            mContext.unregisterReceiver(carmerReceiver);
        }
    }
}
