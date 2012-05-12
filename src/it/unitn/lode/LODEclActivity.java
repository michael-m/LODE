package it.unitn.lode;


import it.unitn.lode.data.Courses;
import it.unitn.lode.data.Lectures;
import it.unitn.lode.data.LodeSaxDataParser;
import it.unitn.lode.data.TimedSlides;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LODEclActivity extends Activity implements OnItemClickListener, OnClickListener{
	private LodeSaxDataParser lecturesParser = null;
	private static ListView lvCourses = null;
	private ListView lvLectures = null;
	private ArrayList<CheckedTextView> courses = null;
	private ArrayList<CheckedTextView> lectures = null;
	private Map<Integer, Boolean> cCheckedStates = null;
	private Map<Integer, Boolean> lCheckedStates = null;
	private RelativeLayout rlCL = null;
	private RelativeLayout.LayoutParams rlCLParams = null;
	private LodeSaxDataParser coursesParser = null;
	private static Runnable coursesPopulator = null;
	private Runnable lecturesPopulator = null;
	private List<Courses> cs = null;
	private List<Lectures> ls = null;
	private Map<Integer, List<Lectures>> cl = null;
	private Iterator<Courses> csIterator = null;
	private Iterator<Lectures> lsIterator = null;
	private static Handler handler = null;
	private final Context coursesContext = this;
	private CheckedTextView tvCourse = null;
	private Courses selectedCourse = null;
	private CheckedTextView tvItem = null;
	private final int LV_COURSES = 100;
	private final int LV_LECTURES = 101;
	private ImageButton btnWatch = null;
	private ImageButton btnDownload = null;
	private static String BASE_URL;
	public static Context CL_CONTEXT;
	private AlertDialog alertNetwork = null, alertExit = null, alertWrongData = null;
	private boolean comingFromSettings = false;
	private ProgressBar pbCL = null;
	private static DisplayMetrics metrics = null;
	public static int scrWidth, scrHeight;
	private RelativeLayout rlLectureInfo = null;
	private RelativeLayout rlLIContainer = null;
	private RelativeLayout rlButtons = null;
	private TextView tvLectureInfo = null;
	private RelativeLayout.LayoutParams rlLIParams = null;
	private Typeface tfApplegaramound = null;
	private final int WATCH = 0, DOWNLOAD = 1, INFO = 2;
	private LectureDownloader lectureDownloader = null;
	private ProgressDialog dProgressDialog = null;
	private String lectureDir = "";
	private LodeSaxDataParser tsParser = null;
	private List<TimedSlides> ts = null;
	private boolean downloadCancelled = false;
	private final String SD_CARD = Environment.getExternalStorageDirectory().toString();
	private boolean urlGoAhead = true, connectedJustNow = false;
	private static boolean isConnCheckerSleeping = false;
	private String videoUrl;
	private int SCR_LAYOUT;
	private final int SCR_MASK = Configuration.SCREENLAYOUT_SIZE_MASK;
	private final int SCR_NORMAL = Configuration.SCREENLAYOUT_SIZE_NORMAL;
	private final int MEDIUM_DENSITY_PHONE = 999;
	private final int MEDIUM_DENSITY_TABLET = 888;
	private final int HIGH_DENSITY_PHONE = 777;
	private final int HIGH_DENSITY_TABLET = 666;
	private int THIS_DEVICE;
	private Thread bgConnectionChecker = null;
	private SharedPreferences lodeSettings = null;
	private final String LODE_PREFS = "LODE_PREFS";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.courses_lectures);
        
        metrics = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metrics);
	    SCR_LAYOUT = getResources().getConfiguration().screenLayout;
        THIS_DEVICE = getDeviceCategory();
        scrWidth = metrics.widthPixels;
        scrHeight = metrics.heightPixels;
