package it.unitn.lode;

import it.unitn.lode.data.Courses;
import it.unitn.lode.data.Lectures;
import it.unitn.lode.data.LodeSaxDataParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
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
			        	tvCourse.setText("Available Courses");
			        	courses.add(tvCourse);
			        	Log.e("Title: ", "Available Courses");
				        while(csIterator.hasNext()){
				        	title = csIterator.next().getTitoloc().trim().replaceAll(" +", " ").replace("\n", "");
				        	title = title.trim();
				            tvCourse = new TextView(coursesContext);
				        	tvCourse.setText(title);
				        	courses.add(tvCourse);
				        	Log.e("Title: ", title);
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
//			@Override
//			public boolean isEnabled(int position) {
//				if(position == 0)
//					return false;
//				return true;
//			}
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
 	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
							String url = "http://latemar.science.unitn.it/itunes/feeds/" + selectedCourse.getFolderc() + "/LECTURES.XML";
							LodeSaxDataParser lecturesParser = new LodeSaxDataParser(url);
							ls = lecturesParser.parseLectures();
							lsIterator = ls.iterator();
							handler.post(new Runnable() {
								@Override
								public void run() {
									lectures.removeAll(lectures);
							        while(lsIterator.hasNext()){
							        	String lTitle = lsIterator.next().getTitolol().trim().replaceAll(" +", " ").replace("\n", "");
							        	lTitle = lTitle.trim();
							            tvItem = new TextView(coursesContext);
							        	tvItem.setText(lTitle);
							        	lectures.add(tvItem);
							        	Log.e("Title: ", lTitle);
							        }
							        lvLectures.setChoiceMode(ListView.CHOICE_MODE_NONE);
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
	}
}
