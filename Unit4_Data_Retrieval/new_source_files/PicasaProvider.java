package com.example.acamp.dip;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.SparseArray;


public class PicasaProvider extends ContentProvider {

    // Who says? I say.
    public static final String LOG_TAG = PicasaProvider.class.getName();

    // Handle to a new DatabaseHelper.
    SQLiteOpenHelper mHelper;
    // Uri matcher to decode incoming URIs.
    private UriMatcher mUriMatcher;
    // MIME types that apply to the results of queries
    SparseArray<String> mMimeTypes;

    // The incoming URI matches the main table URI pattern
    public static final int METADATA_QUERY = 1;
    // The incoming URI matches the main table row ID URI pattern
    public static final int METADATA_QUERY_ID = 2;

    private static final String ITEM_PATTERN = "/#";

    @Override
    public boolean onCreate() {

        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMimeTypes = new SparseArray<String>();

        String authority = getContext().getString(R.string.picasa_authority);
        mHelper = new PicasaFeatured.PicasaContentDBHelper(getContext());

        
        /*
         * Standard Android mime types using the vnd (vendor-specific) mime type
         * vnd.android.cursor.dir/vnd.[authority].[table] for a table or
         * vnd.android.cursor.item/vnd.[authority].[table] for a single item
         */

        String table = getContext().getString(R.string.picasa_faves_table);
        mUriMatcher.addURI(authority, table, METADATA_QUERY);
        mUriMatcher.addURI(authority, table + ITEM_PATTERN, METADATA_QUERY_ID);

        mMimeTypes.put(METADATA_QUERY, "vnd.android.cursor.dir/vnd." + authority + "." + table);
        mMimeTypes.put(METADATA_QUERY_ID, "vnd.android.cursor.item/vnd." + authority + "." + table);

        return true;
    }

    public static boolean initialized;

    static {
        initialized = false;
    }
    
