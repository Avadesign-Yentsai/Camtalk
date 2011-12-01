package org.videolan.vlc.android;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.avadesign.camtalker.Recorder;
import com.avadesign.camvideo.Camlist;
import com.avadesign.camvideo.KeyFinder;
import com.avadesign.camvideo.LoadCamlist;
import com.avadesign.camvideo.R;
import com.avadesign.camvideo.Socket_service;
import com.avadesign.codecs.GSM;

public class VideoPlayerActivity extends Activity 
{

	public final static String TAG = "VLC/VideoPlayerActivity";
	
	private AudioManager audioManager;

	private SurfaceView mSurface;
	private SurfaceHolder mSurfaceHolder;
	private LibVLC mLibVLC;
	private ImageButton MIC;
	
	private static final int SURFACE_FIT_HORIZONTAL = 0;
	private static final int SURFACE_FIT_VERTICAL = 1;
	private static final int SURFACE_FILL = 2;
	private static final int SURFACE_16_9 = 3;
	private static final int SURFACE_4_3 = 4;
	private static final int SURFACE_ORIGINAL = 5;
	private int mCurrentSize = SURFACE_FIT_HORIZONTAL;
	
	private static Recorder recorder;
	private static boolean isStarting = true;
	private static boolean isTalking = false;
	
	
	/** Overlay */
	private static final int SURFACE_SIZE = 3;

	// size of the video
	private int mVideoHeight;
	private int mVideoWidth;

	// stop screen from dimming
	private WakeLock mWakeLock;
	
	
	private String camUrl;
	private String micIP;
	private int micPort;
	private String micAc;
	private String micPw;
	private String userAcc;
	private String userPwd;
	
	private String[][] attr; //[i][j], 第 i 台 IPCAM 的第 j 個屬性
	private String[] caminfo = null; //個別一台 IPCAM 的屬性集合
	private String[] camip;
	private String[] camname;
	private Map<String, String> map; //傳遞給SocketConn.java用,<cam ip ,cam name>
	
