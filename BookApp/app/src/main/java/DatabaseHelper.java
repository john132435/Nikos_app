package com.example.bill.movierecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

//Core class for having Database creation and instantiation methods.


public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "BookWorm.db";

    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_PUBLISHER = "publisher";
        public static final String COLUMN_NAME_RATING = "rating";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER," +
                    FeedEntry.COLUMN_NAME_TITLE + " TEXT," +
                    FeedEntry.COLUMN_NAME_PUBLISHER + " TEXT," +
                    FeedEntry.COLUMN_NAME_RATING + " DOUBLE," +
                    "PRIMARY KEY(" + FeedEntry.COLUMN_NAME_TITLE + "," + FeedEntry.COLUMN_NAME_PUBLISHER + "))";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;


    //Core method for inserting a new entry to the database.
    public void InsertEntryToDb(String title, String publisher, double rating) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_TITLE, title);
        values.put(FeedEntry.COLUMN_NAME_PUBLISHER, publisher);
        values.put(FeedEntry.COLUMN_NAME_RATING, rating);
        long newRowId = db.insert(FeedEntry.TABLE_NAME, null, values);

    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    //Method for getting the rows of the table of the database.
    public int getRows() {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                DatabaseHelper.FeedEntry.COLUMN_NAME_TITLE,
                DatabaseHelper.FeedEntry.COLUMN_NAME_PUBLISHER,
                DatabaseHelper.FeedEntry.COLUMN_NAME_RATING
        };

        Cursor cursor = db.query(
                DatabaseHelper.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,            // The columns for the WHERE clause
                null,            // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        int returningRows = 0;
        while(cursor.moveToNext()) {
            returningRows++;
        }
        return returningRows;
    }

    //Method for finding the book inside the table of the database.
    public boolean findBook(String title, String publisher) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                DatabaseHelper.FeedEntry.COLUMN_NAME_TITLE,
                DatabaseHelper.FeedEntry.COLUMN_NAME_PUBLISHER,
                DatabaseHelper.FeedEntry.COLUMN_NAME_RATING
        };

        Cursor cursor = db.query(
                DatabaseHelper.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,            // The columns for the WHERE clause
                null,            // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        while(cursor.moveToNext()) {
            String tempTitle = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_TITLE));
            String tempPublisher = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_PUBLISHER));
            if(tempTitle.equals(title) && tempPublisher.equals(publisher)) {
                return true;
            }
        }

        return false;
    }
}