/******* LECTURE INFO AND OPTIONS LAYOUT ******/
        // instantiate it within the onCreate method
        dProgressDialog = new ProgressDialog(LODEclActivity.this);
        dProgressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Pause", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadCancelled = false;
				lectureDownloader.cancel(true);
		        makeToast("Download has been paused.");
			}
		});
        dProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadCancelled = true;
				lectureDownloader.cancel(true);
		        makeToast("Download has been cancelled.");
			}
		});
        dProgressDialog.setCancelable(false);
        dProgressDialog.setCanceledOnTouchOutside(false);
        dProgressDialog.setMessage("Downloading lecture");
        dProgressDialog.setIndeterminate(false);
        dProgressDialog.setMax(100);
        dProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        
        rlLIContainer = new RelativeLayout(this);
        rlLIContainer.setBackgroundColor(Color.TRANSPARENT);
        rlLIContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        rlLIContainer.setVisibility(View.GONE);

        rlButtons = new RelativeLayout(this);
        rlLectureInfo = new RelativeLayout(this);
        rlLectureInfo.setBackgroundResource(R.layout.lecture_info);
        btnWatch = new ImageButton(this);
        btnDownload = new ImageButton(this);
        tvLectureInfo = new TextView(this);
        
        btnWatch.setImageResource(android.R.drawable.ic_media_play);
        btnWatch.setOnClickListener(this);
        btnWatch.setId(WATCH);
        btnDownload.setImageResource(android.R.drawable.stat_sys_download);
        btnDownload.setOnClickListener(this);
        btnDownload.setId(DOWNLOAD);
        tvLectureInfo.setId(INFO);
        tvLectureInfo.setTypeface(tfApplegaramound, Typeface.NORMAL);
        if(THIS_DEVICE == MEDIUM_DENSITY_TABLET || THIS_DEVICE == HIGH_DENSITY_TABLET){
            tvLectureInfo.setTextSize(17);
        }
        else if(THIS_DEVICE == HIGH_DENSITY_PHONE){
            tvLectureInfo.setTextSize(15);
        }
        else{
        	tvLectureInfo.setTextSize(13);
        }
        tvLectureInfo.setTextColor(Color.BLACK);
        tvLectureInfo.setGravity(Gravity.CENTER_HORIZONTAL);

        rlLectureInfo.setGravity(Gravity.CENTER_HORIZONTAL);
        rlLectureInfo.setPadding(10, 10, 10, 10);
        
        rlLIParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 150);
        rlLectureInfo.addView(tvLectureInfo, rlLIParams);
        
        rlButtons.setPadding(0, 15, 10, 0);
        
        rlLIParams = new RelativeLayout.LayoutParams(50, 50);
        rlLIParams.leftMargin = scrWidth / 4 - 55;
        rlButtons.addView(btnWatch, rlLIParams);

        rlLIParams = new RelativeLayout.LayoutParams(50, 50);
        rlLIParams.leftMargin = scrWidth / 4 + 5;
        rlButtons.addView(btnDownload, rlLIParams);
        
        rlLIParams = new RelativeLayout.LayoutParams(scrWidth / 2, scrHeight / 2);
        rlLIContainer.addView(rlLectureInfo, rlLIParams);
        rlLIParams = new RelativeLayout.LayoutParams(scrWidth / 2, 80);
        rlLIParams.topMargin = scrHeight / 2 - 80;
        rlLIContainer.addView(rlButtons, rlLIParams);
/********** END ***********/      
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please make sure you have an active data connection.")
		       .setCancelable(false)
		       .setTitle("Lode4Android: No Internet Access")
		       .setPositiveButton("Ignore", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   if(!isConnCheckerSleeping){
		        		   bgConnectionChecker = new Thread(new BgConnectionChecker(System.currentTimeMillis()));
		        		   bgConnectionChecker.start();
			       			Log.e("alertNetwork", "bgConncetionChecker is sleeping");
		        	   }
		        	   else{
			       			Log.e("alertNetwork", "bgConncetionChecker is not sleeping");
		        	   }
		        	   dialog.dismiss();
		               pbCL.setVisibility(View.GONE);
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
		builder.setMessage("Do you want to leave LODE4Android?")
		       .setCancelable(true)
		       .setTitle("LODE4Android: Exit")
		       .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                LODEclActivity.this.finish();
		           }
		       })
		       .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id){
		        	   dialog.dismiss();
		           }
		       });
		alertExit = builder.create();
		alertExit.setOwnerActivity(this);

		builder = new AlertDialog.Builder(this);
		builder.setMessage("The course or lecture data is incorrect.")
		       .setCancelable(false)
		       .setTitle("LODE4Android: Error")
		       .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.dismiss();
		           }
		       });
		alertWrongData = builder.create();
		alertWrongData.setOwnerActivity(this);

		pbCL = (ProgressBar) findViewById(R.id.pbCL);
		pbCL.setVisibility(View.GONE);
		
		CL_CONTEXT = this;
        handler = new Handler();
        lvCourses = new ListView(this);
        lvCourses.setId(LV_COURSES);
        lvLectures = new ListView(this);
        lvLectures.setId(LV_LECTURES);
        
        rlCL = (RelativeLayout) findViewById(R.id.rlCL);

