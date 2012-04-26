package it.unitn.lode;

import it.unitn.lode.data.Courses;
import it.unitn.lode.data.Lectures;
import it.unitn.lode.data.LodeSaxDataParser;

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
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LODEclActivity extends Activity implements OnItemClickListener, OnClickListener{
	private LodeSaxDataParser lecturesParser = null;
	private ListView lvCourses = null;
	private ListView lvLectures = null;
	private ArrayList<TextView> courses = null;
	private ArrayList<TextView> lectures = null;
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
	private TextView tvCourse = null;
	private Courses selectedCourse = null;
	private TextView tvItem = null;
	private final int LV_COURSES = 0;
	private final int LV_LECTURES = 1;
	private int currPos = 0;
	private ImageButton btnWatch = null;
	private ImageButton btnDownload = null;
	private final String baseUrl = "http://latemar.science.unitn.it/itunes/feeds/";
	public static Context CL_CONTEXT;
	private AlertDialog alertNetwork = null, alertExit = null, alertWrongData = null;
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
	private final int WATCH = 0, DOWNLOAD = 1, INFO = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.courses_lectures);
	    metrics = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metrics);
        scrWidth = metrics.widthPixels;
        scrHeight = metrics.heightPixels;

        
/******* LECTURE INFO AND OPTIONS LAYOUT ******/
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
		       .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                LODEclActivity.this.finish();
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
        courses = new ArrayList<TextView>();
        lectures = new ArrayList<TextView>();
        cl = new TreeMap<Integer, List<Lectures>>();
        tvCourse = new TextView(this);
        
        coursesPopulator = new Runnable() {
			@Override
			public void run() {
				try{
					coursesParser = new LodeSaxDataParser(baseUrl + "COURSES.XML");
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
				            tvCourse = new TextView(coursesContext);
				        	tvCourse.setText("\nAvailable Courses");
				        	courses.add(tvCourse);
//				        	Log.e("Title: ", "Available Courses");
					        while(csIterator.hasNext()){
					        	title = csIterator.next().getTitoloc().trim().replaceAll(" +", " ").replace("\n", "");
					        	title = title.trim();
					            tvCourse = new TextView(coursesContext);
					        	tvCourse.setText(title);
					        	courses.add(tvCourse);
					        }
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
        lvCourses.setAdapter(new CoursesAdapter(this, R.layout.courses, courses, metrics){
			@Override
			public boolean isEnabled(int position) {
				if(position == 0)
					return false;
				return true;
			}
		});
        lvLectures.setAdapter(new LecturesAdapter(this, R.layout.lectures, lectures, metrics){
			@Override
			public boolean isEnabled(int position) {
				if(position == 0)
					return false;
				return true;
			}
		});
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
		final Integer POSITION = position;
		if(parent.getId() == LV_COURSES){
		     lvLectures.setEnabled(false);
			if(rlLIContainer.getVisibility() == View.VISIBLE){
				rlLIContainer.setVisibility(View.INVISIBLE);
			}
			currPos = position;
			tvItem = (TextView) parent.getItemAtPosition(position);
			
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
									String url = baseUrl + selectedCourse.getFolderc() + "/LECTURES.XML";
									lecturesParser = new LodeSaxDataParser(url);
								}
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
							            tvItem = new TextView(coursesContext);
							            String courseDetails = "Professor in charge: " + selectedCourse.getDocentec() + "\n";
							            courseDetails+= "Academic Year: " + selectedCourse.getYear();
							        	tvItem.setText(courseDetails);
							        	lectures.add(tvItem);
										
								        while(lsIterator.hasNext()){
								        	String lTitle = lsIterator.next().getTitolol().trim().replaceAll(" +", " ").replace("\n", "");
								        	lTitle = lTitle.trim();
								            tvItem = new TextView(coursesContext);
								        	tvItem.setText(lTitle);
								        	lectures.add(tvItem);
								        }
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
			tvLectureInfo.setText("\nTopic: " + cl.get(currPos).get(position - 1).getTitolol() + "\nDate: "
								+ cl.get(currPos).get(position - 1).getDatel() + "\nLecturer: "
								+ cl.get(currPos).get(position - 1).getDocentel());
			btnWatch.setTag(R.id.videoUrl, cl.get(currPos).get(position - 1).getUrllez());
			btnWatch.setTag(R.id.lectureDataUrl, baseUrl + cl.get(currPos).get(position - 1).getFolderl());
			rlLIContainer.setVisibility(View.VISIBLE);
			rlLIContainer.bringToFront();
		}
	}
	@Override
	public void onBackPressed() {
		if(rlLIContainer.getVisibility() == View.VISIBLE){
			rlLIContainer.setVisibility(View.GONE);
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
	        courses = new ArrayList<TextView>();
			comingFromSettings = false;
			new Thread(coursesPopulator).start();
		}
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
			Bundle bundle = new Bundle();
			bundle.putString("videoUrl",videoUrl);
			bundle.putString("lectureDataUrl", lectureDataUrl);
			Intent intent = new Intent(this, LODEActivity.class);
			rlLIContainer.setVisibility(View.GONE);
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}
}
