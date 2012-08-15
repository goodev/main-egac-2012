package com.example.acamp.dip;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.acamp.dip.dummy.DummyContent;

public class PicDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    DummyContent.DummyItem mItem;
    ImageView mImageView;

    public PicDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        } else {
            SharedPreferences sp =
                    getActivity().getSharedPreferences(DummyContent.PREFS_GROUP,
                            Context.MODE_PRIVATE);
            String lastItemKey = sp.getString(DummyContent.LAST_ITEM_KEY, "");
            if (!TextUtils.isEmpty(lastItemKey)) {
                mItem = DummyContent.ITEM_MAP.get(lastItemKey);
            }
        }
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pic_detail, container, false);
        mImageView = (ImageView) rootView.findViewById(R.id.pic_detail_pic);

        if (mItem != null) {

            if (mImageView != null) {

                Bitmap b = null;
                GetImageTask git = new GetImageTask();

                // The documentation implies that ImageView.setImageBitmap
                // does not run on the UI thread, so we don't need to do this in
                // an AsyncTask.
                try {
                    b = git.execute(mItem.content).get();
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

    public final class GetImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            String picUrlStr = params[0];
            URL picUrl;
            InputStream is;
            Bitmap b = null;

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
