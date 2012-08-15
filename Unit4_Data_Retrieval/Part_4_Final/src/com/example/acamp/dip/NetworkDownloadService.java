package com.example.acamp.dip;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParserException;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

public class NetworkDownloadService extends IntentService {

    public static final String LOG_TAG = "PicasaService";

    // a reasonable user-agent string
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android "
            + android.os.Build.VERSION.RELEASE + ";"
            + Locale.getDefault().toString() + "; " + android.os.Build.DEVICE
            + "/" + android.os.Build.ID + ")";

    public NetworkDownloadService(String name) {
        super(name);
    }
    public NetworkDownloadService() {
        super("PicasaFeaturedService");
    }
    
    @Override
    protected void onHandleIntent(Intent paramIntent) {

        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        String str = paramIntent.getDataString();
        URL localURL;
        
        try {
            localURL = new URL(str);
            URLConnection localURLConnection = localURL.openConnection();

            if ((localURLConnection instanceof HttpURLConnection)) {

                HttpURLConnection localHttpURLConnection = (HttpURLConnection) localURLConnection;
                localHttpURLConnection.setRequestProperty("User-Agent",
                        USER_AGENT);


                int responseCode = localHttpURLConnection.getResponseCode();
                switch (responseCode) {
                case 200:

                    PicasaPullParser localPicasaPullParser = new PicasaPullParser();
                    localPicasaPullParser.parseXml(
                            localURLConnection.getInputStream());

                    Vector<ContentValues> cvv = localPicasaPullParser
                            .getImages();

                    int size = cvv.size();
                    ContentValues[] cvArray = new ContentValues[size];
                    cvArray = cvv.toArray(cvArray);
                    
                    getContentResolver().bulkInsert(
                            PicasaProvider.getUriByType(this,
                                    PicasaProvider.METADATA_QUERY), cvArray);
                    
                    break;
                case 304:
                    Log.d(LOG_TAG, "Response not modified.");
                    break;
                }
            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "onHandleIntent Complete");
    }

}
