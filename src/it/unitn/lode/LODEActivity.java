package it.unitn.lode;

import it.unitn.lode.data.LodeSaxDataParser;
import it.unitn.lode.data.TimedSlides;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.util.Log;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.SlidingDrawer.OnDrawerScrollListener;
import android.widget.SlidingDrawer;
import android.widget.TextView;

public class LODEActivity extends Activity implements OnClickListener, OnTouchListener,
OnCompletionListener, OnSeekBarChangeListener, OnDrawerScrollListener, OnDrawerOpenListener,
OnItemSelectedListener, OnItemClickListener, OnPreparedListener{
    /** Called when the activity is first created. */
	private Display devDisplay = null;
	public static int scrWidth, scrHeight;
	private final int VIDEO = 0, PLAY = 1, FF = 2, RR = 3, SLIDER = 4, SLIDE = 5, TITLE = 6, FS = 7;
	private RelativeLayout rlMain = null;
	private RelativeLayout.LayoutParams rlMainParams = null;
	private TextView tvTitle = null, tvSlidePos = null;
	public static VidView vidView = null;
	private ImageView imView = null;
	private ImageButton btnF = null;
	private ImageButton btnR = null;
	private ImageButton btnPlay = null;
	private ImageButton btnFullScreen = null;
	private SeekBar sbSlider = null;
	private int playState = 0;
	public static int currPos = 0;
	private boolean isStarted = false, firstTime = true, isResuming = false, fullScreen = false;
	public static boolean hasFinished = false;
	private Handler handler = null;
	private Runnable waitAndHide = null;
	private Runnable sliderUpdater = null, listPopulator = null;
	private Thread thread = null, dead = null, sliderThread = null;
	private SlidingDrawer sdTimeline = null;
	private FrameLayout flTimeline = null;
	private FrameLayout.LayoutParams flParams = null;
	private ListView lvTimeline = null;
	private RelativeLayout rlTimeline = null;
	private ArrayList<TextView> slidePos = null;
	private Intent fsIntent = null;
	private Bundle fsBundle = null;
	private String videoUrl = "";
	private ProgressBar pbVideo = null;
	private Iterator<TimedSlides> tsIterator = null;
	private LodeSaxDataParser tsParser = null;
	private List<TimedSlides> ts = null;
	private final Context LodeActivityContext = this;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        flTimeline = (FrameLayout) findViewById(R.id.flTimeline);
        rlTimeline = (RelativeLayout) findViewById(R.id.rlTimeline);

        rlTimeline.setBackgroundResource(R.layout.timeline);
        
        handler = new Handler();
        waitAndHide = new Runnable(){
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.post(new Runnable() {
					@Override
					public void run() {
						hideMc();
						isStarted = false;
					}
				});
			}
        };
        sliderUpdater = new Runnable(){
			@Override
			public void run() {
				while(true){
					handler.post(new Runnable() {
						@Override
						public void run() {
							int progress = (vidView.getCurrentPosition() * 100) / vidView.getDuration();
							sbSlider.setProgress(progress);
						}
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
        };
        devDisplay = getWindowManager().getDefaultDisplay();
        scrWidth = devDisplay.getWidth();
        scrHeight = devDisplay.getHeight();

        rlMain = (RelativeLayout) findViewById(R.id.rlMain);
        rlMain.setBackgroundColor(Color.LTGRAY);

        tvTitle = new TextView(this);
        tvTitle.setId(TITLE);

        imView = new ImageView(this);
        imView.setId(SLIDE);
        imView.setOnClickListener(this);
        
        lvTimeline = (ListView) findViewById(R.id.lvTimeline);

        slidePos = new ArrayList<TextView>();
        
        tvTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        tvTitle.setText("Placeholder Text for Video Title");
        tvTitle.setTextColor(Color.BLACK);
        tvTitle.setTextSize(10);
        tvTitle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        
        //tvSlidePos.setClickable(false);
        //tvSlidePos.setFocusable(false);

        listPopulator = new Runnable() {
			@Override
			public void run() {
		        tsParser = new LodeSaxDataParser("");
		        ts = tsParser.parseSlides();
		        tsIterator = ts.iterator();
				handler.post(new Runnable() {
					@Override
					public void run() {
						String title;
				        while(tsIterator.hasNext()){
				        	title = tsIterator.next().getTitolo().trim().replaceAll(" +", " ").replace("\n", "");
				        	title = title.trim();
				            tvSlidePos = new TextView(LodeActivityContext);
				            tvSlidePos.setClickable(false);
				            tvSlidePos.setFocusable(false);
				        	tvSlidePos.setText(title);
				        	slidePos.add(tvSlidePos);
				        	Log.e("Title: ", title);
				        }
			            tvSlidePos = new TextView(LodeActivityContext);
			            tvSlidePos.setClickable(false);
			            tvSlidePos.setFocusable(false);
			        	tvSlidePos.setText(" --- End ---");
			        	slidePos.add(tvSlidePos);
					}
				});
			}
		};
		new Thread(listPopulator).start();
        
        lvTimeline.setAdapter(new TimeLineAdapter(this, R.layout.slide_pos, slidePos));
        //lvTimeline.setOnItemSelectedListener(this);
        lvTimeline.setOnItemClickListener(this);
        
        vidView = new VidView(this);
        vidView.setBackgroundResource(R.layout.corners);
        vidView.setId(VIDEO);
//        videoUrl = "http://itunes.unitn.it/itunes/archive" +
//        		"/ScienzeMMFFNN/ProgrammazioneAndroid/video/02_Introduzione_2012-02-23b.mp4";
        videoUrl = "http://commonsware.com/misc/test2.3gp";
        vidView.setVideoURI(Uri.parse(videoUrl));

//        File clip = new File(Environment.getExternalStorageDirectory(), "test2.3gp");
//        vidView.setVideoPath(clip.getAbsolutePath());
//        videoUrl = clip.getAbsolutePath();

        vidView.setOnTouchListener(this);
        vidView.setOnCompletionListener(this);
        vidView.setOnPreparedListener(this);
        vidView.setLongClickable(true);
        
        btnPlay = new ImageButton(this);
        btnF = new ImageButton(this);
        btnR = new ImageButton(this);
        btnFullScreen = new ImageButton(this);
        sbSlider = new SeekBar(this);
        pbVideo = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        pbVideo.setVisibility(View.GONE);

        hideMc();

        btnFullScreen.setOnClickListener(this);
        btnFullScreen.setImageResource(R.drawable.fullscreen);
        btnFullScreen.setId(FS);
        btnFullScreen.setBackgroundColor(Color.TRANSPARENT);
        
        btnPlay.setOnClickListener(this);
        btnPlay.setImageResource(R.drawable.ic_media_play);
        btnPlay.setId(PLAY);
        
        btnF.setOnClickListener(this);
        btnF.setImageResource(R.drawable.ic_media_ff);
        btnF.setId(FF);
        
        btnR.setOnClickListener(this);
        btnR.setImageResource(R.drawable.ic_media_rew);
        btnR.setId(RR);
        
        sbSlider.setId(SLIDER);
        sbSlider.setThumbOffset(10);
        sbSlider.setProgressDrawable(getResources().getDrawable(R.layout.progress_slider));
        sbSlider.setMax(100);
        sbSlider.setProgress(0);
        sbSlider.setOnSeekBarChangeListener(this);
        
        sdTimeline = (SlidingDrawer) findViewById(R.id.sdTimeline);
        flParams = new FrameLayout.LayoutParams(scrWidth / 4 , scrHeight);
        flParams.topMargin = 10;
        flParams.bottomMargin = 10;
        flParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        flParams.gravity = Gravity.RIGHT;
        sdTimeline.setLayoutParams(flParams);

        sdTimeline.setFocusable(false);
        sdTimeline.setClickable(false);

        sdTimeline.setOnDrawerOpenListener(this);

        rlMainParams = new RelativeLayout.LayoutParams(scrHeight * 3 / 4, 30);
        rlMainParams.topMargin = 10;
        rlMainParams.leftMargin = 0;
        rlMain.addView(tvTitle, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(scrHeight * 3 / 4, scrHeight * 3 / 4);
        rlMainParams.topMargin = 30;
        rlMainParams.leftMargin = 10;
        rlMain.addView(vidView, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(30, 30);
        rlMainParams.topMargin = scrHeight * 3 / 8;
        rlMainParams.leftMargin = scrHeight * 3 / 8;
        rlMain.addView(pbVideo, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(50, 50);
        rlMainParams.topMargin = scrHeight * 3 / 4 - 30;
        rlMainParams.leftMargin = 20;
        rlMain.addView(btnPlay, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(50, 50);
        rlMainParams.topMargin = scrHeight * 3 / 4 - 30;
        rlMainParams.leftMargin = 70;
        rlMain.addView(btnR, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(50, 50);
        rlMainParams.topMargin = scrHeight * 3 / 4 - 30;
        rlMainParams.leftMargin = 120;
        rlMain.addView(btnF, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(50, 50);
        rlMainParams.topMargin = 30;
        rlMainParams.leftMargin = 10;
        rlMain.addView(btnFullScreen, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(scrHeight * 3 / 4 - 180 , 50);
        rlMainParams.topMargin = scrHeight * 3 / 4 - 30;
        rlMainParams.leftMargin = 170;
        rlMain.addView(sbSlider, rlMainParams);

        imView.setImageResource(R.drawable.slide);
        imView.setBackgroundResource(R.layout.corners);
        imView.setScaleType(ScaleType.FIT_XY);
        rlMainParams = new RelativeLayout.LayoutParams(scrWidth - scrHeight * 3 / 4, scrHeight * 4 / 5);
        rlMainParams.topMargin = 30;
        rlMainParams.leftMargin = scrHeight * 3 / 4 + scrHeight / 30;
        rlMainParams.rightMargin = 10;
        rlMain.addView(imView, rlMainParams);
	}
	@Override
	public void onClick(View view) {
		if(view.getId() == PLAY){
			if(playState == 0){
				playState = 1;
				btnPlay.setImageResource(R.drawable.ic_media_pause);
				pbVideo.bringToFront();
				vidView.start();
				new Thread(waitAndHide).start();
				if(sliderThread == null){
					sliderThread = new Thread(sliderUpdater);
					sliderThread.start();
				}
				isStarted = true;
			}
			else{
				playState = 0;
				btnPlay.setImageResource(R.drawable.ic_media_play);
				vidView.pause();
				dead = sliderThread;
				sliderThread = null;
				dead.interrupt();
			}
		}
		else if(view.getId() == FF){
			vidView.seekTo(vidView.getCurrentPosition() + 10000 < vidView.getDuration() ?
					vidView.getCurrentPosition() + 10000 : 
						vidView.getDuration() - 1);
		}
		else if(view.getId() == RR){
			vidView.seekTo(vidView.getCurrentPosition() - 10000 > 0 ?
					vidView.getCurrentPosition() - 10000 : 
						0);
		}
		else if(view.getId() == SLIDE){
			if(sdTimeline.isOpened()){
				sdTimeline.animateClose();
			}
		}
		else if(view.getId() == FS){
			fsIntent = new Intent(this, PlayInFullScreenActivity.class);
			fsBundle = new Bundle();
			fsBundle.putInt("CurrPos", vidView.getCurrentPosition());
			fsBundle.putString("VideoURL", videoUrl);
			fsIntent.putExtras(fsBundle);
			btnPlay.setImageResource(R.drawable.ic_media_play);
			playState = 0;
			vidView.pause();
			if(sliderThread != null){
				dead = sliderThread;
				sliderThread = null;
				dead.interrupt();
			}
			fullScreen = true;
			startActivity(fsIntent);
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		vidView.pause();
		playState = 0;
		btnPlay.setImageResource(R.drawable.ic_media_play);
	}
	@Override
	protected void onResume() {
		super.onResume();
		if(firstTime || fullScreen){
	        pbVideo.setVisibility(View.VISIBLE);
		}
		if(!firstTime & !hasFinished & fullScreen){
			playState = 1;
			btnPlay.setImageResource(R.drawable.ic_media_pause);
			vidView.seekTo(currPos);
			isResuming = true;
			isStarted = true;
			fullScreen = false;
			vidView.start();
			sliderThread = new Thread(sliderUpdater);
			sliderThread.start();
		}
		else{
			if(hasFinished){
				hasFinished = false;
			}
			firstTime = false;
		}
	}
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if(view.getId() == VIDEO){
			if(btnPlay.getVisibility() == View.INVISIBLE){
				showMc();
				if(!isStarted){
					isStarted = true;
				}
				else{
					dead = thread;
					thread = null;
					dead.interrupt();
				}
				thread = new Thread(waitAndHide);
				thread.start();
			}
			if(sdTimeline.isOpened()){
				sdTimeline.close();
			}
		}
		return false;
	}
	public void hideMc(){
		btnPlay.setVisibility(View.INVISIBLE);
		btnF.setVisibility(View.INVISIBLE);
		btnR.setVisibility(View.INVISIBLE);
		btnFullScreen.setVisibility(View.INVISIBLE);
		sbSlider.setVisibility(View.INVISIBLE);
	}
	public void showMc(){
		btnPlay.setVisibility(View.VISIBLE);
		btnF.setVisibility(View.VISIBLE);
		btnR.setVisibility(View.VISIBLE);
		btnFullScreen.setVisibility(View.VISIBLE);
		sbSlider.setVisibility(View.VISIBLE);
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		btnPlay.setImageResource(R.drawable.ic_media_play);
		dead = sliderThread;
		sliderThread = null;
		dead.interrupt();
		sbSlider.setProgress(0);
		playState = 0;
		showMc();
	}
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(fromUser || isResuming){
			vidView.seekTo((vidView.getDuration() * progress) / 100);
			isResuming = false;
		}
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onScrollEnded() {
		flTimeline.bringToFront();
		if(sdTimeline.isOpened()){
			sdTimeline.close();
		}
	}
	@Override
	public void onScrollStarted() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onDrawerOpened() {
	}
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		sdTimeline.animateClose();
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		sdTimeline.animateClose();
	}
	@Override
	protected void onStart() {
		super.onStart();
		flTimeline.bringToFront();
	}
	@Override
	public void onPrepared(MediaPlayer mp) {
		pbVideo.setVisibility(View.INVISIBLE);
	}
}