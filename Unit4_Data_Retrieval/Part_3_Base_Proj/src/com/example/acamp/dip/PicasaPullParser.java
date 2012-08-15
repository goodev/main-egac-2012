package com.example.acamp.dip;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import com.example.acamp.dip.dummy.DummyContent;

public class PicasaPullParser extends DefaultHandler {
    private static final String CONTENT = "media:content";
    private static final String ITEM = "item";
//    private static final int NUM_IMAGES = 100;
    private static final String THUMBNAIL = "media:thumbnail";
//    private static ContentValues mImage;
//    private Vector<ContentValues> mImages;
    private static final String LOG_TAG = "PPP";

//    public Vector<ContentValues> getImages() {
//        return this.mImages;
//    }
    
    public void parseXml(InputStream paramInputStream)
            throws XmlPullParserException, IOException {
        XmlPullParserFactory localXmlPullParserFactory = XmlPullParserFactory
                .newInstance();
        localXmlPullParserFactory.setNamespaceAware(false);
        XmlPullParser localXmlPullParser = localXmlPullParserFactory
                .newPullParser();
        localXmlPullParser.setInput(paramInputStream, null);
        int i = localXmlPullParser.getEventType();
        int j = 1;
        if (i != 0)
            return;
//        this.mImages = new Vector<ContentValues>(NUM_IMAGES);
        while (true) {
            int k = localXmlPullParser.next();
            if (Thread.currentThread().isInterrupted())
                throw new XmlPullParserException("Cancelled");
            else if (k == XmlPullParser.END_DOCUMENT)
                break;
            else if (k == XmlPullParser.START_DOCUMENT)
                continue;
            else if (k == XmlPullParser.START_TAG) {
                String str1 = localXmlPullParser.getName();
                if (str1.equalsIgnoreCase(ITEM)) {
                    if ( Constants.LOGV ) {
                        Log.d(LOG_TAG, "ITEM");
                    }
//                    mImage = new ContentValues();
                } else {
                    String key;
                    if (str1.equalsIgnoreCase(CONTENT)) {
                        if ( Constants.LOGV ) {
                            Log.v(LOG_TAG, "CONTENT");
                        }
                        key = Constants.IMAGE_URL;
                    } else if (str1.equalsIgnoreCase(THUMBNAIL)) {
                        if ( Constants.LOGV ) {
                            Log.v(LOG_TAG, "THUMBNAIL");
                        }
                        key = Constants.IMAGE_THUMB_URL;
                    } else {
                        continue;
                    }
                    String value = localXmlPullParser.getAttributeValue(null, "url");
                    if (value == null)
                        break;
//                    mImage.put(key, value);
                    if (key.equals(Constants.IMAGE_URL)) {
                        DummyContent.addItem("Image " + j, value);                        
                    }
                }
            }
            else if ((k == XmlPullParser.END_TAG)
                    && (localXmlPullParser.getName().equalsIgnoreCase(ITEM))
//                    && (mImage != null)
                    ) {
//                this.mImages.add(mImage);
//                mImage = null;
                j++;
            }
        }
    }
}
