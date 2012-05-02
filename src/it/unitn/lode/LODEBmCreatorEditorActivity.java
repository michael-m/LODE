package it.unitn.lode;

import it.unitn.lode.contentprovider.BookmarksContentProvider;
import it.unitn.lode.data.db.BookmarksTable;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class LODEBmCreatorEditorActivity extends Activity {
	private EditText etNote = null;
	private Button btnAddBookmark = null;
	private Uri bookmarksUri = null;
	private RelativeLayout rlCEChild = null;
	private RelativeLayout.LayoutParams rlCEParams = null;
	private DisplayMetrics metrics = null;
	private static int scrWidth, scrHeight;
	private Bundle extras = null;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        
        scrWidth = metrics.widthPixels;
        scrHeight = metrics.heightPixels;

        setContentView(R.layout.create_edit_bm);

		rlCEChild = (RelativeLayout) findViewById(R.id.rlCEChild);
		
		etNote = new EditText(this);
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
		etNote.setWidth((scrHeight * 5) / 6);
		etNote.setHeight(scrHeight / 4);
		rlCEParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rlCEChild.addView(etNote, rlCEParams);

		rlCEParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rlCEParams.topMargin = scrHeight / 4;
		rlCEParams.leftMargin = scrWidth / 6;
		rlCEChild.addView(btnAddBookmark, rlCEParams);

//        rlCEParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//        rlCE.removeView(rlCEChild);
//		rlCE.addView(rlCEChild, rlCEParams);

		extras = getIntent().getExtras();

		// Check from the saved Instance
		bookmarksUri = (bundle == null) ? null : (Uri) bundle.getParcelable(BookmarksContentProvider.CONTENT_ITEM_TYPE);
		// Or passed from the other activity
		if (extras.getParcelable(BookmarksContentProvider.CONTENT_ITEM_TYPE) != null) {
			bookmarksUri = extras.getParcelable(BookmarksContentProvider.CONTENT_ITEM_TYPE);
			if(bookmarksUri == null)
				Log.e("bookmarksUri", "NULL");
			fillData(bookmarksUri);
		}
	}
	private void fillData(Uri uri) {
		String[] projection = {BookmarksTable.COLUMN_NOTE};
		Cursor cursor = getContentResolver().query(uri, projection, null, null,	null);
		if (cursor != null) {
			cursor.moveToFirst();
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
		values.put(BookmarksTable.COLUMN_TIME, extras.getString("time"));
		values.put(BookmarksTable.COLUMN_LECTURE_ID, extras.getString("lectureId"));
		if (bookmarksUri == null) {
			// New Bookmark
			bookmarksUri = getContentResolver().insert(BookmarksContentProvider.CONTENT_URI, values);
			} else {
			// Update Bookmark
			getContentResolver().update(bookmarksUri, values, null, null);
		}
	}
	private void makeToast(){
		Toast.makeText(this, "Please add a note to your bookmark", Toast.LENGTH_LONG).show();
	}
}
