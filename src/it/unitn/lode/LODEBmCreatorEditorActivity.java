package it.unitn.lode;

import it.unitn.lode.contentprovider.BookmarksContentProvider;
import it.unitn.lode.data.db.BookmarksTable;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class LODEBmCreatorEditorActivity extends Activity {
	private EditText etNote = null;
	private TextView tvTime = null;
	private Button btnAddBookmark = null;
	private Uri bookmarksUri = null;
	private RelativeLayout rlCE = null, rlCEChild = null;
	private RelativeLayout.LayoutParams rlCEParams = null;
	private DisplayMetrics metrics = null;
	private static int scrWidth, scrHeight;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        
        scrWidth = metrics.widthPixels;
        scrHeight = metrics.heightPixels;

        setContentView(R.layout.create_edit_bm);

		rlCE = (RelativeLayout) findViewById(R.id.rlCE);
		rlCEChild = new RelativeLayout(this);
		
		etNote = new EditText(this);
		tvTime = new TextView(this);
		btnAddBookmark = new Button(this);
		btnAddBookmark.setText("Add Bookmark");
		btnAddBookmark.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(etNote.getText().toString())){
					makeToast();
				} else {
					setResult(RESULT_OK);
					finish();
				}
			}
		});
		rlCEParams = new RelativeLayout.LayoutParams(200, 70);
		tvTime.setBackgroundResource(R.layout.corners);
		tvTime.setTextColor(Color.BLACK);
		tvTime.setText("2:41");
		rlCEChild.addView(tvTime, rlCEParams);
		rlCEParams = new RelativeLayout.LayoutParams(scrWidth / 2, 70);
		rlCEParams.topMargin = 100;
		rlCEChild.addView(etNote, rlCEParams);
		rlCEParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rlCEParams.topMargin = 200;
		rlCEChild.addView(btnAddBookmark, rlCEParams);
		rlCEParams = new RelativeLayout.LayoutParams(scrWidth / 2, scrHeight);
		rlCEParams.topMargin = 100;
		rlCEParams.leftMargin = 100;
		rlCE.addView(rlCEChild, rlCEParams);

		Bundle extras = getIntent().getExtras();

		// Check from the saved Instance
		bookmarksUri = (bundle == null) ? null : (Uri) bundle.getParcelable(BookmarksContentProvider.CONTENT_ITEM_TYPE);
		// Or passed from the other activity
		if (extras != null) {
			bookmarksUri = extras.getParcelable(BookmarksContentProvider.CONTENT_ITEM_TYPE);
			fillData(bookmarksUri);
		}
	}
	private void fillData(Uri uri) {
		String[] projection = { BookmarksTable.COLUMN_NOTE,	BookmarksTable.COLUMN_TIME};
		Cursor cursor = getContentResolver().query(uri, projection, null, null,	null);
		if (cursor != null) {
			cursor.moveToFirst();
			tvTime.setText(cursor.getString(cursor.getColumnIndexOrThrow(BookmarksTable.COLUMN_TIME)));
			etNote.setText(cursor.getString(cursor.getColumnIndexOrThrow(BookmarksTable.COLUMN_NOTE)));
			//Close the cursor
			cursor.close();
		}
	}
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState();
		outState.putParcelable(BookmarksContentProvider.CONTENT_ITEM_TYPE, bookmarksUri);
	}
	@Override
	protected void onPause() {
		super.onPause();
		saveState();
	}
	private void saveState() {
		String note = (String) etNote.getText().toString();
		// Only save if there is text in the 'note' field
		if (note.length() == 0) {
			return;
		}
		ContentValues values = new ContentValues();
		values.put(BookmarksTable.COLUMN_NOTE, note);
		values.put(BookmarksTable.COLUMN_TIME, "2:00");
		if (bookmarksUri == null) {
			// New Bookmark
			bookmarksUri = getContentResolver().insert(BookmarksContentProvider.CONTENT_URI, values);
			} else {
			// Update Bookmark
			getContentResolver().update(bookmarksUri, values, null, null);
		}
	}
	private void makeToast() {
		Toast.makeText(this, "Please add a note to your bookmark", Toast.LENGTH_LONG).show();
	}
}
