package it.unitn.lode;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class CoursesAdapter extends ArrayAdapter<CheckedTextView>{
	private ArrayList<CheckedTextView> courses = null;
	private Map<Integer, Boolean> checkedStates = null;
	private Typeface tfApplegaramound = null;
	private ArrayList<Integer> selectedIds = new ArrayList<Integer>();
	private DisplayMetrics metrics = null;
	private int SCR_LAYOUT;
	private final int SCR_MASK = Configuration.SCREENLAYOUT_SIZE_MASK;
	private final int SCR_NORMAL = Configuration.SCREENLAYOUT_SIZE_NORMAL;
	private final int MEDIUM_DENSITY_PHONE = 999;
	private final int MEDIUM_DENSITY_TABLET = 888;
	private final int HIGH_DENSITY_PHONE = 777;
	private final int HIGH_DENSITY_TABLET = 666;
	private int THIS_DEVICE;
	public CoursesAdapter(Context context, int textViewResourceId, ArrayList<CheckedTextView> courses, DisplayMetrics metrics
			, Map<Integer, Boolean> checkedStates){
        super(context, textViewResourceId, courses);
        this.SCR_LAYOUT = context.getResources().getConfiguration().screenLayout;
        this.courses = courses;
        this.checkedStates = checkedStates;
        this.tfApplegaramound = Typeface.createFromAsset(LODETabsActivity.ASSETS, "fonts/Applegaramound.ttf");
        this.metrics = metrics;
        THIS_DEVICE = getDeviceCategory();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.courses, null);
        }
        CheckedTextView tv = courses.get(position);
        if (tv != null) {
            CheckedTextView textViewTwo = (CheckedTextView) v.findViewById(R.id.tvCourses);
            if (textViewTwo != null) {
                textViewTwo.setText(tv.getText());
                textViewTwo.setTag(tv.getId());
                textViewTwo.setChecked(checkedStates.get(position));
                if(textViewTwo.isChecked()){
                	textViewTwo.setBackgroundResource(R.layout.courses_corners_clicked);
                }
                else{
                	textViewTwo.setBackgroundColor(Color.TRANSPARENT);
                }
                if(position == 0){
                	if(THIS_DEVICE == MEDIUM_DENSITY_PHONE){
            			textViewTwo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                	}
                	else if(THIS_DEVICE == MEDIUM_DENSITY_TABLET || THIS_DEVICE == HIGH_DENSITY_TABLET){
                        textViewTwo.setTextSize(17);
                        textViewTwo.setTypeface(tfApplegaramound, Typeface.BOLD);
                	}
                	else if(THIS_DEVICE == HIGH_DENSITY_PHONE){
                        textViewTwo.setTextSize(15);
                        textViewTwo.setTypeface(tfApplegaramound, Typeface.BOLD);
                	}
                    textViewTwo.setGravity(Gravity.CENTER);
                }
                else{
                	if(THIS_DEVICE == MEDIUM_DENSITY_PHONE){
                        textViewTwo.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                        textViewTwo.setPadding(10, 0, 0, 0);
                	}
                	else if(THIS_DEVICE == MEDIUM_DENSITY_TABLET || THIS_DEVICE == HIGH_DENSITY_TABLET){
                        textViewTwo.setTextSize(17);
                        textViewTwo.setTypeface(tfApplegaramound, Typeface.NORMAL);
                        textViewTwo.setPadding(15, 5, 10, 5);
                        textViewTwo.setGravity(Gravity.LEFT);
                	}
                	else if(THIS_DEVICE == HIGH_DENSITY_PHONE){
                        textViewTwo.setTextSize(14);
                        textViewTwo.setTypeface(tfApplegaramound, Typeface.NORMAL);
                        textViewTwo.setPadding(15, 5, 10, 5);
                        textViewTwo.setGravity(Gravity.LEFT);
                	}
                }
            }
        }
        return v;
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
}
