package it.unitn.lode.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookmarksDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "lode_data.db";
	private static final int DATABASE_VERSION = 1;

	public BookmarksDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	@Override
	public void onCreate(SQLiteDatabase database) {
		BookmarksTable.onCreate(database);
	}
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion){
		BookmarksTable.onUpgrade(database, oldVersion, newVersion);
	}
}
