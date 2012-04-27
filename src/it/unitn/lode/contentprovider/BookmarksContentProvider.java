package it.unitn.lode.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import it.unitn.lode.data.db.BookmarksDbHelper;
import it.unitn.lode.data.db.BookmarksTable;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class BookmarksContentProvider extends ContentProvider {

	// database
	private BookmarksDbHelper database;

	// Used for the UriMatcher
	private static final int BOOKMARKS = 10;
	private static final int BOOKMARK_ID = 20;

	private static final String AUTHORITY = "it.unitn.lode.contentprovider.BookmarksContentProvider";

	private static final String BASE_PATH = "bookmarks";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/bookmarks";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/bookmarks";

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, BOOKMARKS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", BOOKMARK_ID);
	}

	@Override
	public boolean onCreate() {
		database = new BookmarksDbHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Check if the caller has requested a column which does not exist
		checkColumns(projection);

		// Set the table
		queryBuilder.setTables(BookmarksTable.TABLE_BOOKMARKS);
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case BOOKMARKS:
			break;
		case BOOKMARK_ID:
			// Adding the ID to the original query
			queryBuilder.appendWhere(BookmarksTable.COLUMN_ID + "="	+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		// Make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case BOOKMARKS:
			id = sqlDB.insert(BookmarksTable.TABLE_BOOKMARKS, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case BOOKMARKS:
			rowsDeleted = sqlDB.delete(BookmarksTable.TABLE_BOOKMARKS, selection, selectionArgs);
			break;
		case BOOKMARK_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(BookmarksTable.TABLE_BOOKMARKS, BookmarksTable.COLUMN_ID + "=" + id,	null);
			}
			else{
				rowsDeleted = sqlDB.delete(BookmarksTable.TABLE_BOOKMARKS, BookmarksTable.COLUMN_ID + "=" + id + " and "
						+ selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,	String[] selectionArgs) {

		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case BOOKMARKS:
			rowsUpdated = sqlDB.update(BookmarksTable.TABLE_BOOKMARKS, values, selection, selectionArgs);
			break;
		case BOOKMARK_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(BookmarksTable.TABLE_BOOKMARKS, values, BookmarksTable.COLUMN_ID + "=" + id,	null);
			} else {
				rowsUpdated = sqlDB.update(BookmarksTable.TABLE_BOOKMARKS, values, BookmarksTable.COLUMN_ID + "=" + id 
						+ " and " + selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		String[] available = {BookmarksTable.COLUMN_NOTE, BookmarksTable.COLUMN_TIME, BookmarksTable.COLUMN_ID,
				BookmarksTable.COLUMN_LECTURE_ID};
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(	Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}

}
