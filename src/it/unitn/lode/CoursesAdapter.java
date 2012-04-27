package it.unitn.lode;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CoursesAdapter extends ArrayAdapter<TextView>{
	private ArrayList<TextView> courses;
	private Typeface tfApplegaramound = null;
	public ArrayList<Integer> selectedIds = new ArrayList<Integer>();
	private DisplayMetrics metrics = null;
	public CoursesAdapter(Context context, int textViewResourceId, ArrayList<TextView> courses, DisplayMetrics metrics) {
        super(context, textViewResourceId, courses);
        this.courses = courses;
        this.tfApplegaramound = Typeface.createFromAsset(LODETabsActivity.ASSETS, "fonts/Applegaramound.ttf");
        this.metrics = metrics;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.courses, null);
        }

        TextView tv = courses.get(position);
        if (tv != null) {
            TextView textViewTwo = (TextView) v.findViewById(R.id.tvCourses);
            if (textViewTwo != null) {
                textViewTwo.setText(tv.getText());
                textViewTwo.setTag(tv.getId());
                
                if(position == 0){
                	if(metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM){
                        textViewTwo.setTextSize(13);
                	}
                	else{
                        textViewTwo.setTextSize(15);
                	}
                    textViewTwo.setTypeface(tfApplegaramound, Typeface.BOLD);
                    textViewTwo.setGravity(Gravity.CENTER);
                }
                else{
                	if(metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM){
                        textViewTwo.setTextSize(12);
                	}
                	else{
                        textViewTwo.setTextSize(14);
                	}
                    textViewTwo.setTypeface(tfApplegaramound, Typeface.NORMAL);
                    textViewTwo.setPadding(15, 5, 10, 5);
                    textViewTwo.setGravity(Gravity.LEFT);
                }
            }
        }
        return v;
    }
}
