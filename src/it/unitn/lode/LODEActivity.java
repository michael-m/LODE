package it.unitn.lode;

import it.unitn.lode.contentprovider.BookmarksContentProvider;
import it.unitn.lode.data.LodeSaxDataParser;
import it.unitn.lode.data.TimedSlides;
import it.unitn.lode.data.db.BookmarksTable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
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
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
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
	OnItemClickListener, OnPreparedListener, OnLongClickListener, OnTouchListener, OnErrorListener, OnItemLongClickListener{
    /** Called when the activity is first created. */
	private static int scrWidth, scrHeight;
	private final int VIDEO = 0, PLAY = 1, FF = 2, RR = 3, SLIDER = 4, SLIDE = 5, TITLE = 6, FS = 7, VIDEO_LAYER = 8,
			ZOOMIN = 9, ZOOMOUT = 10, LOCK = 11, BOOKMARK = 12, INFO = 13;
	private RelativeLayout rlMain = null, rlMc = null, rlSlide = null, rlBottomBar = null, rlBookmarks = null;
	private RelativeLayout.LayoutParams rlMainParams = null;
	private TextView tvTitle = null, tvSlidePos = null, tvTime = null;
	public static VidView vidView = null;
	private ImageView ivSlides = null, vidViewLayer = null;
	private ImageButton btnF = null;
	private ImageButton btnR = null;
	private ImageButton btnPlay = null;
	private ImageButton btnFullScreen = null;
	private ImageButton btnZoomIn = null;
	private ImageButton btnZoomOut = null;
	private ImageButton btnLockInPlace = null;
	private ImageButton btnBookmark = null;
	private ImageButton btnInfo = null;
	private SeekBar sbSlider = null;
	private int playState = 0;
	public static int currPos = 0;
	private final int D_LOW = DisplayMetrics.DENSITY_LOW, D_MEDIUM = DisplayMetrics.DENSITY_MEDIUM, D_HIGH = DisplayMetrics.DENSITY_HIGH;
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
	//private RelativeLayout rlTimeline = null;
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
	private boolean isFromTimeline = false, slideIsLarge = false, videoIsLarge = false, isLocked = true,
			slideInMain = false, keepCounting = false;
	private int vidViewCurrPos;
	private Matrix defaultMatrix = null, currMatrix = null, prevMatrix = null;
	private int mode;
	private static final int ZOOM = 0, DRAG = 1, NONE = 2;
	private PointF start = null, mid = null;
	private float prevDist = 1f;
	private static long duration = 0;
	private boolean slidesReady = false, isMediumSize = false;
	private AlertDialog alertNetwork = null, alertWrongData = null, alertEndLesson = null;
	private DisplayMetrics metrics = null;

	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private SimpleCursorAdapter adapter;
	private Button btnAddBookmark = null;
	private String selectedId = "";
	private String selectedTime = "";
	private List<String> bookmarkIds = null;
	private List<String> bookmarkTimes = null;
	private ListView lvBookmarks = null;
	static class ViewHolder{
		TextView tvTime;
	}
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		lvBookmarks = (ListView) findViewById(R.id.lvBookmarks);
		
		rlBookmarks = new RelativeLayout(this);
		rlBookmarks.setBackgroundColor(android.R.color.transparent);
		rlBookmarks.setGravity(Gravity.CENTER);

		bookmarkIds = new ArrayList<String>();
		bookmarkTimes = new ArrayList<String>();
		btnAddBookmark = (Button) findViewById(R.id.btnAddBookmark);
		btnAddBookmark.setVisibility(View.GONE);
		btnAddBookmark.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), LODEBmCreatorEditorActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("lectureId", lectureDataUrl);
				bundle.putString("time", convertTime(TimeUnit.MILLISECONDS.toSeconds((vidView.getCurrentPosition()))));
				i.putExtras(bundle);
				startActivityForResult(i, ACTIVITY_CREATE);
			}
		});
		registerForContextMenu(lvBookmarks);
		lvBookmarks.setOnItemClickListener(this);
		lvBookmarks.setOnItemLongClickListener(this);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please make sure you have an active data connection.")
		       .setCancelable(false)
		       .setTitle("Lode4Android: No Internet Access")
		       .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                LODEActivity.this.finish();
		           }
		       })
		       .setNegativeButton("Settings", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
		           }
		       });
		alertNetwork = builder.create();
		alertNetwork.setOwnerActivity(this);

		builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you want to end this lecture and return to the previous screen?")
		       .setCancelable(true)
		       .setTitle("LODE4Android: End Lecture")
		       .setPositiveButton("End Lecture", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
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
		                LODEActivity.this.finish();
			           }
		       })
		       .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.dismiss();
		        	   LODEActivity.this.onClick(btnPlay);
		           }
		       });
		alertEndLesson = builder.create();
		alertEndLesson.setOwnerActivity(this);

		builder = new AlertDialog.Builder(this);
		builder.setMessage("Lecture data is incorrect.")
		       .setCancelable(false)
		       .setTitle("LODE4Android: Wrong Data")
		       .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   pbSlide.setVisibility(View.GONE);
		        	   ivSlides.setScaleType(ScaleType.CENTER);
		        	   ivSlides.setImageResource(android.R.drawable.stat_notify_error);
		        	   sdTimeline.setVisibility(View.GONE);
		        	   ivSlides.setOnClickListener(null);
		        	   ivSlides.setOnLongClickListener(null);
		        	   btnZoomIn.setEnabled(false);
		        	   btnLockInPlace.setEnabled(false);
		               dialog.dismiss();
		           }
		       })
		       .setNegativeButton("Go back", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   LODEActivity.this.finish();
		           }
		       });
		alertWrongData = builder.create();
		alertWrongData.setOwnerActivity(this);

		Bundle watchBundle = getIntent().getExtras();
        videoUrl = watchBundle.getString("videoUrl");
        lectureDataUrl = watchBundle.getString("lectureDataUrl");
        watchBundle = null;
        
		fillData();

		ASSETS = getAssets();
        tfApplegaramound = Typeface.createFromAsset(ASSETS, "fonts/Applegaramound.ttf");
        flTimeline = (FrameLayout) findViewById(R.id.flTimeline);
        //rlTimeline = (RelativeLayout) findViewById(R.id.rlTimeline);

        //rlTimeline.setBackgroundResource(R.layout.timeline);
        
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
					}
				}
			}
        };
        closestSlideUpdater = new Runnable(){
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						pbSlide.setVisibility(View.VISIBLE);
					}
				});
				while(!slidesReady){}
				if(timeAndSlide.get(closestTempo) != null){
					closestSlide = getSlide(timeAndSlide.get(closestTempo));
				}
				handler.post(new Runnable() {
					@Override
					public void run() {
						if(closestSlide != null){
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
					}
				});
			}
        };				

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        
        scrWidth = metrics.widthPixels;
        scrHeight = metrics.heightPixels;
