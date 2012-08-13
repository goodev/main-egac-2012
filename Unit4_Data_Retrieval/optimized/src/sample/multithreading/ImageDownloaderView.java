
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
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.net.URL;

public class ImageDownloaderView extends ImageView {
    private boolean mCached;
    private boolean mDrawn;
    private WeakReference<View> mHideView;
    private int mHideViewId = -1;
    private URL mImageURL;
    private ImageDownloaderThread mThread;
    private boolean mRecycleBitmap;
    private Bitmap mBitmap;

    public ImageDownloaderView(Context paramContext) {
        super(paramContext);
    }

    public ImageDownloaderView(Context paramContext,
            AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        getAttributes(paramAttributeSet);
    }

    public ImageDownloaderView(Context paramContext,
            AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        getAttributes(paramAttributeSet);
    }

    private void getAttributes(AttributeSet paramAttributeSet) {
        TypedArray attributes = getContext().obtainStyledAttributes(paramAttributeSet,
                R.styleable.ImageDownloaderView);
        this.mHideViewId = attributes.getResourceId(
                R.styleable.ImageDownloaderView_hideShowSibling, -1);
        this.mRecycleBitmap = attributes.getBoolean(R.styleable.ImageDownloaderView_recycleBitmaps,
                false);
    }

    private void showView(int paramInt) {
        if (this.mHideView != null) {
            View localView = (View) this.mHideView.get();
            if (localView != null)
                localView.setVisibility(paramInt);
        }
    }

    public void clearImage() {
        setImageDrawable(null);
        showView(View.VISIBLE);
    }

    final URL getLocation() {
        return this.mImageURL;
    }

    final private Bitmap getRecycleBitmapInternal() {
        if ( null != mBitmap ) {
            return mBitmap;
        }
        Drawable currentDrawable = getDrawable();
        if (mRecycleBitmap && currentDrawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) currentDrawable;
            return bd.getBitmap();
        }
        return null;
    }

    final Bitmap getRecycleBitmap() {
        Bitmap localBitmap = mBitmap;
        mBitmap = null;
        return localBitmap;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if ((this.mHideViewId != -1) && ((getParent() instanceof View))) {
            View localView = ((View) getParent())
                    .findViewById(this.mHideViewId);
            if (localView != null)
                this.mHideView = new WeakReference<View>(localView);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        setImageURL(null, false, null);
        Drawable localDrawable = getDrawable();
        if (localDrawable != null)
            localDrawable.setCallback(null);
        if (this.mHideView != null) {
            this.mHideView.clear();
            this.mHideView = null;
        }
        this.mThread = null;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas paramCanvas) {
        if ((!this.mDrawn) && (this.mImageURL != null)) {
            ImageDownloaderThread.startDownload(this, this.mCached);
            this.mDrawn = true;
        }
        super.onDraw(paramCanvas);
    }

    public void setHideView(View paramView) {
        this.mHideView = new WeakReference<View>(paramView);
    }

    @Override
    public void setImageBitmap(Bitmap paramBitmap) {
        super.setImageBitmap(paramBitmap);
    }

    @Override
    public void setImageDrawable(Drawable paramDrawable) {
        int viewState;
        if (paramDrawable == null) {
            viewState = View.VISIBLE;
        } else {
            viewState = View.INVISIBLE;
        }
        showView(viewState);
        super.setImageDrawable(paramDrawable);
    }

    @Override
    public void setImageResource(int paramInt) {
        super.setImageResource(paramInt);
    }

    @Override
    public void setImageURI(Uri paramUri) {
        super.setImageURI(paramUri);
    }

    public void setImageURL(URL paramURL, boolean paramBoolean,
            Drawable paramDrawable) {
        if (this.mImageURL != null) {
            if (!this.mImageURL.equals(paramURL)) {
                ImageDownloaderThread.removeDownload(this.mThread);
            } else {
                return;
            }
        }
        mBitmap = getRecycleBitmapInternal();
        setImageDrawable(paramDrawable);
        this.mImageURL = paramURL;
        if ((this.mDrawn) && (paramURL != null)) {
            this.mCached = paramBoolean;
            this.mThread = ImageDownloaderThread.startDownload(this,
                    paramBoolean);
        }
    }

    public void setStatusDrawable(Drawable paramDrawable) {
        mBitmap = getRecycleBitmapInternal();
        if (this.mHideView == null) {
            setImageDrawable(paramDrawable);
        }
    }

    public void setStatusResource(int paramInt) {
        mBitmap = getRecycleBitmapInternal();
        if (this.mHideView == null) {
            setImageResource(paramInt);
        }
    }
}
