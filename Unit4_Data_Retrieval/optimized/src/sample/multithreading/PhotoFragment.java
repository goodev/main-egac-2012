
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
import compatible.shareactionprovider.CompatibleShareActionProvider;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.net.MalformedURLException;
import java.net.URL;

public class PhotoFragment extends Fragment implements View.OnClickListener {
    private static final String LOG_TAG = "ImageDownloaderThread";
    private static final String PHOTO_URL_KEY = "PUK";
    ImageDownloaderView mPhotoView;
    String mURLString;
    Intent mShareIntent;
    CompatibleShareActionProvider mCSAP;

    public String getURLString() {
        return this.mURLString;
    }

    public void loadPhoto() {
        if (this.mURLString != null) {
            try {
                URL localURL = new URL(this.mURLString);
                this.mPhotoView.setImageURL(localURL, false, null);
                Intent localIntent = new Intent(Intent.ACTION_SEND);
                localIntent.setType("text/plain");
                localIntent.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.picasa_share_subject));
                localIntent.putExtra(Intent.EXTRA_TEXT, this.mURLString);
                mShareIntent = localIntent;
                if (null != mCSAP) {
                    mCSAP.setShareIntent(mShareIntent);
                }
            } catch (MalformedURLException localMalformedURLException) {
                localMalformedURLException.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View paramView) {
        Intent localIntent = new Intent(DownloaderActivity.ACTION_ZOOM_IMAGE);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(localIntent);
    }

    @Override
    public void onCreate(Bundle paramBundle) {
        setHasOptionsMenu(true);
        super.onCreate(paramBundle);
    }

    @Override
    public void onCreateOptionsMenu(Menu paramMenu,
            MenuInflater paramMenuInflater) {
        paramMenuInflater.inflate(R.menu.photoviewmenu, paramMenu);
        if (Build.VERSION.SDK_INT >= 14) {
            MenuItem item = paramMenu.findItem(R.id.share);
            mCSAP = CompatibleShareActionProvider.getActionProvider(item);
            if (null != mShareIntent) {
                mCSAP.setShareIntent(mShareIntent);
            }
        }
        super.onCreateOptionsMenu(paramMenu, paramMenuInflater);
    }

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater,
            ViewGroup paramViewGroup, Bundle paramBundle) {
        super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
        View localView = paramLayoutInflater.inflate(R.layout.photo,
                paramViewGroup, false);
        this.mPhotoView = ((ImageDownloaderView) localView
                .findViewById(R.id.photoView));
        this.mPhotoView.setOnClickListener(this);
        if (paramBundle != null)
            this.mURLString = paramBundle.getString(PHOTO_URL_KEY);
        if (this.mURLString != null)
            loadPhoto();
        return localView;
    }

    @Override
    public void onDestroyView() {
        Log.d(LOG_TAG, "onDestroyView");
        if (this.mPhotoView != null) {
            this.mPhotoView.setOnClickListener(null);
            this.mPhotoView = null;
        }
        LayoutUtils.nullViewDrawables(getView());
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        Log.d(LOG_TAG, "onDetach");
        this.mURLString = null;
        super.onDetach();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
        switch (paramMenuItem.getItemId()) {
            case R.id.share:
                startActivity(Intent.createChooser(mShareIntent,
                        getString(R.string.share_activity_title)));
            default:
                return super.onOptionsItemSelected(paramMenuItem);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle paramBundle) {
        super.onSaveInstanceState(paramBundle);
        paramBundle.putString(PHOTO_URL_KEY, this.mURLString);
    }

    public void setPhoto(String paramString) {
        this.mURLString = paramString;
    }
}
