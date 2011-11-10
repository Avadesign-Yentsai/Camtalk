package org.videolan.vlc.android;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.avadesign.camtalker.Recorder;
import com.avadesign.camvideo.Home;
import com.avadesign.camvideo.KeyFinder;
import com.avadesign.camvideo.LoadCamlist;
import com.avadesign.camvideo.R;
import com.avadesign.codecs.GSM;

public class VideoPlayerActivity extends Activity 
{

	public final static String TAG = "VLC/VideoPlayerActivity";
	
	private AudioManager audioManager;

	private SurfaceView mSurface;
	private SurfaceHolder mSurfaceHolder;
	private LibVLC mLibVLC;
	private ImageButton MIC;
	private ImageButton getCamList;
	
	private static final int SURFACE_FIT_HORIZONTAL = 0;
	private static final int SURFACE_FIT_VERTICAL = 1;
	private static final int SURFACE_FILL = 2;
	private static final int SURFACE_16_9 = 3;
	private static final int SURFACE_4_3 = 4;
	private static final int SURFACE_ORIGINAL = 5;
	private int mCurrentSize = SURFACE_FIT_HORIZONTAL;
	
	private static Recorder recorder;
	private static boolean isStarting = true;
	private static boolean istalking = false;
	
	
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_surface);
		
		Bundle bundle=this.getIntent().getExtras();
        camUrl=bundle.getString("KEY_CAMIP");
        micIP=bundle.getString("KEY_MICIP");
        micPort=bundle.getInt("KEY_MICPORT");
        micAc=bundle.getString("KEY_MICAC");
        micPw=bundle.getString("KEY_MICPW");
        userAcc=bundle.getString("KEY_UserAcc");
        userPwd=bundle.getString("KEY_UserPwd");
        
        /*
        //Audio
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
        */
        
        //MIC
        init();
        MIC = (ImageButton)findViewById(R.id.play_talk);
        MIC.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		if(!istalking)
        		{
        			MIC.setBackgroundColor(Color.argb(255, 255, 0, 0));
        			istalking=true;
        			recorder.resumeAudio(micIP,micPort,micAc,micPw);
        			Log.d("Camtalker", "mBtStartTalk");
        		}
        		else
        		{
        			MIC.setBackgroundColor(Color.argb(0, 0, 0, 0));
        			istalking=false;
        			recorder.pauseAudio();
        			Log.d("Camtalker", "mBtStopTalk");
        		}

        	}
        });
      
        //CamList
        getCamList = (ImageButton)findViewById(R.id.play_camlist);
        getCamList.setOnClickListener(new Button.OnClickListener()
        {
        	public void onClick(View V)
        	{
        		LoadCamData();
        		
        		AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayerActivity.this);   
        		builder.setTitle("Camera List");  
        		builder.setItems(camname, new DialogInterface.OnClickListener()
		        {  
        			public void onClick(DialogInterface dialog, int which) 
		            {  
        				
        				//必須和key順序相符
        				final String name = attr[which][0];
        				final String talkac = attr[which][1];
        				final String talkpw = attr[which][2];
        				final String talkport = attr[which][3];
        				final String videoport = attr[which][4];
        				final String videocode = attr[which][5];
        				final String ip = attr[which][6];
        				
        				finish();
        				Intent intent=new Intent();
		                intent.setClass(VideoPlayerActivity.this, VideoPlayerActivity.class);
		               
		                Bundle bundle =new Bundle();
		                bundle.putString("KEY_MICIP", ip);
	                	bundle.putInt("KEY_MICPORT",Integer.valueOf(talkport));
	                	bundle.putString("KEY_MICAC",talkac);
	                	bundle.putString("KEY_MICPW", talkpw);
	                	bundle.putString("KEY_UserAcc", userAcc);
	                	bundle.putString("KEY_UserPwd", userPwd);
	                	
		                if(videocode.equals("h264"))
		                {
		                	bundle.putString("KEY_CAMIP", "rtsp://"+ip+":"+videoport+"/ipcam_h264.sdp");
		                }
		                else if(videocode.equals("mpeg4"))
		                {
		                	bundle.putString("KEY_CAMIP", "rtsp://"+ip+":"+videoport+"/ipcam.sdp");
		                }
		                
		                intent.putExtras(bundle);
		               	startActivity(intent);
		            }  
		        });  
        		builder.create().show();  
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

		load();
		
	}
	
	
	protected void LoadCamData() 
	{
		String list = LoadCamlist.load(userAcc, userPwd);
		
		if(!list.equals("[]"))//資料不為空
        {
        	String key[] = {"name","talkac","talkpw","talkport","videoport","videocode","ip"}; //必須和list回傳的資料順序符合 ,名稱是jsp裡的CamInfo.java設定的,若修改,CamInfo.java的getItem()也必須改
        	
        	/*分析傳回的資料開始*/
	        list = list.replace("[", "");
	        list = list.replace("]", "");
	        
	        caminfo=list.split("\\},\\{");
	        
	        attr = new String[caminfo.length][key.length];
	        camip = new String[caminfo.length];
	        camname = new String[caminfo.length];
	        
	        for(int i=0; i<caminfo.length; i++)
	        {
	        	caminfo[i] = caminfo[i].toString().replace("{", "");
	        	caminfo[i] = caminfo[i].toString().replace("}", "");
	        	StringBuffer sb = new StringBuffer("{");
	        	sb = sb.append(caminfo[i].toString());
	        	sb = sb.append("}");
	        	
	        	Log.d(TAG,sb.toString());
	        	
	        	for(int j=0; j<key.length; j++)
	        	{
		        	JSONParser parser = new JSONParser();
		        	KeyFinder finder = new KeyFinder();
		        	try
		        	{
			        	finder.setMatchKey(key[j]);
		        		while(!finder.isEnd())
		        		{
		        			parser.parse(sb.toString(), finder, true);
		        			if(finder.isFound())
		        			{
		        				finder.setFound(false);
		        				attr[i][j]=finder.getValue().toString();
		        				
		        				if(key[j].equals("ip"))
		        				{
		        					camip[i]=finder.getValue().toString();
		        				}
		        				
		        				if(key[j].equals("name"))
		        				{
		        					camname[i]=finder.getValue().toString();
		        				}
		        			}
		        		}   
		        		
		        	}
		        	catch(ParseException pe)
		        	{
		        		pe.printStackTrace();
		        	}
	        	}
	        }
	        /*分析傳回的資料結束*/
	        
	        //map是要傳遞給SocketConn.java用
	        map =  new HashMap<String, String>();
	        for(int i=0; i<caminfo.length; i++)
	        {
	        	map.put(attr[i][6],attr[i][0]); // put(cam ip, cam name);
	        	
	        }
        }
		
	}


	@Override
    public void onStart() 
	{
    	super.onStart();
    	
    	// Initialize codec 
    	GSM.open();
    	
     }
	
	 public void onStop() 
    {
    	super.onStop();
    	
       	//recorder.pauseAudio();    	
    	
    	// Release codec resources
    	GSM.close();
    } 
	
	   
    public void onResume() 
    {
    	super.onResume();  
    	
    }
	
	@Override
	protected void onPause() 
	{
		super.onPause();
		
	}

	@Override
	protected void onDestroy() 
	{
		if (mLibVLC.isPlaying())
			pause();
		
		if (mLibVLC != null) 
		{
			mLibVLC.stop();
		}
		
		
		
		EventManager em = EventManager.getIntance();
		em.removeHandler(eventHandler);
		
		release();    
		
		super.onDestroy();
		
	}

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
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
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
	
	private void pause() {
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
		Intent intent=new Intent();
    	intent.setClass(VideoPlayerActivity.this, Home.class);
	    startActivity(intent);
		System.exit(0);//Yen 離開時會有錯誤,取消System.exit(0)可查看錯誤
	}
}
