
package sample.multithreading;
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
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * In order to help Android clean up memory, we can call the nullViewDrawables
 * function to walk the view hierarchy and try to remove extra references that
 * may slow down the gc process.
 */
public class LayoutUtils {
    private static void nullViewDrawable(View view) {
        if ((view instanceof ImageView)) {
            ImageView imageView = (ImageView) view;
            Drawable d = imageView.getDrawable();
            if (d != null)
                d.setCallback(null);
            imageView.setImageDrawable(null);
        }
        Drawable d = view.getBackground();
        if (d != null)
            d.setCallback(null);
        view.setBackgroundDrawable(null);
    }

    public static void nullViewDrawables(View view) {
        ViewGroup viewGroup;
        if (view != null) {
            nullViewDrawable(view);
            if ((view instanceof ViewGroup)) {
                viewGroup = (ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i <= childCount; i++) {
                    nullViewDrawables(viewGroup.getChildAt(i));
                }
            }
        }
    }
}
