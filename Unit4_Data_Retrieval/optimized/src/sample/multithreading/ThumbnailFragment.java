
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
import compatible.actionbar.ActionBarFragmentActivity;

import sample.multithreading.PicasaContentDB.PicasaFeatured;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.RejectedExecutionException;

/**
 * The thumbnail fragment displays the grid of thumbnails used
 */
public class ThumbnailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    private static final String STATE_IS_HIDDEN = "SIH";
    private int mColumnWidth;
    private Drawable mEmpty;
    private GridView mGridView;
    private boolean mLoaded;
    private Intent mServiceIntent;
    private PicasaListAdapter mAdapter;
    private static final String PICASA_RSS_URL = "http://picasaweb.google.com/data/feed/base/featured?alt=rss&kind=photo&access=public&slabel=featured&hl=en_US&imgmax=1600";

    //   private static final int CURSOR_ID_INDEX = 0;
    private static final int IMAGE_THUMB_URL_INDEX = 2;
//    private static final int IMAGE_URL_INDEX = 1;    

    private static final String[] PROJECTION;
    static
    {
        String[] arrayOfString = new String[3];
        arrayOfString[0] = PicasaFeatured._ID;
        arrayOfString[1] = PicasaFeatured.IMAGE_URL;
        arrayOfString[2] = PicasaFeatured.IMAGE_THUMB_URL;

        PROJECTION = arrayOfString;
    }
    
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle)
    {
        CursorLoader cl = new CursorLoader(getActivity());
        cl.setUri(PicasaContentDB.getUriByType(getActivity(), PicasaContentDB.METADATA_QUERY));
        cl.setProjection(PROJECTION);
        return cl;
    }

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater,
            ViewGroup paramViewGroup, Bundle paramBundle) {
        super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
        View localView = paramLayoutInflater.inflate(R.layout.gridlist,
                paramViewGroup, false);
        this.mAdapter = new PicasaListAdapter(getActivity());
        this.mGridView = ((GridView) localView.findViewById(android.R.id.list));
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(localDisplayMetrics);
        int i = getResources().getDimensionPixelSize(R.dimen.thumbSize);
        int j = localDisplayMetrics.widthPixels / i;
        this.mColumnWidth = (localDisplayMetrics.widthPixels / j);
        this.mGridView.setColumnWidth(this.mColumnWidth);
        this.mGridView.setNumColumns(-1);
        this.mGridView.setAdapter(this.mAdapter);
        this.mGridView.setOnItemClickListener(this);
        this.mGridView.setEmptyView(localView.findViewById(R.id.progressRoot));
        this.mEmpty = getResources().getDrawable(R.drawable.imagenotqueued);
        getLoaderManager().initLoader(0, null, this);
        this.mServiceIntent = new Intent(getActivity(), NetworkDownloadService.class)
                .setData(Uri
                        .parse(PICASA_RSS_URL));
        if (paramBundle == null) {
            if (!this.mLoaded) {
                getActivity().startService(this.mServiceIntent);
            }
        } else if (paramBundle.getBoolean(STATE_IS_HIDDEN, false)) {
            FragmentTransaction localFragmentTransaction = getFragmentManager()
                    .beginTransaction();
            localFragmentTransaction.hide(this);
            localFragmentTransaction.commit();
            ((ActionBarFragmentActivity) getActivity())
                    .getCompatibleActionBar().hide();
        }
        return localView;
    }

    @Override
    public void onDestroyView() {
        this.mGridView = null;
        if (this.mEmpty != null) {
            this.mEmpty.setCallback(null);
            this.mEmpty = null;
        }
        LayoutUtils.nullViewDrawables(getView());
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        try {
            getLoaderManager().destroyLoader(0);
            if (this.mAdapter != null) {
                this.mAdapter.changeCursor(null);
                this.mAdapter = null;
            }
        } catch (Throwable localThrowable) {
        }
        super.onDetach();
        return;
    }

    @Override
    public void onHiddenChanged(boolean paramBoolean) {
        super.onHiddenChanged(paramBoolean);
    }

    @Override
    public void onItemClick(AdapterView<?> paramAdapterView, View paramView,
            int paramInt, long paramLong) {
        String str = ((Cursor) this.mAdapter.getItem(paramInt)).getString(1);
        Intent localIntent = new Intent(DownloaderActivity.ACTION_VIEW_IMAGE);
        localIntent.setData(Uri.parse(str));
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(localIntent);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> paramLoader, Cursor paramCursor) {
        this.mAdapter.changeCursor(paramCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> paramLoader) {
        this.mAdapter.changeCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle paramBundle) {
        paramBundle.putBoolean(STATE_IS_HIDDEN, isHidden());
        super.onSaveInstanceState(paramBundle);
    }

    public void setLoaded(boolean paramBoolean) {
        this.mLoaded = paramBoolean;
    }

    private class PicasaListAdapter extends CursorAdapter {
        
        
        public PicasaListAdapter(Activity arg2) {
            super(arg2, null, false);
        }
        
        @Override
        public void bindView(View paramView, Context cxt, Cursor paramCursor) {
            ImageDownloaderView localImageDownloaderView = (ImageDownloaderView) paramView
                    .getTag();
            try {
                URL localURL = new URL(paramCursor.getString(2));
                localImageDownloaderView.setImageURL(localURL, true,
                        ThumbnailFragment.this.mEmpty);
            } catch (MalformedURLException localMalformedURLException) {
                localMalformedURLException.printStackTrace();
            } catch (RejectedExecutionException localRejectedExecutionException) {
            }
        }

        
        public View newView(Context cxt,
                Cursor paramCursor, ViewGroup paramViewGroup) {
            LayoutInflater li = LayoutInflater.from(cxt);
            View localView1 = li.inflate(R.layout.galleryitem,
                    null);
            View localView2 = localView1.findViewById(R.id.thumbImage);
            localView1.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,
                    ThumbnailFragment.this.mColumnWidth));
            localView1.setTag(localView2);
            return localView1;
        }

    }
}
