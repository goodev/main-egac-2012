package com.example.acamp.dip;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.acamp.dip.PicasaProvider.PicasaFeatured;

public class PicDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    long mId = -1;
    ImageView mImageView;

    public PicDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mId = getArguments().getLong(ARG_ITEM_ID, -1);
        } else {
            SharedPreferences sp = getActivity().getSharedPreferences(Constants.PREFS_GROUP, Context.MODE_PRIVATE);
            long lastItemKey = sp.getLong(Constants.LAST_ITEM_KEY, -1);
            if (lastItemKey > 0) {
                mId = lastItemKey;
            }
        }
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pic_detail, container, false);
        mImageView = (ImageView) rootView.findViewById(R.id.pic_detail_pic);

        if (mId > 0) {

            if (mImageView != null) {

                Bitmap b = null;
                GetImageTask git = new GetImageTask();

                // The documentation implies that ImageView.setImageBitmap
                // does not run on the UI thread, so we don't need to do this in
                // an AsyncTask.
                try {
                    b = git.execute(mId).get();
                    mImageView.setImageBitmap(b);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return rootView;
    }

    public final class GetImageTask extends AsyncTask<Long, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Long... params) {
            String picUrlStr = null;
            URL picUrl;
            InputStream is;
            Bitmap b = null;

            // Inherit context and content resolver from containing Activity.
            Context context = getActivity();
            ContentResolver cr = context.getContentResolver();
            
            // Compose a content URI and access the content resolver, specifically
            // seeking the row matching the ID passed in through params.
            Uri singleUri = ContentUris.withAppendedId(PicasaProvider.getUriByType(context, PicasaProvider.METADATA_QUERY), params[0]);
            Cursor cursor = cr.query(singleUri, PicasaProvider.PicasaFeatured.PROJ_LONG_URL, null, null, null);
   
            // Fetch the content out of the result cursor.
            // NOTE!  Do not just use magic numbers to get the
            // columns.  Better to look up by name.
            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToFirst();
                picUrlStr = cursor.getString(cursor.getColumnIndex(PicasaFeatured.IMAGE_URL));
            }            

            if (TextUtils.isEmpty(picUrlStr)) {
                return null;
            }

            try {
                picUrl = new URL(picUrlStr);
                is = (InputStream) picUrl.getContent();
                b = BitmapFactory.decodeStream(is);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return b;
        }
    }

}
