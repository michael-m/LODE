package it.unitn.lode;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class LODEDownloadsActivity extends Activity{
    /** Called when the activity is first created. */
	private ImageView slideTemp = null;
	private Matrix currMatrix = null, prevMatrix = null;
	private int mode;
	private static final int ZOOM = 0, DRAG = 1, NONE = 2;
	private PointF start = null, mid = null;
	private float prevDist = 1f;
	RelativeLayout rlMain = null;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        rlMain = (RelativeLayout) findViewById(R.id.rlMain);
        rlMain.setBackgroundColor(Color.LTGRAY);

	}
}
