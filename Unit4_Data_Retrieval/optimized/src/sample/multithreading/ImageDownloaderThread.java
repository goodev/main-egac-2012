
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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.util.SparseArray;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Downloads images. We have to be careful here, because we take advantage of
 * static class members to implement our ThreadPool. We implement a number of
 * optimizations based upon the number of active cores that may not be
 * appropriate to all circumstances, as that gives us only a small idea of the
 * available hardware resources present.
 * <p>
 * We use a semaphore to limit the number of simultaneous image decodes to the
 * number of available processor cores.
 * <p>
 * We try to handle running out of memory in the most graceful way possible.
 * That doesn't mean that we always run out of memory gracefully.
 */
public class ImageDownloaderThread implements Runnable {
    private static final int DOWNLOAD_FAILED = -1;
    private static final int DOWNLOAD_STARTED = 1;
    private static final int DECODE_QUEUED = 2;
    private static final int DECODE_STARTED = 3;
    private static final int TASK_COMPLETE = 4;

    private static final int IMAGE_CACHE_SIZE = 1024 * 1024 * 4;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;
    private static final String LOG_TAG = "IDT";
    private static final int CORE_POOL_SIZE = 8;
    private static final int MAXIMUM_POOL_SIZE = 8;
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final int READ_SIZE = 1024 * 2;
    private static final LruCache<URL, byte[]> sCache;
    private static final Semaphore sCoreAvailable;
    private static final BlockingQueue<Runnable> sPoolWorkQueue;
    private static final ThreadPoolExecutor sThreadPool;
    private static final int NUMBER_OF_DECODE_TRIES = 2;
    
    final boolean mCache;
    Handler mHandler;
    final WeakReference<ImageDownloaderView> mIVRef;
    final URL mLocation;
    final int mTargetHeight;
    final int mTargetWidth;
    volatile Thread mThread;
    final static boolean sReuseBitmaps;
    static SparseArray<ConcurrentLinkedQueue<Bitmap>> sBitmapReuseQueues;
    static ConcurrentLinkedQueue<byte[]> sTempStorageQueue;

