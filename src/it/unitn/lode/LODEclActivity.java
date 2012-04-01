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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LODEclActivity extends Activity implements OnItemClickListener {
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.courses_lectures);

        handler = new Handler();
        lvCourses = new ListView(this);
        lvCourses.setId(LV_COURSES);
        lvLectures = new ListView(this);
        lvLectures.setId(LV_LECTURES);

        rlCL = (RelativeLayout) findViewById(R.id.rlCL);
        courses = new ArrayList<TextView>();
        lectures = new ArrayList<TextView>();
        cl = new TreeMap<Integer, List<Lectures>>();
        tvCourse = new TextView(this);
        
        coursesPopulator = new Runnable() {
			@Override
			public void run() {
				coursesParser = new LodeSaxDataParser("http://latemar.science.unitn.it/itunes/feeds/COURSES.XML");
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
        //rlCLParams.topMargin = 10;
        rlCLParams.leftMargin = LODETabsActivity.scrWidth / 3 + 5;
        rlCL.addView(lvLectures, rlCLParams);

//        rlCLParams = new RelativeLayout.LayoutParams(LODETabsActivity.scrWidth / 2, LODETabsActivity.scrHeight / 2);
//        rlCLParams.topMargin = LODETabsActivity.scrHeight / 3;
//        rlCLParams.leftMargin = LODETabsActivity.scrWidth / 3;
//        rlCL.addView(lvLectures, rlCLParams);
	
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
								String url = "http://latemar.science.unitn.it/itunes/feeds/" + selectedCourse.getFolderc() + "/LECTURES.XML";
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
						            String courseDetails = "Professor: " + selectedCourse.getDocentec() + "\n";
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
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			PopupWindow pw = new PopupWindow(inflater.inflate(R.layout.lecture_popup, null, false), 100, 100, true);
			pw.showAtLocation(this.findViewById(R.id.rlCL), Gravity.CENTER, 0, 0);
		}
	}
}