/*** ADD LECTURE INFO AND OPTIONS LAYOUT TO MAIN LAYOUT *****/
        rlLIParams = new RelativeLayout.LayoutParams(scrWidth / 2, scrHeight / 2);
        rlLIParams.topMargin = scrHeight/ 6 + 10;
        rlLIParams.leftMargin = scrWidth / 4 - 15;
        rlCL.addView(rlLIContainer, rlLIParams);
/****** END ******/
        courses = new ArrayList<CheckedTextView>();
        lectures = new ArrayList<CheckedTextView>();
		cCheckedStates = new TreeMap<Integer, Boolean>();
		lCheckedStates = new TreeMap<Integer, Boolean>();
        cl = new TreeMap<Integer, List<Lectures>>();
        tvCourse = new CheckedTextView(this);
        
        getPrefs();
        coursesPopulator = new Runnable() {
			@Override
			public void run() {
		    	File coursesMetaDataDir = new File(SD_CARD + "/lode");
		    	File coursesMetaData = new File(SD_CARD + "/lode/COURSES.XML");
		    	if(!coursesMetaData.exists()){
		    		try {
		    			coursesMetaDataDir.mkdirs();
						URL courseMetaDataUrl = new URL(BASE_URL + "COURSES.XML");
			            FileOutputStream output = null;
			            URLConnection connection = courseMetaDataUrl.openConnection();
			            connection.connect();
			            InputStream input = new BufferedInputStream(courseMetaDataUrl.openStream());
			            output = new FileOutputStream(coursesMetaData);
			            byte data[] = new byte[1024];
			            int count = 0;
			            while ((count = input.read(data)) != -1){
			            	output.write(data, 0, count);
			            }
			            output.flush();
			            output.close();
			            input.close();
						urlGoAhead = true;
					} catch (Exception e) {
			    		handler.post(new Runnable() {
							@Override
							public void run() {
					    		makeToast("Error downloading course data.");
							}
						});
						e.printStackTrace();
						urlGoAhead = false;
					}
		    	}

		    	try{
					coursesParser = new LodeSaxDataParser(new URL(BASE_URL + "COURSES.XML"));
				}catch(Exception e){
		            handler.post(new Runnable(){
						@Override
						public void run() {
							comingFromSettings = true;
							alertNetwork.show();
						}
					});
				}
				try{
			        cs = coursesParser.parseCourses();
		            csIterator = cs.iterator();
		            handler.post(new Runnable(){
						@Override
						public void run() {
							pbCL.bringToFront();
							String title;
				            tvCourse = new CheckedTextView(coursesContext);
				        	tvCourse.setText("\nAvailable Courses");
				        	courses.add(tvCourse);
					        while(csIterator.hasNext()){
					        	title = csIterator.next().getTitoloc().trim().replaceAll(" +", " ").replace("\n", "");
					        	title = title.trim();
					            tvCourse = new CheckedTextView(coursesContext);
					        	tvCourse.setText(title);
					        	courses.add(tvCourse);
					        }
					        for(int a = 0; a < 41; a ++){
					        	CheckedTextView ctv = new CheckedTextView(CL_CONTEXT);
					        	ctv.setText("ctv " + a);
					        	courses.add(ctv);
					        }
							for(int a = 0; a < courses.size(); a++){
								cCheckedStates.put(a, new Boolean(false));
							}
					        lvCourses.setAdapter(new CoursesAdapter(CL_CONTEXT, R.layout.courses, courses, metrics, cCheckedStates){
								@Override
								public boolean isEnabled(int position) {
									if(position == 0)
										return false;
									return true;
								}
							});
							pbCL.setVisibility(View.GONE);
					        lvCourses.invalidateViews();
						}
					});
				}catch(RuntimeException e1){
		            handler.post(new Runnable(){
						@Override
						public void run() {
							comingFromSettings = true;
							if(isConnected()){
								alertWrongData.show();
							}
							else{
								alertNetwork.show();
							}
						}
					});
				}
			}
		};
		pbCL.setVisibility(View.VISIBLE);
		new Thread(coursesPopulator).start();
        lvCourses.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvCourses.setCacheColorHint(Color.parseColor("#00000000"));
        lvCourses.setBackgroundResource(R.layout.courses_corners);
        lvCourses.setSelector(R.layout.courses_corners_clicked);
        lvCourses.setDividerHeight(2);
        lvCourses.setOnItemClickListener(this);
        
        lvLectures.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvLectures.setCacheColorHint(Color.parseColor("#00000000"));
        lvLectures.setBackgroundResource(R.layout.courses_corners);
        lvLectures.setSelector(R.layout.courses_corners_clicked);
        lvLectures.setDividerHeight(2);
        lvLectures.setOnItemClickListener(this);

