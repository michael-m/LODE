package it.unitn.lode;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

public class PlayInFullScreenActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fs_video);

		Bundle fsBundle = getIntent().getExtras();
		VideoView vvFsVideo = (VideoView) findViewById(R.id.vvFsVideo);

		vvFsVideo.setVideoURI(Uri.parse(fsBundle.getString("VideoURL")));
		vvFsVideo.seekTo(fsBundle.getInt("CurrPos"));
		vvFsVideo.start();
	}
}
