package it.unitn.lode;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.TabHost;
import android.widget.TextView;

public class LODETabsActivity extends TabActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs_main);

        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;

        Intent intent = new Intent().setClass(this, LODEActivity.class);

        TextView tv = new TextView(this);
        tv.setText("Lectures");
        tv.setBackgroundResource(R.layout.corners);

        TextView tv1 = new TextView(this);
        tv1.setText("Settings");
        tv1.setBackgroundResource(R.layout.corners);

        TextView tv2 = new TextView(this);
        tv2.setText("Downloads");
        tv2.setBackgroundResource(R.layout.corners);

        spec = tabHost.newTabSpec("Lectures").setIndicator(tv)
        		.setContent(intent);
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Downloads").setIndicator(tv2)
        		.setContent(intent);
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Settings").setIndicator(tv1)
        		.setContent(intent);
        tabHost.addTab(spec);
        
        LayoutParams tabParams = tabHost.getTabWidget().getLayoutParams();
//        tabParams.width = (LODEActivity.scrWidth * 3) /4;
        tabParams.height = 30;
        tabHost.getTabWidget().setLayoutParams(tabParams);
//        for(int a = 0; a < tabHost.getTabWidget().getChildCount(); a++){
//        }
        tabHost.setCurrentTab(1);
	}
}
