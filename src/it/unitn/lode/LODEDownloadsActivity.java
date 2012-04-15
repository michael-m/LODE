package it.unitn.lode;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class LODEDownloadsActivity extends Activity{
    /** Called when the activity is first created. */
	private ImageView slideTemp = null;
	private Matrix currMatrix = null, prevMatrix = null;
	private int mode;
	private static final int ZOOM = 0, DRAG = 1, NONE = 2;
	private PointF start = null, mid = null;
	private float prevDist = 1f;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloads);

        slideTemp = (ImageView) findViewById(R.id.slideTemp);
        slideTemp.setImageResource(R.drawable.slide);
    	slideTemp.setScaleType(ImageView.ScaleType.FIT_CENTER);
        start = new PointF();
        mid = new PointF();
        currMatrix = new Matrix();
        prevMatrix = new Matrix();

        slideTemp.setOnTouchListener(new OnTouchListener() {

        	@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(v.getId() == R.id.slideTemp){
					ImageView view = (ImageView) v;
					view.setScaleType(ImageView.ScaleType.MATRIX);
					
					switch(event.getAction() & MotionEvent.ACTION_MASK){
					case MotionEvent.ACTION_DOWN:{
						prevMatrix.set(currMatrix);
						start.set(event.getX(), event.getY());
						mode = DRAG;
				         Log.e("TAG", "mode=DRAG");
						break;
					}
					case MotionEvent.ACTION_UP:{
						mode = NONE;
				         Log.e("TAG", "mode=NONE");
						break;
					}
					case MotionEvent.ACTION_MOVE:{
						if(mode == DRAG){
							currMatrix.set(prevMatrix);
							currMatrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
							
						}
						else if (mode == ZOOM) {
							float newDist = spacing(event);
							if (newDist > 10f) {
								currMatrix.set(prevMatrix);
								float scale = newDist / prevDist;
								currMatrix.postScale(scale, scale, mid.x, mid.y);
							}
				          }
					break;
					}
					case MotionEvent.ACTION_POINTER_DOWN:{
				         prevDist = spacing(event);
				         if (prevDist > 10f) {
				            prevMatrix.set(currMatrix);
				            midPoint(mid, event);
				            mode = ZOOM;
					         Log.e("TAG", "mode=ZOOM");
				         }
						break;
					}
					case MotionEvent.ACTION_POINTER_UP:{
						mode = NONE;
						break;
					}
					default:
						break;
					}
					view.setImageMatrix(currMatrix);
					view.requestLayout();
					view.invalidate();
				}
				return true;
			}
		});
	}
	   /** Determine the space between the first two fingers */
	   private float spacing(MotionEvent event) {
	      float x = event.getX(0) - event.getX(1);
	      float y = event.getY(0) - event.getY(1);
	      return FloatMath.sqrt(x * x + y * y);
	   }

	   /** Calculate the mid point of the first two fingers */
	   private void midPoint(PointF point, MotionEvent event) {
	      float x = event.getX(0) + event.getX(1);
	      float y = event.getY(0) + event.getY(1);
	      point.set(x / 2, y / 2);
	   }
}
