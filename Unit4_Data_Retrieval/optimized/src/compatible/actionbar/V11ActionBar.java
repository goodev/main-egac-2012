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

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.Window;

public class V11ActionBar implements CompatibleActionBar {
	ActionBar mActionBar;
	Activity mActivity;

	V11ActionBar(Activity paramActivity) {
		this.mActivity = paramActivity;
	}

	@Override
	public void destroy() {
		this.mActivity = null;
		this.mActionBar = null;
	}

	ActionBar getActionBar() {
		if (this.mActionBar == null)
			this.mActionBar = this.mActivity.getActionBar();
		return this.mActionBar;
	}

	@Override
	public void hide() {
		getActionBar().hide();
	}

	@Override
	public void requestOverlayMode() {
		if (Build.VERSION.SDK_INT >= 11)
			this.mActivity.getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
	}

	@Override
	public void setBackgroundDrawable(Drawable paramDrawable) {
		getActionBar().setBackgroundDrawable(paramDrawable);
	}

	@Override
	public void setCustomView(int paramInt) {
		getActionBar().setCustomView(paramInt);
	}

	@Override
	public void setCustomView(View paramView) {
		getActionBar().setCustomView(paramView);
	}

	@Override
	public void setDisplayUseLogoEnabled(boolean paramBoolean) {
		getActionBar().setDisplayUseLogoEnabled(paramBoolean);
	}

	@Override
	public void setLogo(int paramInt) {
	}

	@Override
	public void show() {
		getActionBar().show();
	}

	@Override
	public void showCustomView(boolean paramBoolean) {
		int displayOptions = getActionBar().getDisplayOptions();
		if (paramBoolean) {
			displayOptions |= ActionBar.DISPLAY_SHOW_CUSTOM;
		} else {
			displayOptions &= ~ActionBar.DISPLAY_SHOW_CUSTOM;
		}
		getActionBar().setDisplayOptions(displayOptions);
		return;
	}
}
