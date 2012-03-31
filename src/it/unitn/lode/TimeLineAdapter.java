package it.unitn.lode;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TimeLineAdapter extends ArrayAdapter<TextView> implements OnClickListener {
	private ArrayList<TextView> slidePos;
	private Typeface tfApplegaramound = null;

	public TimeLineAdapter(Context context, int textViewResourceId,
            ArrayList<TextView> slidePos) {
        super(context, textViewResourceId, slidePos);
        this.slidePos = slidePos;
        this.tfApplegaramound = Typeface.createFromAsset(LODEActivity.ASSETS, "fonts/Applegaramound.ttf");
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.slide_pos, null);
        }

        TextView tv = slidePos.get(position);
        if (tv != null) {
            TextView textViewTwo = (TextView) v.findViewById(R.id.tvSlidePos);
            if (textViewTwo != null) {
                textViewTwo.setText(tv.getText());
                // put the id to identify the item clicked
                textViewTwo.setTag(tv.getId());
                textViewTwo.setFocusable(false);
                textViewTwo.setClickable(false);
                if(position == 0 || position == slidePos.size() - 1){
                	textViewTwo.setTextSize(14);
                	textViewTwo.setTypeface(tfApplegaramound, Typeface.BOLD_ITALIC);
                	textViewTwo.setPadding(0, 0, 0, 0);
                	textViewTwo.setGravity(Gravity.CENTER);
                }
                else{
                	textViewTwo.setTextSize(13);
                	textViewTwo.setTypeface(tfApplegaramound, Typeface.NORMAL);
                	textViewTwo.setPadding(15, 5, 10, 5);
                	textViewTwo.setGravity(Gravity.LEFT);
                }
//                textViewTwo.setOnClickListener(this);
            }
        }
        return v;
    }

    @Override
    public void onClick(View v) {
        Log.d("Sample", "Clicked on tag: " + v.getTag());
        // Do something here, like start new activity.
    }
}
