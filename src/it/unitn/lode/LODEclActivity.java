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
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LODEclActivity extends Activity implements OnItemClickListener{
	private ListView lvCourses = null;
	private ListView lvLectures = null;
	private ListView lvLectureInfo = null;
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
	//private final int LV_LECTURE_INFO = 2;
	private int currPos = 0;
	private ImageButton btnWatch = null;
	private ImageButton btnDownload = null;
	private final String baseUrl = "http://latemar.science.unitn.it/itunes/feeds/";
	public static Context CL_CONTEXT;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.courses_lectures);
        CL_CONTEXT = this;
        handler = new Handler();
        lvCourses = new ListView(this);
        lvCourses.setId(LV_COURSES);
        lvLectures = new ListView(this);
        lvLectures.setId(LV_LECTURES);
        //lvLectureInfo = new ListView(this);
        lvLectureInfo = (ListView) findViewById(R.id.lvLectureInfo);
//        lvLectureInfo.setId(LV_LECTURE_INFO);
        
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
				coursesParser = new LodeSaxDataParser(baseUrl + "COURSES.XML");
		        cs = coursesParser.parseCourses();
	            csIterator = cs.iterator();
	            handler.post(new Runnable(){
					@Override
					public void run() {
						String title;
			            tvCourse = new TextView(coursesContext);
			        	tvCourse.setText("\nAvailable Courses");
			        	courses.add(tvCourse);
			        	Log.e("Title: ", "Available Courses");
				        while(csIterator.hasNext()){
				        	title = csIterator.next().getTitoloc().trim().replaceAll(" +", " ").replace("\n", "");
				        	title = title.trim();
				            tvCourse = new TextView(coursesContext);
				        	tvCourse.setText(title);
				        	courses.add(tvCourse);
				        	//Log.e("Title: ", title);
				        }
				        lvCourses.invalidateViews();
					}
				});
			}
		};
		new Thread(coursesPopulator).start();
		
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
        lvCourses.setChoiceMode(ListView.CHOICE_MODE_NONE);
        lvCourses.setCacheColorHint(Color.parseColor("#00000000"));
        lvCourses.setBackgroundResource(R.layout.courses_corners);
        lvCourses.setDividerHeight(2);
        lvCourses.setOnItemClickListener(this);
        rlCLParams = new RelativeLayout.LayoutParams(LODETabsActivity.scrWidth / 3, LODETabsActivity.scrHeight);
        //rlCLParams.topMargin = 10;
        //rlCLParams.leftMargin = 10;
        rlCL.addView(lvCourses, rlCLParams);

        lvLectures.setChoiceMode(ListView.CHOICE_MODE_NONE);
        lvLectures.setCacheColorHint(Color.parseColor("#00000000"));
        lvLectures.setBackgroundResource(R.layout.courses_corners);
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
		final Integer POSITION = position;
		for(int pos = 0; pos < parent.getCount(); pos++){
			if(pos != position){
				parent.getChildAt(pos).setBackgroundColor(Color.parseColor("#30d3d3d3"));
			}
		}
		parent.getChildAt(position).setBackgroundResource(R.layout.courses_corners_clicked);
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
							if(cl.get(POSITION) == null){
								String url = baseUrl + selectedCourse.getFolderc() + "/LECTURES.XML";
								LodeSaxDataParser lecturesParser = new LodeSaxDataParser(url);
								ls = lecturesParser.parseLectures();
								cl.put(POSITION, ls);
					        	Log.e("Retrieving from: ", "online");
							}
							lsIterator = cl.get(POSITION).iterator();
							handler.post(new Runnable() {
								@Override
								public void run() {
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
							        	//Log.e("Title: ", lTitle);
							        }
									for(int pos = 0; pos < lvLectures.getCount(); pos++){
										lvLectures.getChildAt(pos).setBackgroundColor(Color.parseColor("#30d3d3d3"));
									}
									lvLectures.setEnabled(true);
							        lvLectures.invalidateViews();
								}
							});
						}
					};
					new Thread(lecturesPopulator).start();
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
		else
			super.onBackPressed();
	}
}
