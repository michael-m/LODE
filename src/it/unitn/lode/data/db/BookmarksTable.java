package it.unitn.lode.data.db;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BookmarksTable {

	//Bookmarks table
	public static final String TABLE_BOOKMARKS = "bookmarks";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NOTE = "note";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_LECTURE_ID = "lecture_id";

	//SQL for creating DB and Table
	private static final String DATABASE_CREATE = "create table " + TABLE_BOOKMARKS	+ "("
			+ COLUMN_ID + " integer primary key autoincrement, " 
			+ COLUMN_NOTE + " text not null, " 
			+ COLUMN_TIME + " text not null, "
			+ COLUMN_LECTURE_ID + " text not null);";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(BookmarksTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
		onCreate(database);
	}
}
