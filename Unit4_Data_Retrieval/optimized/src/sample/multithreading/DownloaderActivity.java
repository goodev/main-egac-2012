
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
import compatible.actionbar.CompatibleActionBar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * This activity manages the fragments used in the DownloaderActivity. It
 * supports a side-by-side layout (used primarily for tablet-class devices) and
 * a stacked layout. It supports the ICS phone feature to display the images in
 * full-screen without any of the system bar.
 * <p>
 * IntentHandler is used to communicate between the active Fragment and this
 * activity. This pattern simulates some of the communication used between
 * activities, and allows this activity to make choices of how to manage the
 * fragments.
 */
public class DownloaderActivity extends ActionBarFragmentActivity implements OnBackStackChangedListener {
    private static final String PHOTO_FRAGMENT_TAG = "PFT";
    private static final String THUMBNAIL_FRAGMENT_TAG = "TFT";
    private static final String EXTRA_FULLSCREEN = "BE.FS";
    
    public static final String ACTION_VIEW_IMAGE = "DA.VI";
    public static final String ACTION_ZOOM_IMAGE = "DA.ZI";
    
    TextView mActionBarProgressText;
    int mColumnWidth;
    View mMainView;
    ResponseReceiver mReceiver;
    boolean mSideBySide, mHideNavigation;
    boolean mFullScreen;
    int mPreviousStackCount;
    private static final String LOG_TAG = "DownloaderActivity";

    
    
