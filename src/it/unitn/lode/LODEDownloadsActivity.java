package it.unitn.lode;

import it.unitn.lode.data.Courses;
import it.unitn.lode.data.Lectures;
import it.unitn.lode.data.LodeSaxDataParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
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
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LODEDownloadsActivity extends Activity implements OnItemClickListener, OnClickListener{
	private LodeSaxDataParser lecturesParser = null;
	private ListView lvCourses = null;
	private ListView lvLectures = null;
	private ArrayList<CheckedTextView> courses = null;
	private ArrayList<CheckedTextView> lectures = null;
	private Map<Integer, Boolean> cCheckedStates = null;
	private Map<Integer, Boolean> lCheckedStates = null;
	private RelativeLayout rlCL = null;
	private RelativeLayout.LayoutParams rlCLParams = null;
	private LodeSaxDataParser coursesParser = null;
	private Runnable coursesPopulator = null;
	private Runnable lecturesPopulator = null;
	private List<Courses> cs = null;
	private List<Lectures> ls = null;
	private Map<Integer, List<Lectures>> cl = null;
	private Iterator<Courses> csIterator = null;
	private Iterator<Lectures> lsIterator = null;
	private Handler handler = null;
	private final Context coursesContext = this;
	private CheckedTextView tvCourse = null;
	private Courses selectedCourse = null;
	private CheckedTextView tvItem = null;
	private final int LV_COURSES = 100;
	private final int LV_LECTURES = 101;
	private ImageButton btnWatch = null;
	private ImageButton btnDelete = null;
	public static Context CL_CONTEXT;
	private AlertDialog alertNetwork = null, alertExit = null, alertWrongData = null, alertDelete = null;
	private boolean comingFromSettings = false;
	private ProgressBar pbCL = null;
	private DisplayMetrics metrics = null;
	public static int scrWidth, scrHeight;
	private RelativeLayout rlLectureInfo = null;
	private RelativeLayout rlLIContainer = null;
	private RelativeLayout rlButtons = null;
	private TextView tvLectureInfo = null;
	private RelativeLayout.LayoutParams rlLIParams = null;
	private Typeface tfApplegaramound = null;
	private final int WATCH = 0, DELETE = 1, INFO = 2;
	private final String SD_CARD = Environment.getExternalStorageDirectory().toString();
	private final String BASE_URL = SD_CARD + "/lode/";
	private final String WEB_BASE_URL = "http://latemar.science.unitn.it/itunes/feeds/";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
        setContentView(R.layout.courses_lectures);
	    metrics = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metrics);
        scrWidth = metrics.widthPixels;
        scrHeight = metrics.heightPixels;

        rlLIContainer = new RelativeLayout(this);
        rlLIContainer.setBackgroundColor(Color.TRANSPARENT);
        rlLIContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        rlLIContainer.setVisibility(View.GONE);

        rlButtons = new RelativeLayout(this);
        rlLectureInfo = new RelativeLayout(this);
        rlLectureInfo.setBackgroundResource(R.layout.lecture_info);
        btnWatch = new ImageButton(this);
        btnDelete = new ImageButton(this);
        tvLectureInfo = new TextView(this);
        
        btnWatch.setImageResource(android.R.drawable.ic_media_play);
        btnWatch.setOnClickListener(this);
        btnWatch.setId(WATCH);
        btnDelete.setImageResource(android.R.drawable.ic_delete);
        btnDelete.setOnClickListener(this);
        btnDelete.setId(DELETE);
        tvLectureInfo.setId(INFO);
        tvLectureInfo.setTypeface(tfApplegaramound, Typeface.NORMAL);
        tvLectureInfo.setTextSize(13);
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
        rlButtons.addView(btnDelete, rlLIParams);
        
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
		       .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                LODEDownloadsActivity.this.finish();
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
		                LODEDownloadsActivity.this.finish();
		           }
		       })
		       .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.dismiss();
		           }
		       });
		alertExit = builder.create();
		alertExit.setOwnerActivity(this);

		builder = new AlertDialog.Builder(this);
		builder.setMessage("The course or lecture data is incorrect.")
		       .setCancelable(true)
		       .setTitle("LODE4Android: Error")
		       .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.dismiss();
		           }
		       });
		alertWrongData = builder.create();
		alertWrongData.setOwnerActivity(this);

		builder = new AlertDialog.Builder(this);
		builder.setMessage("Press Remove to delete this lecture and free up resources.")
		       .setCancelable(true)
		       .setTitle("LODE4Android: Delete Lecture")
		       .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		   			String deleteUrl = (String) btnDelete.getTag(R.id.deleteDir);
					final File deleteDir = new File(BASE_URL + deleteUrl);
					if(!deleteDir.exists()){
						Log.e(BASE_URL + deleteUrl, "does not exist");
					}
					Thread deleterThread = new Thread(){
						@Override
						public void run() {
							deleteDirAndContents(deleteDir);
							handler.post(new Runnable() {
								@Override
								public void run() {
									new Thread(lecturesPopulator).start();
									rlLIContainer.setVisibility(View.GONE);
									lvCourses.setEnabled(true);
									lvLectures.setEnabled(true);
									lvLectures.invalidate();
								}
							});
						}
					};
					deleterThread.start();
		           }
		       })
		       .setNegativeButton("Keep", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.dismiss();
		           }
		       });
		alertDelete = builder.create();
		alertDelete.setOwnerActivity(this);

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
        
        coursesPopulator = new Runnable() {
			@Override
			public void run() {
				try{
					coursesParser = new LodeSaxDataParser(BASE_URL + "COURSES.XML", true);
					Log.e("coursesMetaData", BASE_URL + "COURSES.XML");
				}catch(RuntimeException e){
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
//				        	Log.e("Title: ", "Available Courses");
					        while(csIterator.hasNext()){
					        	title = csIterator.next().getTitoloc().trim().replaceAll(" +", " ").replace("\n", "");
					        	title = title.trim();
					            tvCourse = new CheckedTextView(coursesContext);
					        	tvCourse.setText(title);
					        	courses.add(tvCourse);
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
					e1.printStackTrace();
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
//        lvCourses.setAdapter(new CoursesAdapter(this, R.layout.courses, courses, metrics){
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
        lvCourses.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvCourses.setCacheColorHint(Color.parseColor("#00000000"));
        lvCourses.setBackgroundResource(R.layout.courses_corners);
        lvCourses.setSelector(R.drawable.list_item);
        lvCourses.setDividerHeight(2);
        lvCourses.setOnItemClickListener(this);

        
        lvLectures.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvLectures.setCacheColorHint(Color.parseColor("#00000000"));
        lvLectures.setBackgroundResource(R.layout.courses_corners);
        lvLectures.setSelector(R.layout.courses_corners_clicked);
        lvLectures.setDividerHeight(2);
        lvLectures.setOnItemClickListener(this);

/***** ADD VIEWS TO LAYOUTS ******/
        if(metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM){
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
//			currPos = position;
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
							boolean urlGoAhead = true;
							try{
								if(cl.get(POSITION) == null){
									String url = BASE_URL + selectedCourse.getFolderc() + "/LECTURES.XML";
							    	File lecturesMetaDataDir = new File(SD_CARD + "/lode/" + selectedCourse.getFolderc());
							    	File lecturesMetaData = new File(url);
							    	if(!lecturesMetaData.exists()){
							    		try {
							    			lecturesMetaDataDir.mkdirs();
											URL courseMetaDataUrl = new URL(WEB_BASE_URL + selectedCourse.getFolderc() + "/LECTURES.XML");
								            FileOutputStream output = null;
								            URLConnection connection = courseMetaDataUrl.openConnection();
								            connection.connect();
								            InputStream input = new BufferedInputStream(courseMetaDataUrl.openStream());
								            output = new FileOutputStream(lecturesMetaData);
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
									if(urlGoAhead){
										lecturesParser = new LodeSaxDataParser(url, true);
									}
								}
							}catch(RuntimeException e){
								e.printStackTrace();
					            handler.post(new Runnable(){
									@Override
									public void run() {
										comingFromSettings = true;
										alertNetwork.show();
									}
								});
							}
							if(urlGoAhead){
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
									        	String fileName = BASE_URL + nextLs.getFolderl() + "/lecture.downloaded";
									        	if(new File(fileName).exists()){
										        	String lTitle = nextLs.getTitolol().trim().replaceAll(" +", " ").replace("\n", "");
										        	lTitle = lTitle.trim();
										            tvItem = new CheckedTextView(coursesContext);
										        	tvItem.setText(lTitle);
										        	tvItem.setTag(R.id.tvLectureDataUrl, BASE_URL + nextLs.getFolderl());
										        	int start = nextLs.getUrllez().lastIndexOf("/") + 1;
										        	String videoName = nextLs.getUrllez().substring(start);
									            	Log.e("videoUrl", BASE_URL + nextLs.getFolderl() + "/" + videoName);
										        	tvItem.setTag(R.id.tvVideoUrl, BASE_URL + nextLs.getFolderl() + "/" + videoName);
										        	tvItem.setTag(R.id.tvLectureDir, nextLs.getFolderl());
										        	tvItem.setTag(R.id.tvLectureInfoConcat,
										        			"\nTopic: "	+ nextLs.getTitolol()
										        			+ "\nDate: " + nextLs.getDatel()
										        			+ "\nLecturer: " + nextLs.getDocentel());
										        	tvItem.setTag(R.id.tvIdForBookmark, nextLs.getFolderl());
										        	lectures.add(tvItem);
									        	}
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
			btnDelete.setTag(R.id.deleteDir, (String) tvLecture.getTag(R.id.tvLectureDir));

			boolean[] availability = checkStorageAvailability();
			if(availability[0]){
				if(availability[1]){
					btnDelete.setEnabled(true);
				}
				else{
					btnDelete.setEnabled(false);
					makeToast("External Storage is write-protected.");
				}
			}
			else{
				btnDelete.setEnabled(false);
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
		if(comingFromSettings){
	        courses = new ArrayList<CheckedTextView>();
			comingFromSettings = false;
			new Thread(coursesPopulator).start();
		}
		new Thread(lecturesPopulator).start();
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
			Log.e("lectureDataUrl", lectureDataUrl);
			Bundle bundle = new Bundle();
			bundle.putString("videoUrl",videoUrl);
			bundle.putString("lectureDataUrl", lectureDataUrl);
			bundle.putString("idForBookmark", idForBookmark);
			bundle.putString("LectureInfo", tvLectureInfo.getText().toString());
			bundle.putBoolean("isLocal", true);
			Intent intent = new Intent(this, LODEActivity.class);
			rlLIContainer.setVisibility(View.GONE);
			lvCourses.setEnabled(true);
			lvLectures.setEnabled(true);
			intent.putExtras(bundle);
			startActivity(intent);
		}
		else if(v.getId() == DELETE){
			alertDelete.show();
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
	void deleteDirAndContents(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles()){
	        	deleteDirAndContents(child);
	        }
	    fileOrDirectory.delete();
	}
}
