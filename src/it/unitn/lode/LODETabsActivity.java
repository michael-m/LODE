package it.unitn.lode;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

public class LODETabsActivity extends TabActivity{
	private TabHost tabHost = null;
	private Typeface tfApplegaramound = null;
	public static int scrWidth, scrHeight;
	protected static AssetManager ASSETS = null;
	private DisplayMetrics metrics = null;
	private int SCR_LAYOUT;
	private final int SCR_MASK = Configuration.SCREENLAYOUT_SIZE_MASK;
	private final int SCR_NORMAL = Configuration.SCREENLAYOUT_SIZE_NORMAL;
	private final int MEDIUM_DENSITY_PHONE = 9999;
	private final int MEDIUM_DENSITY_TABLET = 8888;
	private final int HIGH_DENSITY_PHONE = 7777;
	private final int HIGH_DENSITY_TABLET = 6666;
	private int THIS_DEVICE;
	private SharedPreferences lodeDefaults = null;
	private SharedPreferences.Editor lodeEditor = null;
	private final String LODE_DEFAULTS = "LODE_DEFAULTS";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs_main);

        lodeDefaults = getSharedPreferences(LODE_DEFAULTS, MODE_PRIVATE);
        if(lodeDefaults.getString("coursesURL", null) == null){
        	lodeEditor = lodeDefaults.edit();
        	lodeEditor.putString("coursesURL", "http://latemar.science.unitn.it/itunes/feeds/");
        	lodeEditor.commit();
        }

        ASSETS = getAssets();
        metrics = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metrics);
	    SCR_LAYOUT = getResources().getConfiguration().screenLayout;
        THIS_DEVICE = getDeviceCategory();
	    
        scrWidth = metrics.widthPixels;
        scrHeight = metrics.heightPixels;

        tfApplegaramound = Typeface.createFromAsset(getAssets(), "fonts/Applegaramound.ttf");
        tabHost = getTabHost();
        TabHost.TabSpec spec;
        TabHost.TabSpec spec1;
//        TabHost.TabSpec spec2;
        TabHost.TabSpec spec3;

        Intent intent = new Intent().setClass(this, LODEclActivity.class);
        Intent intent1 = new Intent().setClass(this, LODEDownloadsActivity.class);
//        Intent intent2 = new Intent().setClass(this, TunesViewerActivity.class);
        Intent intent3 = new Intent().setClass(this, LODESettingsActivity.class);

        TextView tv = new TextView(this);
        if(THIS_DEVICE == MEDIUM_DENSITY_PHONE){
        	tv.setTextSize(14);
            tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        else if(THIS_DEVICE == HIGH_DENSITY_PHONE){
            tv.setTypeface(tfApplegaramound, Typeface.BOLD);
        	tv.setTextSize(15);
        }
        else{
            tv.setTypeface(tfApplegaramound, Typeface.BOLD);
        	tv.setTextSize(17);
        }
        tv.setText("Courses");
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundResource(R.drawable.tab_normal);

        TextView tv1 = new TextView(this);
        if(THIS_DEVICE == MEDIUM_DENSITY_PHONE){
        	tv1.setTextSize(14);
            tv1.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        else if(THIS_DEVICE == HIGH_DENSITY_PHONE){
            tv1.setTypeface(tfApplegaramound, Typeface.BOLD);
        	tv1.setTextSize(15);
        }
        else{
            tv1.setTypeface(tfApplegaramound, Typeface.BOLD);
        	tv1.setTextSize(17);
        }
        tv1.setText("Downloads");
        tv1.setGravity(Gravity.CENTER);
        tv1.setBackgroundResource(R.drawable.tab_normal);

//        TextView tv2 = new TextView(this);
//        if(THIS_DEVICE == MEDIUM_DENSITY_PHONE){
//        	tv2.setTextSize(14);
//            tv2.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//        }
//        else if(THIS_DEVICE == HIGH_DENSITY_PHONE){
//            tv2.setTypeface(tfApplegaramound, Typeface.BOLD);
//        	tv2.setTextSize(15);
//        }
//        else{
//            tv2.setTypeface(tfApplegaramound, Typeface.BOLD);
//        	tv2.setTextSize(17);
//        }
//        tv2.setText("iTunes U");
//        tv2.setGravity(Gravity.CENTER);
//        tv2.setBackgroundResource(R.drawable.tab_normal);

        TextView tv3 = new TextView(this);
        if(THIS_DEVICE == MEDIUM_DENSITY_PHONE){
        	tv3.setTextSize(14);
            tv3.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        else if(THIS_DEVICE == HIGH_DENSITY_PHONE){
            tv3.setTypeface(tfApplegaramound, Typeface.BOLD);
        	tv3.setTextSize(15);
        }
        else{
            tv3.setTypeface(tfApplegaramound, Typeface.BOLD);
        	tv3.setTextSize(17);
        }
        tv3.setText("Settings");
        tv3.setGravity(Gravity.CENTER);
        tv3.setBackgroundResource(R.drawable.tab_normal);
        
        spec = tabHost.newTabSpec("Courses").setIndicator(tv).setContent(intent);
        tabHost.addTab(spec);

        spec1 = tabHost.newTabSpec("Downloads").setIndicator(tv1).setContent(intent1);
        tabHost.addTab(spec1);

//        spec2 = tabHost.newTabSpec("iTunesU").setIndicator(tv2).setContent(intent2);
//        tabHost.addTab(spec2);
        
        spec3 = tabHost.newTabSpec("Settings").setIndicator(tv3).setContent(intent3);
        tabHost.addTab(spec3);

        LinearLayout llTabs = (LinearLayout) findViewById(R.id.llTabs);
        LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams((scrWidth * 3) /4, 30);
        tabParams.gravity = Gravity.CENTER;
        llTabs.setLayoutParams(tabParams);
        setDefaultTab(0);
        tabHost.setCurrentTab(0);
	}
	private int getDeviceCategory(){
    	if(metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM){
    		if((SCR_LAYOUT & SCR_MASK) == SCR_NORMAL){
    			return MEDIUM_DENSITY_PHONE;
    		}else{
    			return MEDIUM_DENSITY_TABLET;
    		}
    	}
    	else{
    		if((SCR_LAYOUT & SCR_MASK) == SCR_NORMAL){
    			return HIGH_DENSITY_PHONE;
    		}else{
    			return HIGH_DENSITY_TABLET;
    		}
    	}
	}
}
