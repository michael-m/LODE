package it.unitn.lode;

import java.util.ArrayList;
import java.util.List;
import it.unitn.lode.contentprovider.BookmarksContentProvider;
import it.unitn.lode.data.db.BookmarksTable;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

public class LODEDownloadsActivity extends ListActivity implements OnCreateContextMenuListener, OnItemLongClickListener{
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;
	private static final int DELETE_ID = Menu.FIRST + 1;
	private SimpleCursorAdapter adapter;
	private Button btnAddBookmark = null;
	private String selectedId = "";
	private List<String> bookmarkIds = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_dp);
		bookmarkIds = new ArrayList<String>();
		fillData();
		btnAddBookmark = (Button) findViewById(R.id.btnAddBookmark);
		btnAddBookmark.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), LODEBmCreatorEditorActivity.class);
				startActivityForResult(i, ACTIVITY_CREATE);
			}
		});
		registerForContextMenu(getListView());
		getListView().setOnItemLongClickListener(this);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(resultCode == Activity.RESULT_OK){
			refreshIds();
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}
	private void fillData() {
		String[] projection = {BookmarksTable.COLUMN_ID, BookmarksTable.COLUMN_NOTE, BookmarksTable.COLUMN_TIME};
		Cursor cursor = getContentResolver().query(BookmarksContentProvider.CONTENT_URI, projection, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				bookmarkIds.add(cursor.getString(cursor.getColumnIndex(BookmarksTable.COLUMN_ID)));
				cursor.moveToNext();
			}
			String[] from = new String[] {BookmarksTable.COLUMN_ID, BookmarksTable.COLUMN_NOTE, BookmarksTable.COLUMN_TIME};
			int[] to = new int[] {R.id.tvId, R.id.tvBookmarkNote, R.id.tvBookmarkTime};
			adapter = new SimpleCursorAdapter(this, R.layout.bookmark_row, cursor, from, to);
			getListView().setAdapter(adapter);
		}
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 1, "Delete bookmark");
		menu.add(0, ACTIVITY_EDIT, 0, "Edit bookmark");
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			Uri uri = Uri.parse(BookmarksContentProvider.CONTENT_URI + "/" + info.id);
			getContentResolver().delete(uri, null, null);
			fillData();
			return true;
		case ACTIVITY_EDIT:
			Intent i = new Intent(this, LODEBmCreatorEditorActivity.class);
			Uri bookmarkUri = Uri.parse(BookmarksContentProvider.CONTENT_URI + "/" + selectedId);
			i.putExtra(BookmarksContentProvider.CONTENT_ITEM_TYPE, bookmarkUri);
			startActivityForResult(i, ACTIVITY_EDIT);
			break;
		}
		return super.onContextItemSelected(item);
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		selectedId = bookmarkIds.get(position);
		return false;
	}
	private void refreshIds(){
		String[] projection = {BookmarksTable.COLUMN_ID};
		Cursor cursor = getContentResolver().query(BookmarksContentProvider.CONTENT_URI, projection, null, null, null);
		if (cursor != null) {
			bookmarkIds.removeAll(bookmarkIds);
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				bookmarkIds.add(cursor.getString(cursor.getColumnIndex(BookmarksTable.COLUMN_ID)));
				cursor.moveToNext();
			}
		}
	}
}
