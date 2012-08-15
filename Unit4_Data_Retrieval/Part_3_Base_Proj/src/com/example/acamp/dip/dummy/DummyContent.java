package com.example.acamp.dip.dummy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

public class DummyContent {

    public static class DummyItem {

        public String id;
        public String content;

        public DummyItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return id;
        }
    }

    public static final String PREFS_GROUP = "DummyPrefs";
    public static final String LAST_ITEM_KEY = "ItemId";

    public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();
    public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    public static boolean initialized = false;

    public static void Init(Context context) {
        if (!initialized) {
            new ListAssetsTask().execute(context);
            initialized = true;
        }
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class ListAssetsTask extends AsyncTask<Context, Void, String[]> {

        Context mContext = null;
        String[] mAssetFiles = null;

        static final String ASSET_IMG_DIR = "images";

        @Override
        protected String[] doInBackground(Context... params) {

            mContext = params[0];

            AssetManager assetManager = mContext.getAssets();

            try {
                mAssetFiles = assetManager.list(ASSET_IMG_DIR);
            } catch (IOException e) {
                Log.e("tag", e.getMessage());
            }

            for (String filename : mAssetFiles) {
                addItem(new DummyItem(filename, ASSET_IMG_DIR + java.io.File.separator + filename));
            }
            return mAssetFiles;
        }

    } // ListAssetsTask

}
