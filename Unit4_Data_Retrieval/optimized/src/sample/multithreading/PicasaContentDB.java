package sample.multithreading;

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.SparseArray;

import java.lang.reflect.Field;

public class PicasaContentDB extends ContentProvider {
	private static final String DATABASE_NAME = "PicasaFavesDB";
	private static final int DATABASE_VERSION = 2;
	public static final String LOG_TAG = PicasaContentDB.class.getName();
	SQLiteOpenHelper mHelper;
	private UriMatcher mUriMatcher;
	SparseArray<String> mMimeTypes;

	public PicasaContentDB() {
	}

	private long handleImage(String imageURL, String thumbURL,
			SQLiteDatabase paramSQLiteDatabase) {
		ContentValues localContentValues = new ContentValues();
		localContentValues.put(PicasaFeatured.IMAGE_URL, imageURL);
		localContentValues.put(PicasaFeatured.IMAGE_THUMB_URL, thumbURL);
		return paramSQLiteDatabase.insert(PicasaFeatured.TABLE_NAME,
				PicasaFeatured.IMAGE_URL, localContentValues);
	}

	public void close() {
		this.mHelper.close();
	}

	public Cursor getFeaturedImages(String[] projection) {
		return this.mHelper.getReadableDatabase().query(
				PicasaFeatured.TABLE_NAME, projection, null, null, null, null,
				null);
	}

	protected static class PicasaContentDBHelper extends SQLiteOpenHelper {
		PicasaContentDBHelper(Context paramContext) {
			super(paramContext, DATABASE_NAME, null, DATABASE_VERSION);
		}

		private String createTableQueryFromArray(String paramString,
				String[][] paramArrayOfString) {
			StringBuilder localStringBuilder = new StringBuilder();
			localStringBuilder.append("CREATE TABLE ");
			localStringBuilder.append(paramString);
			localStringBuilder.append(" (");
			int i = paramArrayOfString.length;
			for (int j = 0;; j++) {
				if (j >= i) {
					localStringBuilder
							.setLength(localStringBuilder.length() - 1);
					localStringBuilder.append(");");
					return localStringBuilder.toString();
				}
				String[] arrayOfString = paramArrayOfString[j];
				localStringBuilder.append(' ');
				localStringBuilder.append(arrayOfString[0]);
				localStringBuilder.append(' ');
				localStringBuilder.append(arrayOfString[1]);
				localStringBuilder.append(',');
			}
		}

		private void dropTables(SQLiteDatabase paramSQLiteDatabase) {
			Class<?>[] arrayOfClass = PicasaContentDB.class
					.getDeclaredClasses();
			int numClasses = arrayOfClass.length;
			for (int i = 0; i < numClasses; i++) {
				Class<?> localClass = arrayOfClass[i];
				if (BaseColumns.class.isAssignableFrom(localClass)) {
					try {
						String str = (String) localClass.getDeclaredField(
								"TABLE_NAME").get(null);
						paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + str);
					} catch (Exception localException) {
						while (true)
							localException.printStackTrace();
					}
				}
			}
		}

