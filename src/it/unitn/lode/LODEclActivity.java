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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LODEclActivity extends Activity implements OnItemClickListener{
	private LodeSaxDataParser lecturesParser = null;
	private ListView lvCourses = null;
	private ListView lvLectures = null;
	public static ListView lvLectureInfo = null;
	private ArrayList<TextView> courses = null;
	private ArrayList<TextView> lectures = null;
	private ArrayList<LectureInfo> lectureInfo = null;
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
	private TextView tvInfoItem = null;
	private Courses selectedCourse = null;
	private TextView tvItem = null;
	private final int LV_COURSES = 0;
	private final int LV_LECTURES = 1;
	private final int LV_LECTURE_INFO = 2;
	//private final int LV_LECTURE_INFO = 2;
	private int currPos = 0;
	private ImageButton btnWatch = null;
	private ImageButton btnDownload = null;
	private final String baseUrl = "http://latemar.science.unitn.it/itunes/feeds/";
	public static Context CL_CONTEXT;
	private AlertDialog alertNetwork = null, alertExit = null, alertWrongData = null;
	private boolean comingFromSettings = false;
	private ProgressBar pbCL = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.courses_lectures);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please make sure you have an active data connection.")
		       .setCancelable(false)
		       .setTitle("No Internet Access")
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
		       .setCancelable(false)
		       .setTitle("Exit LODE4Android:")
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
        lvLectureInfo = (ListView) findViewById(R.id.lvLectureInfo);
        lvLectureInfo.setId(LV_LECTURE_INFO);
        
        rlCL = (RelativeLayout) findViewById(R.id.rlCL);
        btnWatch = (ImageButton) findViewById(R.id.btnWatch);
        btnDownload = (ImageButton) findViewById(R.id.btnDownload);

        courses = new ArrayList<TextView>();
        lectures = new ArrayList<TextView>();
        lectureInfo = new ArrayList<LectureInfo>();
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
							alertWrongData.show();
						}
					});
				}
			}
		};
//		if(isConnected()){
		pbCL.setVisibility(View.VISIBLE);
		new Thread(coursesPopulator).start();
//		}
//		else{
//			alert.show();
//		}
        lvCourses.setAdapter(new CoursesAdapter(this, R.layout.courses, courses){
			@Override
			public boolean isEnabled(int position) {
				if(position == 0)
					return false;
				return true;
			}
		});
        lvLectures.setAdapter(new LecturesAdapter(this, R.layout.lectures, lectures){
			@Override
			public boolean isEnabled(int position) {
				if(position == 0)
					return false;
				return true;
			}
		});
		lvLectureInfo.setAdapter(new LectureInfoAdapter(this, R.layout.lecture_info_layout, lectureInfo){
			@Override
			public boolean isEnabled(int position) {
				return false;
			}
		});
        lvCourses.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvCourses.setCacheColorHint(Color.parseColor("#00000000"));
        lvCourses.setBackgroundResource(R.layout.courses_corners);
        //lvCourses.setSelector(R.layout.courses_corners_clicked);
        lvCourses.setSelector(R.drawable.list_item);
        lvCourses.setDividerHeight(2);
        lvCourses.setOnItemClickListener(this);
        
        rlCLParams = new RelativeLayout.LayoutParams(LODETabsActivity.scrWidth / 3, LODETabsActivity.scrHeight);
        rlCL.addView(lvCourses, rlCLParams);

        lvLectures.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvLectures.setCacheColorHint(Color.parseColor("#00000000"));
        lvLectures.setBackgroundResource(R.layout.courses_corners);
        lvLectures.setSelector(R.layout.courses_corners_clicked);
        lvLectures.setDividerHeight(2);
        lvLectures.setOnItemClickListener(this);
        
        rlCLParams = new RelativeLayout.LayoutParams(LODETabsActivity.scrWidth * 2 / 3 - 10, LODETabsActivity.scrHeight);
        rlCLParams.leftMargin = LODETabsActivity.scrWidth / 3 + 5;
        rlCL.addView(lvLectures, rlCLParams);

        rlCLParams = new RelativeLayout.LayoutParams((LODETabsActivity.scrWidth * 6) / 14, (LODETabsActivity.scrHeight * 6) / 14);
        rlCLParams.topMargin = LODETabsActivity.scrHeight / 4;
        rlCLParams.leftMargin = (LODETabsActivity.scrWidth * 28) / 100;
        lvLectureInfo.setChoiceMode(ListView.CHOICE_MODE_NONE);
        lvLectureInfo.setCacheColorHint(Color.parseColor("#00000000"));
        lvLectureInfo.setBackgroundResource(R.layout.lecture_info);
        lvLectureInfo.setDividerHeight(0);
        lvLectureInfo.setVisibility(View.INVISIBLE);
        rlCL.removeView(lvLectureInfo);
        rlCL.addView(lvLectureInfo, rlCLParams);
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		//parent.setSelection(position);
//		Log.e("On item click", "being called");
		final Integer POSITION = position;
		if(parent.getId() == LV_COURSES){
		     lvLectures.setEnabled(false);
			if(lvLectureInfo.getVisibility() == View.VISIBLE){
				lvLectureInfo.setVisibility(View.INVISIBLE);
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
//						        	Log.e("Retrieving from: ", "online");
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
										for(int pos = 0; pos < lvLectures.getCount(); pos++){
											lvLectures.getChildAt(pos).setBackgroundColor(Color.parseColor("#30d3d3d3"));
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
										alertWrongData.show();
									}
								});
							}
						}
					};
//					if(isConnected()){
					pbCL.setVisibility(View.VISIBLE);
					new Thread(lecturesPopulator).start();
//					}
//					else{
//						alert.show();
//					}
					break;
				}
			}
		}
		else if(parent.getId() == LV_LECTURES){
			tvInfoItem = new TextView(this);
			tvInfoItem.setText("\nTopic: " + cl.get(currPos).get(position - 1).getTitolol() + "\nDate: "
								+ cl.get(currPos).get(position - 1).getDatel() + "\nLecturer: "
								+ cl.get(currPos).get(position - 1).getDocentel());
			lectureInfo.removeAll(lectureInfo);
			LectureInfo lecInfo = new LectureInfo(tvInfoItem, btnWatch, btnDownload, cl.get(currPos).get(position - 1).getUrllez(),
					baseUrl + cl.get(currPos).get(position - 1).getFolderl());
			lectureInfo.add(lecInfo);
			lvLectureInfo.invalidateViews();
			lvLectureInfo.setVisibility(View.VISIBLE);
			lvLectures.setEnabled(false);
			lvLectureInfo.bringToFront();
		}
	}
	@Override
	public void onBackPressed() {
		if(lvLectureInfo.getVisibility() == View.VISIBLE){
			lvLectureInfo.setVisibility(View.INVISIBLE);
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
//	    boolean isWifi = false;
//	    boolean isMobile = false;

	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    return cm.getActiveNetworkInfo().isAvailable();
//	    for (NetworkInfo ni : netInfo) {
//	        if (("WIFI").equals(ni.getTypeName())){
//	            if (ni.isConnected()){
//	                isWifi = true;
//	            }
//	        }
//	        if (("MOBILE").equals(ni.getTypeName())){
//	            if (ni.isConnected()){
//	                isMobile = true;
//	            }
//	        }
//	    }
//	    return isWifi || isMobile;
	}
}
