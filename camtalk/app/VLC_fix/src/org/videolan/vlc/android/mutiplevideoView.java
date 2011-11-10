package org.videolan.vlc.android;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

import com.avadesign.camvideo.R;

public class mutiplevideoView extends Activity {
	public final static String TAG = "mutiplevideoView";
	
	private SurfaceView mSurface1;
	//private SurfaceView mSurface2;
	//private SurfaceView mSurface3;
	//private SurfaceView mSurface4;
	
	private SurfaceHolder mSurfaceHolder1;
	//private SurfaceHolder mSurfaceHolder2;
	//private SurfaceHolder mSurfaceHolder3;
	//private SurfaceHolder mSurfaceHolder4;
	
	private LibVLC mLibVLC_1;
	//private LibVLC mLibVLC_2;
	//private LibVLC mLibVLC_3;
	//private LibVLC mLibVLC_4;
	
	private static final int SURFACE_FIT_HORIZONTAL = 0;
	private static final int SURFACE_FIT_VERTICAL = 1;
	private static final int SURFACE_FILL = 2;
	private static final int SURFACE_16_9 = 3;
	private static final int SURFACE_4_3 = 4;
	private static final int SURFACE_ORIGINAL = 5;
	private int mCurrentSize = SURFACE_FIT_HORIZONTAL;
	
	private static final int FADE_OUT = 1;
	private static final int SHOW_PROGRESS = 2;
	private static final int SURFACE_SIZE = 3;
	private static final int FADE_OUT_INFO = 4;
	
