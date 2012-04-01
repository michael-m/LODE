package it.unitn.lode;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class LODESettingsActivity extends Activity implements OnClickListener, OnFocusChangeListener {
	private final int SEARCHBAR = 0;
	private RelativeLayout rlLectures = null;
	private RelativeLayout.LayoutParams rlLecturesParams = null;
	private EditText etAddressBar = null;
	private Display devDisplay = null;
	private int scrWidth, scrHeight;
	private Typeface tfApplegaramound = null;
	private boolean firstTime = true;
	private ImageButton btnSearch = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        devDisplay = getWindowManager().getDefaultDisplay();
        scrWidth = devDisplay.getWidth();
        scrHeight = devDisplay.getHeight();

        tfApplegaramound = Typeface.createFromAsset(getAssets(), "fonts/Applegaramound.ttf");
        
        rlLectures = (RelativeLayout) findViewById(R.id.rlLectures);

        rlLecturesParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlLecturesParams.topMargin = 10;
        rlLecturesParams.leftMargin = 10;
        
        etAddressBar = new EditText(this);
        etAddressBar.setId(SEARCHBAR);
        etAddressBar.setOnClickListener(this);
        etAddressBar.setOnFocusChangeListener(this);
        etAddressBar.setTypeface(tfApplegaramound, Typeface.ITALIC);
        etAddressBar.setBackgroundResource(R.layout.address_bar);
        etAddressBar.setSingleLine(true);
        etAddressBar.setHintTextColor(Color.WHITE);
        etAddressBar.setHint("Touch to enter URL for courses");
        etAddressBar.setTextColor(Color.WHITE);
        etAddressBar.setGravity(Gravity.CENTER_VERTICAL);
        etAddressBar.setWidth(scrWidth / 2);
        rlLectures.addView(etAddressBar, rlLecturesParams);
        
        btnSearch = new ImageButton(this);
        btnSearch.setBackgroundResource(R.drawable.ic_menu_search);
        rlLecturesParams = new RelativeLayout.LayoutParams(60, 60);
        rlLecturesParams.topMargin = 10;
        rlLecturesParams.leftMargin = scrWidth / 2 + 10;
        rlLectures.addView(btnSearch, rlLecturesParams);
	}
	@Override
	public void onClick(View v) {
		if(v.getId() == SEARCHBAR){
			etAddressBar.setHint("");
		}
	}
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(firstTime)
			firstTime = false;
		else{
			if(v.getId() == SEARCHBAR){
				if(hasFocus)
					etAddressBar.setHint("");
				else
			        etAddressBar.setHint("Touch to enter URL for courses");
			}
		}
	}
}
