package it.unitn.lode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class LODECompatibilityActivity extends Activity {
	private AlertDialog alertUnsupportedDevice = null;
	private DisplayMetrics metrics = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder = new AlertDialog.Builder(this);
		builder.setMessage("Sorry, this device is not supported by LODE4Android yet.")
		       .setCancelable(false)
		       .setTitle("LODE4Android: Unsupported Device")
		       .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.dismiss();
		        	   LODECompatibilityActivity.this.finish();
		           }
		       });
		alertUnsupportedDevice = builder.create();
		alertUnsupportedDevice.setOwnerActivity(this);
	    metrics = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metrics);
	    if(metrics.densityDpi == DisplayMetrics.DENSITY_LOW){
	    	alertUnsupportedDevice.show();
	    }
	    else{
	    	Intent intent = new Intent(this, LODETabsActivity.class);
	    	startActivity(intent);
	    	LODECompatibilityActivity.this.finish();
	    }
	}
}
