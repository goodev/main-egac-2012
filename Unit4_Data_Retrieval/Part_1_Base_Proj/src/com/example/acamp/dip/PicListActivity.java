package com.example.acamp.dip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class PicListActivity extends FragmentActivity
        implements PicListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_list);

        if (findViewById(R.id.pic_detail_container) != null) {
            mTwoPane = true;
            ((PicListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.pic_list))
                    .setActivateOnItemClick(true);
        }
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(PicDetailFragment.ARG_ITEM_ID, id);
            PicDetailFragment fragment = new PicDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.pic_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, PicDetailActivity.class);
            detailIntent.putExtra(PicDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