    static {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        sPoolWorkQueue = new LinkedBlockingQueue<Runnable>();
        sThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, sPoolWorkQueue);
        sCoreAvailable = new Semaphore(NUMBER_OF_CORES, true);
        sCache = new LruCache<URL, byte[]>(IMAGE_CACHE_SIZE) {
            protected int sizeOf(URL paramURL, byte[] paramArrayOfByte) {
                return paramArrayOfByte.length;
            }
        };
        sReuseBitmaps = Build.VERSION.SDK_INT >= 11;
        if ( sReuseBitmaps ) {
            sBitmapReuseQueues = new SparseArray<ConcurrentLinkedQueue<Bitmap>>();
        }
        sTempStorageQueue = new ConcurrentLinkedQueue<byte[]>();
    }

    static final private int getBitmapKey(final int width, final int height) {
    	return width<<4 + height;
    }
    
    private ImageDownloaderThread(ImageDownloaderView paramImageDownloaderView,
            boolean paramBoolean) {
        this.mLocation = paramImageDownloaderView.getLocation();
        this.mIVRef = new WeakReference<ImageDownloaderView>(
                paramImageDownloaderView);
        this.mCache = paramBoolean;
        this.mTargetWidth = paramImageDownloaderView.getWidth();
        this.mTargetHeight = paramImageDownloaderView.getHeight();
        this.mHandler = new Handler() {
            public void handleMessage(Message paramMessage) {
                ImageDownloaderView localImageDownloaderView = (ImageDownloaderView) ImageDownloaderThread.this.mIVRef
                        .get();
                if (localImageDownloaderView != null) {
                    URL localURL = localImageDownloaderView.getLocation();
                    
                    // Intentionally doing a cheap object compare here.  Only update the bitmap
                    // if this ImageDownloaderThread is supposed to be servicing this ImageDownloaderView.                    
                    if (ImageDownloaderThread.this.mLocation == localURL)
                        switch (paramMessage.what) {
                            case DOWNLOAD_STARTED:
                                localImageDownloaderView
                                        .setStatusResource(R.drawable.imagedownloading);
                                break;
                            case DECODE_QUEUED:
                                localImageDownloaderView
                                        .setStatusResource(R.drawable.decodequeued);
                                break;
                            case DECODE_STARTED:
                                localImageDownloaderView
                                        .setStatusResource(R.drawable.decodedecoding);
                                break;
                            case TASK_COMPLETE:
                                if ( sReuseBitmaps ) {
                                    Bitmap recycleBitmap = localImageDownloaderView.getRecycleBitmap();
                                    if ( null != recycleBitmap ) {   
                                    	ConcurrentLinkedQueue<Bitmap> clq = sBitmapReuseQueues.get(getBitmapKey(recycleBitmap.getWidth(), recycleBitmap.getHeight()));
                                    	if ( null == clq ) {
                                    		// only cache one size for now
                                    		sBitmapReuseQueues.clear();
                                    		clq = new ConcurrentLinkedQueue<Bitmap>();
                                    		sBitmapReuseQueues.put(recycleBitmap.getByteCount(), clq);
                                    	}
                                        clq.add(recycleBitmap);
                                        Log.d(LOG_TAG, "Recycle bitmap stored.");
                                    }
                                }
                                localImageDownloaderView
                                        .setImageBitmap((Bitmap) paramMessage.obj);
                                ImageDownloaderThread.this.mIVRef.clear();
                                ImageDownloaderThread.this.mHandler = null;
                                break;
                            case DOWNLOAD_FAILED:
                                localImageDownloaderView
                                        .setStatusResource(R.drawable.imagedownloadfailed);
                                ImageDownloaderThread.this.mIVRef.clear();
                                ImageDownloaderThread.this.mHandler = null;
                                break;
                            default:
                                super.handleMessage(paramMessage);
                        }
                }
            }
        };
    }

    public static void cancelAll() {
        ImageDownloaderThread[] arrayOfImageDownloaderThread = new ImageDownloaderThread[sPoolWorkQueue
                .size()];
        sPoolWorkQueue.toArray(arrayOfImageDownloaderThread);
        int len = arrayOfImageDownloaderThread.length;
        for (int j = 0; j < len; j++) {
            Thread t = arrayOfImageDownloaderThread[j].mThread;
            if ( null != t ) {
                t.interrupt();
            }
        }
    }

    public static void removeDownload(
            ImageDownloaderThread paramImageDownloaderThread) {
        if (paramImageDownloaderThread != null) {
            Thread t = paramImageDownloaderThread.mThread;
            if ( null != t ) t.interrupt();
            sThreadPool.remove(paramImageDownloaderThread);
        }
    }

    public static ImageDownloaderThread startDownload(
            ImageDownloaderView paramImageDownloaderView, boolean paramBoolean) {
        ImageDownloaderThread localImageDownloaderThread = new ImageDownloaderThread(
                paramImageDownloaderView, paramBoolean);
        sThreadPool.execute(localImageDownloaderThread);
        paramImageDownloaderView.setStatusResource(R.drawable.imagequeued);
        return localImageDownloaderThread;
    }

    public void run() {
        mThread = Thread.currentThread();        
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        Bitmap bmReturn = null;
        try {
            if (Thread.interrupted())
                return;
            byte[] imageBuf = sCache.get(mLocation);
            if (null == imageBuf) {
                mHandler.sendEmptyMessage(DOWNLOAD_STARTED);
                InputStream is = null;
                try {
                    HttpURLConnection httpConn = (HttpURLConnection) mLocation
                            .openConnection();
                    httpConn.setRequestProperty(
                            "User-Agent",
                            NetworkDownloadService.USER_AGENT);
                    if (Thread.interrupted())
                        return;
                    is = httpConn.getInputStream();
                    if (Thread.interrupted())
                        return;
                    int contentLength = httpConn.getContentLength();

                    /*
                     * We take advantage of the content length if it is returned
                     * to preallocate our buffer. If it is not returned, we end
                     * up thrashing memory a bit more.
                     */
                    if (-1 == contentLength) {
                        byte[] buf = new byte[READ_SIZE];
                        int bufferLeft = buf.length;
                        int offset = 0;
                        int result = 0;
                        outer: do {
                            while (bufferLeft > 0) {
                                result = is.read(buf, offset, bufferLeft);
                                if (result < 0) {
                                    // we're done
                                    break outer;
                                }
                                offset += result;
                                bufferLeft -= result;
                                if (Thread.interrupted())
                                    return;
                            }
                            // resize
                            bufferLeft = READ_SIZE;
                            int newSize = buf.length + READ_SIZE;
                            byte[] newBuf = new byte[newSize];
                            System.arraycopy(buf, 0, newBuf, 0, buf.length);
                            buf = newBuf;
                        } while (true);
                        imageBuf = new byte[offset];
                        System.arraycopy(buf, 0, imageBuf, 0, offset);
                    } else {
                        imageBuf = new byte[contentLength];
                        int length = contentLength;
                        int offset = 0;
                        while (length > 0) {
                            int result = is.read(imageBuf, offset, length);
                            if (result < 0) {
                                throw new EOFException();
                            }
                            offset += result;
                            length -= result;
                            if (Thread.interrupted())
                                return;
                        }
                    }
                    if (Thread.interrupted())
                        return;
                } catch (IOException e) {
                    return;
                } finally {
                    if (null != is) {
                        try {
                            is.close();
                        } catch (Exception e) {

                        }
                    }
                }
            }
            mHandler.sendEmptyMessage(DECODE_QUEUED);
            byte[] tempStorage = sTempStorageQueue.poll();
            try {
                sCoreAvailable.acquire();
                mHandler.sendEmptyMessage(DECODE_STARTED);
                BitmapFactory.Options bfo = new BitmapFactory.Options();
                int targetWidth = mTargetWidth;
                int targetHeight = mTargetHeight;
                if (Thread.interrupted())
                    return;
                if ( null == tempStorage ) {
                    tempStorage = new byte[16*1024];
                }
                bfo.inTempStorage = tempStorage;
                bfo.inJustDecodeBounds = true;
                
                BitmapFactory.decodeByteArray(imageBuf, 0, imageBuf.length, bfo);
                int hScale = bfo.outHeight / targetHeight;
                int wScale = bfo.outWidth / targetWidth;
                int sampleSize = Math.max(hScale, wScale);
                int outHeight;
                int outWidth;
                bfo.inSampleSize = 1;
//                if (sampleSize > 1) {
//                    bfo.inSampleSize = sampleSize;
//                    outHeight = bfo.outHeight / sampleSize;
//                   outWidth = bfo.outWidth / sampleSize;
//                } else {
                    outHeight = bfo.outHeight;
                    outWidth = bfo.outWidth;
//                }
//                bfo.inMutable = true;
                if (Thread.interrupted())
                    return;
                bfo.inJustDecodeBounds = false;
                // oom handling in decode stage
                for ( int i = 0; i < NUMBER_OF_DECODE_TRIES; i++ ) {
                    try {
                    	ConcurrentLinkedQueue<Bitmap> clq = sBitmapReuseQueues.get(getBitmapKey(outWidth, outHeight));
                    	if ( null != clq ) {
                    		Bitmap bmReuse = clq.poll();
	                        if ( null != bmReuse && bmReuse.getHeight() == outHeight && bmReuse.getWidth() == outWidth ) {
	                            bfo.inBitmap = bmReuse;
	                            Log.d(LOG_TAG, "Reusing bitmap");
	                            try {
	                                bmReturn = BitmapFactory.decodeByteArray(imageBuf, 0,
	                                        imageBuf.length, bfo);
	                                break;
	                            } catch(Throwable e) {
	                                
	                            }
	                            bfo.inBitmap = null;
	                        }
                    	}
                        bmReturn = BitmapFactory.decodeByteArray(imageBuf, 0,
                                imageBuf.length, bfo);
                        // break out of OOM loop
                        break;
                    } catch (Throwable e) {
                        Log.e(LOG_TAG,
                                "Out of memory in decode stage. Throttling.");
                        java.lang.System.gc();
                        if (Thread.interrupted())
                            return;
                        try {
                            Thread.sleep(0xfa);
                        } catch (java.lang.InterruptedException ix) {
    
                        }
                    }
                }
                if (mCache) {
                    sCache.put(mLocation, imageBuf);
                }
            } catch (java.lang.InterruptedException x) {
                x.printStackTrace();
            } finally {
                sCoreAvailable.release();          
                if ( null != tempStorage ) {
                    sTempStorageQueue.add(tempStorage);
                }
            }
        } finally {
            mThread = null;
            if (null == bmReturn) {
                mHandler.sendEmptyMessage(DOWNLOAD_FAILED);
            } else {
                Message completeMessage = mHandler.obtainMessage(TASK_COMPLETE,
                        bmReturn);
                completeMessage.sendToTarget();
            }
            // clear interrupt flag
            Thread.interrupted();
        }
    }
}
