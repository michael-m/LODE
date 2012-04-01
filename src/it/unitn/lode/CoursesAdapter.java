package it.unitn.lode;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CoursesAdapter extends ArrayAdapter<TextView>{
	private ArrayList<TextView> courses;
	private Typeface tfApplegaramound = null;

	public CoursesAdapter(Context context, int textViewResourceId,
            ArrayList<TextView> courses) {
        super(context, textViewResourceId, courses);
        this.courses = courses;
        this.tfApplegaramound = Typeface.createFromAsset(LODETabsActivity.ASSETS, "fonts/Applegaramound.ttf");
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
                // put the id to identify the item clicked
                textViewTwo.setTag(tv.getId());
                textViewTwo.setFocusable(false);
                textViewTwo.setClickable(false);
                
                if(position == 0){
                    textViewTwo.setTextSize(15);
                    textViewTwo.setTypeface(tfApplegaramound, Typeface.BOLD_ITALIC);
                    textViewTwo.setGravity(Gravity.CENTER);
                }
                else{
                    textViewTwo.setTextSize(14);
                    textViewTwo.setTypeface(tfApplegaramound, Typeface.NORMAL);
                    textViewTwo.setPadding(15, 5, 10, 5);
                    textViewTwo.setGravity(Gravity.LEFT);
                }
            }
        }
        return v;
    }

}