	// size of the video
	private int mVideoHeight;
	private int mVideoWidth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_multivideo);
		
		mSurface1 = (SurfaceView)findViewById(R.id.screen_multivideo_surfaceView1);
		//mSurface2 = (SurfaceView)findViewById(R.id.screen_multivideo_surfaceView2);
		//mSurface3 = (SurfaceView)findViewById(R.id.screen_multivideo_surfaceView3);
		//mSurface4 = (SurfaceView)findViewById(R.id.screen_multivideo_surfaceView4);
		
		mSurfaceHolder1 = mSurface1.getHolder();
		mSurfaceHolder1.setKeepScreenOn(true);
		mSurfaceHolder1.setFormat(PixelFormat.RGB_888);
		mSurfaceHolder1.addCallback(mSurfaceCallback);
		
		/*mSurfaceHolder2 = mSurface2.getHolder();
		mSurfaceHolder2.setKeepScreenOn(true);
		mSurfaceHolder2.setFormat(PixelFormat.RGB_888);
		mSurfaceHolder2.addCallback(mSurfaceCallback);
		
		mSurfaceHolder3 = mSurface3.getHolder();
		mSurfaceHolder3.setKeepScreenOn(true);
		mSurfaceHolder3.setFormat(PixelFormat.RGB_888);
		mSurfaceHolder3.addCallback(mSurfaceCallback);
		
		mSurfaceHolder4 = mSurface4.getHolder();
		mSurfaceHolder4.setKeepScreenOn(true);
		mSurfaceHolder4.setFormat(PixelFormat.RGB_888);
		mSurfaceHolder4.addCallback(mSurfaceCallback);*/
		
		try {
			mLibVLC_1 = LibVLC.getInstance();
			//mLibVLC_2 = LibVLC.getInstance();
			//mLibVLC_3 = LibVLC.getInstance();
			//mLibVLC_4 = LibVLC.getInstance();
		} catch (LibVlcException e) {
			e.printStackTrace();
		}
		
		EventManager em = EventManager.getIntance();
		em.addHandler(eventHandler);
		
		load();
	}
	
	/**
	 * attach and disattach surface to the lib
	 */
	private SurfaceHolder.Callback mSurfaceCallback = new Callback() {		
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			//if(holder.equals(mSurfaceHolder1)) {
				mLibVLC_1.attachSurface(holder.getSurface(), mutiplevideoView.this, width, height);
			//}
			/*else if(holder.equals(mSurfaceHolder2)) {
				mLibVLC_2.attachSurface(holder.getSurface(), mutiplevideoView.this, width, height);
			}
			else if(holder.equals(mSurfaceHolder3)) {
				mLibVLC_3.attachSurface(holder.getSurface(), mutiplevideoView.this, width, height);
			}
			else {
				mLibVLC_4.attachSurface(holder.getSurface(), mutiplevideoView.this, width, height);
			}*/
			
		}

		public void surfaceCreated(SurfaceHolder holder) { }

		public void surfaceDestroyed(SurfaceHolder holder) {
			//if(holder.equals(mSurfaceHolder1)) {
				mLibVLC_1.detachSurface();
			//}
			/*else if(holder.equals(mSurfaceHolder2)) {
				mLibVLC_2.detachSurface();
			}
			else if(holder.equals(mSurfaceHolder3)) {
				mLibVLC_3.detachSurface();
			}
			else {
				mLibVLC_4.detachSurface();
			}*/
		}
	};
	
	@Override
	protected void onPause() {
		if (mLibVLC_1.isPlaying() /*|| mLibVLC_2.isPlaying() ||
				mLibVLC_3.isPlaying() || mLibVLC_4.isPlaying()*/ )
			pause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (mLibVLC_1 != null) {
			mLibVLC_1.stop();
		}
		/*if (mLibVLC_2 != null) {
			mLibVLC_2.stop();
		}
		if (mLibVLC_3 != null) {
			mLibVLC_3.stop();
		}
		if (mLibVLC_4 != null) {
			mLibVLC_4.stop();
		}*/
		
		EventManager em = EventManager.getIntance();
		em.removeHandler(eventHandler);
		
		super.onDestroy();
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
                    mutiplevideoView.this.finish();
                    break;
                default:
                    Log.e(TAG, "Event not handled");
                    break;
            }
            //updateOverlayPausePlay();
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
					//hideOverlay(false);
					break;
				case SHOW_PROGRESS:
					/*int pos = setOverlayProgress();
					if (!mDragging && mShowing && mLibVLC.isPlaying()) {
						msg = obtainMessage(SHOW_PROGRESS);
						sendMessageDelayed(msg, 1000 - (pos % 1000));
					}*/
					break;
				case SURFACE_SIZE:
					changeSurfaceSize();
					break;
				case FADE_OUT_INFO:
					//mInfo.startAnimation(AnimationUtils.loadAnimation(
					//		mutiplevideoView.this, android.R.anim.fade_out));
					//mInfo.setVisibility(View.INVISIBLE);
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
		
		mSurfaceHolder1.setFixedSize(mVideoWidth, mVideoHeight);
		//mSurfaceHolder2.setFixedSize(mVideoWidth, mVideoHeight);
		//mSurfaceHolder3.setFixedSize(mVideoWidth, mVideoHeight);
		//mSurfaceHolder4.setFixedSize(mVideoWidth, mVideoHeight);
		LayoutParams lp_1 = mSurface1.getLayoutParams();
		//LayoutParams lp_2 = mSurface2.getLayoutParams();
		//LayoutParams lp_3 = mSurface3.getLayoutParams();
		//LayoutParams lp_4 = mSurface4.getLayoutParams();
		lp_1.width = dw;
		lp_1.height = dh;
		/*lp_2.width = dw;
		lp_2.height = dh;
		lp_3.width = dw;
		lp_3.height = dh;
		lp_4.width = dw;
		lp_4.height = dh;*/
		mSurface1.setLayoutParams(lp_1);
		//mSurface2.setLayoutParams(lp_2);
		//mSurface3.setLayoutParams(lp_3);
		//mSurface4.setLayoutParams(lp_4);
		mSurface1.invalidate();
		//mSurface2.invalidate();
		//mSurface3.invalidate();
		//mSurface4.invalidate();
	}
	
	/**
	 * 
	 */
	private void pause() {
		mLibVLC_1.pause();
		if (mLibVLC_1 != null) {
			mLibVLC_1.stop();
		}
		//mLibVLC_2.pause();
		//mLibVLC_3.pause();
		//mLibVLC_4.pause();
	}
    
    private void load() {
    	mLibVLC_1.readMedia("rtsp://192.168.1.225/ipcam_h264.sdp");
    	//mLibVLC_2.readMedia("rtsp://192.168.1.225/ipcam_h264.sdp");
    	//mLibVLC_3.readMedia("rtsp://192.168.1.225/ipcam_h264.sdp");
    	//mLibVLC_4.readMedia("rtsp://192.168.1.225/ipcam_h264.sdp");
    }
}
