package it.unitn.lode;

import it.unitn.lode.data.LodeSaxDataParser;
import it.unitn.lode.data.TimedSlides;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import android.util.FloatMath;
import android.util.Log;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.SlidingDrawer.OnDrawerScrollListener;
import android.widget.SlidingDrawer;
import android.widget.TextView;

public class LODEActivity extends Activity implements OnClickListener,
	OnCompletionListener, OnSeekBarChangeListener, OnDrawerScrollListener, OnDrawerOpenListener,
	OnItemClickListener, OnPreparedListener, OnLongClickListener, OnTouchListener{
    /** Called when the activity is first created. */
	private Display devDisplay = null;
	public static int scrWidth, scrHeight;
	private final int VIDEO = 0, PLAY = 1, FF = 2, RR = 3, SLIDER = 4, SLIDE = 5, TITLE = 6, FS = 7, VIDEO_LAYER = 8, RESET = 9
			, LOCK = 10;
	private RelativeLayout rlMain = null, rlMc = null, rlSlide = null, rlBottomBar = null;
	private RelativeLayout.LayoutParams rlMainParams = null;
	private TextView tvTitle = null, tvSlidePos = null, tvTime = null;
	public static VidView vidView = null;
	private ImageView ivSlides = null, vidViewLayer = null;
	private ImageButton btnF = null;
	private ImageButton btnR = null;
	private ImageButton btnPlay = null;
	private ImageButton btnFullScreen = null;
	private ImageButton btnResetSlide = null;
	private ImageButton btnLockInPlace = null;
	private SeekBar sbSlider = null;
	private int playState = 0;
	public static int currPos = 0;
	private boolean isStarted = false, firstTime = true, isResuming = false, fullScreen = false, activityFirstRun = true;
	public static boolean hasFinished = false;
	private Handler handler = null;
	private Runnable waitAndHide = null, timeUpdater = null;
	private Runnable sliderUpdater = null, listPopulator = null, slideChanger = null;
	private Thread timeUpdaterThread = null, dead = null, sliderThread = null, slideChangerThread = null, slideGetterThread = null,
			closestSetterThread = null;
	private SlidingDrawer sdTimeline = null;
	private FrameLayout flTimeline = null;
	private FrameLayout.LayoutParams flParams = null;
	private ListView lvTimeline = null;
	private RelativeLayout rlTimeline = null;
	private ArrayList<TextView> slidePos = null;
	private Intent fsIntent = null;
	private Bundle fsBundle = null;
	private ProgressBar pbVideo = null, pbSlide = null;
	private Iterator<TimedSlides> tsIterator = null, tsSlideIterator = null;
	private LodeSaxDataParser tsParser = null;
	private List<TimedSlides> ts = null, tsNext = null;
	private final Context LodeActivityContext = this;
	private Typeface tfApplegaramound = null;
	private String videoUrl = null;
	private String lectureDataUrl = null;
	public static AssetManager ASSETS = null;
	private Drawable singleSlide = null, closestSlide = null;
	private TreeMap<Integer, String>timeAndSlide = null;
	private ArrayList<String> slideTitles = null;
	private int progress = 0, prevProgress = 0;
	private ArrayList<Integer> slideTempo;
	private int closestTempo = Integer.MAX_VALUE, currentTempo = 0;
	private Runnable closestSlideUpdater = null;
	private boolean isFromTimeline = false, slideIsLarge = false, videoIsLarge = false, isLocked = true, justNow = false,
			slideInMain = false, keepCounting = false;
	private int vidViewCurrPos;
	private Matrix defaultMatrix = null, currMatrix = null, prevMatrix = null;
	private int mode;
	private static final int ZOOM = 0, DRAG = 1, NONE = 2;
	private PointF start = null, mid = null;
	private float prevDist = 1f;
	private static long duration = 0;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Bundle watchBundle = getIntent().getExtras();
        videoUrl = watchBundle.getString("videoUrl");
        lectureDataUrl = watchBundle.getString("lectureDataUrl");
        watchBundle = null;
        
        ASSETS = getAssets();
        tfApplegaramound = Typeface.createFromAsset(ASSETS, "fonts/Applegaramound.ttf");
        flTimeline = (FrameLayout) findViewById(R.id.flTimeline);
        rlTimeline = (RelativeLayout) findViewById(R.id.rlTimeline);

        rlTimeline.setBackgroundResource(R.layout.timeline);
        
        slideTitles = new ArrayList<String>();
        slideTempo = new ArrayList<Integer>();
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
//						hideMc();
						isStarted = false;
					}
				});
			}
        };
        timeUpdater = new Runnable() {
        	String time = "";
        	@Override
			public void run() {
        		while(keepCounting){
					long seconds = TimeUnit.MILLISECONDS.toSeconds(vidView.getCurrentPosition());
					time = convertTime(seconds);
    				handler.post(new Runnable() {

    					@Override
    					public void run() {
    						tvTime.setText(time);
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
        sliderUpdater = new Runnable(){
			@Override
			public void run() {
				while(true){
					handler.post(new Runnable() {
						@Override
						public void run() {
							prevProgress = progress;
							progress = vidView.getCurrentPosition();
							if(progress != prevProgress)
								sbSlider.setProgress(progress);
						}
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						//e.printStackTrace();
					}
				}
			}
        };
        closestSlideUpdater = new Runnable(){
			@Override
			public void run() {
//				Log.e("CLOSEST SLIDE UPDATER", "I'M BEING CALLED");
					closestSlide = getSlide(timeAndSlide.get(closestTempo));
				handler.post(new Runnable() {
					@Override
					public void run() {
//						Log.e("CLOSEST SLIDE UPDATER", "I'M CHANGING THE SLIDE");
						pbSlide.setVisibility(View.VISIBLE);
						ivSlides.setImageDrawable(closestSlide);
						pbSlide.setVisibility(View.GONE);
						rlSlide.bringToFront();
						flTimeline.bringToFront();
						if(videoIsLarge){
							vidView.bringToFront();
							rlMc.bringToFront();
							btnFullScreen.bringToFront();
						}
					}
				});
			}
        };				


        devDisplay = getWindowManager().getDefaultDisplay();
        scrWidth = devDisplay.getWidth();
        scrHeight = devDisplay.getHeight();

        rlMain = (RelativeLayout) findViewById(R.id.rlMain);
        rlMain.setBackgroundColor(Color.LTGRAY);
        rlMain.setOnClickListener(this);
        
        rlMc = (RelativeLayout) findViewById(R.id.rlMc);
        //rlMc.setBackgroundColor(Color.TRANSPARENT);
        rlMc.setBackgroundResource(R.drawable.a9p_09_11_00015);

        rlBottomBar = (RelativeLayout) findViewById(R.id.rlBottomBar);
        rlBottomBar.setBackgroundResource(android.R.drawable.bottom_bar);
        //rlBottomBar.setBackgroundResource(R.drawable.a9p_09_11_00063);
        rlBottomBar.setGravity(Gravity.CENTER_VERTICAL);
        
        rlSlide = (RelativeLayout) findViewById(R.id.rlSlide);
        rlSlide.setBackgroundColor(Color.TRANSPARENT);
        rlSlide.setBackgroundResource(R.layout.curved_corners);
        rlSlide.setPadding(4, 0, 0, 4);

        tvTitle = new TextView(this);
        tvTitle.setId(TITLE);

        start = new PointF();
        mid = new PointF();
        currMatrix = new Matrix();
        prevMatrix = new Matrix();
        defaultMatrix = new Matrix();

        ivSlides = new ImageView(this);
        ivSlides.setId(SLIDE);
        ivSlides.setOnClickListener(this);
        ivSlides.setOnLongClickListener(this);
		ivSlides.setScaleType(ImageView.ScaleType.FIT_XY);

        
        
        

        
        
        
        
        
        ivSlides.setOnTouchListener(null);
			

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        

        
        
        
        
        
        
        
        
        
        
        
        lvTimeline = (ListView) findViewById(R.id.lvTimeline);
        lvTimeline.setOnItemClickListener(this);

        lvTimeline.setCacheColorHint(Color.parseColor("#00000000"));
        lvTimeline.setSelector(R.layout.courses_corners_clicked);
        lvTimeline.setDividerHeight(2);
        lvTimeline.setOnItemClickListener(this);

        slidePos = new ArrayList<TextView>();
        
        tvTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        tvTitle.setTextColor(Color.BLACK);
        tvTitle.setTextSize(14);
        tvTitle.setTypeface(tfApplegaramound, Typeface.BOLD);
        
        listPopulator = new Runnable() {
			@Override
			public void run() {
		        tsParser = new LodeSaxDataParser(lectureDataUrl + "/TIMED_SLIDES.XML");
		        ts = tsParser.parseSlides();
		        tsNext = ts;
		        tsSlideIterator = tsNext.iterator();
		        tsIterator = ts.iterator();
				handler.post(new Runnable() {
					@Override
					public void run() {
			            tvSlidePos = new TextView(LodeActivityContext);
			        	tvSlidePos.setText("Start");
			        	slidePos.add(tvSlidePos);
						String title;
				        while(tsIterator.hasNext()){
				        	title = tsIterator.next().getTitolo().trim().replaceAll(" +", " ").replace("\n", "");
				        	title = title.trim();
				            tvSlidePos = new TextView(LodeActivityContext);
				        	tvSlidePos.setText(title);
				        	slidePos.add(tvSlidePos);
//				        	Log.e("Title: ", title);
				        }
			            tvSlidePos = new TextView(LodeActivityContext);
			        	tvSlidePos.setText("End");
			        	slidePos.add(tvSlidePos);
			        	lvTimeline.invalidateViews();
					}
				});
			}
		};
		new Thread(listPopulator).start();
		
		slideChanger = new Runnable() {
			Iterator<String> titleIterator = slideTitles.iterator();
			@Override
			public void run() {
				if(timeAndSlide.containsKey(vidViewCurrPos)){
					singleSlide = getSlide(timeAndSlide.get(vidViewCurrPos));
					if(singleSlide == null){
//						Log.e("It's", "empty");
					}
				}
				handler.post(new Runnable() {
				@Override
				public void run() {
					if(singleSlide != null){
						pbSlide.setVisibility(View.VISIBLE);
						pbSlide.bringToFront();
						pbSlide.setVisibility(View.VISIBLE);
						ivSlides.setImageDrawable(singleSlide);
						pbSlide.setVisibility(View.GONE);
						rlSlide.bringToFront();
						if(titleIterator.hasNext()){
							tvTitle.setText(titleIterator.next());
							tvTitle.bringToFront();
						}
						if(videoIsLarge){
							vidView.bringToFront();
							rlMc.bringToFront();
							btnFullScreen.bringToFront();
						}
						flTimeline.bringToFront();
					}
				}
			});
			}
		};
		SlideDataGetter sdg = new SlideDataGetter();
        slideGetterThread = new Thread(sdg);
        slideGetterThread.start();
		
		lvTimeline.setAdapter(new TimeLineAdapter(this, R.layout.slide_pos, slidePos){
			@Override
			public boolean isEnabled(int position) {
				if(position == 0 || position == slidePos.size() - 1)
					return false;
				return true;
			}
		});
        vidView = new VidView(this);
        vidView.setBackgroundResource(R.layout.corners);
        vidView.setId(VIDEO);
        vidView.setVideoURI(Uri.parse(videoUrl));
        //vidView.setOnLongClickListener(this);
        //vidView.setOnTouchListener(this);
        vidView.setOnCompletionListener(this);
        vidView.setOnPreparedListener(this);
        //vidView.setLongClickable(true);
        
        vidViewLayer = new ImageView(this);
        vidViewLayer.setId(VIDEO_LAYER);
        vidViewLayer.setOnLongClickListener(this);
        vidViewLayer.setOnClickListener(this);
        vidViewLayer.setBackgroundColor(Color.TRANSPARENT);
//        vidViewLayer.setBackgroundResource(R.layout.curved_corners);

        btnPlay = new ImageButton(this);
        btnF = new ImageButton(this);
        btnR = new ImageButton(this);
        btnFullScreen = new ImageButton(this);
        sbSlider = new SeekBar(this);
        pbVideo = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        pbVideo.setVisibility(View.GONE);
        pbSlide = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        
        btnResetSlide = new ImageButton(this);
        btnResetSlide.setId(RESET);
        btnResetSlide.setBackgroundResource(android.R.drawable.btn_minus);
        btnResetSlide.setOnClickListener(this);
        
        btnLockInPlace = new ImageButton(this);
        btnLockInPlace.setId(LOCK);
        btnLockInPlace.setBackgroundResource(android.R.drawable.ic_lock_lock);
        btnLockInPlace.setOnClickListener(this);
        //pbSlide.setVisibility(View.GONE);

//        hideMc();

        btnFullScreen.setOnClickListener(this);
        btnFullScreen.setImageResource(R.drawable.fullscreen);
        btnFullScreen.setId(FS);
        btnFullScreen.setBackgroundColor(Color.TRANSPARENT);
        
        btnPlay.setOnClickListener(this);
        btnPlay.setImageResource(android.R.drawable.ic_media_play);
        btnPlay.setId(PLAY);
        
        btnF.setOnClickListener(this);
        btnF.setImageResource(android.R.drawable.ic_media_ff);
        btnF.setId(FF);
        
        btnR.setOnClickListener(this);
        btnR.setImageResource(android.R.drawable.ic_media_rew);
        btnR.setId(RR);
        
        sbSlider.setId(SLIDER);
        sbSlider.setThumbOffset(10);
        sbSlider.setProgressDrawable(getResources().getDrawable(R.layout.progress_slider));
        sbSlider.setProgress(0);
        sbSlider.setOnSeekBarChangeListener(this);
        
        sdTimeline = (SlidingDrawer) findViewById(R.id.sdTimeline);
        flParams = new FrameLayout.LayoutParams(scrWidth / 4 + 30, scrHeight);
        flParams.topMargin = 10;
        flParams.bottomMargin = 10;
        flParams.rightMargin = 5;
        flParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        flParams.gravity = Gravity.RIGHT;
        sdTimeline.setLayoutParams(flParams);

        sdTimeline.setFocusable(false);
        sdTimeline.setClickable(false);

        sdTimeline.setOnDrawerOpenListener(this);
        //sdTimeline.setEnabled(false);

        tvTime = new TextView(this);
        tvTime.setBackgroundResource(R.layout.no_stroke);
        tvTime.setTypeface(tfApplegaramound);
        tvTime.setTextColor(Color.BLACK);
        tvTime.setGravity(Gravity.CENTER);
        tvTime.setText("00:00");
        
        rlMainParams = new RelativeLayout.LayoutParams(scrHeight * 3 / 4, 30);
        rlMainParams.topMargin = 10;
        rlMainParams.leftMargin = 0;
        rlMain.addView(tvTitle, rlMainParams);

//        rlMainParams = new RelativeLayout.LayoutParams(scrHeight * 3 / 4 + 5, scrHeight * 4 / 5);
        rlMainParams = new RelativeLayout.LayoutParams((scrHeight * 5) / 6, (scrHeight * 15) / 24);
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = 0;
        rlMain.addView(vidView, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams((scrHeight * 5) / 6, (scrHeight * 15) / 24);
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = 0;
        rlMain.addView(vidViewLayer, rlMainParams);
        
        rlMainParams = new RelativeLayout.LayoutParams(25, 25);
        rlMainParams.topMargin = scrHeight * 3 / 8;
        rlMainParams.leftMargin = scrHeight * 3 / 8;
        rlMain.addView(pbVideo, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(50, 50);
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = 5;
        rlMc.addView(btnPlay, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(50, 50);
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = 55;
        rlMc.addView(btnR, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(50, 50);
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = 105;
        rlMc.addView(btnF, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(scrHeight * 3 / 4 - 200 , 50);
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = 155;
        rlMc.addView(sbSlider, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(80 , 50);
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = ((scrHeight * 5) / 6) - 85;
        rlMc.addView(tvTime, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams((scrHeight * 5) / 6, 70);
        rlMainParams.topMargin = (scrHeight * 15) / 24;
        rlMainParams.leftMargin = 0;
        rlMc.setGravity(Gravity.CENTER_VERTICAL);
        rlMain.removeView(rlMc);
        rlMain.addView(rlMc, rlMainParams);
        
//        rlMainParams = new RelativeLayout.LayoutParams(scrWidth , 80);
//        rlMainParams.topMargin = 0;
//        rlMainParams.leftMargin = 0;
//        rlBottomBar.addView(ibBottomBar, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(scrWidth - 6, 80);
        rlMainParams.topMargin = scrHeight - 83;
        rlMainParams.leftMargin = 3;
        rlMain.removeView(rlBottomBar);
        rlMain.addView(rlBottomBar, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(60, 50);
        rlMainParams.width = LayoutParams.WRAP_CONTENT;
        rlMainParams.height = LayoutParams.WRAP_CONTENT;
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = scrWidth - 110;
        rlBottomBar.addView(btnResetSlide, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(60, 50);
        rlMainParams.width = LayoutParams.WRAP_CONTENT;
        rlMainParams.height = LayoutParams.WRAP_CONTENT;
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = scrWidth - 150;
        rlBottomBar.addView(btnLockInPlace, rlMainParams);
//        rlMainParams = new RelativeLayout.LayoutParams(50, 50);
//        rlMainParams.topMargin = scrHeight * 3 / 4 - 60;
//        rlMainParams.leftMargin = 10;
//        rlMain.addView(btnPlay, rlMainParams);
//
//        rlMainParams = new RelativeLayout.LayoutParams(50, 50);
//        rlMainParams.topMargin = scrHeight * 3 / 4 - 60;
//        rlMainParams.leftMargin = 60;
//        rlMain.addView(btnR, rlMainParams);
//
//        rlMainParams = new RelativeLayout.LayoutParams(50, 50);
//        rlMainParams.topMargin = scrHeight * 3 / 4 - 60;
//        rlMainParams.leftMargin = 110;
//        rlMain.addView(btnF, rlMainParams);
//
//        rlMainParams = new RelativeLayout.LayoutParams(50, 50);
//        rlMainParams.topMargin = 0;
//        rlMainParams.leftMargin = 0;
//        rlMain.addView(btnFullScreen, rlMainParams);
//
//        rlMainParams = new RelativeLayout.LayoutParams(scrHeight * 3 / 4 - 180 , 50);
//        rlMainParams.topMargin = scrHeight * 3 / 4 - 60;
//        rlMainParams.leftMargin = 160;
//        rlMain.addView(sbSlider, rlMainParams);

//        ivSlides.setBackgroundColor(Color.WHITE);
        rlMainParams = new RelativeLayout.LayoutParams(scrWidth - (scrHeight * 5) / 6, ((scrHeight * 15) / 24) + 70);
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = (scrHeight * 5) / 6;
        rlMain.removeView(rlSlide);
        rlMain.addView(rlSlide, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(scrWidth - (scrHeight * 5) / 6, scrHeight * 4 / 5);
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = 0;
        rlSlide.addView(ivSlides, rlMainParams);
        
        rlMainParams = new RelativeLayout.LayoutParams(25, 25);
        rlMainParams.topMargin = scrHeight * 3 / 8 + 15;
        rlMainParams.leftMargin = ((scrHeight * 3) / 4 + scrHeight / 30) + ((scrWidth - scrHeight * 3 / 4) / 2) - 15;
        rlMain.addView(pbSlide, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(50, 50);
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = 0;
        rlMain.addView(btnFullScreen, rlMainParams);
}
	@Override
	public void onClick(View view) {
		if(view.getId() == PLAY){
			if(playState == 0){
				playState = 1;
				btnPlay.setImageResource(android.R.drawable.ic_media_pause);
				pbVideo.bringToFront();
				vidView.start();
		        sbSlider.setMax(vidView.getDuration());
				//new Thread(waitAndHide).start();
				if(activityFirstRun){
					activityFirstRun = false;
					if(timeUpdaterThread == null){
						Log.e("onClick", "Starting Time Updater");
						timeUpdaterThread = new Thread(timeUpdater);
						timeUpdaterThread.start();
					}
					if(slideTempo != null){
						vidViewCurrPos = slideTempo.get(0);
					}
					else{
						vidViewCurrPos = 0;
					}
			        slideChangerThread = new Thread(slideChanger);
			        slideChangerThread.start();
				}
				if(sliderThread == null){
					sliderThread = new Thread(sliderUpdater);
					sliderThread.start();
				}
				isStarted = true;
			}
			else{
				playState = 0;
				btnPlay.setImageResource(android.R.drawable.ic_media_play);
				vidView.pause();
				dead = sliderThread;
				sliderThread = null;
				dead.interrupt();
				
			}
		}
		else if(view.getId() == FF){
			vidView.seekTo(vidView.getCurrentPosition() + 500 < vidView.getDuration() ?
					vidView.getCurrentPosition() + 500 : 
						vidView.getDuration() - 1);
		}
		else if(view.getId() == RR){
			vidView.seekTo(vidView.getCurrentPosition() - 500 > 0 ?
					vidView.getCurrentPosition() - 500 : 
						0);
		}
		else if(view.getId() == SLIDE){
			if(sdTimeline.isOpened()){
				sdTimeline.animateClose();
			}
			else{
				rlSlide.bringToFront();
				vidView.requestLayout();
				vidView.invalidate();
				rlMc.requestLayout();
				rlMc.invalidate();
				flTimeline.bringToFront();
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
		else if(view.getId() == VIDEO_LAYER || view.getId() == VIDEO){
			if(sdTimeline.isOpened()){
				sdTimeline.close();
			}
			else{
				if(btnPlay.getVisibility() == View.INVISIBLE){
//					showMc();
					if(!isStarted){
						isStarted = true;
					}
//					else{
//						if(thread != null){
//							dead = thread;
//							thread = null;
//							dead.interrupt();
//						}
//					}
//					thread = new Thread(waitAndHide);
//					thread.start();
				}
				vidView.bringToFront();
				vidView.requestLayout();
				vidView.invalidate();
				btnFullScreen.bringToFront();
				vidViewLayer.bringToFront();
				rlMc.bringToFront();
				rlMc.requestLayout();
				rlMc.invalidate();
			}
		}
		else if(view.getId() == R.id.rlMain){
			if(sdTimeline.isOpened()){
				sdTimeline.animateClose();
			}
		}
		else if(view.getId() == RESET){
			correctSlideMatrix(ScaleType.FIT_XY);
			if(!slideInMain){
				isLocked = true;
				btnLockInPlace.setBackgroundResource(android.R.drawable.ic_lock_lock);
			}
		}
		else if(view.getId() == LOCK){
			if(isLocked){
				btnLockInPlace.setBackgroundResource(android.R.drawable.ic_lock_idle_lock);
				isLocked = false;
				growInMainLayout();
			}
			else{
				btnLockInPlace.setBackgroundResource(android.R.drawable.ic_lock_lock);
				isLocked = true;
				ivSlides.setOnTouchListener(null);
				lockIntoPosition();
				correctSlideMatrix(ScaleType.FIT_XY);
			}
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		vidView.pause();
		playState = 0;
		btnPlay.setImageResource(R.drawable.ic_media_play);
		firstTime = false;
		hasFinished = false;
		fullScreen = true;
		currPos = vidView.getCurrentPosition();
		keepCounting = false;
	}
	@Override
	protected void onResume() {
		super.onResume();
		keepCounting = true;
		if(timeUpdaterThread == null){
			Log.e("onResume", "Starting Time Updater");
			timeUpdaterThread = new Thread(timeUpdater);
			timeUpdaterThread.start();
		}
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
			new Thread(closestSlideUpdater).start();
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
	public void hideMc(){
		rlMc.setVisibility(View.GONE);
		btnFullScreen.setVisibility(View.GONE);
		btnPlay.setVisibility(View.INVISIBLE);
		btnF.setVisibility(View.INVISIBLE);
		btnR.setVisibility(View.INVISIBLE);
		sbSlider.setVisibility(View.INVISIBLE);
	}
	public void showMc(){
		rlMc.setVisibility(View.VISIBLE);
		btnFullScreen.setVisibility(View.VISIBLE);
		btnPlay.setVisibility(View.VISIBLE);
		btnF.setVisibility(View.VISIBLE);
		btnR.setVisibility(View.VISIBLE);
		sbSlider.setVisibility(View.VISIBLE);
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		btnPlay.setImageResource(R.drawable.ic_media_play);
		if(sliderThread != null){
			dead = sliderThread;
			sliderThread = null;
			dead.interrupt();
		}
		sbSlider.setProgress(0);
		playState = 0;
//		showMc();
	}
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean isFromUser) {
		if(isFromUser || isResuming || isFromTimeline){
			if(isFromTimeline){
//				Log.d("OnProgressChanged", "Progress Changed from timeline");
			}
			if(isFromUser){
//				Log.d("OnProgressChanged", "Progress Changed by user");
			}
			if(isResuming){
//				Log.d("OnProgressChanged", "is Resuming");
			}
			vidView.seekTo(progress);
//			if(!isFromTimeline){
//				if(videoIsLarge){
//					shrinkVideo();
//				}
//				else{
//					if(slideIsLarge){
//						shrinkSlide();
//					}
//					growVideo();
//				}
//			}
//			else{
//				isFromTimeline = false;
//			}
			isFromTimeline = false;
			isResuming = false;
		}
		//Log.e("I'm at: ", String.valueOf(vidViewCurrPos));
//		if(timeAndSlide.containsKey(vidView.getCurrentPosition() / 1000)){
		vidViewCurrPos = progress / 1000;
		if(timeAndSlide.containsKey(vidViewCurrPos)){
			if(isFromUser){
//				Log.d("OnProgressChanged", "contains key");
			}
			if(slideChangerThread != null){
//				Log.e("SLIDE CHANGER THREAD", "I'M STILL ALIVE");
				dead = slideChangerThread;
				slideChangerThread = null;
				dead.interrupt();
			}
	        slideChangerThread = new Thread(slideChanger);
	        slideChangerThread.start();
		}
		else{
			if(isFromUser){
//				Log.d("OnProgressChanged", "does not contain key");
			}
			closestTempo = getClosestTempo(vidViewCurrPos);
//			Log.d("closestTempo", String.valueOf(closestTempo));
			if(currentTempo != closestTempo){
				currentTempo = closestTempo;
				if(closestSetterThread != null){
//					Log.e("KILLING", "CLOSEST SETTER THREAD");
					dead = closestSetterThread;
					closestSetterThread = null;
					dead.interrupt();
				}
//				Log.e("CALLING", "CLOSEST SETTER THREAD");
				closestSetterThread = new Thread(closestSlideUpdater);
				closestSetterThread.start();
			}
		}
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	@Override
	public void onScrollEnded() {
		flTimeline.bringToFront();
		if(sdTimeline.isOpened()){
			sdTimeline.animateClose();
		}
	}
	@Override
	public void onScrollStarted() {
	}
	@Override
	public void onDrawerOpened() {
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		Log.e("Timeline - position", String.valueOf(position));
		if(parent.getId() == R.id.lvTimeline){
			isFromTimeline = true;
			sbSlider.setProgress(slideTempo.get(position - 1) * 1000);
		}
		parent.setSelection(position);
		sdTimeline.animateClose();
	}
	@Override
	protected void onStart() {
		super.onStart();
		flTimeline.bringToFront();
	}
	@Override
	public void onPrepared(MediaPlayer mp) {
		//sdTimeline.setEnabled(true);
		pbVideo.setVisibility(View.INVISIBLE);
		onClick(btnPlay);
	}
	@Override
	public void onBackPressed() {
		keepCounting = false; 
		if(sdTimeline.isOpened()){
			sdTimeline.animateClose();
		}
		else{
			sbSlider.setProgress(0);
			if(slideChangerThread != null){
				dead = slideChangerThread;
				slideChangerThread = null;
				dead.interrupt();
			}			
			if(slideGetterThread != null){
				dead = slideGetterThread;
				slideGetterThread = null;
				dead.interrupt();
			}
			if(slideChangerThread != null){
				dead = slideChangerThread;
				slideChangerThread = null;
				dead.interrupt();
			}
			this.finish();
		}
	}
	Drawable getSlide(String slideUrl){
		Bitmap bitmap = null;
		Drawable drSlide = null;
	    try {
		    URL thisUrl = new URL(slideUrl);
		    HttpURLConnection conn = (HttpURLConnection) thisUrl.openConnection();
		    conn.connect();
		    InputStream input = conn.getInputStream();
		    bitmap = BitmapFactory.decodeStream(input);
		    drSlide = new BitmapDrawable(bitmap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return drSlide;
	}
	private class SlideDataGetter implements Runnable{
		@Override
		public void run() {
			while(tsSlideIterator == null){
				try{
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			timeAndSlide = new TreeMap<Integer, String>();
			while(tsSlideIterator.hasNext()){
				TimedSlides singleItem = tsSlideIterator.next();
				slideTempo.add(singleItem.getTempo());
				slideTitles.add(singleItem.getTitolo());
				timeAndSlide.put(singleItem.getTempo(), lectureDataUrl + "/" + singleItem.getImmagine());
				//Log.e(String.valueOf(singleItem.getTempo()), singleItem.getImmagine());
			}
		}
		
	}
	private int getClosestTempo(int tempo){
		//Log.e("tempo:", String.valueOf(tempo));
		int currentTempo;
		int closestTempo = Integer.MAX_VALUE;
		Iterator<Integer> closestIterator = slideTempo.iterator();
		while(closestIterator.hasNext()){
			currentTempo = closestIterator.next(); 
			//Log.e("currentTempo:", String.valueOf(currentTempo));
			if(currentTempo < tempo){
				if(Math.abs(currentTempo - tempo) < Math.abs(closestTempo - tempo)){
					closestTempo = currentTempo;
				}
				else if(Math.abs(currentTempo - tempo) == Math.abs(closestTempo - tempo)){
					if(currentTempo < closestTempo)
						closestTempo = currentTempo;
				}
			}
		}
		//Log.e("closestTempo:", String.valueOf(closestTempo));
		return closestTempo;
	}
	@Override
	public boolean onLongClick(View v) {
		if(v.getId() == SLIDE){
			if(slideIsLarge){
				//ivSlides.setOnTouchListener(null);
				shrinkSlide();
				defaultMatrix.postScale(0, 0, (scrWidth - (scrHeight * 5) / 6) / 2, (((scrHeight * 15) / 24) + 70) / 2);
			}
			else{
				if(videoIsLarge){
					shrinkVideo();
				}
				growSlide();
				defaultMatrix.postScale(0, 0, ((scrWidth * 2) / 3) / 2, scrHeight / 2);
				//justNow = true;
				//ivSlides.setOnTouchListener(this);
			}
			ivSlides.setImageMatrix(defaultMatrix);
			start = new PointF();
			mid = new PointF();
			currMatrix = new Matrix(); 
			prevMatrix = new Matrix(); 
		}
		else if(v.getId() == VIDEO){
			if(videoIsLarge){
				shrinkVideo();
			}
			else{
				if(slideIsLarge){
					//ivSlides.setOnTouchListener(null);
					shrinkSlide();
				}
				growVideo();
			}
		}
		else if(v.getId() == VIDEO_LAYER){
			if(videoIsLarge){
				shrinkVideo();
			}
			else{
				if(slideIsLarge){
					//ivSlides.setOnTouchListener(null);
					shrinkSlide();
				}
				growVideo();
			}
			btnPlay.bringToFront();
			btnF.bringToFront();
			btnR.bringToFront();
			sbSlider.bringToFront();
		}
		return false;
	}
	public void growSlide(){
		if(sdTimeline.isOpened()){
			sdTimeline.animateClose();
		}
		slideIsLarge = true;
        rlMainParams = new RelativeLayout.LayoutParams(0, 0);
		rlMainParams.width = (scrWidth * 2) / 3;
		rlMainParams.height = scrHeight;
		rlMainParams.leftMargin = (scrWidth / 3);
		rlMain.removeView(rlSlide);
		rlMain.addView(rlSlide, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(0, 0);
		rlMainParams.width = (scrWidth * 2) / 3;
		rlMainParams.height = scrHeight;
		rlMainParams.topMargin = 0;
		rlMainParams.leftMargin = 0;
		rlSlide.removeView(ivSlides);
		rlSlide.addView(ivSlides, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(60, 50);
        rlMainParams.width = LayoutParams.WRAP_CONTENT;
        rlMainParams.height = LayoutParams.WRAP_CONTENT;
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = 70;
        rlBottomBar.removeView(btnResetSlide);
        rlBottomBar.addView(btnResetSlide, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(60, 50);
//        rlMainParams.width = LayoutParams.WRAP_CONTENT;
//        rlMainParams.height = LayoutParams.WRAP_CONTENT;
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = 0;
        rlBottomBar.removeView(btnLockInPlace);
        rlBottomBar.addView(btnLockInPlace, rlMainParams);

        ivSlides.setScaleType(ScaleType.FIT_XY);
		
		flTimeline.bringToFront();
	}
	public void shrinkSlide(){
		ivSlides.setOnTouchListener(null);
		if(sdTimeline.isOpened()){
			sdTimeline.animateClose();
		}
		slideIsLarge = false;
        rlMainParams = new RelativeLayout.LayoutParams(scrWidth - (scrHeight * 5) / 6, ((scrHeight * 15) / 24) + 70);
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = (scrHeight * 5) / 6;
		rlMain.removeView(rlSlide);
        rlMain.addView(rlSlide, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(scrWidth - (scrHeight * 5) / 6, scrHeight * 4 / 5);
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = 0;
        if(slideInMain){
    		rlMain.removeView(ivSlides);
    		btnLockInPlace.setBackgroundResource(android.R.drawable.ic_lock_lock);
    		isLocked = true;
    		slideInMain = false;
        }
        else{
    		rlSlide.removeView(ivSlides);
        }
        rlSlide.addView(ivSlides, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(60, 50);
        rlMainParams.width = LayoutParams.WRAP_CONTENT;
        rlMainParams.height = LayoutParams.WRAP_CONTENT;
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = scrWidth - 110;
        rlBottomBar.removeView(btnResetSlide);
        rlBottomBar.addView(btnResetSlide, rlMainParams);

        rlMainParams = new RelativeLayout.LayoutParams(60, 50);
        rlMainParams.width = LayoutParams.WRAP_CONTENT;
        rlMainParams.height = LayoutParams.WRAP_CONTENT;
        rlMainParams.topMargin = 0;
        rlMainParams.leftMargin = scrWidth - 150;
        rlBottomBar.removeView(btnLockInPlace);
        rlBottomBar.addView(btnLockInPlace, rlMainParams);
        
        ivSlides.setScaleType(ScaleType.FIT_XY);
        
        flTimeline.bringToFront();
	}
	public void growVideo(){
		if(sdTimeline.isOpened()){
			sdTimeline.animateClose();
		}
		if(vidView.isPlaying()){
			videoIsLarge = true;
			LayoutParams vidParams = vidView.getLayoutParams();
			vidParams.width = (scrWidth * 3) / 4;
			vidParams.height = scrHeight;
			vidView.setLayoutParams(vidParams);
			vidView.getHolder().setFixedSize(vidParams.width, vidParams.height);
			vidView.requestLayout();
			vidView.invalidate();

			vidParams = vidViewLayer.getLayoutParams();
			vidParams.width = (scrWidth * 3) / 4;
			vidParams.height = scrHeight;
			vidViewLayer.setLayoutParams(vidParams);
			
	        rlMainParams = new RelativeLayout.LayoutParams((scrWidth * 3) / 4, 70);
	        rlMc.setGravity(Gravity.CENTER);
	        rlMainParams.topMargin = scrHeight - 60;
	        rlMainParams.leftMargin = 0;
	        rlMain.removeView(rlMc);
	        rlMain.addView(rlMc, rlMainParams);

	        rlMc.bringToFront();
			vidView.bringToFront();
			vidViewLayer.bringToFront();
	        btnFullScreen.bringToFront();
			flTimeline.bringToFront();
		}
	}
	public void shrinkVideo(){
		if(sdTimeline.isOpened()){
			sdTimeline.animateClose();
		}
		videoIsLarge = false;
		LayoutParams vidParams = vidView.getLayoutParams();
		vidParams.width = (scrHeight * 5) / 6;
		vidParams.height = (scrHeight * 15) / 24;
		vidView.setLayoutParams(vidParams);
		vidView.getHolder().setFixedSize(vidParams.width, vidParams.height);
		vidView.requestLayout();
		vidView.invalidate();
		vidView.bringToFront();

		vidParams = vidViewLayer.getLayoutParams();
		vidParams.width = ((scrHeight * 3) / 4) + 5;
		vidParams.height = (scrHeight * 3) / 4;
		vidViewLayer.setLayoutParams(vidParams);

        rlMainParams = new RelativeLayout.LayoutParams((scrHeight * 5) / 6, 70);
        rlMainParams.topMargin = (scrHeight * 15) / 24;;
        rlMainParams.leftMargin = 0;
        rlMc.setGravity(Gravity.CENTER_VERTICAL);
        rlMain.removeView(rlMc);
        rlMain.addView(rlMc, rlMainParams);

        rlMc.bringToFront();
        btnFullScreen.bringToFront();
		flTimeline.bringToFront();
	}
	private void dumpEvent(MotionEvent event) {
		   String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
		      "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
		   StringBuilder sb = new StringBuilder();
		   int action = event.getAction();
		   int actionCode = action & MotionEvent.ACTION_MASK;
		   sb.append("event ACTION_" ).append(names[actionCode]);
		   if (actionCode == MotionEvent.ACTION_POINTER_DOWN
		         || actionCode == MotionEvent.ACTION_POINTER_UP) {
		      sb.append("(pid " ).append(
		      action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
		      sb.append(")" );
		   }
		   sb.append("[" );
		   for (int i = 0; i < event.getPointerCount(); i++) {
		      sb.append("#" ).append(i);
		      sb.append("(pid " ).append(event.getPointerId(i));
		      sb.append(")=" ).append((int) event.getX(i));
		      sb.append("," ).append((int) event.getY(i));
		      if (i + 1 < event.getPointerCount())
		         sb.append(";" );
		   }
		   sb.append("]" );
		   Log.d("TOUCH_EVENT", sb.toString());
		}
   /** Determine the space between the first two fingers */
   private float spacing(MotionEvent event) {
      float x = event.getX(0) - event.getX(1);
      float y = event.getY(0) - event.getY(1);
      return FloatMath.sqrt(x * x + y * y);
   }
   /** Calculate the mid point of the first two fingers */
   private void midPoint(PointF point, MotionEvent event) {
      float x = event.getX(0) + event.getX(1);
      float y = event.getY(0) + event.getY(1);
      point.set(x / 2, y / 2);
   }
   private void correctSlideMatrix(ScaleType scaleType){
		if(slideIsLarge){
			defaultMatrix.postScale(0, 0, ((scrWidth * 2) / 3) / 2, scrHeight / 2);
		}
		else{
			defaultMatrix.postScale(0, 0, (scrWidth - (scrHeight * 5) / 6) / 2, (((scrHeight * 15) / 24) + 70) / 2);
		}
		ivSlides.setScaleType(scaleType);
		ivSlides.setImageMatrix(defaultMatrix);
		start = new PointF();
		mid = new PointF();
		currMatrix = new Matrix(); 
		prevMatrix = new Matrix();
		defaultMatrix = new Matrix();
   }
	@Override
	public boolean onTouch(View v, MotionEvent event) {
//		dumpEvent(event);
		if(v.getId() == SLIDE){
			ImageView view = (ImageView) v;
			//view.setScaleType(ImageView.ScaleType.MATRIX);
			
			switch(event.getAction() & MotionEvent.ACTION_MASK){
			case MotionEvent.ACTION_DOWN:{
				duration = System.currentTimeMillis();
		        //Log.e("DURATION", String.valueOf(duration));
				prevMatrix.set(currMatrix);
				start.set(event.getX(), event.getY());
				mode = DRAG;
		         //Log.e("TAG", "mode=DRAG");
				break;
			}
			case MotionEvent.ACTION_UP:{
//				if(!justNow){
					mode = NONE;
			        //Log.e("DURATION", String.valueOf(duration));
			        duration = System.currentTimeMillis() - duration;
			        //Log.e("DURATION", String.valueOf(duration));
			        if(duration < 1000){
			        	onClick(v);
			        	Log.e("DURATION", "ON CLICK");
			        }
			        else{
			        	onLongClick(v);
			        	Log.e("DURATION", "ON LONG CLICK");
			        }
					duration = 0;
//				}
//				else{
//					justNow = false;
//				}
				break;
			}
			case MotionEvent.ACTION_MOVE:{
				if(mode == DRAG){
					double distance = Math.sqrt((Math.pow(event.getX() - start.x, 2) + Math.pow(event.getY() - start.y, 2)));
					Log.e("DISTANCE", String.valueOf(distance));
					if(distance > 20){
						duration = System.currentTimeMillis();
					}
					currMatrix.set(prevMatrix);
					currMatrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
				}
				else if (mode == ZOOM) {
					float newDist = spacing(event);
					if (newDist > 10f) {
						duration = System.currentTimeMillis();
						currMatrix.set(prevMatrix);
						float scale = newDist / prevDist;
						currMatrix.postScale(scale, scale, mid.x, mid.y);
					}
		          }
			break;
			}
			case MotionEvent.ACTION_POINTER_DOWN:{
		         prevDist = spacing(event);
		         if (prevDist > 10f) {
		            prevMatrix.set(currMatrix);
		            midPoint(mid, event);
		            mode = ZOOM;
			         //Log.e("TAG", "mode=ZOOM");
		         }
				break;
			}
			case MotionEvent.ACTION_POINTER_UP:{
				mode = NONE;
				break;
			}
			default:
				break;
			}
			view.setImageMatrix(currMatrix);
			view.requestLayout();
			view.invalidate();
		}
		return true;
	}
	private void growInMainLayout(){
		rlSlide.setBackgroundResource(R.layout.no_stroke);
        rlMainParams = new RelativeLayout.LayoutParams(0, 0);
		rlMainParams.width = scrWidth;
		rlMainParams.height = scrHeight;
		rlMainParams.topMargin = 0;
		rlMainParams.leftMargin = 0;
		rlSlide.removeView(ivSlides);
		rlMain.addView(ivSlides, rlMainParams);
		ivSlides.setOnTouchListener(this);
		ivSlides.bringToFront();
		ivSlides.requestLayout();
		ivSlides.invalidate();
		rlBottomBar.bringToFront();
		rlBottomBar.requestLayout();
		rlBottomBar.invalidate();
		slideInMain = true;
		slideIsLarge = true;

		defaultMatrix = new Matrix();
		defaultMatrix.postScale(1, 1, scrWidth / 2, scrHeight / 2);
		ivSlides.setImageMatrix(defaultMatrix);
		ivSlides.setScaleType(ScaleType.MATRIX);
		start = new PointF();
		mid = new PointF();
		currMatrix = new Matrix(); 
		prevMatrix = new Matrix(); 
	}
	private void lockIntoPosition(){
		rlSlide.setBackgroundResource(R.layout.curved_corners);
        rlMainParams = new RelativeLayout.LayoutParams(0, 0);
		rlMainParams.width = (scrWidth * 2) / 3;
		rlMainParams.height = scrHeight;
		rlMainParams.topMargin = 0;
		rlMainParams.leftMargin = 0;
		rlMain.removeView(ivSlides);
		rlSlide.addView(ivSlides, rlMainParams);
		ivSlides.setOnTouchListener(null);
		ivSlides.requestLayout();
		ivSlides.invalidate();
		rlBottomBar.requestLayout();
		rlBottomBar.invalidate();
		slideInMain = false;
		slideIsLarge = false;

		start = new PointF();
		mid = new PointF();
		currMatrix = new Matrix(); 
		prevMatrix = new Matrix(); 
	}
	private String convertTime(long seconds){
		Log.w("SECONDS", String.valueOf(seconds));
		long minutes = (seconds / 60) % 60;
		long hours = seconds / 3600;
		String time = "";
		if(hours < 10){
			time = "0" + hours;
		}
		else{
			time = String.valueOf(hours);
		}
		if(minutes < 10){
			time += ":0" + String.valueOf(minutes); 
		}
		else{
			time += ":" + String.valueOf(minutes);
		}
		return time;
	}
}
