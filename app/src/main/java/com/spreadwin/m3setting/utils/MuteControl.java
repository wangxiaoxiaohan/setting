package com.spreadwin.m3setting.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * 静音控制
 * @author Tong Yilu
 *
 */
public class MuteControl {
	public static final String TAG = "MuteControl";
    public static final boolean DEBUG = true;
    
    public static final String MUSIC_MUTE_CHANGED_ACTION = "android.media.MUSIC_MUTE_CHANGED_ACTION";
    public static final String BLUETOOTH_MUTE_CHANGED_ACTION = "android.media.BLUETOOTH_MUTE_CHANGED_ACTION";
    public static final String EXTRA_MUSIC_VOLUME_MUTED = "android.media.EXTRA_MUSIC_VOLUME_MUTED";
    public static final String EXTRA_BLUETOOTH_VOLUME_MUTED = "android.media.EXTRA_BLUETOOTH_VOLUME_MUTED";
    public static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    public static final String MUSIC_MUTE_SET_OTHER_ACTION = "android.media.MUSIC_MUTE_SET_OTHER_ACTION";
    public static final String MUSIC_MUTE_SET_NAVI_ACTION = "android.media.MUSIC_MUTE_SET_NAVI_ACTION";
    public static final String MUSIC_MUTE_RESTORE_ACTION = "android.media.MUSIC_MUTE_RESTORE_ACTION";
    public static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    
    
    public static MuteControl mMuteControl;
    
    private Context mContext;
    
    private AudioManager mAudioManager;
    
    private boolean mNaviMute = false;
	private boolean mOnlyMusic = false;
	private boolean mOtherMute = false;
	
	private int  STREAM_FM =10;
	private int  mOldFM = 0;
	private Long checkTime = 0L;
    
    private MuteStatusListener mMuteStatusListener;
    
	public MuteControl(Context context) {
		this.mContext = context;
		init();
	}
	private void init() {
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		registerReceiver();
	}	
	
	public static MuteControl getInstance(Context context) {
		if (mMuteControl == null) {
			mMuteControl = new MuteControl(context);
		}
		return mMuteControl;
	}
	
