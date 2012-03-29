package it.unitn.lode;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

public class LODETabsActivity extends TabActivity implements OnTabChangeListener {
	private TabHost tabHost = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs_main);

        tabHost = getTabHost();
        tabHost.setOnTabChangedListener(this);
        TabHost.TabSpec spec;

        Intent intent = new Intent().setClass(this, LODEActivity.class);
        Intent intent1 = new Intent().setClass(this, LODEDownloadsActivity.class);
        Intent intent2 = new Intent().setClass(this, LODESettingsActivity.class);

        TextView tv = new TextView(this);
        tv.setText("Lectures");
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundResource(R.drawable.tab_normal);

        TextView tv1 = new TextView(this);
        tv1.setText("Settings");
        tv1.setGravity(Gravity.CENTER);
        tv1.setBackgroundResource(R.drawable.tab_normal);

        TextView tv2 = new TextView(this);
        tv2.setText("Downloads");
        tv2.setGravity(Gravity.CENTER);
        tv2.setBackgroundResource(R.drawable.tab_normal);

        spec = tabHost.newTabSpec("Lectures").setIndicator(tv)
        		.setContent(intent);
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Downloads").setIndicator(tv2)
        		.setContent(intent1);
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Settings").setIndicator(tv1)
        		.setContent(intent2);
        tabHost.addTab(spec);
        
        LinearLayout llTabs = (LinearLayout) findViewById(R.id.llTabs);
        LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams((LODEActivity.scrWidth * 3) /4, 30);
        tabParams.gravity = Gravity.CENTER;
        llTabs.setLayoutParams(tabParams);
//        for(int a = 0; a < tabHost.getTabWidget().getChildCount(); a++){
//        	tabParams = tabHost.getTabWidget().getChildAt(a).getLayoutParams();
//        	tabParams.width = 102;
//        	tabHost.getTabWidget().getChildAt(a).setLayoutParams(tabParams);
//        }
//        tabHost.setCurrentTab(0);
	}

	@Override
	public void onTabChanged(String tabId) {
	}
}
