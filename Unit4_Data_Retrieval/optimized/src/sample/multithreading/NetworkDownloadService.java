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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParserException;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * This service is really a generic HTTP downloader. It will pull the content of
 * the passed-in URL and broadcast the state of the world to the application.
 */
public class NetworkDownloadService extends IntentService implements
		ProgressNotifier {
	public static final String BROADCAST_ACTION = "sample.downloader.PicasaContentDB";
	public static final String EXTRA_LOG = "PS.LOG";
	public static final String EXTRA_STATUS = "PS.EXS";
	public static final String LOG_TAG = "PicasaService";
	public static final int STATE_ACTION_STARTED = 0;
	public static final int STATE_ACTION_CONNECTING = 1;
	public static final int STATE_ACTION_PARSING = 2;
	public static final int STATE_ACTION_WRITING = 3;
	public static final int STATE_ACTION_COMPLETE = 4;
	public static final int STATE_LOG = -1;

	private LocalBroadcastManager mBroadcastManager;

	// a reasonable user-agent string
	public static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android "
			+ android.os.Build.VERSION.RELEASE + ";"
			+ Locale.getDefault().toString() + "; " + android.os.Build.DEVICE
			+ "/" + android.os.Build.ID + ")";

	public NetworkDownloadService() {
		super("PicasaFeaturedService");
	}

	@Override
	public void onCreate() {
		mBroadcastManager = LocalBroadcastManager.getInstance(this);
		super.onCreate();
	}

	void broadcastIntentWithState(int paramInt) {
		Intent localIntent = new Intent();
		localIntent.setAction(BROADCAST_ACTION);
		localIntent.putExtra(EXTRA_STATUS, paramInt);
		localIntent.addCategory(Intent.CATEGORY_DEFAULT);
		mBroadcastManager.sendBroadcast(localIntent);
	}

	public void notifyProgress(String paramString) {
		Intent localIntent = new Intent();
		localIntent.setAction(BROADCAST_ACTION);
		localIntent.putExtra(EXTRA_STATUS, -1);
		localIntent.putExtra(EXTRA_LOG, paramString);
		localIntent.addCategory(Intent.CATEGORY_DEFAULT);
		mBroadcastManager.sendBroadcast(localIntent);
	}

	@Override
	protected void onHandleIntent(Intent paramIntent) {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		String str = paramIntent.getDataString();
		final String[] dateProjection = new String[] {
				PicasaContentDB.AppdataColumns._ID,
				PicasaContentDB.AppdataColumns.DATE };
		URL localURL;
		Cursor cur = null;
		try {
			localURL = new URL(str);
			URLConnection localURLConnection = localURL.openConnection();
			if ((localURLConnection instanceof HttpURLConnection)) {
				broadcastIntentWithState(STATE_ACTION_STARTED);
				HttpURLConnection localHttpURLConnection = (HttpURLConnection) localURLConnection;
				localHttpURLConnection.setRequestProperty("User-Agent",
						USER_AGENT);
				cur = getContentResolver().query(
						PicasaContentDB.getUriByType(this,
								PicasaContentDB.APPDATA_QUERY), dateProjection,
						null, null, null);
				boolean bInsert;
				if (null != cur && cur.moveToFirst()) {
					long date = cur.getLong(1);
					if (0 != date) {
						localHttpURLConnection
								.setRequestProperty(
										"If-Modified-Since",
										org.apache.http.impl.cookie.DateUtils
												.formatDate(
														new Date(date),
														org.apache.http.impl.cookie.DateUtils.PATTERN_RFC1123));
					}
					bInsert = false;
				} else {
					bInsert = true;
				}
				broadcastIntentWithState(STATE_ACTION_CONNECTING);
				int responseCode = localHttpURLConnection.getResponseCode();
				switch (responseCode) {
				case 200:
					long returnDate = localHttpURLConnection.getLastModified();
					broadcastIntentWithState(STATE_ACTION_PARSING);
					PicasaPullParser localPicasaPullParser = new PicasaPullParser();
					localPicasaPullParser.parseXml(
							localURLConnection.getInputStream(), this);
					broadcastIntentWithState(STATE_ACTION_WRITING);
					Vector<ContentValues> cvv = localPicasaPullParser
							.getImages();
					int size = cvv.size();
					ContentValues[] cvArray = new ContentValues[size];
					cvArray = cvv.toArray(cvArray);
					getContentResolver().bulkInsert(
							PicasaContentDB.getUriByType(this,
									PicasaContentDB.METADATA_QUERY), cvArray);
					ContentValues dateCV = new ContentValues();
					dateCV.put(PicasaContentDB.AppdataColumns.DATE, returnDate);
					Uri appDataUri = PicasaContentDB.getUriByType(this,
							PicasaContentDB.APPDATA_QUERY);
					if (bInsert) {
						getContentResolver().insert(appDataUri, dateCV);
					} else {
						getContentResolver().update(
								appDataUri,
								dateCV,
								PicasaContentDB.AppdataColumns._ID + "="
										+ cur.getString(0), null);
					}
					break;
				case 304:
					Log.d(LOG_TAG, "Response not modified.");
					break;
				}
				broadcastIntentWithState(STATE_ACTION_COMPLETE);
			}
		} catch (MalformedURLException localMalformedURLException) {
			localMalformedURLException.printStackTrace();
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		} catch (XmlPullParserException localXmlPullParserException) {
			localXmlPullParserException.printStackTrace();
		} finally {
			if ( null != cur ) {
				cur.close();
			}
		}
		Log.d(LOG_TAG, "onHandleIntent Complete");
	}

}
