package org.videolan.vlc.android;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.avadesign.camvideo.R;

public class VideoPlayerActivity_bak extends Activity {

	public final static String TAG = "VLC/VideoPlayerActivity";

	private SurfaceView mSurface;
	private SurfaceHolder mSurfaceHolder;
	private LibVLC mLibVLC;
	
	//David
	/*
	private SurfaceView mSurface2;
	private SurfaceHolder mSurfaceHolder2;
	private LibVLC mLibVLC2;
	*/
	
	private static final int SURFACE_FIT_HORIZONTAL = 0;
	private static final int SURFACE_FIT_VERTICAL = 1;
	private static final int SURFACE_FILL = 2;
	private static final int SURFACE_16_9 = 3;
	private static final int SURFACE_4_3 = 4;
	private static final int SURFACE_ORIGINAL = 5;
	private int mCurrentSize = SURFACE_FIT_HORIZONTAL;
	
	/** Overlay */
	private LinearLayout mOverlay;
	private LinearLayout mDecor;
	private View mSpacer;
	private static final int OVERLAY_TIMEOUT = 4000;
	private static final int FADE_OUT = 1;
	private static final int SHOW_PROGRESS = 2;
	private static final int SURFACE_SIZE = 3;
	private static final int FADE_OUT_INFO = 4;
	private boolean mDragging;
	private boolean mShowing;
	private SeekBar mSeekbar;
	private TextView mTime;
	private TextView mLength;
	private TextView mInfo;
	private ImageButton mPause;
	private ImageButton mLock;
	private ImageButton mSize;
	
	// size of the video
	private int mVideoHeight;
	private int mVideoWidth;

	// stop screen from dimming
	private WakeLock mWakeLock;
	
