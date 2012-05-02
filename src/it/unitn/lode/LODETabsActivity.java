package it.unitn.lode;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

public class LODETabsActivity extends TabActivity{
	private TabHost tabHost = null;
	private Typeface tfApplegaramound = null;
	private Display devDisplay = null;
	public static int scrWidth, scrHeight;
	protected static AssetManager ASSETS = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs_main);

        ASSETS = getAssets();
        devDisplay = getWindowManager().getDefaultDisplay();
        scrWidth = devDisplay.getWidth();
        scrHeight = devDisplay.getHeight();

        tfApplegaramound = Typeface.createFromAsset(getAssets(), "fonts/Applegaramound.ttf");
        tabHost = getTabHost();
        TabHost.TabSpec spec;
        TabHost.TabSpec spec1;
        TabHost.TabSpec spec2;

        Intent intent = new Intent().setClass(this, LODEclActivity.class);
        Intent intent1 = new Intent().setClass(this, LODEDownloadsActivity.class);
        Intent intent2 = new Intent().setClass(this, LODESettingsActivity.class);

        TextView tv = new TextView(this);
        tv.setTextSize(13);
        tv.setTypeface(tfApplegaramound, Typeface.BOLD);
        tv.setText("Courses");
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundResource(R.drawable.tab_normal);

        TextView tv1 = new TextView(this);
        tv1.setTextSize(13);
        tv1.setTypeface(tfApplegaramound, Typeface.BOLD);
        tv1.setText("Settings");
        tv1.setGravity(Gravity.CENTER);
        tv1.setBackgroundResource(R.drawable.tab_normal);

        TextView tv2 = new TextView(this);
        tv2.setTextSize(13);
        tv2.setTypeface(tfApplegaramound, Typeface.BOLD);
        tv2.setText("Downloads");
        tv2.setGravity(Gravity.CENTER);
        tv2.setBackgroundResource(R.drawable.tab_normal);

        spec = tabHost.newTabSpec("Lectures").setIndicator(tv).setContent(intent);
        tabHost.addTab(spec);

        spec1 = tabHost.newTabSpec("Downloads").setIndicator(tv2).setContent(intent1);
        tabHost.addTab(spec1);

        spec2 = tabHost.newTabSpec("Settings").setIndicator(tv1).setContent(intent2);
        tabHost.addTab(spec2);
        
        LinearLayout llTabs = (LinearLayout) findViewById(R.id.llTabs);
        LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams((scrWidth * 3) /4, 30);
        tabParams.gravity = Gravity.CENTER;
        llTabs.setLayoutParams(tabParams);
	}
}
