package it.unitn.lode;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class LODEDownloadsActivity extends Activity{
    /** Called when the activity is first created. */
	private ImageView imView = null;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imView = new ImageView(this);
        imView.setBackgroundResource(R.drawable.ic_tab_artists_white);
        setContentView(imView);
	}
}