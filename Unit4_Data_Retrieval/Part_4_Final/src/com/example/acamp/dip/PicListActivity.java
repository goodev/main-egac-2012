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
    private long mItemSelected;

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
                    getSharedPreferences(Constants.PREFS_GROUP, Context.MODE_PRIVATE);
            mItemSelected = sp.getLong(Constants.LAST_ITEM_KEY, -1);
            if (mItemSelected > 0) {
                this.onItemSelected(mItemSelected);
            }
        }
    }

    @Override
    public void onItemSelected(long id) {
        mItemSelected = id;
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putLong(PicDetailFragment.ARG_ITEM_ID, id);
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
        if (mItemSelected > 0) {
            SharedPreferences sp =
                    getSharedPreferences(Constants.PREFS_GROUP, Context.MODE_PRIVATE);
            Editor spe = sp.edit();
            spe.putLong(Constants.LAST_ITEM_KEY, mItemSelected);
            spe.commit();
        }
        super.onStop();
    }
}