/***** ADD VIEWS TO LAYOUTS ******/
        if(THIS_DEVICE == MEDIUM_DENSITY_PHONE){
            rlCLParams = new RelativeLayout.LayoutParams(dp(scrWidth / 3), dp(scrHeight));
            rlCL.addView(lvCourses, rlCLParams);
            
            rlCLParams = new RelativeLayout.LayoutParams(dp(scrWidth * 2 / 3 - 10), dp(scrHeight));
            rlCLParams.leftMargin = dp(scrWidth / 3 + 5);
            rlCL.addView(lvLectures, rlCLParams);
        }
        else{
            rlCLParams = new RelativeLayout.LayoutParams(scrWidth / 3, scrHeight);
            rlCL.addView(lvCourses, rlCLParams);
            
            rlCLParams = new RelativeLayout.LayoutParams(scrWidth * 2 / 3 - 10, scrHeight);
            rlCLParams.leftMargin = scrWidth / 3 + 5;
            rlCL.addView(lvLectures, rlCLParams);
        }
/************* END ******************/
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id){
//		parent.setSelection(position);
		final Integer POSITION = position;
		if(parent.getId() == LV_COURSES){
			for(int i = 0; i < courses.size(); i++){
				if(i == position){
					cCheckedStates.put(i, true);
				}
				else{
					cCheckedStates.put(i, false);
				}
			}
			CheckedTextView ctv = (CheckedTextView) view.findViewById(R.id.tvCourses);
			ctv.toggle();

			lvLectures.setEnabled(false);
			if(rlLIContainer.getVisibility() == View.VISIBLE){
				rlLIContainer.setVisibility(View.INVISIBLE);
			}
			tvItem = (CheckedTextView) parent.getItemAtPosition(position);
			
			Iterator<Courses> selectedIterator = cs.iterator();
			String title;
			while(selectedIterator.hasNext()){
				selectedCourse = selectedIterator.next();
				title = selectedCourse.getTitoloc().trim().replaceAll(" +", " ").replace("\n", "");
				title = title.trim();
				if(title.equals(tvItem.getText())){
					lecturesPopulator = new Runnable() {
						@Override
						public void run() {
							try{
								if(cl.get(POSITION) == null){
									String url = BASE_URL + selectedCourse.getFolderc() + "/LECTURES.XML";
									lecturesParser = new LodeSaxDataParser(new URL(url));
								}
							}catch(Exception e){
					            handler.post(new Runnable(){
									@Override
									public void run() {
										comingFromSettings = true;
										alertNetwork.show();
									}
								});
							}
							try{
								if(cl.get(POSITION) == null){
									ls = lecturesParser.parseLectures();
									cl.put(POSITION, ls);
								}
								lsIterator = cl.get(POSITION).iterator();
								handler.post(new Runnable() {
									@Override
									public void run() {
										pbCL.setVisibility(View.VISIBLE);
										pbCL.bringToFront();
										lectures.removeAll(lectures);
							            tvItem = new CheckedTextView(coursesContext);
							            String courseDetails = "Professor in charge: " + selectedCourse.getDocentec() + "\n";
							            courseDetails+= "Academic Year: " + selectedCourse.getYear();
							        	tvItem.setText(courseDetails);
							        	lectures.add(tvItem);
										
								        while(lsIterator.hasNext()){
								        	Lectures nextLs = lsIterator.next();
								        	String lTitle = nextLs.getTitolol().trim().replaceAll(" +", " ").replace("\n", "");
								        	lTitle = lTitle.trim();
								            tvItem = new CheckedTextView(coursesContext);
								        	tvItem.setText(lTitle);
								        	tvItem.setTag(R.id.tvLectureDataUrl, BASE_URL + nextLs.getFolderl());
								        	tvItem.setTag(R.id.tvVideoUrl, nextLs.getUrllez());
								        	tvItem.setTag(R.id.tvLectureDir, nextLs.getFolderl());
								        	tvItem.setTag(R.id.tvLectureInfoConcat,
								        			"\nTopic: "	+ nextLs.getTitolol()
								        			+ "\nDate: " + nextLs.getDatel()
								        			+ "\nLecturer: " + nextLs.getDocentel());
								        	tvItem.setTag(R.id.tvIdForBookmark, nextLs.getFolderl());
								        	lectures.add(tvItem);
								        }
								        for(int a = 0; a < 41; a ++){
								        	CheckedTextView ctv = new CheckedTextView(CL_CONTEXT);
								        	ctv.setText("ltv " + a);
								        	courses.add(ctv);
								        }
										for(int a = 0; a < lectures.size(); a++){
											lCheckedStates.put(a, new Boolean(false));
										}
								        lvLectures.setAdapter(new LecturesAdapter(CL_CONTEXT, R.layout.lectures, lectures, metrics, lCheckedStates){
											@Override
											public boolean isEnabled(int position) {
												if(position == 0)
													return false;
												return true;
											}
										});
										pbCL.setVisibility(View.GONE);
										lvLectures.setEnabled(true);
								        lvLectures.invalidateViews();
									}
								});
							}catch(RuntimeException e1){
					            handler.post(new Runnable(){
									@Override
									public void run() {
										comingFromSettings = true;
										if(isConnected()){
											alertWrongData.show();
										}
										else{
											alertNetwork.show();
										}
									}
								});
							}
						}
					};
					pbCL.setVisibility(View.VISIBLE);
					new Thread(lecturesPopulator).start();
					break;
				}
			}
		}
		else if(parent.getId() == LV_LECTURES){
			for(int i = 0; i < lectures.size(); i++){
				if(i == position){
					lCheckedStates.put(i, true);
				}
				else{
					lCheckedStates.put(i, false);
				}
			}
			CheckedTextView ctv = (CheckedTextView) view.findViewById(R.id.tvCourses);
			ctv.toggle();

			TextView tvLecture = (TextView) parent.getItemAtPosition(position);
			tvLectureInfo.setText((String) tvLecture.getTag(R.id.tvLectureInfoConcat));
			btnWatch.setTag(R.id.videoUrl, (String) tvLecture.getTag(R.id.tvVideoUrl));
			btnWatch.setTag(R.id.lectureDataUrl, (String) tvLecture.getTag(R.id.tvLectureDataUrl));
			btnWatch.setTag(R.id.IdForBookmark, (String) tvLecture.getTag(R.id.tvIdForBookmark));
			btnDownload.setTag(R.id.lectureDir, (String) tvLecture.getTag(R.id.tvLectureDir));
			lectureDir = (String) tvLecture.getTag(R.id.tvIdForBookmark);

			boolean[] availability = checkStorageAvailability();
			if(availability[0]){
				if(availability[1]){
					btnDownload.setEnabled(true);
				}
				else{
					btnDownload.setEnabled(false);
					makeToast("External Storage is write-protected.");
				}
			}
			else{
				btnDownload.setEnabled(false);
				makeToast("External storage is not present.");
			}
			rlLIContainer.setVisibility(View.VISIBLE);
			rlLIContainer.bringToFront();
			lvCourses.setEnabled(false);
			lvLectures.setEnabled(false);
		}
	}
	@Override
	public void onBackPressed() {
		if(rlLIContainer.getVisibility() == View.VISIBLE){
			rlLIContainer.setVisibility(View.GONE);
			lvCourses.setEnabled(true);
			lvLectures.setEnabled(true);
		}
		else{
			alertExit.show();
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		getPrefs();
		
//		checkedStates = new ArrayList<Boolean>();
//		Iterator<CheckedTextView> iter = courses.iterator();
//		Log.w("size", String.valueOf(courses.size()));
//		while(iter.hasNext()){
//			checkedStates.add(new Boolean(iter.next().isChecked()));
//		}
//        lvCourses.setAdapter(new CoursesAdapter(this, R.layout.courses, courses, metrics, checkedStates){
//			@Override
//			public boolean isEnabled(int position) {
//				if(position == 0)
//					return false;
//				return true;
//			}
//		});
//        lvLectures.setAdapter(new LecturesAdapter(this, R.layout.lectures, lectures, metrics){
//			@Override
//			public boolean isEnabled(int position) {
//				if(position == 0)
//					return false;
//				return true;
//			}
//		});

//        CheckedTextView tv = new CheckedTextView(this);
//        tv.setText("test");
//        for(int i = 0; i < 40; i++){
//        	courses.add(tv);
//        }
//		if(comingFromSettings){
//	        courses = new ArrayList<TextView>();
//			comingFromSettings = false;
//			new Thread(coursesPopulator).start();
//		}
//		if(connectedJustNow){
//			connectedJustNow = false;
//			Log.e("bgConncetionChecker", "null");
//		}
	}
	private boolean isConnected() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    return cm.getActiveNetworkInfo() == null ? false : cm.getActiveNetworkInfo().isAvailable();
	}
	public int dp(int pixels){
		//return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels , getResources().getDisplayMetrics());
		return (int) (pixels / getResources().getDisplayMetrics().density + 0.5);
	}
	@Override
	public void onClick(View v) {
		if(v.getId() == WATCH){
			String videoUrl = (String) btnWatch.getTag(R.id.videoUrl);
			String lectureDataUrl = (String) btnWatch.getTag(R.id.lectureDataUrl);
			String idForBookmark = (String) btnWatch.getTag(R.id.IdForBookmark); 
			Bundle bundle = new Bundle();
			bundle.putString("videoUrl",videoUrl);
			bundle.putString("lectureDataUrl", lectureDataUrl);
			bundle.putString("idForBookmark", idForBookmark);
			bundle.putString("LectureInfo", tvLectureInfo.getText().toString());
			bundle.putBoolean("isLocal", false);
			Intent intent = new Intent(this, LODEActivity.class);
			rlLIContainer.setVisibility(View.GONE);
			lvCourses.setEnabled(true);
			lvLectures.setEnabled(true);
			intent.putExtras(bundle);
			startActivity(intent);
		}
		else if(v.getId() == DOWNLOAD){
			videoUrl = (String) btnWatch.getTag(R.id.videoUrl);
			lectureDownloader = new LectureDownloader();
			lectureDownloader.execute(BASE_URL + lectureDir);
			rlLIContainer.setVisibility(View.GONE);
			lvCourses.setEnabled(true);
			lvLectures.setEnabled(true);
		}
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		courses.removeAll(courses);
		courses = new ArrayList<CheckedTextView>();
		super.onRestoreInstanceState(savedInstanceState);
	}
	private class LectureDownloader extends AsyncTask<String, Integer, String>{
		String status = "Determining download size ...";
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	    	handler.post(new Runnable() {
				@Override
				public void run() {
					dProgressDialog.setMessage(status);
					dProgressDialog.setProgress(0);
					dProgressDialog.setSecondaryProgress(0);
				}
			});
	        dProgressDialog.show();
	    }
	    @Override
	    protected String doInBackground(String... sUrl){
	    	if(urlGoAhead){
	    		File lectureDirectory = null;
	    		List<URL> urls = new ArrayList<URL>();
		    	File downloadCheck = new File(SD_CARD + "/lode/"  + lectureDir + "/lecture.downloaded");
		    	if(downloadCheck.exists()){
		    		handler.post(new Runnable() {
						@Override
						public void run() {
				    		makeToast("Lecture has already been downloaded.");
						}
					});
		    	}
		    	else{
			        try {
			            // create a File object for the parent directory
			            lectureDirectory = new File(SD_CARD + "/lode/"  + lectureDir + "/");
			            File slidesDirectory = new File(SD_CARD	+ "/lode/"  + lectureDir + "/img");
		            	urls.add(new URL(sUrl[0] + "/LECTURE.XML"));
		            	urls.add(new URL(sUrl[0] + "/TIMED_SLIDES.XML"));
		            	int start = videoUrl.lastIndexOf("/") + 1;
		            	String videoName = videoUrl.substring(start);
				        tsParser = new LodeSaxDataParser(new URL(sUrl[0] + "/TIMED_SLIDES.XML"));
						try{
							ts = new ArrayList<TimedSlides>();
					        ts = tsParser.parseSlides();
					        Iterator<TimedSlides> tsIterator = ts.iterator();
					        while(tsIterator.hasNext()){
					        	String imageUrl = tsIterator.next().getImmagine();
					        	urls.add(new URL(sUrl[0] +"/" + imageUrl));
					        }
						}catch(RuntimeException e){
				            handler.post(new Runnable(){
								@Override
								public void run() {
									alertWrongData.show();
								}
							});
						}
			            // have the object build the directory structure, if needed.
			            lectureDirectory.mkdirs();
			            slidesDirectory.mkdirs();

			            Iterator<URL> urlIterator = urls.iterator();
			            File outputFile = null;
			            FileOutputStream output = null;
			            int fileLength = 0, lectureLength = 0;
			            long total = 0, lectureTotal = 0;
						while(urlIterator.hasNext()){
							if(!isCancelled()){
								URL nextUrl = urlIterator.next();
					            URLConnection connection = nextUrl.openConnection();
					            connection.connect();
//					            // this will be useful so that you can show a typical 0-100% progress bar
					            lectureLength += connection.getContentLength();
							}
							else{
				            	break;
							}
						}
						if(!isCancelled()){
							URLConnection connection = (new URL(videoUrl)).openConnection();
							connection.connect();
							lectureLength += connection.getContentLength();
						}
						urlIterator = urls.iterator();
						while(urlIterator.hasNext()){
							if(!isCancelled()){
								URL nextUrl = urlIterator.next();
					            URLConnection connection = nextUrl.openConnection();
					            connection.connect();
//					            // this will be useful so that you can show a typical 0-100% progress bar
					            fileLength = connection.getContentLength();
//					            // download the file
					            InputStream input = new BufferedInputStream(nextUrl.openStream());
					            String fileName = nextUrl.toString().replace(sUrl[0], "");
					            if(fileName.contains("/img")){
					            	fileName = fileName.replace("/img", "");
						            outputFile = new File(slidesDirectory, fileName);
					            }
					            else{
					            	outputFile = new File(lectureDirectory, fileName);
					            }
					            if(!outputFile.exists()){
						            status = "Downloading " + fileName.replace("/", "");
//						            // now attach the OutputStream to the file object, instead of a String representation
						            output = new FileOutputStream(outputFile);
						            byte data[] = new byte[1024];
						            total = 0;
						            int count;
						            while ((count = input.read(data)) != -1){
						            	total += count;
//						                // publishing the progress....
						            	output.write(data, 0, count);
						            	publishProgress((int) ((lectureTotal * 100) / lectureLength), (int) ((total * 100) / fileLength));
						            }
						            lectureTotal += total;
						            output.flush();
						            output.close();
						            input.close();
					            }
					            else{
						            status = fileName.replace("/", "") + " already exists.";
					            	total = outputFile.length();
					            	publishProgress((int) ((lectureTotal * 100) / lectureLength), (int) ((total * 100) / fileLength));
					            	lectureTotal += total;
					            }
				            	publishProgress((int) ((lectureTotal * 100) / lectureLength), (int) ((total * 100) / fileLength));
							}
							else{
								break;
							}
						}
						if(!isCancelled()){
							handler.post(new Runnable() {
								@Override
								public void run() {
									dProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setEnabled(false);
								}
							});
							URL vidUrl = new URL(videoUrl);
				            URLConnection connection = vidUrl.openConnection();
				            connection.connect();
//				            // this will be useful so that you can show a typical 0-100% progress bar
				            fileLength = connection.getContentLength();
//				            // download the file
				            InputStream input = new BufferedInputStream(vidUrl.openStream());
			            	outputFile = new File(lectureDirectory, videoName);
				            if(!outputFile.exists()){
					            status = "Downloading " + videoName;
//					            // now attach the OutputStream to the file object, instead of a String representation
					            output = new FileOutputStream(outputFile);
					            byte data[] = new byte[1024];
					            total = 0;
					            int count;
					            while ((count = input.read(data)) != -1){
					            	if(!isCancelled()){
						            	total += count;
						                // publishing the progress....
						            	output.write(data, 0, count);
						            	publishProgress((int) ((total * 100) / fileLength), (int) ((lectureTotal * 100) / lectureLength));
					            	}
					            	else{
					            		outputFile.delete();
					            		break;
					            	}
					            }
					            lectureTotal += total;
					            output.flush();
					            output.close();
					            input.close();
					            handler.post(new Runnable() {
									@Override
									public void run() {
										dProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setEnabled(true);
									}
								});
				            }
				            else{
					            status = videoName + " already exists.";
				            	total = outputFile.length();
				            	publishProgress((int) ((total * 100) / fileLength), (int) ((lectureTotal * 100) / lectureLength));
				            	lectureTotal += total;
				            }
			            	publishProgress((int) ((total * 100) / fileLength), (int) ((lectureTotal * 100) / lectureLength));
						}
						//Write FLAG to indicate lecture has been already been downloaded or there is no request for cancel.
			            if(!isCancelled()){
				            outputFile = new File(lectureDirectory, "lecture.downloaded");
				            output = new FileOutputStream(outputFile);
				            OutputStreamWriter downloadCheckWriter = new OutputStreamWriter(output);
				            downloadCheckWriter.write("Lecture has been downloaded successfully.");
				            downloadCheckWriter.flush();
				            downloadCheckWriter.close();
				            output.flush();
				            output.close();
				            handler.post(new Runnable() {
								@Override
								public void run() {
		            				makeToast("Lecture has been downloaded successfully.");
								}
							});
			            }
			            else{
			            	final File deleteDir = lectureDirectory;
			            	if(downloadCancelled){
			            		Thread deleterThread = new Thread(){
			            			@Override
			            			public void run() {
					            		deleteDirAndContents(deleteDir);
			            			}
			            		};
			            		deleterThread.start();
			            	}
			            }
			        } catch (Exception e) {
			        	e.printStackTrace();
		            	final File deleteDir = lectureDirectory;
				    	handler.post(new Runnable() {
							@Override
							public void run() {
				            	if(downloadCancelled){
				            		Thread deleterThread = new Thread(){
				            			@Override
				            			public void run() {
						            		deleteDirAndContents(deleteDir);
				            			}
				            		};
				            		deleterThread.start();
				            	}
								dProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setEnabled(true);
					        	makeToast("Error downloading lecture.");
							}
						});
			        }
		    	}
	    	}
	        return null;
	    }
	    @Override
	    protected void onProgressUpdate(Integer... progress) {
	        super.onProgressUpdate(progress);
	        dProgressDialog.setMessage(status);
	        dProgressDialog.setProgress(progress[0]);
	        dProgressDialog.setSecondaryProgress(progress[1]);
	        //dProgressDialog.setSecondaryProgress(progress[1]);
	    }
	    @Override
	    protected void onPostExecute(String result) {
	    	super.onPostExecute(result);
	    	handler.post(new Runnable() {
				@Override
				public void run() {
					dProgressDialog.dismiss();
					dProgressDialog.getButton(ProgressDialog.BUTTON_POSITIVE).setEnabled(true);
					status = "Downloading lecture";
					dProgressDialog.setMessage(status);
				}
			});
	    }
	}
	private boolean[] checkStorageAvailability(){
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		boolean[] availability = {mExternalStorageAvailable, mExternalStorageWriteable};
		return availability;
	}
	private void makeToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	private void deleteDirAndContents(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles()){
	        	deleteDirAndContents(child);
	        }
	    fileOrDirectory.delete();
	}
	private int getDeviceCategory(){
    	if(metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM){
    		if((SCR_LAYOUT & SCR_MASK) == SCR_NORMAL){
    			return MEDIUM_DENSITY_PHONE;
    		}else{
    			return MEDIUM_DENSITY_TABLET;
    		}
    	}
    	else{
    		if((SCR_LAYOUT & SCR_MASK) == SCR_NORMAL){
    			return HIGH_DENSITY_PHONE;
    		}else{
    			return HIGH_DENSITY_TABLET;
    		}
    	}
	}
	private class BgConnectionChecker implements Runnable{
		long id;
		protected BgConnectionChecker(long id){
			this.id = id;
		}
		@Override
		public void run() {
			isConnCheckerSleeping = true;
			while(!isConnected()){
				try{
					Log.e("Thread " + id, "sleeping for 5 seconds");
					Thread.sleep(5000);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
			connectedJustNow = true;
			Log.e("bgConnectionChecker", "finishing");
			handler.post(new Runnable() {
				@Override
				public void run() {
					courses.removeAll(courses);
			        courses = new ArrayList<CheckedTextView>();
					new Thread(coursesPopulator).start();
					ListAdapter lvCoursesAdapter = lvCourses.getAdapter();
					lvCoursesAdapter = null;
					lvCoursesAdapter = new CoursesAdapter(CL_CONTEXT, R.layout.courses, courses, metrics, cCheckedStates){
						@Override
						public boolean isEnabled(int position) {
							if(position == 0)
								return false;
							return true;
						}
					};
			        lvCourses.setAdapter(lvCoursesAdapter);
			        lvCourses.invalidate();
				}
			});
			isConnCheckerSleeping = false;
		}
	}
	private void getPrefs(){
        lodeSettings = getSharedPreferences(LODE_PREFS, MODE_PRIVATE);
        BASE_URL = lodeSettings.getString("coursesURL", "http://latemar.science.unitn.it/itunes/feeds/");
        makeToast(BASE_URL);
	}
}