	private boolean noReply;
	private boolean lock_clic; //遇到無法連線的IPCAM太早返回會當機,大約10秒等VLC的執行序結束再跳出才正常
	private int ReplyTime = 11000;
	private Handler handler = new Handler();
	private ProgressBar pb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_surface);
		
		Bundle bundle=this.getIntent().getExtras();
        camUrl=bundle.getString("KEY_CAMURL");
        micIP=bundle.getString("KEY_MICIP");
        micPort=bundle.getInt("KEY_MICPORT");
        micAc=bundle.getString("KEY_MICAC");
        micPw=bundle.getString("KEY_MICPW");
        userAcc=bundle.getString("KEY_UserAcc");
        userPwd=bundle.getString("KEY_UserPwd");
        
        Intent intent = new Intent();
		intent.setAction(Socket_service.CH_WATCHING);
		intent.putExtra("URL", camUrl);
		sendBroadcast(intent);
		
		/*
        //Audio
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
        */
        
        //MIC
		
        init();
        MIC = (ImageButton)findViewById(R.id.play_talk);
        MIC.setVisibility(View.INVISIBLE);
        MIC.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		if(!isTalking)
        		{
        			MIC.setBackgroundColor(Color.argb(255, 255, 0, 0));
        			isTalking=true;
        			recorder.resumeAudio(micIP,micPort,micAc,micPw);
        			Log.d("Camtalker", "mBtStartTalk");
        		}
        		else
        		{
        			MIC.setBackgroundColor(Color.argb(0, 0, 0, 0));
        			isTalking=false;
        			recorder.pauseAudio();
        			Log.d("Camtalker", "mBtStopTalk");
        		}

        	}
        });
      	
       
		
        // stop screen from dimming
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
		
		/** initialize Views an their Events */
		mSurface = (SurfaceView) findViewById(R.id.player_surface);
		mSurfaceHolder = mSurface.getHolder();	
		mSurfaceHolder.setKeepScreenOn(true);
		mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
		mSurfaceHolder.addCallback(mSurfaceCallback);
		try 
		{
			mLibVLC = LibVLC.getInstance();
		} 
		catch (LibVlcException e) 
		{
			e.printStackTrace();
		}		

		EventManager em = EventManager.getIntance();
		em.addHandler(eventHandler);

		noReply = true;
		lock_clic = true;
		handler.postDelayed(checkRe, ReplyTime);
		
	}
	
	private Runnable checkRe = new Runnable() 
	{
        public void run() 
        {
        	lock_clic = false;
        	if(noReply)
        	{
        		Toast.makeText(VideoPlayerActivity.this, "Unable to open this RTSP", Toast.LENGTH_LONG).show();
        		backCamlist();
        	}
        }
    };
	
	@Override
    public void onStart() 
	{
    	super.onStart();
    	
    	// Initialize codec 
    	GSM.open();
    	
    	
     }
	
	@Override
	public void onStop() 
    {
    	super.onStop();
    	
       	recorder.pauseAudio();    	
    	
    	// Release codec resources
    	GSM.close();
    	
    	
    } 
	
	@Override   
    public void onResume() 
    {
		Intent intent = new Intent();
		intent.setAction(Socket_service.CH_WATCHING);
		intent.putExtra("URL", camUrl);
		sendBroadcast(intent);
		load();
    	super.onResume();  
    }
	
	@Override
	protected void onPause() 
	{
		super.onPause();
		
		//finish();// 加上這行改善情況:若在看cam的情況下收到動態通知再去看動態通知的cam,返回不會error
		
		if (mLibVLC.isPlaying())
		{
			pause();
		}
		
		release();    
		
		Intent intent = new Intent();
		intent.setAction(Socket_service.CH_WATCHING);
		intent.putExtra("URL", "");
		sendBroadcast(intent);
		
	}
	/*
	@Override //這段用在Jeff的HTC手機會當機
	protected void onDestroy() 
	{
		if (mLibVLC.isPlaying())
			pause();
		
		if (mLibVLC != null) 
		{
			mLibVLC.stop();
			mLibVLC.destroy(); //應該是在destroy的時候會出錯
		}
		
		EventManager em = EventManager.getIntance();
		em.removeHandler(eventHandler);
		
		release();    
		
		Intent intent = new Intent();
		intent.setAction(Socket_service.CH_WATCHING2);
		intent.putExtra("URL", "");
		sendBroadcast(intent);
		
		super.onDestroy();
		
	}
	*/

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		//showOverlay();
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
	
	private Runnable m_Timer = new Runnable() 
	{
		@Override
		public void run() 
		{
			noReply = false;
            lock_clic = false;
            buildInterface();
		}

		private void buildInterface() 
		{
			pb = (ProgressBar)findViewById(R.id.play_progressBar);
	        pb.setVisibility(View.GONE);
			MIC.setVisibility(View.VISIBLE);
		}
		
	};
	
	/**
     *  Handle libvlc asynchronous events 
     */
    private Handler eventHandler = new Handler() 
    {
        @Override
        public void handleMessage(Message msg) 
        {
            switch (msg.getData().getInt("event"))
            {
                case EventManager.MediaPlayerPlaying:
                	mHandler.post(m_Timer);
                    Log.e(TAG, "MediaPlayerPlaying");
                    break;
                case EventManager.MediaPlayerPaused:
                    Log.e(TAG, "MediaPlayerPaused");
                    break;
                case EventManager.MediaPlayerStopped:
                    Log.e(TAG, "MediaPlayerStopped");
                    //Toast.makeText(VideoPlayerActivity.this, "MediaPlayerStopped", Toast.LENGTH_SHORT).show();
                    break;
                case EventManager.MediaPlayerEndReached:
                    Log.e(TAG, "MediaPlayerEndReached");
                    /* Exit player when reach the end */
                    VideoPlayerActivity.this.finish();
                    break;
                default:
                    Log.e(TAG, "Event not handled");
                    break;
            }
           
        }

		
    };
	
	/**
	 * Handle resize of the surface and the overlay
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SURFACE_SIZE:
					changeSurfaceSize();
					break;
				
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
		
	}
	

	
	/**
	 * attach and disattach surface to the lib
	 */
	private SurfaceHolder.Callback mSurfaceCallback = new Callback() {		
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
		{
			mLibVLC.attachSurface(holder.getSurface(), VideoPlayerActivity.this, width, height);
		}

		public void surfaceCreated(SurfaceHolder holder) { }

		public void surfaceDestroyed(SurfaceHolder holder) {
			mLibVLC.detachSurface();
		}
	};
	
	
	private void play() {
		mLibVLC.play();
		mWakeLock.acquire();
	}
	
	private void pause() 
	{
		mLibVLC.pause();
		mWakeLock.release();
		
		if (mLibVLC != null) {
			mLibVLC.stop();
			
		}
	}
	
	private void load()
	{
		mLibVLC.readMedia(camUrl);
		mWakeLock.acquire();
	}
	
	
	private void init() 
	{

    	// When the volume keys will be pressed the audio stream volume will be changed. 
		//   	setVolumeControlStream(AudioManager.STREAM_MUSIC);
    	    	    	
   	    	    	
    	if(isStarting) 
    	{    
    		recorder = new Recorder();
    		recorder.start(); 
    		isStarting = false;    		
    	}
    }
	
	private void release() 
    {    	
    	// If the back key was pressed.
    	if(isFinishing()) 
    	{
  
    		// Force threads to finish.
  		    		
    		recorder.finish();
    		
    		try 
    		{
      			recorder.join();
    		}
    		catch(InterruptedException e) 
    		{
    			Log.d("Camtalker", e.toString());
    		}

    		recorder = null;
    	    		
    		// Resetting isStarting.
    		isStarting = true;     		
    	}
    }
	
	
	@Override
	public void onBackPressed() 
	{
		
		if(lock_clic)
		{
			//do nothing
		}
		else
		{
			//finish();
			backCamlist();
		}
		
		
		/*
		Intent intent=new Intent();
    	intent.setClass(VideoPlayerActivity.this, Home.class);
	    startActivity(intent);
	    */
		
	}
	
	private void backCamlist()
	{
		Intent intent=new Intent();
    	intent.setClass(VideoPlayerActivity.this, Camlist.class);
	    startActivity(intent);
	}
	
}
