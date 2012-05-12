package it.unitn.lode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

public class LODESettingsActivity extends Activity implements OnClickListener, OnFocusChangeListener {
	private final int SEARCHBAR = 9090, SAVE = 1209, RESET = 5902;
	private RelativeLayout rlLectures = null;
	private RelativeLayout.LayoutParams rlLecturesParams = null;
	private EditText etAddressBar = null;
	private Display devDisplay = null;
	private int scrWidth;
	private Typeface tfApplegaramound = null;
	private boolean firstTime = true;
	private Button btnSave = null, btnReset = null;
	private SharedPreferences lodeSettings = null;
	private SharedPreferences lodeDefaults = null;
	private SharedPreferences.Editor lodeEditor = null;
	private String coursesURL = null;
	private final String LODE_PREFS = "LODE_PREFS";
	private final String LODE_DEFAULTS = "LODE_DEFAULTS";
	private AlertDialog alertExit = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you want to leave LODE4Android?")
		       .setCancelable(true)
		       .setTitle("LODE4Android: Exit")
		       .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                LODESettingsActivity.this.finish();
		           }
		       })
		       .setNegativeButton("Stay", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id){
		        	   dialog.dismiss();
		           }
		       });
		alertExit = builder.create();
		alertExit.setOwnerActivity(this);

		devDisplay = getWindowManager().getDefaultDisplay();
        scrWidth = devDisplay.getWidth();

        tfApplegaramound = Typeface.createFromAsset(getAssets(), "fonts/Applegaramound.ttf");
        
        rlLectures = (RelativeLayout) findViewById(R.id.rlLectures);

        rlLecturesParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlLecturesParams.topMargin = 10;
        rlLecturesParams.leftMargin = 10;
        
        lodeSettings = getSharedPreferences(LODE_PREFS, MODE_PRIVATE);
        coursesURL = lodeSettings.getString("coursesURL", "http://latemar.science.unitn.it/itunes/feeds/");

        lodeDefaults = getSharedPreferences(LODE_DEFAULTS, MODE_PRIVATE);
        
        etAddressBar = new EditText(this);
        etAddressBar.setId(SEARCHBAR);
        etAddressBar.setOnClickListener(this);
        etAddressBar.setOnFocusChangeListener(this);
        etAddressBar.setTypeface(tfApplegaramound, Typeface.ITALIC);
        etAddressBar.setBackgroundResource(R.layout.address_bar);
        etAddressBar.setSingleLine(true);
        etAddressBar.setHintTextColor(Color.WHITE);
        etAddressBar.setHint(coursesURL);
        etAddressBar.setTextColor(Color.WHITE);
        etAddressBar.setGravity(Gravity.CENTER_VERTICAL);
        etAddressBar.setWidth(scrWidth / 2);
        rlLectures.addView(etAddressBar, rlLecturesParams);
        
        btnSave = new Button(this);
        btnSave.setText("Save Settings");
        btnSave.setOnClickListener(this);
        btnSave.setId(SAVE);
        
        btnReset = new Button(this);
        btnReset.setText("Reset to Default");
        btnReset.setOnClickListener(this);
        btnReset.setId(RESET);
        
        rlLecturesParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlLecturesParams.topMargin = 10;
        rlLecturesParams.leftMargin = scrWidth / 2 + 10;
        rlLectures.addView(btnSave, rlLecturesParams);

        rlLecturesParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rlLecturesParams.topMargin = 80;
        rlLecturesParams.leftMargin = scrWidth / 2 + 10;
        rlLectures.addView(btnReset, rlLecturesParams);
	}
	@Override
	public void onClick(View v) {
		if(v.getId() == SEARCHBAR){
			//etAddressBar.setHint("");
		}
		else if(v.getId() == SAVE){
			setPrefs();
			makeToast("Preferences have been saved.");
		}
		else if(v.getId() == RESET){
			resetPrefs();
		}
	}
	@Override
	public void onBackPressed() {
		alertExit.show();
	}
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
//		if(firstTime)
//			firstTime = false;
//		else{
//			if(v.getId() == SEARCHBAR){
//				if(hasFocus)
//					etAddressBar.setHint("");
//				else
//			        etAddressBar.setHint(coursesURL);
//			}
//		}
	}
	private void setPrefs(){
		lodeEditor = lodeSettings.edit();
		lodeEditor.putString("coursesURL", etAddressBar.getText().toString().trim());
		lodeEditor.commit();
	}
	private void makeToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	private void resetPrefs(){
		coursesURL = lodeDefaults.getString("coursesURL", null);
		etAddressBar.setText(coursesURL);
		setPrefs();
		makeToast("Defaults have been set.");
	}
}
