package com.example.acamp.dip;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.example.acamp.dip.dummy.DummyContent;

public class PicListActivity extends FragmentActivity implements PicListFragment.Callbacks {

    private boolean mTwoPane;
    private String mItemSelected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_list);

        // Initialize our dummy data
        // Should probably be done at application scope,
        // but I feel lazy
        DummyContent.Init(this);

        if (findViewById(R.id.pic_detail_container) != null) {
            mTwoPane = true;
            ((PicListFragment) getSupportFragmentManager().findFragmentById(R.id.pic_list))
                    .setActivateOnItemClick(true);
        }

        // If two-pane view, then if first launch, try to restore last item viewed
        if (mTwoPane && (savedInstanceState == null)) {
            SharedPreferences sp =
                    getSharedPreferences(DummyContent.PREFS_GROUP, Context.MODE_PRIVATE);
            mItemSelected = sp.getString(DummyContent.LAST_ITEM_KEY, "");
            if (!TextUtils.isEmpty(mItemSelected)) {
                this.onItemSelected(mItemSelected);
            }
        }
    }

    @Override
    public void onItemSelected(String id) {
        mItemSelected = id;
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(PicDetailFragment.ARG_ITEM_ID, id);
            PicDetailFragment fragment = new PicDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().replace(R.id.pic_detail_container,
                    fragment).commit();

        } else {
            Intent detailIntent = new Intent(this, PicDetailActivity.class);
            detailIntent.putExtra(PicDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    protected void onStop() {
        if (!TextUtils.isEmpty(mItemSelected)) {
            SharedPreferences sp =
                    getSharedPreferences(DummyContent.PREFS_GROUP, Context.MODE_PRIVATE);
            Editor spe = sp.edit();
            spe.putString(DummyContent.LAST_ITEM_KEY, mItemSelected);
            spe.commit();
        }
        super.onStop();
    }
}
