package it.unitn.lode;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LectureInfoAdapter extends ArrayAdapter<LectureInfo> implements OnClickListener{
	private ArrayList<LectureInfo> lectureInfo;
	private Typeface tfApplegaramound = null;
	private RelativeLayout rlLectureInfo = null;
	private RelativeLayout.LayoutParams rlLectureInfoParams = null;
	private ImageButton btnWatch = null;
	private ImageButton btnDownload = null;
	private TextView tvLectureInfo = null;
	private String videoUrl = null;
	private String lectureDataUrl = null;
	private final int LV_WIDTH = (LODETabsActivity.scrWidth * 6) / 14;
	private final int LV_HEIGHT = (LODETabsActivity.scrHeight * 6) / 14;
	public LectureInfoAdapter(Context context, int textViewResourceId, ArrayList<LectureInfo> lectureInfo) {
        super(context, textViewResourceId, lectureInfo);
        this.lectureInfo = lectureInfo;
        this.tfApplegaramound = Typeface.createFromAsset(LODETabsActivity.ASSETS, "fonts/Applegaramound.ttf");
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	this.videoUrl = this.lectureInfo.get(position).getVideoUrl();
    	this.lectureDataUrl = this.lectureInfo.get(position).getLectureDataUrl();
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.lecture_info_layout, null);
        }
        if(this.rlLectureInfo == null){
        	this.rlLectureInfo = (RelativeLayout) v.findViewById(R.id.rlLectureInfo);
            this.btnWatch = (ImageButton) v.findViewById(R.id.btnWatch);
            this.btnWatch.setImageResource(android.R.drawable.ic_media_play);
            this.btnWatch.setOnClickListener(this);
            this.rlLectureInfoParams = new RelativeLayout.LayoutParams(50, 50);
            this.rlLectureInfoParams.topMargin = (LV_HEIGHT * 2) / 3 + 10;
            this.rlLectureInfoParams.leftMargin = (LV_WIDTH / 2) - 55;
            this.rlLectureInfo.removeView(this.btnWatch);
            this.rlLectureInfo.addView(this.btnWatch, rlLectureInfoParams);

        	this.rlLectureInfo = (RelativeLayout) v.findViewById(R.id.rlLectureInfo);
            this.btnDownload = (ImageButton) v.findViewById(R.id.btnDownload);
            this.btnDownload.setImageResource(android.R.drawable.stat_sys_download);
            this.rlLectureInfoParams = new RelativeLayout.LayoutParams(50, 50);
            this.rlLectureInfoParams.topMargin = (LV_HEIGHT * 2) / 3 + 10;
            this.rlLectureInfoParams.leftMargin = (LV_WIDTH / 2) + 5;
            this.rlLectureInfo.removeView(this.btnDownload);
            this.rlLectureInfo.addView(this.btnDownload, rlLectureInfoParams);

            this.tvLectureInfo = (TextView) v.findViewById(R.id.tvLectureInfo);
            this.tvLectureInfo.setGravity(Gravity.TOP);
            this.rlLectureInfoParams = new RelativeLayout.LayoutParams(LV_WIDTH - 10, (LV_HEIGHT * 2) / 3);
            this.rlLectureInfo.removeView(tvLectureInfo);
            this.rlLectureInfo.addView(tvLectureInfo, rlLectureInfoParams);
        }
        TextView tv = (TextView) lectureInfo.get(position).getTvLectureInfo();
        if (tv != null) {
        	TextView textViewTwo = (TextView) v.findViewById(R.id.tvLectureInfo);
        	if (textViewTwo != null) {
        		textViewTwo.setText(tv.getText());
                // put the id to identify the item clicked
        		textViewTwo.setTag(tv.getId());
        		textViewTwo.setFocusable(false);
        		textViewTwo.setClickable(false);
                    
       			textViewTwo.setTextSize(13);
       			textViewTwo.setTypeface(tfApplegaramound, Typeface.NORMAL);
       			textViewTwo.setGravity(Gravity.CENTER);
        	}
        }
        return v;
    }
	@Override
	public void onClick(View v) {
		Bundle bundle = new Bundle();
		bundle.putString("videoUrl", this.videoUrl);
		bundle.putString("lectureDataUrl", this.lectureDataUrl);
		
		Intent intent = new Intent(LODEclActivity.CL_CONTEXT, LODEActivity.class);
		intent.putExtras(bundle);
		LODEclActivity.CL_CONTEXT.startActivity(intent);
	}

}
