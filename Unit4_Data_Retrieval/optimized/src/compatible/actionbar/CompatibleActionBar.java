
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
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * A minimal implementation of an ActionBar that allows this application to take
 * advantage of the new ICS ActionBar look and feel while still preserving
 * compatibility with previous release of Android.
 */
public abstract interface CompatibleActionBar {
    public abstract void destroy();

    public abstract void hide();

    public abstract void requestOverlayMode();

    public abstract void setBackgroundDrawable(Drawable paramDrawable);

    public abstract void setCustomView(int paramInt);

    public abstract void setCustomView(View paramView);

    public abstract void setDisplayUseLogoEnabled(boolean paramBoolean);

    public abstract void setLogo(int paramInt);

    public abstract void show();

    public abstract void showCustomView(boolean paramBoolean);
}