		@Override
		public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
			Class<?>[] arrayOfClass = PicasaContentDB.class
					.getDeclaredClasses();
			int numClasses = arrayOfClass.length;
			for (int i = 0; i < numClasses; i++) {
				Class<?> localClass = arrayOfClass[i];
				if (BaseColumns.class.isAssignableFrom(localClass)) {
					try {
						Field localField1 = localClass
								.getDeclaredField("SCHEMA");
						Field localField2 = localClass
								.getDeclaredField("TABLE_NAME");
						String[][] arrayOfString = (String[][]) localField1
								.get(null);
						paramSQLiteDatabase.execSQL(createTableQueryFromArray(
								(String) localField2.get(null), arrayOfString));
					} catch (Exception localException) {
						while (true)
							localException.printStackTrace();
					}
				}
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase paramSQLiteDatabase,
				int paramInt1, int paramInt2) {
			Log.w(PicasaContentDBHelper.class.getName(),
					"Upgrading database from version " + paramInt1 + " to "
							+ paramInt2 + ", which will destroy all old data");
			dropTables(paramSQLiteDatabase);
			onCreate(paramSQLiteDatabase);
		}
	}

	public static final int METADATA_QUERY = 1;
	public static final int APPDATA_QUERY = 2;

	public static class PicasaFeatured implements BaseColumns {
		public static final String IMAGE_THUMB_URL = "thumbURL";
		public static final String IMAGE_URL = "imageURL";
		public static final String[][] SCHEMA = {
				{ BaseColumns._ID, "INTEGER PRIMARY KEY" },
				{ IMAGE_THUMB_URL, "TEXT" }, { IMAGE_URL, "TEXT" } };
		public static final String TABLE_NAME = "PicasaFeatured";
		public static final String _ID = "PicasaFeatured._id";
	}

	public static class AppdataColumns implements BaseColumns {
		public static final String DATE = "DOWNLOADDATE";

		public static final String[][] SCHEMA = {
				{ BaseColumns._ID, "INTEGER PRIMARY KEY" }, { DATE, "INTEGER" } };
		public static final String TABLE_NAME = "MetadataColumns";
		public static final String _ID = "MetadataColumns._id";
	}

	static public Uri getUriByType(Context cxt, int type) {
		String authority = cxt.getString(R.string.picasa_authority);
		Uri authorityUri = Uri.parse("content://" + authority);
		switch (type) {
		case METADATA_QUERY:
			return Uri.withAppendedPath(authorityUri,
					cxt.getString(R.string.picasa_faves_table));
		case APPDATA_QUERY:
			return Uri.withAppendedPath(authorityUri,
					cxt.getString(R.string.application_data_table));
		}
		return null;
	}

	@Override
	public boolean onCreate() {
		mHelper = new PicasaContentDBHelper(getContext());
		String authority = getContext().getString(R.string.picasa_authority);
		String table = getContext().getString(R.string.picasa_faves_table);
		mUriMatcher = new UriMatcher(0);
		mUriMatcher.addURI(authority, table, METADATA_QUERY);

		/*
		 * Standard Android mime types using the vnd (vendor-specific) mime type
		 * vnd.android.cursor.dir/vnd.[authority].[table] for a table or
		 * vnd.android.cursor.item/vnd.[authority].[table] for a single item
		 */
		mMimeTypes = new SparseArray<String>();
		mMimeTypes.put(METADATA_QUERY, "vnd.android.cursor.dir/vnd."
				+ authority + "." + table);

		table = getContext().getString(R.string.application_data_table);
		mUriMatcher.addURI(authority, table, APPDATA_QUERY);
		mMimeTypes.put(APPDATA_QUERY, "vnd.android.cursor.item/vnd."
				+ authority + "." + table);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch (mUriMatcher.match(uri)) {
		case METADATA_QUERY:
			Cursor returnCursor = getFeaturedImages(projection);
			returnCursor.setNotificationUri(getContext().getContentResolver(),
					uri);
			return returnCursor;
		case APPDATA_QUERY:
			SQLiteDatabase localSQLiteDatabase = this.mHelper
					.getReadableDatabase();
			return localSQLiteDatabase
					.query(AppdataColumns.TABLE_NAME, projection, selection,
							selectionArgs, null, null, sortOrder);
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		return this.mMimeTypes.get(mUriMatcher.match(uri));
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (mUriMatcher.match(uri)) {
		case APPDATA_QUERY:
			SQLiteDatabase localSQLiteDatabase = this.mHelper
					.getWritableDatabase();
			long id = localSQLiteDatabase.insert(AppdataColumns.TABLE_NAME,
					AppdataColumns.DATE, values);
			if (-1 != id) {
				getContext().getContentResolver().notifyChange(uri, null);
				return Uri.withAppendedPath(uri, Long.toString(id));
			}
			break;
		}
		return null;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		switch (mUriMatcher.match(uri)) {
		case METADATA_QUERY:
			SQLiteDatabase localSQLiteDatabase = this.mHelper
					.getWritableDatabase();
			localSQLiteDatabase.beginTransaction();
			localSQLiteDatabase.delete(PicasaFeatured.TABLE_NAME, null, null);
			int numValues = values.length;
			for (int i = 0; i < numValues; i++) {
				ContentValues row = values[i];
				handleImage(row.getAsString(PicasaFeatured.IMAGE_URL),
						row.getAsString(PicasaFeatured.IMAGE_THUMB_URL),
						localSQLiteDatabase);
			}
			localSQLiteDatabase.setTransactionSuccessful();
			localSQLiteDatabase.endTransaction();
			localSQLiteDatabase.close();
			getContext().getContentResolver().notifyChange(uri, null);
			return numValues;
		}
		return super.bulkInsert(uri, values);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		switch (mUriMatcher.match(uri)) {
		case APPDATA_QUERY:
			SQLiteDatabase localSQLiteDatabase = this.mHelper
					.getWritableDatabase();
			int rows = localSQLiteDatabase.update(AppdataColumns.TABLE_NAME,
					values, selection, selectionArgs);
			if (0 != rows) {
				getContext().getContentResolver().notifyChange(uri, null);
			}
			break;
		}
		return 0;
	}
}
