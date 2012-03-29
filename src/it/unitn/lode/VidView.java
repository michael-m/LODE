package it.unitn.lode;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class VidView extends VideoView {

	public VidView(Context context) {
		super(context);
	}
	public VidView(Context context, AttributeSet attributeSet){
		super(context, attributeSet);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(0, widthMeasureSpec);
		int height = getDefaultSize(0, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}
}
