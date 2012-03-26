package it.unitn.lode;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TimeLineAdapter extends ArrayAdapter<TextView> implements OnClickListener {
	private ArrayList<TextView> slidePos;

    public TimeLineAdapter(Context context, int textViewResourceId,
            ArrayList<TextView> slidePos) {
        super(context, textViewResourceId, slidePos);
        this.slidePos = slidePos;
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
                textViewTwo.setOnClickListener(this);
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
