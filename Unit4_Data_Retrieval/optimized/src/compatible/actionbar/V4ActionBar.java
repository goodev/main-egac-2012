
package compatible.actionbar;
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
import sample.multithreading.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class in no way tries to implement all of the functionality of
 * ActionBar. Instead it implements some useful pieces so that our application
 * can take advantage of real action bars in reasonable ways when they are
 * available.
 */
public class V4ActionBar implements CompatibleActionBar, View.OnClickListener {
    private Activity mActivity;
    private View mCustomView;
    private LinearLayout mLayout;
    private ImageView mLogoView;
    private boolean mOverlayMode;
    private TextView mTitleText;
    private int mActionBarHeight;

    /**
     * ActionBars take the place of the window title, so activities that are
     * associated with a compatible ActionBar will automatically gain the Window
     * feature NO_TITLE.
     * 
     * @param paramActivity the activity to adorn with the ActionBar
     */
    public V4ActionBar(Activity paramActivity) {
        this.mActivity = paramActivity;
        paramActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    static CompatibleActionBar getActionBar(Activity paramActivity) {
        if (Build.VERSION.SDK_INT >= 11) {
            return new V11ActionBar(paramActivity);
        } else {
            return new V4ActionBar(paramActivity);
        }
    }

    @Override
    public void destroy() {
        this.mActivity = null;
        this.mLayout = null;
        if (this.mLogoView != null) {
            Drawable localDrawable = this.mLogoView.getDrawable();
            if (localDrawable != null)
                localDrawable.setCallback(null);
            this.mLogoView.setOnClickListener(null);
            this.mLogoView.setImageDrawable(null);
            this.mLogoView = null;
        }
        this.mTitleText = null;
        this.mCustomView = null;
    }

    /**
     * This is a hacky way to show/hide the actionbar
     * @param margin
     */
    private void addSiblingMargin(int margin) {
        ViewParent parent = mLayout.getParent();
        if ( parent instanceof FrameLayout ) {
            FrameLayout mainFrame = (FrameLayout) parent;
            int numChildren = mainFrame.getChildCount();
            if ( numChildren == 2 ) {
                View sibling = mainFrame.getChildAt(0);
                FrameLayout.LayoutParams layParam = (FrameLayout.LayoutParams) sibling.getLayoutParams();
                layParam.topMargin += margin;
                layParam.gravity = Gravity.BOTTOM | Gravity.LEFT;
                sibling.setLayoutParams(layParam);
                sibling.requestLayout();
            }
        }        
    }
  

    /**
     * This function implements the "magic" of the V4 action bar. It only works
     * in "overlay" mode, and takes advantage of the not-often-used capability
     * of adding a content view to the activity. Since the content view is a
     * FrameLayout, it will overlay its sibling content view.
     * 
     * @return either the existing or the new LinearLayout associated with the
     *         ActionBar.
     */
    public LinearLayout getLayout() {
        if (this.mLayout == null) {
            FrameLayout localFrameLayout = new FrameLayout(this.mActivity);
            this.mLayout = ((LinearLayout) this.mActivity.getLayoutInflater()
                    .inflate(R.layout.actionbar_compatible, localFrameLayout, false));
            ViewGroup.LayoutParams localLayoutParams1 = this.mLayout
                    .getLayoutParams();
            ViewGroup.LayoutParams localLayoutParams2 = new ViewGroup.LayoutParams(
                    localLayoutParams1.width, localLayoutParams1.height);
            this.mActivity.addContentView(this.mLayout, localLayoutParams2);
            mActionBarHeight = localLayoutParams1.height;
            /*
             * This is a hack. It assumes that the parent is a FrameLayout so we can alter
             * the margins of our sibling to account for the size of the actionbar.
             */
            if ( !this.mOverlayMode ) {
                addSiblingMargin(mActionBarHeight);
            }            
            this.mTitleText = ((TextView) this.mLayout.findViewById(R.id.title_text));
            if (this.mTitleText != null)
                this.mTitleText.setText(this.mActivity.getTitle());
        }
        return this.mLayout;
    }

    @Override
    public void hide() {
        if ( mLayout.getVisibility() != View.INVISIBLE ) {
            this.mLayout.setVisibility(View.INVISIBLE);
            if ( !this.mOverlayMode ) {
                addSiblingMargin(-mActionBarHeight);
            }
        }
    }

    @Override
    public void onClick(View paramView) {
        this.mActivity.onOptionsItemSelected(new MenuItemStub(android.R.id.home));
    }

    @Override
    public void requestOverlayMode() {
        this.mOverlayMode = true;
    }

    @Override
    public void setBackgroundDrawable(Drawable paramDrawable) {
        getLayout().setBackgroundDrawable(paramDrawable);
    }

    @Override
    public void setCustomView(int paramInt) {
        setCustomView(this.mActivity.getLayoutInflater()
                .inflate(paramInt, null));
    }

    @Override
    public void setCustomView(View paramView) {
        if (this.mCustomView != null) {
            this.mLayout.removeView(this.mCustomView);
            this.mCustomView = null;
        }
        paramView.setVisibility(View.VISIBLE);
        this.mLayout.addView(paramView);
        this.mCustomView = paramView;
    }

    @Override
    public void setDisplayUseLogoEnabled(boolean paramBoolean) {
    }

    @Override
    public void setLogo(int paramInt) {
        Drawable localDrawable = this.mActivity.getResources().getDrawable(
                paramInt);
        if (this.mLogoView == null)
            this.mLogoView = ((ImageView) this.mLayout.findViewById(R.id.image_logo));
        this.mLogoView.setImageDrawable(localDrawable);
        this.mLogoView.setVisibility(0);
        this.mLogoView.setOnClickListener(this);
    }

    @Override
    public void show() {
        if ( mLayout.getVisibility() != View.VISIBLE ) {
            this.mLayout.setVisibility(View.VISIBLE);
            if ( !this.mOverlayMode ) {
                addSiblingMargin(mActionBarHeight);
            }
        }
    }

    @Override
    public void showCustomView(boolean paramBoolean) {
        View localView;
        if (this.mCustomView != null) {
            localView = this.mCustomView;
            localView.setVisibility(paramBoolean ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * This class is a bit unfortunate. In order to be able to simulate the way
     * ActionBars handle menu functionality, we need to be able to create
     * MenuItem objects that can return the selected id. This class does just
     * that.
     */
    private class MenuItemStub implements MenuItem {
        private final int m_id;

        MenuItemStub(int arg2) {
            this.m_id = arg2;
        }

        @Override
        public View getActionView() {
            return null;
        }

        @Override
        public char getAlphabeticShortcut() {
            return '\000';
        }

        @Override
        public int getGroupId() {
            return 0;
        }

        @Override
        public Drawable getIcon() {
            return null;
        }

        @Override
        public Intent getIntent() {
            return null;
        }

        @Override
        public int getItemId() {
            return this.m_id;
        }

        @Override
        public ContextMenu.ContextMenuInfo getMenuInfo() {
            return null;
        }

        @Override
        public char getNumericShortcut() {
            return '\000';
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public SubMenu getSubMenu() {
            return null;
        }

        @Override
        public CharSequence getTitle() {
            return null;
        }

        @Override
        public CharSequence getTitleCondensed() {
            return null;
        }

        @Override
        public boolean hasSubMenu() {
            return false;
        }

        @Override
        public boolean isCheckable() {
            return false;
        }

        @Override
        public boolean isChecked() {
            return false;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public boolean isVisible() {
            return false;
        }

        @Override
        public MenuItem setActionView(int paramInt) {
            return null;
        }

        @Override
        public MenuItem setActionView(View paramView) {
            return null;
        }

        @Override
        public MenuItem setAlphabeticShortcut(char paramChar) {
            return null;
        }

        @Override
        public MenuItem setCheckable(boolean paramBoolean) {
            return null;
        }

        @Override
        public MenuItem setChecked(boolean paramBoolean) {
            return null;
        }

        @Override
        public MenuItem setEnabled(boolean paramBoolean) {
            return null;
        }

        @Override
        public MenuItem setIcon(int paramInt) {
            return null;
        }

        @Override
        public MenuItem setIcon(Drawable paramDrawable) {
            return null;
        }

        @Override
        public MenuItem setIntent(Intent paramIntent) {
            return null;
        }

        @Override
        public MenuItem setNumericShortcut(char paramChar) {
            return null;
        }

        @Override
        public MenuItem setOnMenuItemClickListener(
                MenuItem.OnMenuItemClickListener paramOnMenuItemClickListener) {
            return null;
        }

        @Override
        public MenuItem setShortcut(char paramChar1, char paramChar2) {
            return null;
        }

        @Override
        public void setShowAsAction(int paramInt) {
        }

        @Override
        public MenuItem setTitle(int paramInt) {
            return null;
        }

        @Override
        public MenuItem setTitle(CharSequence paramCharSequence) {
            return null;
        }

        @Override
        public MenuItem setTitleCondensed(CharSequence paramCharSequence) {
            return null;
        }

        @Override
        public MenuItem setVisible(boolean paramBoolean) {
            return null;
        }

        @Override
        public boolean collapseActionView() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean expandActionView() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public ActionProvider getActionProvider() {
            return null;
        }

        @Override
        public boolean isActionViewExpanded() {
            return false;
        }

        @Override
        public MenuItem setActionProvider(ActionProvider arg0) {
            return null;
        }

        @Override
        public MenuItem setOnActionExpandListener(OnActionExpandListener arg0) {
            return null;
        }

        @Override
        public MenuItem setShowAsActionFlags(int arg0) {
            return null;
        }
    }
}