	public void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(VOLUME_CHANGED_ACTION);
		filter.addAction(MUSIC_MUTE_CHANGED_ACTION);
		filter.addAction(MUSIC_MUTE_SET_OTHER_ACTION);
		filter.addAction(MUSIC_MUTE_SET_NAVI_ACTION);
		filter.addAction(MUSIC_MUTE_RESTORE_ACTION);
		mContext.registerReceiver(audioReceiver, filter);
	}
	
	 BroadcastReceiver audioReceiver = new BroadcastReceiver() {		
		@Override
		public void onReceive(Context context, Intent intent) {
			mLog( "action =="+intent.getAction());
			if (intent.getAction().equals(MUSIC_MUTE_CHANGED_ACTION)) {
				if (mMuteStatusListener != null) {
					if (intent.getBooleanExtra(EXTRA_MUSIC_VOLUME_MUTED, false)) {
//					mMute.setImageResource(R.drawable.btn_mute_off);		
						mMuteStatusListener.onMuteStatusChange(true);
					}else {
						mMuteStatusListener.onMuteStatusChange(false);
//					mMute.setImageResource(R.drawable.btn_mute_on);		
					}					
				}
			}else if (intent.getAction().equals(VOLUME_CHANGED_ACTION)) {
				int streamType = intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, AudioManager.STREAM_MUSIC);
				mLog( "streamType =="+streamType);
				mOnlyMusic = false;
				if (streamType == AudioManager.STREAM_MUSIC) {
					resetStreamMode();					
				}
			}
			else if (intent.getAction().equals(MUSIC_MUTE_SET_OTHER_ACTION)) {
				boolean setMute = intent.getBooleanExtra(EXTRA_MUSIC_VOLUME_MUTED, false);
				mOnlyMusic = intent.getBooleanExtra("only_music", false);
				otherSetStreamMode(setMute);
			}
			else if (intent.getAction().equals(MUSIC_MUTE_SET_NAVI_ACTION)) {
				boolean setMute = intent.getBooleanExtra(EXTRA_MUSIC_VOLUME_MUTED, false);
				mOnlyMusic = intent.getBooleanExtra("only_music", false);
				naviSetStreamMode(setMute);
			}
			else if (intent.getAction().equals(MUSIC_MUTE_RESTORE_ACTION)) {
				mOnlyMusic = intent.getBooleanExtra("only_music", false);
				otherRestoreStreamMode();
			}			
		}
	};
	
	
	
	/**
	 * 恢复静音
	 */
	private void resetStreamMode() {			
		mLog("resetStreamMode mute =="+getStreamMute());
		if (getStreamMute()) {			
			//audio.setStreamMute(AudioManager.STREAM_MUSIC, false);		
			naviSetStreamMode(false);
//			mMute.setImageResource(R.drawable.btn_mute_on);	
		}
	}
	
	/**
	 * 临时静音
	 * @param mute
	 */
	private void otherSetStreamMode(boolean mute) {
		mOtherMute = mute;
		if(mOtherMute != getStreamMute()) {
			mLog("otherSetStreamMode "+mOtherMute);
			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, mOtherMute);			
			onBTMute(mOtherMute);
		}
	}

	/**
	 * 恢复临时静音
	 */
	private void otherRestoreStreamMode() {
		if(mNaviMute != getStreamMute()) {
			mLog("otherRestoreStreamMode "+mNaviMute);
			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, mNaviMute);
			onBTMute(mNaviMute);
			if(mMuteStatusListener != null)
				mMuteStatusListener.onMuteStatusChange(mNaviMute);		
		}
	}
	
	/**
	 * 全局静音
	 * @param mute
	 */
	private void naviSetStreamMode(boolean mute) {
		mNaviMute = mute;
		if(mNaviMute != getStreamMute()) {
			mLog("naviSetStreamMode "+mNaviMute);
			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, mNaviMute);			
			onBTMute(mNaviMute);
			int index = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			mLog("naviSetStreamMode index =="+index);
			if(mMuteStatusListener != null)
				mMuteStatusListener.onMuteStatusChange(mNaviMute);		
		}
        if(mNaviMute != getAlarmStreamMute()) {
			mAudioManager.setStreamMute(AudioManager.STREAM_ALARM, mNaviMute);
			mLog("naviSetStreamMode alarm "+mNaviMute);
        }
	}
	
	/**
	 * 更新BT状态
	 * @param status
	 */
	public void onBTMute(boolean status) {
		mLog("onFmMute111 =="+status+"; mOldFM =="+mOldFM+"; mOnlyMusic =="+mOnlyMusic);
		if (!mOnlyMusic) {
			Intent intent = new Intent(BLUETOOTH_MUTE_CHANGED_ACTION);
			intent.putExtra(EXTRA_BLUETOOTH_VOLUME_MUTED, status);
			mContext.sendBroadcast(intent);			
		}		
	}
	
	/**
	 * 改变静音状态
	 */
	public void setChangeStreamMode(){
		mLog("setChangeStreamMode getStreamMute()=="+getStreamMute());
		if (getStreamMute()) {
			naviSetStreamMode(false);
		}else {
			naviSetStreamMode(true);
		}
//		mLog("setChangeStreamMode getStreamMute=="+getStreamMute());
//		if(mMuteStatusListener != null)
//			mMuteStatusListener.onMuteStatusChange(mNaviMute);			
	}
	
	/**
	 * 设置声音大小，1为加声音，-1为减声音
	 * @param step
	 */
	public void setStreamVolume(int val) {
		if(getStreamMute()){
			resetStreamMode();
		}
		int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int index = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mLog("setStreamVolume val 1 =="+val+" max "+max);
		if (val < 0) {
            val = 0;
		}
        else if (val > max) {
            val = max;
		}		
		mLog("setStreamVolume val 2 =="+val+" max "+max);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, val, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		
	}
	
    public int getStreamVolume() {
		return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		
	}

    public int getMaxVolume() {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

	/**
	 * 反射获取isStreamMute
	 * @return
	 */
	public boolean getStreamMute() {
		Class<?> cmClass = mAudioManager.getClass();
		Boolean isMute = false;
		try {
			Method method = cmClass.getDeclaredMethod("isStreamMute",Integer.TYPE);
			isMute = (Boolean) method.invoke(mAudioManager, AudioManager.STREAM_MUSIC);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mLog("getStreamMute =="+isMute);
		return isMute;
	}

    public boolean getAlarmStreamMute() {
		Class<?> cmClass = mAudioManager.getClass();
		Boolean isMute = false;
		try {
			Method method = cmClass.getDeclaredMethod("isStreamMute",Integer.TYPE);
			isMute = (Boolean) method.invoke(mAudioManager, AudioManager.STREAM_ALARM);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mLog("getAlarmStreamMute =="+isMute);
		return isMute;
	}


	
	public void unregisterReceiver(){
		mContext.unregisterReceiver(audioReceiver);		
		mMuteControl = null;
		mContext = null;
	}
	
	public interface MuteStatusListener{
		/**
		 * true 为静音状态，false反之
		 * @param status
		 */
		public void onMuteStatusChange(boolean status);
	}
	
	public void onMuteStatusChangeListener(MuteStatusListener _muteListener){
		mMuteStatusListener = _muteListener;
	}
	
	public static void mLog(String string) {
		if (DEBUG) {
			Log.d(TAG,string);
		}
	}
}
