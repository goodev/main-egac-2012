package com.example.acamp.dip;

import java.io.IOException;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.acamp.dip.dummy.DummyContent;

public class PicDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    DummyContent.DummyItem mItem;

    public PicDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pic_detail, container, false);
        if (mItem != null) {

            // The documentation implies that ImageView.setImageDrawable
            // does not run on the UI thread, so we don't need to do this in
            // an AsyncTask.
            AssetManager am = getActivity().getAssets();
            try {
                Drawable d = Drawable.createFromStream(am.open(mItem.content), null);
                ((ImageView) rootView.findViewById(R.id.pic_detail_pic)).setImageDrawable(d);
            } catch (IOException e) {
                ((TextView) rootView.findViewById(R.id.pic_detail))
                        .setText("Could not load asset: " + mItem.content);
            }
        }
        return rootView;
    }
}
