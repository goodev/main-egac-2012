
package compatible.shareactionprovider;
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
import android.content.Intent;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

/**
 * A simple wrapper class for some of the functionality of the the
 * ShareActionProvier added in V14 of Android.
 */
public class V14ShareActionProvider extends CompatibleShareActionProvider {
    final ShareActionProvider mSharedActionProvider;

    public V14ShareActionProvider(MenuItem item) {
        super(item);
        mSharedActionProvider = (ShareActionProvider) item.getActionProvider();
    }

    @Override
    public void setShareHistoryFileName(String shareHistoryFile) {
        if (null != mSharedActionProvider) {
            mSharedActionProvider.setShareHistoryFileName(shareHistoryFile);
        }

    }

    @Override
    public void setShareIntent(Intent shareIntent) {
        if (null != mSharedActionProvider) {
            mSharedActionProvider.setShareIntent(shareIntent);
        }
    }

}