//        Log.e("scrWidth:px", String.valueOf(scrWidth));
//        Log.e("scrHeight:px", String.valueOf(scrHeight));
//        Log.e("scrWidth:dp", String.valueOf(dp(scrWidth)));
//        Log.e("scrHeight:dp", String.valueOf(dp(scrHeight)));
//        Log.e("vidWidth",String.valueOf(dp(480 * 5 / 6)));
        if(metrics.densityDpi == D_LOW){
            Log.e("Density","LOW");
        }
        else if(metrics.densityDpi == D_MEDIUM){
            Log.e("Density","MEDIUM");
        }
        else if(metrics.densityDpi == D_HIGH){
            Log.e("Density","HIGH");
        }
        else{
            Log.e("Density","X-HIGH");
        }

        rlMain = (RelativeLayout) findViewById(R.id.rlMain);
        rlMain.setBackgroundColor(Color.LTGRAY);
        rlMain.setOnClickListener(this);
        
        rlMc = (RelativeLayout) findViewById(R.id.rlMc);
        rlMc.setBackgroundResource(R.drawable.a9p_09_11_00015);

        rlBottomBar = (RelativeLayout) findViewById(R.id.rlBottomBar);
        rlBottomBar.setBackgroundResource(android.R.drawable.bottom_bar);
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
				try{
			        tsParser = new LodeSaxDataParser(lectureDataUrl + "/TIMED_SLIDES.XML");
				}catch(RuntimeException e){
		            handler.post(new Runnable(){
						@Override
						public void run() {
							alertNetwork.show();
						}
					});
				}
				try{
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
					        }
				            tvSlidePos = new TextView(LodeActivityContext);
				        	tvSlidePos.setText("End");
				        	slidePos.add(tvSlidePos);
				        	lvTimeline.invalidateViews();
						}
					});
				}catch(RuntimeException e){
		            handler.post(new Runnable(){
						@Override
						public void run() {
							alertWrongData.show();
						}
					});
				}
			}
		};

		new Thread(listPopulator).start();
		slideChanger = new Runnable() {
			Iterator<String> titleIterator = slideTitles.iterator();
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						pbSlide.setVisibility(View.VISIBLE);
					}
				});
				if(timeAndSlide != null){
					if(timeAndSlide.containsKey(vidViewCurrPos)){
						singleSlide = getSlide(timeAndSlide.get(vidViewCurrPos));
						if(singleSlide == null){
//							Log.e("It's", "empty");
						}
					}
				}
				handler.post(new Runnable() {
				@Override
				public void run() {
					if(singleSlide != null){
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
        vidView.setOnCompletionListener(this);
        vidView.setOnPreparedListener(this);
        vidView.setOnErrorListener(this);
        
        vidViewLayer = new ImageView(this);
        vidViewLayer.setId(VIDEO_LAYER);
        vidViewLayer.setOnLongClickListener(this);
        vidViewLayer.setOnClickListener(this);
        vidViewLayer.setBackgroundColor(Color.TRANSPARENT);

        btnPlay = new ImageButton(this);
        btnF = new ImageButton(this);
        btnR = new ImageButton(this);
        btnFullScreen = new ImageButton(this);
        sbSlider = new SeekBar(this);
        pbVideo = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        pbVideo.setVisibility(View.GONE);
        pbSlide = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        
        btnZoomIn = new ImageButton(this);
        btnZoomIn.setId(ZOOMIN);
        btnZoomIn.setBackgroundResource(android.R.drawable.btn_plus);
        btnZoomIn.setEnabled(false);
        btnZoomIn.setOnClickListener(this);
        
        btnZoomOut = new ImageButton(this);
        btnZoomOut.setId(ZOOMOUT);
        btnZoomOut.setBackgroundResource(android.R.drawable.btn_minus);
        btnZoomOut.setEnabled(false);
        btnZoomOut.setOnClickListener(this);

        btnLockInPlace = new ImageButton(this);
        btnLockInPlace.setId(LOCK);
        btnLockInPlace.setBackgroundResource(R.drawable.lock_locked);
        btnLockInPlace.setOnClickListener(this);
        
        btnBookmark = new ImageButton(this);
        btnBookmark.setId(BOOKMARK);
        btnBookmark.setBackgroundResource(R.drawable.bookmark);
        btnBookmark.setOnClickListener(this);
        
        btnInfo = new ImageButton(this);
        btnInfo.setId(INFO);
        btnInfo.setBackgroundResource(android.R.drawable.ic_dialog_info);

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

        tvTime = new TextView(this);
        tvTime.setBackgroundResource(R.layout.no_stroke);
        tvTime.setTypeface(tfApplegaramound);
        tvTime.setTextColor(Color.BLACK);
        tvTime.setGravity(Gravity.LEFT);
        tvTime.setText("00:00");

        
        

        
        
        
//ADD VIEWS TO LAYOUTS
        
//        rlMainParams = new RelativeLayout.LayoutParams(convertToDp(scrHeight * 3 / 4), convertToDp(30));
//        rlMainParams.topMargin = 10;
//        rlMainParams.leftMargin = 0;
//        rlMain.addView(tvTitle, rlMainParams);
//
        if(metrics.densityDpi == D_MEDIUM){
            rlMainParams = new RelativeLayout.LayoutParams(dp((scrWidth * 2) / 3 - 75), dp((scrWidth * 2 - 75)/ 4));
            rlMainParams.topMargin = 0;
            rlMainParams.leftMargin = 0;
            rlMain.addView(vidView, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(dp((scrWidth * 2) / 3 - 75), dp((scrWidth * 2 - 75)/ 4));
            rlMainParams.topMargin = 0;
            rlMainParams.leftMargin = 0;
            rlMain.addView(vidViewLayer, rlMainParams);
            
            rlMainParams = new RelativeLayout.LayoutParams(25, 25);
            rlMainParams.topMargin = scrHeight * 3 / 8;
            rlMainParams.leftMargin = scrHeight * 3 / 8;
            rlMain.addView(pbVideo, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(dp(40), dp(40));
            rlMc.addView(btnPlay, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(dp(40), dp(40));
            rlMainParams.leftMargin = dp(40);
            rlMc.addView(btnR, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(dp(40), dp(40));
            rlMainParams.leftMargin = dp(80);
            rlMc.addView(btnF, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(dp((scrWidth * 2) / 3 - 260) , dp(40));
            rlMainParams.leftMargin = dp(40) + dp(80);
            rlMc.addView(sbSlider, rlMainParams);

            tvTime.setTextSize(10);
            tvTime.setPadding(5, 15, 0, 0);
            rlMainParams = new RelativeLayout.LayoutParams(dp(120) , dp(50));
            rlMainParams.leftMargin = dp(120) + dp((scrWidth * 2) / 3 - 260);
            rlMc.addView(tvTime, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(dp((scrWidth * 2) / 3 - 75), dp(50));
            rlMainParams.topMargin = dp((scrWidth * 2 - 75)/ 4);
            rlMainParams.leftMargin = 0;
            rlMain.removeView(rlMc);
            rlMain.addView(rlMc, rlMainParams);
            
            rlMc.bringToFront();
            rlMainParams = new RelativeLayout.LayoutParams(scrWidth, dp(50));
            rlMainParams.topMargin = scrHeight - dp(50);
            rlMainParams.leftMargin = 0;
            rlMain.removeView(rlBottomBar);
            rlMain.addView(rlBottomBar, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(0, 0);
            rlMainParams.width = LayoutParams.WRAP_CONTENT;
            rlMainParams.height = LayoutParams.WRAP_CONTENT;
            rlMainParams.topMargin = dp(5);
            rlMainParams.leftMargin = 0;
            rlBottomBar.addView(btnZoomOut, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(0, 0);
            rlMainParams.width = LayoutParams.WRAP_CONTENT;
            rlMainParams.height = LayoutParams.WRAP_CONTENT;
            rlMainParams.topMargin = dp(5);
            rlMainParams.leftMargin = dp(80);
            rlBottomBar.addView(btnZoomIn, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(dp(25), dp(25));
            rlMainParams.topMargin = dp(15);
            rlMainParams.leftMargin = dp(130);
            rlBottomBar.addView(btnLockInPlace, rlMainParams);
            
            
            
            
            
            
            
            
            rlMainParams = new RelativeLayout.LayoutParams(0, 0);
            rlMainParams.width = LayoutParams.WRAP_CONTENT;
            rlMainParams.height = LayoutParams.WRAP_CONTENT;
            rlMainParams.topMargin = 7;
            rlMainParams.leftMargin = 230;
            rlBottomBar.addView(btnBookmark, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(0, 0);
            rlMainParams.width = LayoutParams.WRAP_CONTENT;
            rlMainParams.height = LayoutParams.WRAP_CONTENT;
            rlMainParams.topMargin = 3;
            rlMainParams.leftMargin = scrWidth - 70;
            rlBottomBar.addView(btnInfo, rlMainParams);
            
            
            
            
            
            
            
            
            
            

            rlMainParams = new RelativeLayout.LayoutParams(dp(scrWidth) - dp((scrWidth * 2) / 3 - 75), dp((scrWidth * 2 - 75)/ 4));
            rlMainParams.topMargin = 0;
            rlMainParams.leftMargin = dp((scrWidth * 2) / 3 - 75);
            rlMain.removeView(rlSlide);
            rlMain.addView(rlSlide, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(dp(scrWidth) - dp((scrWidth * 2) / 3 - 75), dp((scrWidth * 2 - 75)/ 4));
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
        else{

            rlMainParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, scrHeight / 3);
            rlMain.removeView(lvBookmarks);
            rlBookmarks.addView(lvBookmarks, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            rlMainParams.topMargin = scrHeight / 3;
            rlMainParams.leftMargin = scrWidth / 6;
            rlMain.removeView(btnAddBookmark);
            rlBookmarks.addView(btnAddBookmark, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(scrWidth / 2, scrWidth / 2);
            rlMain.addView(rlBookmarks, rlMainParams);

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

            rlMainParams = new RelativeLayout.LayoutParams(scrHeight * 3 / 4 - 220 , 50);
            rlMainParams.topMargin = 0;
            rlMainParams.leftMargin = 155;
            rlMc.addView(sbSlider, rlMainParams);

            tvTime.setTextSize(12);
            tvTime.setPadding(0, 15, 0, 0);
            rlMainParams = new RelativeLayout.LayoutParams(110 , 50);
            rlMainParams.topMargin = 0;
            rlMainParams.leftMargin = ((scrHeight * 5) / 6) - 100;
            rlMc.addView(tvTime, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams((scrHeight * 5) / 6, 70);
            rlMainParams.topMargin = (scrHeight * 15) / 24;
            rlMainParams.leftMargin = 0;
            rlMc.setGravity(Gravity.CENTER_VERTICAL);
            rlMain.removeView(rlMc);
            rlMain.addView(rlMc, rlMainParams);
            
            rlMc.bringToFront();
            rlMainParams = new RelativeLayout.LayoutParams(scrWidth - 6, 80);
            rlMainParams.topMargin = scrHeight - 83;
            rlMainParams.leftMargin = 3;
            rlMain.removeView(rlBottomBar);
            rlMain.addView(rlBottomBar, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(85, 50);
            rlMainParams.topMargin = 10;
            rlMainParams.leftMargin = 0;
            rlBottomBar.addView(btnZoomOut, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(85, 50);
            rlMainParams.topMargin = 10;
            rlMainParams.leftMargin = 100;
            rlBottomBar.addView(btnZoomIn, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(40, 40);
            rlMainParams.topMargin = 12;
            rlMainParams.leftMargin = 170;
            rlBottomBar.addView(btnLockInPlace, rlMainParams);

            
            
            
            
            
            
            rlMainParams = new RelativeLayout.LayoutParams(55, 55);
            rlMainParams.topMargin = 7;
            rlMainParams.leftMargin = 230;
            rlBottomBar.addView(btnBookmark, rlMainParams);

            rlMainParams = new RelativeLayout.LayoutParams(60, 60);
            rlMainParams.topMargin = 3;
            rlMainParams.leftMargin = scrWidth - 70;
            rlBottomBar.addView(btnInfo, rlMainParams);

            
            
            
            
            
            
            
            
            
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
	}

	
	
	
	
	
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(resultCode == Activity.RESULT_OK){
			refreshIds();
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}
	private void fillData() {
		String[] projection = {BookmarksTable.COLUMN_ID, BookmarksTable.COLUMN_NOTE, BookmarksTable.COLUMN_TIME};
		String selection = BookmarksTable.COLUMN_LECTURE_ID + "=?";
		String[] selectionArgs = {lectureDataUrl};
		String sortOrder = BookmarksTable.COLUMN_TIME;
		Cursor cursor = getContentResolver().query(BookmarksContentProvider.CONTENT_URI, projection, selection, selectionArgs,
				sortOrder);
		if (cursor != null) {
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				bookmarkIds.add(cursor.getString(cursor.getColumnIndex(BookmarksTable.COLUMN_ID)));
				bookmarkTimes.add(cursor.getString(cursor.getColumnIndex(BookmarksTable.COLUMN_TIME)));
				cursor.moveToNext();
			}
			String[] from = new String[] {BookmarksTable.COLUMN_ID, BookmarksTable.COLUMN_NOTE, BookmarksTable.COLUMN_TIME};
			int[] to = new int[] {R.id.tvId, R.id.tvBookmarkNote, R.id.tvBookmarkTime};
			adapter = new SimpleCursorAdapter(this, R.layout.bookmark_row, cursor, from, to);
			lvBookmarks.setAdapter(adapter);
		}
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 1, "Delete bookmark");
		menu.add(0, ACTIVITY_EDIT, 0, "Edit bookmark");
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			Uri uri = Uri.parse(BookmarksContentProvider.CONTENT_URI + "/" + info.id);
			getContentResolver().delete(uri, null, null);
			fillData();
			return true;
		case ACTIVITY_EDIT:
			Intent i = new Intent(this, LODEBmCreatorEditorActivity.class);
			Uri bookmarkUri = Uri.parse(BookmarksContentProvider.CONTENT_URI + "/" + selectedId);
			Bundle bundle = new Bundle();
			bundle.putParcelable(BookmarksContentProvider.CONTENT_ITEM_TYPE, bookmarkUri);
			bundle.putString("lectureId", lectureDataUrl);
			bundle.putString("time", selectedTime);
			i.putExtras(bundle);
			startActivityForResult(i, ACTIVITY_EDIT);
			break;
		}
		return super.onContextItemSelected(item);
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		selectedId = bookmarkIds.get(position);
		selectedTime = bookmarkTimes.get(position);
		return false;
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
					if(slideTempo != null & slideTempo.size() > 0){
						vidViewCurrPos = slideTempo.get(0);
					}
					else{
						vidViewCurrPos = 0;
					}
			        slideChangerThread = new Thread(slideChanger);
			        slideChangerThread.start();
//					closestSetterThread = new Thread(closestSlideUpdater);
//					closestSetterThread.start();
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
			btnPlay.setImageResource(android.R.drawable.ic_media_play);
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
				pbVideo.bringToFront();
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
		else if(view.getId() == ZOOMIN){
			correctSlideMatrix(ScaleType.FIT_XY);
			btnZoomIn.setEnabled(false);
			btnZoomOut.setEnabled(true);
			if(!slideInMain){
				isLocked = true;
				btnLockInPlace.setBackgroundResource(R.drawable.lock_locked);
			}
		}
		else if(view.getId() == ZOOMOUT){
			defaultMatrix = new Matrix();
			defaultMatrix.postScale(1, 1, scrWidth / 2, scrHeight / 2);
			ivSlides.setImageMatrix(defaultMatrix);
			ivSlides.setScaleType(ScaleType.MATRIX);
			start = new PointF();
			mid = new PointF();
			currMatrix = new Matrix(); 
			prevMatrix = new Matrix(); 

			btnZoomIn.setEnabled(true);
			btnZoomOut.setEnabled(false);
			if(!slideInMain){
				isLocked = true;
				btnLockInPlace.setBackgroundResource(R.drawable.lock_locked);
			}
		}
		else if(view.getId() == LOCK){
			if(isLocked){
				btnLockInPlace.setBackgroundResource(R.drawable.lock_unlocked);
				isLocked = false;
				growInMainLayout();
			}
			else{
				btnLockInPlace.setBackgroundResource(R.drawable.lock_locked);
				isLocked = true;
				ivSlides.setOnTouchListener(null);
				lockIntoPosition();
				correctSlideMatrix(ScaleType.FIT_XY);
			}
		}
		else if(view.getId() == BOOKMARK){
			lvBookmarks.setVisibility(lvBookmarks.getVisibility() == View.VISIBLE?View.GONE:View.VISIBLE);
			btnAddBookmark.setVisibility(btnAddBookmark.getVisibility() == View.VISIBLE?View.GONE:View.VISIBLE);
			rlBookmarks.bringToFront();
			lvBookmarks.bringToFront();
			btnAddBookmark.bringToFront();
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		vidView.pause();
		playState = 0;
		btnPlay.setImageResource(android.R.drawable.ic_media_play);
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
		if(timeUpdaterThread != null){
			Log.e("onResume", "Starting Time Updater");
			dead = timeUpdaterThread;
			dead.interrupt();
			timeUpdaterThread = null;
			timeUpdaterThread = new Thread(timeUpdater);
			timeUpdaterThread.start();
		}
		else{
			timeUpdaterThread = new Thread(timeUpdater);
			timeUpdaterThread.start();
		}
		if(firstTime){
	        pbVideo.setVisibility(View.VISIBLE);
		}
		if(!firstTime & !hasFinished & fullScreen){
			playState = 1;
			btnPlay.setImageResource(android.R.drawable.ic_media_pause);
			vidView.seekTo(currPos);
			isResuming = true;
			isStarted = true;
			fullScreen = false;
			vidView.start();

			//new Thread(closestSlideUpdater).start();
			
			sliderThread = new Thread(sliderUpdater);
			sliderThread.start();
		}
		else{
			if(hasFinished){
				hasFinished = false;
			}
			firstTime = false;
			fullScreen = false;
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
		btnPlay.setImageResource(android.R.drawable.ic_media_play);
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
			vidView.seekTo(progress);
			isFromTimeline = false;
			isResuming = false;
		}
		vidViewCurrPos = progress / 1000;
		if(timeAndSlide != null){
			if(timeAndSlide.containsKey(vidViewCurrPos)){
				if(slideChangerThread != null){
					dead = slideChangerThread;
					slideChangerThread = null;
					dead.interrupt();
				}
		        slideChangerThread = new Thread(slideChanger);
		        slideChangerThread.start();
			}
			else{
				closestTempo = getClosestTempo(vidViewCurrPos);
				if(currentTempo != closestTempo){
					currentTempo = closestTempo;
					if(closestSetterThread != null){
						dead = closestSetterThread;
						closestSetterThread = null;
						dead.interrupt();
					}
					closestSetterThread = new Thread(closestSlideUpdater);
					closestSetterThread.start();
				}
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
		if(parent.getId() == R.id.lvTimeline){
			isFromTimeline = true;
			sbSlider.setProgress(slideTempo.get(position - 1) * 1000);
		}
		else if(parent.getId() == R.id.lvBookmarks){
			try{
				vidView.seekTo(calculateTime(bookmarkTimes.get(position)) * 1000);
				if(!vidView.isPlaying()){
					onClick(btnPlay);
				}
				onClick(btnBookmark);
			}catch(IndexOutOfBoundsException e){
				e.printStackTrace();
			}
		}
		parent.setSelection(position);
		if(sdTimeline.isOpened()){
			sdTimeline.animateClose();
		}
		
	}
	@Override
	protected void onStart() {
		super.onStart();
		flTimeline.bringToFront();
	}
	@Override
	public void onPrepared(MediaPlayer mp) {
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
			LODEActivity.this.onClick(btnPlay);
			alertEndLesson.show();
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
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					alertNetwork.show();
				}
			});
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
			}
			slidesReady = true;
		}
		
	}
	private int getClosestTempo(int tempo){
		int currentTempo;
		int closestTempo = Integer.MAX_VALUE;
		Iterator<Integer> closestIterator = slideTempo.iterator();
		while(closestIterator.hasNext()){
			currentTempo = closestIterator.next(); 
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
		return closestTempo;
	}
	@Override
	public boolean onLongClick(View v) {
		if(v.getId() == SLIDE){
			if(slideIsLarge){
				shrinkSlide();
				defaultMatrix.postScale(0, 0, (scrWidth - (scrHeight * 5) / 6) / 2, (((scrHeight * 15) / 24) + 70) / 2);
			}
			else{
				if(videoIsLarge){
					shrinkVideo();
				}
				growSlide();
				defaultMatrix.postScale(0, 0, ((scrWidth * 2) / 3) / 2, scrHeight / 2);
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
		isMediumSize = true;
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

        ivSlides.setScaleType(ScaleType.FIT_XY);
		flTimeline.bringToFront();
	}
	public void shrinkSlide(){
		ivSlides.setOnTouchListener(null);
		isMediumSize = false;
		if(sdTimeline.isOpened()){
			sdTimeline.animateClose();
		}
		slideIsLarge = false;

		if(metrics.densityDpi == D_MEDIUM){
	        rlMainParams = new RelativeLayout.LayoutParams(dp(scrWidth) - dp((scrWidth * 2) / 3 - 75), dp((scrWidth * 2 - 75)/ 4));
	        rlMainParams.topMargin = 0;
	        rlMainParams.leftMargin = dp((scrWidth * 2) / 3 - 75);
	        rlMain.removeView(rlSlide);
	        rlMain.addView(rlSlide, rlMainParams);

	        rlMainParams = new RelativeLayout.LayoutParams(dp(scrWidth) - dp((scrWidth * 2) / 3 - 75), dp((scrWidth * 2 - 75)/ 4));
	        rlMainParams.topMargin = 0;
	        rlMainParams.leftMargin = 0;
		}
		else{
			rlMainParams = new RelativeLayout.LayoutParams(scrWidth - (scrHeight * 5) / 6, ((scrHeight * 15) / 24) + 70);
	        rlMainParams.topMargin = 0;
	        rlMainParams.leftMargin = (scrHeight * 5) / 6;
			rlMain.removeView(rlSlide);
	        rlMain.addView(rlSlide, rlMainParams);

	        rlMainParams = new RelativeLayout.LayoutParams(scrWidth - (scrHeight * 5) / 6, scrHeight * 4 / 5);
	        rlMainParams.topMargin = 0;
	        rlMainParams.leftMargin = 0;
		}
        if(slideInMain){
    		rlMain.removeView(ivSlides);
			btnLockInPlace.setBackgroundResource(R.drawable.lock_locked);
    		isLocked = true;
    		slideInMain = false;
        }
        else{
    		rlSlide.removeView(ivSlides);
        }
        rlSlide.addView(ivSlides, rlMainParams);
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
	        if(metrics.densityDpi == D_MEDIUM){
	        	rlMc.setPadding(0, dp(13), 0, 0);
	        }
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
        if(metrics.densityDpi == D_MEDIUM){
    		LayoutParams vidParams = vidView.getLayoutParams();
    		vidParams.width = dp((scrWidth * 2) / 3 - 75);
    		vidParams.height = dp((scrWidth * 2 - 75)/ 4);
    		vidView.setLayoutParams(vidParams);
    		vidView.getHolder().setFixedSize(vidParams.width, vidParams.height);
    		vidView.requestLayout();
    		vidView.invalidate();
    		vidView.bringToFront();

    		vidParams = vidViewLayer.getLayoutParams();
    		vidParams.width = dp((scrWidth * 2) / 3 - 75);
    		vidParams.height = dp((scrWidth * 2 - 75)/ 4);
    		vidViewLayer.setLayoutParams(vidParams);

            rlMainParams = new RelativeLayout.LayoutParams(dp((scrWidth * 2) / 3 - 75), dp(50));
            rlMainParams.topMargin = dp((scrWidth * 2 - 75)/ 4);
            rlMainParams.leftMargin = 0;
        	rlMc.setPadding(0, dp(5), 0, 0);
        }
        else{
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
        }
        rlMain.removeView(rlMc);
        rlMain.addView(rlMc, rlMainParams);

        rlMc.bringToFront();
        btnFullScreen.bringToFront();
		flTimeline.bringToFront();
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
		if(v.getId() == SLIDE){
			ImageView view = (ImageView) v;
			switch(event.getAction() & MotionEvent.ACTION_MASK){
			case MotionEvent.ACTION_DOWN:{
				duration = System.currentTimeMillis();
				prevMatrix.set(currMatrix);
				start.set(event.getX(), event.getY());
				mode = DRAG;
				break;
			}
			case MotionEvent.ACTION_UP:{
					mode = NONE;
			        duration = System.currentTimeMillis() - duration;
			        if(duration < 1000){
			        	onClick(v);
			        	Log.e("DURATION", "ON CLICK");
			        }
			        else{
			        	onLongClick(v);
			        	Log.e("DURATION", "ON LONG CLICK");
			        }
					duration = 0;
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
		if(metrics.densityDpi == D_MEDIUM){
	        rlMainParams = new RelativeLayout.LayoutParams(0, 0);
			rlMainParams.width = dp(scrWidth);
			rlMainParams.height = dp(scrHeight);
			rlMainParams.topMargin = 0;
			rlMainParams.leftMargin = 0;
			rlSlide.removeView(ivSlides);
			rlMain.addView(ivSlides, rlMainParams);

			defaultMatrix = new Matrix();
			defaultMatrix.postScale(1, 1, 0, 0);
		}
		else{
	        rlMainParams = new RelativeLayout.LayoutParams(0, 0);
			rlMainParams.width = scrWidth;
			rlMainParams.height = scrHeight;
			rlMainParams.topMargin = 0;
			rlMainParams.leftMargin = 0;
			rlSlide.removeView(ivSlides);
			rlMain.addView(ivSlides, rlMainParams);

			defaultMatrix = new Matrix();
			defaultMatrix.postScale(1, 1, scrWidth / 2, scrHeight / 2);
		}
		ivSlides.setOnTouchListener(this);
		ivSlides.bringToFront();
		ivSlides.requestLayout();
		ivSlides.invalidate();
		rlBottomBar.bringToFront();
		rlBottomBar.requestLayout();
		rlBottomBar.invalidate();
		slideInMain = true;
		slideIsLarge = true;
		btnZoomIn.setEnabled(true);
		ivSlides.setImageMatrix(defaultMatrix);
		ivSlides.setScaleType(ScaleType.MATRIX);
		start = new PointF();
		mid = new PointF();
		currMatrix = new Matrix(); 
		prevMatrix = new Matrix(); 
	}
	private void lockIntoPosition(){
		btnZoomIn.setEnabled(false);
		btnZoomOut.setEnabled(false);
		rlSlide.setBackgroundResource(R.layout.curved_corners);
        rlMainParams = new RelativeLayout.LayoutParams(0, 0);
		rlMainParams.width = (scrWidth * 2) / 3;
		rlMainParams.height = scrHeight;
		rlMainParams.topMargin = 0;
		rlMainParams.leftMargin = 0;
		rlMain.removeView(ivSlides);
		rlSlide.addView(ivSlides, rlMainParams);
		rlSlide.bringToFront();
		rlSlide.requestLayout();
		rlSlide.invalidate();
		flTimeline.bringToFront();
		slideInMain = false;
		slideIsLarge = false;
		if(isMediumSize){
			onLongClick(ivSlides);
		}
	}
	private String convertTime(long seconds){
		long minutes = (seconds / 60) % 60;
		long hours = seconds / 3600;
		seconds = seconds % 60;
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
		
		if(seconds < 10){
			time += ":0" + String.valueOf(seconds); 
		}
		else{
			time += ":" + String.valueOf(seconds);
		}
		return time;
	}
	private int calculateTime(String time){
		String[] hHmMsS = time.split(":");
		int seconds = Integer.parseInt(hHmMsS[0]) * 3600;
		seconds += Integer.parseInt(hHmMsS[1]) * 60;
		seconds += Integer.parseInt(hHmMsS[2]);
		Log.e("time", String.valueOf(TimeUnit.SECONDS.toMillis(seconds)));
		return seconds;
	}
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		//NEED TO RELEASE MEDIA PLAYER AND INSTANTIATE A NEW ONE.
		alertNetwork.show();
		return true;
	}
	public int dp(int pixels){
		//return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels , getResources().getDisplayMetrics());
		return (int) (pixels / getResources().getDisplayMetrics().density + 0.5);
	}
	private void refreshIds(){
		String[] projection = {BookmarksTable.COLUMN_ID};
		Cursor cursor = getContentResolver().query(BookmarksContentProvider.CONTENT_URI, projection, null, null, null);
		if (cursor != null) {
			bookmarkIds.removeAll(bookmarkIds);
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				bookmarkIds.add(cursor.getString(cursor.getColumnIndex(BookmarksTable.COLUMN_ID)));
				cursor.moveToNext();
			}
		}
	}
}
