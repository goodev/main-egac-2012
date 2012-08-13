
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
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentValues;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * PicasaPullParser pulls pictures, presents progress. It's a very simple media RSS parser
 */
public class PicasaPullParser extends DefaultHandler {
    private static final String CONTENT = "media:content";
    private static final String ITEM = "item";
    private static final int NUM_IMAGES = 500;
    private static final String THUMBNAIL = "media:thumbnail";
    private static ContentValues mImage;
    private Vector<ContentValues> mImages;
    private static final String LOG_TAG = "PPP";

    public Vector<ContentValues> getImages() {
        return this.mImages;
    }

    public void parseXml(InputStream paramInputStream,
            ProgressNotifier paramProgressNotifier)
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
        this.mImages = new Vector<ContentValues>(NUM_IMAGES);
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
                    mImage = new ContentValues();
                } else {
                    String key;
                    if (str1.equalsIgnoreCase(CONTENT)) {
                        if ( Constants.LOGV ) {
                            Log.v(LOG_TAG, "CONTENT");
                        }
                        key = PicasaContentDB.PicasaFeatured.IMAGE_URL;
                    } else if (str1.equalsIgnoreCase(THUMBNAIL)) {
                        if ( Constants.LOGV ) {
                            Log.v(LOG_TAG, "THUMBNAIL");
                        }
                        key = PicasaContentDB.PicasaFeatured.IMAGE_THUMB_URL;
                    } else {
                        continue;
                    }
                    String value = localXmlPullParser.getAttributeValue(null, "url");
                    if (value == null)
                        break;
                    mImage.put(key, value);
                }
            }
            else if ((k == XmlPullParser.END_TAG)
                    && (localXmlPullParser.getName().equalsIgnoreCase(ITEM))
                    && (mImage != null)) {
                this.mImages.add(mImage);
                paramProgressNotifier.notifyProgress("Parsed Image[" + j + "]:"
                        + mImage.getAsString(PicasaContentDB.PicasaFeatured.IMAGE_URL));
                mImage = null;
                j++;
            }
        }
    }
}