    public static void Init(Context context) {
        if (!initialized) {
            
            // adapted from Picasa sample
            Intent initIntent = new Intent(context, NetworkDownloadService.class);
            Uri localUri = Uri.parse(Constants.PICASA_RSS_URL);
            initIntent.setData(localUri);
            context.startService(initIntent);

            initialized = true;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Get a reference to the database.
        SQLiteDatabase db = mHelper.getWritableDatabase();

        // Number of rows matching selector.
        int count = 0;

        switch (mUriMatcher.match(uri)) {
            case METADATA_QUERY: {
                // If URI is main table, delete uses incoming where clause and args.
                count = db.delete(PicasaFeatured.TABLE_NAME, selection, selectionArgs);
                break;
            }

            case METADATA_QUERY_ID: {
                // If URI is for a particular row ID, delete is based on incoming
                // data but modified to restrict to the given ID. Modifies the where
                // clause to restrict it to the particular note ID.
                String finalWhere =
                        DatabaseUtils.concatenateWhere(PicasaFeatured._ID + " = "
                                + ContentUris.parseId(uri), selection);

                // Delete the row(s) from the database.
                // There should only be either 0 or 1 matching row.
                count = db.delete(PicasaFeatured.TABLE_NAME, finalWhere, selectionArgs);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Notify any clients referring to this content provider.
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public String getType(Uri uri) {
        return mMimeTypes.get(mUriMatcher.match(uri));
    }

    static public Uri getUriByType(Context cxt, int type) {
        String authority = cxt.getString(R.string.picasa_authority);
        Uri authorityUri = Uri.parse("content://" + authority);
        switch (type) {
        case METADATA_QUERY:
            return Uri.withAppendedPath(authorityUri,
                    cxt.getString(R.string.picasa_faves_table));
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (mUriMatcher.match(uri)) {
            case METADATA_QUERY: {
                SQLiteDatabase localSQLiteDatabase = this.mHelper.getWritableDatabase();
                long id = localSQLiteDatabase.insert(PicasaFeatured.TABLE_NAME, null, values);
                if (id > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(uri, Long.toString(id));
                }
                break;
            }

            default:
                break;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        Cursor returnCursor = null;
        switch (mUriMatcher.match(uri)) {
            case METADATA_QUERY:
            {
                // If the incoming URI is for whole table.
                returnCursor = getFeaturedImages(projection);
                returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            }
            case METADATA_QUERY_ID:
            {
                // The incoming URI is for a single row.
                // Construct a new query builder and sets its table name.
                SQLiteDatabase db = mHelper.getReadableDatabase();
                
                // If URI is for a particular row ID, update is based on incoming
                // data but modified to restrict to the given ID.
                String finalWhere =
                        DatabaseUtils.concatenateWhere(PicasaFeatured._ID + " = "
                                + ContentUris.parseId(uri), selection);

                returnCursor = db.query(PicasaFeatured.TABLE_NAME, projection, finalWhere, selectionArgs, null, null, null);
                break;
            }
        }

        return returnCursor;
    }

    public Cursor getFeaturedImages(String[] projection) {
        return this.mHelper.getReadableDatabase().query(PicasaFeatured.TABLE_NAME, projection,
                null, null, null, null, null);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase localSQLiteDatabase = mHelper.getWritableDatabase();

        // Number of rows matching selector.
        int count = 0;

        switch (mUriMatcher.match(uri)) {
            case METADATA_QUERY: {
                // If URI is main table, update uses incoming where clause and args.
                count =
                        localSQLiteDatabase.update(PicasaFeatured.TABLE_NAME, values, selection,
                                selectionArgs);
                break;
            }
            case METADATA_QUERY_ID: {
                // If URI is for a particular row ID, update is based on incoming
                // data but modified to restrict to the given ID.
                String finalWhere =
                        DatabaseUtils.concatenateWhere(PicasaFeatured._ID + " = "
                                + ContentUris.parseId(uri), selection);

                // Update the row(s) from the database.
                // There should only be either 0 or 1 matching row.
                count =
                        localSQLiteDatabase.update(PicasaFeatured.TABLE_NAME, values, finalWhere,
                                selectionArgs);
                break;
            }
        }
        return count;
    }

    /**
     * SQLite Database holding URLs retrieved from the Internet feed. BaseColumns adds a couple of
     * properties that ListView relies on: particularly the _ID column, a monotonically increasing
     * unique integer used to index the table.
     */
    public static class PicasaFeatured implements BaseColumns {

        private static final String DATABASE_NAME = "PicasaFavesDB";
        private static final int DATABASE_VERSION = 2;

        // SQL table name
        public static final String TABLE_NAME = "PicasaFeatured";

        // SQL table column names
        public static final String IMAGE_THUMB_URL = "thumbURL";
        public static final String IMAGE_URL = "imageURL";

        /**
         * SQLiteOpenHelper takes care of some of the basics of maintaining the database: Creating,
         * opening, closing, and updating the tables at the right times.
         */
        public static class PicasaContentDBHelper extends SQLiteOpenHelper {
            PicasaContentDBHelper(Context paramContext) {
                super(paramContext, DATABASE_NAME, null, DATABASE_VERSION);
            }

            /**
             * Creates the underlying database with table name and column names defined above.
             */
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY,"
                        + IMAGE_THUMB_URL + " TEXT," + IMAGE_URL + " TEXT" + ");");
            }

            /**
             * Deletes the old table and recreates database. A more sophisticated application would
             * convert and transfer records from the old version of the database to the new one.
             */
            @Override
            public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
                Log.w(PicasaContentDBHelper.class.getName(), "Migrating database from version "
                        + paramInt1 + " to " + paramInt2 + ", which destroys all old data.");
                // Kills the table and existing data
                paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(paramSQLiteDatabase);
            }

            @Override
            public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                onUpgrade(db, oldVersion, newVersion);
            }
        } // PicasaContentDBHelper

        // These are the rows that we will retrieve.
        public static final String[] PROJ_LONG_URL = new String[] { 
                PicasaProvider.PicasaFeatured._ID,
                PicasaProvider.PicasaFeatured.IMAGE_URL, 
        };
    }
}
