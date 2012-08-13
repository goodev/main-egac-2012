
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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

/**
 * This class corrects an anomaly that impacts the progress bar on
 * Honeycomb-class devices. It stands as an interesting example of using
 * onMeasure (now deprecated) to fix the aspect ratio of a view element.
 */
public class FixedRatioProgressBar extends ProgressBar {
    boolean mFix;

    public FixedRatioProgressBar(Context paramContext) {
        super(paramContext);
        this.mFix = Build.VERSION.SDK_INT < 11;
    }

    public FixedRatioProgressBar(Context paramContext,
            AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        this.mFix = Build.VERSION.SDK_INT < 11;
    }

    public FixedRatioProgressBar(Context paramContext,
            AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        this.mFix = Build.VERSION.SDK_INT < 11;
    }

    private int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case View.MeasureSpec.UNSPECIFIED:
                break;
            case View.MeasureSpec.AT_MOST:
                desiredSize = Math.min(desiredSize, specSize);
                break;
            case View.MeasureSpec.EXACTLY:
                desiredSize = specSize;
                break;
        }
        return desiredSize;
    }

    /** @deprecated */
    protected synchronized void onMeasure(int widthMeasureSpec,
            int heightMeasureSpec) {
        if (this.mFix && this.isIndeterminate()) {
            Drawable localDrawable = getIndeterminateDrawable();
            float desiredAspect = 1.0F;
            boolean resizeWidth;
            boolean resizeHeight;
            int widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec);
            int heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec);
            resizeWidth = (widthSpecMode != View.MeasureSpec.EXACTLY);
            resizeHeight = (heightSpecMode != View.MeasureSpec.EXACTLY);
            int pLeft = getPaddingLeft();
            int pRight = getPaddingRight();
            int pTop = getPaddingTop();
            int pBottom = getPaddingBottom();
            int widthSize = resolveAdjustedSize(
                    pRight + pLeft + localDrawable.getIntrinsicWidth(),
                    widthMeasureSpec);
            int heightSize = resolveAdjustedSize(
                    pTop + pBottom + localDrawable.getIntrinsicHeight(),
                    heightMeasureSpec);

            float actualAspect = (float) (widthSize - pLeft - pRight)
                    / (heightSize - pTop - pBottom);

            if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {

                boolean done = false;

                // Try adjusting width to be proportional to height
                if (resizeWidth) {
                    int newWidth = (int) (desiredAspect * (heightSize - pTop - pBottom))
                            + pLeft + pRight;
                    if (newWidth <= widthSize) {
                        widthSize = newWidth;
                        done = true;
                    }
                }

                // Try adjusting height to be proportional to width
                if (!done && resizeHeight) {
                    int newHeight = (int) ((widthSize - pLeft - pRight) / desiredAspect)
                            + pTop + pBottom;
                    if (newHeight <= heightSize) {
                        heightSize = newHeight;
                    }
                }
                setMeasuredDimension(widthSize, heightSize);
                return;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
