package it.unitn.lode;

import android.content.Context;
import android.view.View;
import android.widget.MediaController;

public class MediaCnt extends MediaController {

	public MediaCnt(Context context, View view) {
		super(context);
		super.setAnchorView(view);
	}
	@Override
	public void setAnchorView(View view) {
	}
	
}