	//Yen
	private String IP;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_surface);
		
		//Yen
		Bundle bundle=this.getIntent().getExtras();
        IP=bundle.getString("KEY_IP");
		//Yen
        
		// stop screen from dimming
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
		
		
		/** initialize Views an their Events */
		 
		/* yen
		mDecor = (LinearLayout)findViewById(R.id.player_overlay_decor);
		mSpacer = (View)findViewById(R.id.player_overlay_spacer);
		*/
		mSpacer.setOnTouchListener(mTouchListener);
		
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mOverlay = (LinearLayout)inflater.inflate(R.layout.player_overlay, null);
		
		mTime = (TextView) mOverlay.findViewById(R.id.player_overlay_time);
		mLength = (TextView) mOverlay.findViewById(R.id.player_overlay_length);
		// the info textView is not on the overlay
		
		/* yen
		mInfo = (TextView) findViewById(R.id.player_overlay_info);
		*/
		mPause = (ImageButton) mOverlay.findViewById(R.id.player_overlay_play);
		mPause.setOnClickListener(mPauseListener);
		
		mLock = (ImageButton) mOverlay.findViewById(R.id.player_overlay_lock);
		mLock.setOnClickListener(mLockListener);
		
		mSize = (ImageButton) mOverlay.findViewById(R.id.player_overlay_size);
		mSize.setOnClickListener(mSizeListener);
		
		mSurface = (SurfaceView) findViewById(R.id.player_surface);
		mSurfaceHolder = mSurface.getHolder();	
		mSurfaceHolder.setKeepScreenOn(true);
		mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
		mSurfaceHolder.addCallback(mSurfaceCallback);

		mSeekbar = (SeekBar)mOverlay.findViewById(R.id.player_overlay_seekbar);	
		mSeekbar.setOnSeekBarChangeListener(mSeekListener);
		
		

        try {
			mLibVLC = LibVLC.getInstance();
			
			//David
			//mLibVLC2 = LibVLC.getInstance();
		} catch (LibVlcException e) {
			e.printStackTrace();
		}		

		EventManager em = EventManager.getIntance();
		em.addHandler(eventHandler);

		load();
		
	}
		
	@Override
	protected void onPause() {
		if (mLibVLC.isPlaying())
			pause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (mLibVLC != null) {
			mLibVLC.stop();
		}
		
		EventManager em = EventManager.getIntance();
		em.removeHandler(eventHandler);
		
		super.onDestroy();
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		showOverlay();
		return true;
	}
	

    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		setSurfaceSize(mVideoWidth, mVideoHeight);
		super.onConfigurationChanged(newConfig);
	}


	public void setSurfaceSize(int width, int height) {
		// store video size
		mVideoHeight = height;
		mVideoWidth = width;	
		Message msg = mHandler.obtainMessage(SURFACE_SIZE);
		mHandler.sendMessage(msg);
    }
	
	/**
	 * Lock screen rotation
	 */
	private void lockScreen() {
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		
		
		
		switch (display.getRotation()) {
		case Surface.ROTATION_0:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		case Surface.ROTATION_90:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		case Surface.ROTATION_270:
			// FIXME: API Level 9+ (not tested on a device with API Level < 9)
			setRequestedOrientation(8); // SCREEN_ORIENTATION_REVERSE_LANDSCAPE
			break;
		}
		
		showInfo("locked", 500);
	}
	
	/**
	 * Remove screen lock
	 */
	private void unlockScreen() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		showInfo("unlocked", 500);
	}
	
	/**
	 * Show text in the info view for "duration" milliseconds
	 * @param text
	 * @param duration
	 */
	private void showInfo(String text, int duration) {
		
		mInfo.setVisibility(View.VISIBLE);
		mInfo.setText(text);
		
		mHandler.removeMessages(FADE_OUT_INFO);
		mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, duration);
	}
	
	/**
	 * Show text in the info view
	 * @param text
	 */
	private void showInfo(String text) {
		
		mInfo.setVisibility(View.VISIBLE);
		mInfo.setText(text);
		
		mHandler.removeMessages(FADE_OUT_INFO);
	}
	
	/**
	 * hide the info view with "delay" milliseconds delay
	 * @param delay
	 */
	private void hideInfo(int delay) {
		mHandler.sendEmptyMessageDelayed(FADE_OUT_INFO, delay);
	}
	
	/**
	 * hide the info view
	 */
	private void hideInfo() {
		hideInfo(0);
	}
	
    /**
     *  Handle libvlc asynchronous events 
     */
    private Handler eventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.getData().getInt("event")) {
                case EventManager.MediaPlayerPlaying:
                    Log.e(TAG, "MediaPlayerPlaying");
                    break;
                case EventManager.MediaPlayerPaused:
                    Log.e(TAG, "MediaPlayerPaused");
                    break;
                case EventManager.MediaPlayerStopped:
                    Log.e(TAG, "MediaPlayerStopped");
                    break;
                case EventManager.MediaPlayerEndReached:
                    Log.e(TAG, "MediaPlayerEndReached");
                    /* Exit player when reach the end */
                    VideoPlayerActivity_bak.this.finish();
                    break;
                default:
                    Log.e(TAG, "Event not handled");
                    break;
            }
            updateOverlayPausePlay();
        }
    };
	
	/**
	 * Handle resize of the surface and the overlay
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case FADE_OUT:
					hideOverlay(false);
					break;
				case SHOW_PROGRESS:
					int pos = setOverlayProgress();
					if (!mDragging && mShowing && mLibVLC.isPlaying()) {
						msg = obtainMessage(SHOW_PROGRESS);
						sendMessageDelayed(msg, 1000 - (pos % 1000));
					}
					break;
				case SURFACE_SIZE:
					changeSurfaceSize();
					break;
				case FADE_OUT_INFO:
					
					mInfo.startAnimation(AnimationUtils.loadAnimation(
							VideoPlayerActivity_bak.this, android.R.anim.fade_out));
					mInfo.setVisibility(View.INVISIBLE);
					
			}
		}
	};
	
	private void changeSurfaceSize() {
		// get screen size
		int dw = getWindowManager().getDefaultDisplay().getWidth();
		int dh = getWindowManager().getDefaultDisplay().getHeight();
				
		// calculate aspect ratio
		double ar = (double)mVideoWidth / (double)mVideoHeight;
		// calculate display aspect ratio
		double dar = (double)dw / (double)dh;
		
		switch (mCurrentSize) {
		case SURFACE_FIT_HORIZONTAL:
			dh = (int) (dw / ar);
			break;
		case SURFACE_FIT_VERTICAL:
			dw = (int) (dh * ar);
			break;
		case SURFACE_FILL:	
			break;
		case SURFACE_16_9:	
			ar = 16.0/9.0;
			if (dar < ar)
				dh = (int) (dw / ar);
			else
				dw = (int) (dh * ar);
			break;
		case SURFACE_4_3:			
			ar = 4.0/3.0;
			if (dar < ar)
				dh = (int) (dw / ar);
			else
				dw = (int) (dh * ar);
			break;
		case SURFACE_ORIGINAL:
			dh = mVideoHeight;
			dw = mVideoWidth;
			break;
		}
		
		mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
		LayoutParams lp = mSurface.getLayoutParams();
		lp.width = dw;
		lp.height = dh;
		mSurface.setLayoutParams(lp);
		mSurface.invalidate();
		
		//David
		//mSurfaceHolder2.setFixedSize(mVideoWidth, mVideoHeight);   //Yen:���}�o��v���|�qplayer_surface1��qplayer_surface2�X�{,½��ù�����surface���X�{
		/*LayoutParams lp2 = mSurface2.getLayoutParams();
		lp2.width = dw;
		lp2.height = dh;
		mSurface2.setLayoutParams(lp2);
		mSurface2.invalidate();*/
	}
	

	/**
	 * show/hide the overlay
	 */
    private OnTouchListener mTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (!mShowing) {
                    showOverlay();
                } else {
                	hideOverlay(true);
                }
            }
            return false;
        }
    };
    
    
    /**
     * handle changes of the seekbar (slicer)
     */
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
    	
		public void onStartTrackingTouch(SeekBar seekBar) {
			mDragging = true;
			showOverlay(3600000);
		}
		
		public void onStopTrackingTouch(SeekBar seekBar) {
			mDragging = false;
			showOverlay();
			hideInfo();
		}
		
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				mLibVLC.setTime(progress);
				setOverlayProgress();	
				mTime.setText(Util.millisToString(progress));
				showInfo(Util.millisToString(progress));
			}
			
		}
	};
	
	
	/**
	 * 
	 */
	private OnClickListener mPauseListener = new OnClickListener() {		
		public void onClick(View v) {
			doPausePlay();
			showOverlay();
		}
	};
	
	/**
	 * 
	 */
	private OnClickListener mLockListener = new OnClickListener() {		
		boolean isLocked = false;
		public void onClick(View v) {
			if (isLocked) {
				unlockScreen();
				isLocked = false;
			} else {
				lockScreen();
				isLocked = true;
			}
			showOverlay();
		}
	};
	
	/**
	 * 
	 */
	private OnClickListener mSizeListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mCurrentSize < SURFACE_ORIGINAL) {
				mCurrentSize++;
			} else {
				mCurrentSize = 0;
			}
			changeSurfaceSize();
			switch (mCurrentSize) {
			case SURFACE_FIT_HORIZONTAL:
				showInfo("fit horizontal", 500);
				break;
			case SURFACE_FIT_VERTICAL:
				showInfo("fit vertival", 500);
				break;
			case SURFACE_FILL:	
				showInfo("fill", 500);
				break;
			case SURFACE_16_9:	
				showInfo("16:9", 500);
				break;
			case SURFACE_4_3:			
				showInfo("4:3", 500);
				break;
			case SURFACE_ORIGINAL:
				showInfo("original", 500);
				break;
			}
			showOverlay();
		}		
	};


	
	/**
	 * attach and disattach surface to the lib
	 */
	private SurfaceHolder.Callback mSurfaceCallback = new Callback() {		
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			mLibVLC.attachSurface(holder.getSurface(), VideoPlayerActivity_bak.this, width, height);
		}

		public void surfaceCreated(SurfaceHolder holder) { }

		public void surfaceDestroyed(SurfaceHolder holder) {
			mLibVLC.detachSurface();
		}
	};
	
	

	
	/**
	 * show overlay the the default timeout
	 */
	private void showOverlay() {
		showOverlay(OVERLAY_TIMEOUT);
	}
	
	
	/**
	 * show overlay
	 */
	private void showOverlay(int timeout) {
		mHandler.sendEmptyMessage(SHOW_PROGRESS);
		if (!mShowing) {
			mShowing = true;
			
			mDecor.addView(mOverlay);
			
		}
		Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
        updateOverlayPausePlay();
	}
	
	
	/**
	 * hider overlay
	 */
	private void hideOverlay(boolean fromUser) {
		if (mShowing) {
			mHandler.removeMessages(SHOW_PROGRESS);
        	Log.i(TAG, "remove View!");
        	if (!fromUser) {
	        	mOverlay.startAnimation(AnimationUtils.loadAnimation(
	        			this, android.R.anim.fade_out));
        	}
        	
			mDecor.removeView(mOverlay);
			
			mShowing = false;
		}
	}

	
	private void updateOverlayPausePlay() {
		if (mLibVLC == null) {
			return;
		}
		
		if (mLibVLC.isPlaying()) {
			mPause.setBackgroundResource(R.drawable.ic_pause);
		} else {
			mPause.setBackgroundResource(R.drawable.ic_play);
		}
	}
	
	
	/**
	 * play or pause the media
	 */
	private void doPausePlay() {
		if (mLibVLC.isPlaying()) {
			pause();
		} else {
			play();
		}
	}
	
	
	/**
	 * update the overlay
	 */
	private int setOverlayProgress() {
		if (mLibVLC == null) {
			return 0;
		}
		int time = (int)mLibVLC.getTime();
		int length = (int)mLibVLC.getLength();
		// Update all view elements

		mSeekbar.setMax(length);
		mSeekbar.setProgress(time);
		mTime.setText(Util.millisToString(time));
		mLength.setText(Util.millisToString(length));
		return time;
	}

	/**
	 * 
	 */
	private void play() {
		mLibVLC.play();
		
		mWakeLock.acquire();
	}
	
	/**
	 * 
	 */
	private void pause() {
		mLibVLC.pause();
		mWakeLock.release();
		
		
		if (mLibVLC != null) {
			mLibVLC.stop();
		}
		
	}
	
	/**
	 * 
	 */
	private void load()
	{
		
		//Yen
		mLibVLC.readMedia(IP);
		//mLibVLC.readMedia("rtsp://192.168.1.225/ipcam_h264.sdp");
		mWakeLock.acquire();
		
		
	}
}
