
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
import android.support.v4.app.FragmentActivity;

/**
 * A class that associates a compatible action bar with the FragmentActivity
 * from the Android support library. The goal of the classes in this package is
 * to provide a minimal amount of backwards-compatible support for the features
 * of ActionBar that are useful to this application rather than to provide a
 * complete implementation.
 */
public class ActionBarFragmentActivity extends FragmentActivity {
    private CompatibleActionBar mCAB;

    public CompatibleActionBar getCompatibleActionBar() {
        if (this.mCAB == null)
            this.mCAB = V4ActionBar.getActionBar(this);
        return this.mCAB;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mCAB.destroy();
        this.mCAB = null;
    }
}
