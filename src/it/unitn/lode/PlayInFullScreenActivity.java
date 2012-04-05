package it.unitn.lode;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
//import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class PlayInFullScreenActivity extends Activity implements OnPreparedListener, OnCompletionListener {
	private VidView vvFsVideo = null;
	private LinearLayout llFsVideo = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fs_video);

		Bundle fsBundle = getIntent().getExtras();
		vvFsVideo = (VidView) findViewById(R.id.vvFsVideo);
		vvFsVideo.setOnPreparedListener(this);
		vvFsVideo.setOnCompletionListener(this);
		
		llFsVideo = (LinearLayout) findViewById(R.id.llFsVideo);

//		vvFsVideo.setVideoURI(Uri.parse(fsBundle.getString("VideoURL")));
		vvFsVideo.setVideoPath(fsBundle.getString("VideoURL"));
//		vvFsVideo = LODEActivity.vidView;
		vvFsVideo.seekTo(fsBundle.getInt("CurrPos"));
		fsBundle = null;
		vvFsVideo.start();
	}
	@Override
	protected void onResume() {
		super.onResume();
		llFsVideo.setVisibility(View.VISIBLE);
	}
	@Override
	public void onPause() {
		super.onPause();
		LODEActivity.currPos = vvFsVideo.getCurrentPosition();
		vvFsVideo.stopPlayback();
		vvFsVideo = null;
		finish();
	}
	@Override
	public void onPrepared(MediaPlayer mp) {
		llFsVideo.setVisibility(View.GONE);
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		LODEActivity.hasFinished = true;
		finish();
	}
}