    private void setProgressText(int paramInt) {
        if (this.mActionBarProgressText != null)
            this.mActionBarProgressText.setText(paramInt);
    }
    
    
    public void setFullScreen(final boolean fullscreen) {
        getWindow().setFlags(
                fullscreen ? WindowManager.LayoutParams.FLAG_FULLSCREEN : 0,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        CompatibleActionBar localCompatibleActionBar = getCompatibleActionBar();
        mFullScreen = fullscreen;
        if (Build.VERSION.SDK_INT >= 11) {
            int flag = fullscreen ? View.SYSTEM_UI_FLAG_LOW_PROFILE : 0;
            if (Build.VERSION.SDK_INT >= 14 && fullscreen) {
                flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            mMainView.setSystemUiVisibility(flag);
        }        
        if ( fullscreen ) {
            localCompatibleActionBar.hide();
            Log.d(LOG_TAG, "Action Bar Hidden");
        } else {
            localCompatibleActionBar.show();
            Log.d(LOG_TAG, "Action Bar Shown");
        }                
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(EXTRA_FULLSCREEN, mFullScreen);
        super.onSaveInstanceState(outState);
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /**
         * This BroadcastReceiver does the heavy lifting of managing the
         * fragments for the downloader activity, and also deals with hiding the
         * navigation interface when showing the picture full-screen.
         * <p>
         * It knows whether we are using a side by side layout and will add or
         * replace fragments appropriately to make the navigation experience
         * pleasant across large screen and small screen devices.
         */
        @Override
        public void onReceive(Context c, Intent paramIntent) {
            FragmentManager fm;
            PhotoFragment photoFragment;
            String str;
            if (paramIntent.getAction().equals(ACTION_VIEW_IMAGE)) {
                fm = getSupportFragmentManager();
                photoFragment = (PhotoFragment) fm
                        .findFragmentByTag(PHOTO_FRAGMENT_TAG);
                str = paramIntent.getDataString();
                if (photoFragment != null) {
                    if (!str.equals(photoFragment.getURLString())) {
                        photoFragment.setPhoto(str);
                        photoFragment.loadPhoto();
                    }
                } else {
                    photoFragment = new PhotoFragment();
                    photoFragment.setPhoto(str);
                    FragmentTransaction localFragmentTransaction2 = fm
                            .beginTransaction();
                    if (mSideBySide) {
                        localFragmentTransaction2.add(R.id.fragmentHost,
                                photoFragment, PHOTO_FRAGMENT_TAG);
                    } else {
                        localFragmentTransaction2.replace(R.id.fragmentHost,
                                photoFragment, PHOTO_FRAGMENT_TAG);
                    }
                    localFragmentTransaction2.addToBackStack(null);
                    localFragmentTransaction2.commit();
                }
                if (!mSideBySide) setFullScreen(true);
            } else if (paramIntent.getAction().equals(ACTION_ZOOM_IMAGE)) {
                if (mSideBySide) {
                    FragmentManager localFragmentManager1 = getSupportFragmentManager();
                    ThumbnailFragment localThumbnailFragment = (ThumbnailFragment) localFragmentManager1
                            .findFragmentByTag(THUMBNAIL_FRAGMENT_TAG);
                    if (null != localThumbnailFragment) {
                        if (localThumbnailFragment.isVisible()) {
                            FragmentTransaction localFragmentTransaction1 = localFragmentManager1
                                    .beginTransaction();
                            localFragmentTransaction1.hide(localThumbnailFragment);
                            localFragmentTransaction1.addToBackStack(null);
                            localFragmentTransaction1.commit();
                        } else {
                            localFragmentManager1.popBackStack();
                        }
                    }
                    setFullScreen(true);            
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);
        CompatibleActionBar localCompatibleActionBar = getCompatibleActionBar();
        localCompatibleActionBar.requestOverlayMode();
        this.mMainView = getLayoutInflater().inflate(R.layout.fragmenthost,
                null);
        setContentView(this.mMainView);
        localCompatibleActionBar.setBackgroundDrawable(getResources()
                .getDrawable(R.drawable.actionbarbg));
        localCompatibleActionBar.setDisplayUseLogoEnabled(true);
        localCompatibleActionBar.setLogo(R.drawable.picasalogo);
        View localView = getLayoutInflater().inflate(R.layout.progress, null);
        localCompatibleActionBar.setCustomView(localView);
        this.mActionBarProgressText = ((TextView) localView
                .findViewById(R.id.actionBarProgressText));
        IntentFilter localIntentFilter = new IntentFilter(
                NetworkDownloadService.BROADCAST_ACTION);
        localIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        this.mReceiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mReceiver, localIntentFilter);
        IntentFilter fragmentIntentFilter = new IntentFilter(
                ACTION_VIEW_IMAGE);
        fragmentIntentFilter.addDataScheme("http");
        LocalBroadcastManager.getInstance(this).registerReceiver(mIntentReceiver, fragmentIntentFilter);
       
        fragmentIntentFilter = new IntentFilter(
                ACTION_ZOOM_IMAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mIntentReceiver, fragmentIntentFilter);
        
        FragmentManager localFragmentManager = getSupportFragmentManager();
        this.mSideBySide = getResources().getBoolean(R.bool.sideBySide);
        this.mHideNavigation = getResources().getBoolean(R.bool.hideNavigation);
        localFragmentManager.addOnBackStackChangedListener(this);
        if (paramBundle == null) {
            FragmentTransaction localFragmentTransaction = localFragmentManager
                    .beginTransaction();
            localFragmentTransaction.add(R.id.fragmentHost,
                    new ThumbnailFragment(), THUMBNAIL_FRAGMENT_TAG);
            localFragmentTransaction.commit();
        } else {
            mFullScreen = paramBundle.getBoolean(EXTRA_FULLSCREEN);
            setFullScreen(mFullScreen);
            mPreviousStackCount = localFragmentManager.getBackStackEntryCount();
        }        
    }

    @Override
    public void onDestroy() {
        if (this.mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mIntentReceiver);
        this.mActionBarProgressText = null;
        if (this.mMainView != null) {
            LayoutUtils.nullViewDrawables(this.mMainView);
            this.mMainView = null;
        }
        super.onDestroy();
    }

    /**
     * This works with the compatible action bar as well as with menus on
     * pre-Honeycomb devices.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
        switch (paramMenuItem.getItemId()) {
            case android.R.id.home: {
                FragmentManager localFragmentManager = getSupportFragmentManager();
                int count = localFragmentManager.getBackStackEntryCount();
                if (count > 0) {
                    localFragmentManager.popBackStack();
                }
            }
            default:
                return super.onOptionsItemSelected(paramMenuItem);
        }
    }

    /**
     * It's important to try to stop all of our background threads when our
     * activity stops.
     */
    @Override
    protected void onStop() {
        ImageDownloaderThread.cancelAll();
        super.onStop();
    }

    public void showProgress(boolean paramBoolean) {
        getCompatibleActionBar().showCustomView(paramBoolean);
    }

    /**
     * This BroadcastReceiver implementation is used to track the state of the
     * downloads from our service.
     */
    private class ResponseReceiver extends BroadcastReceiver {
        private ResponseReceiver() {
        }

        @Override
        public void onReceive(Context paramContext, Intent paramIntent) {
            switch (paramIntent.getIntExtra(NetworkDownloadService.EXTRA_STATUS,
                    NetworkDownloadService.STATE_ACTION_COMPLETE)) {
                case NetworkDownloadService.STATE_ACTION_STARTED:
                    DownloaderActivity.this
                            .setProgressText(R.string.progress_starting_update);
                    DownloaderActivity.this.showProgress(true);
                    break;
                case NetworkDownloadService.STATE_ACTION_CONNECTING:
                    DownloaderActivity.this
                            .setProgressText(R.string.progress_connecting);
                    DownloaderActivity.this.showProgress(true);
                    break;
                case NetworkDownloadService.STATE_ACTION_PARSING:
                    DownloaderActivity.this
                            .setProgressText(R.string.progress_parsing);
                    DownloaderActivity.this.showProgress(true);
                    break;
                case NetworkDownloadService.STATE_ACTION_WRITING:
                    Log.d(LOG_TAG, "Download Writing");
                    DownloaderActivity.this
                            .setProgressText(R.string.progress_writing);
                    DownloaderActivity.this.showProgress(true);
                    break;
                case NetworkDownloadService.STATE_ACTION_COMPLETE:
                    Log.d(LOG_TAG, "Download Complete!");
                    DownloaderActivity.this.showProgress(false);
                    ThumbnailFragment localThumbnailFragment = (ThumbnailFragment) DownloaderActivity.this
                            .getSupportFragmentManager().findFragmentByTag(
                                    THUMBNAIL_FRAGMENT_TAG);
                    if ((localThumbnailFragment == null)
                            || (!localThumbnailFragment.isVisible()))
                        return;
                    localThumbnailFragment.setLoaded(true);
                    break;
                default:
                    DownloaderActivity.this.showProgress(false);
                    break;
            }
        }
    }
    
    @Override
    public void onBackStackChanged() {
        int previousStackCount = mPreviousStackCount;
        FragmentManager localFragmentManager = getSupportFragmentManager();
        int currentStackCount = localFragmentManager.getBackStackEntryCount();
        mPreviousStackCount = currentStackCount;
        boolean popping = currentStackCount < previousStackCount;
        Log.d(LOG_TAG, "backstackchanged: popping = " + popping);
        if (popping) {
            setFullScreen(false);
        }
    }
    
}
