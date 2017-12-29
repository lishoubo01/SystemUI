package com.freeme.systemui.statusbar.policy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import com.android.systemui.R;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.freeme.systemui.tint.TintManager;

public class FreemeKeyButtonView extends KeyButtonView {
    private static final String TAG = "FreemeKeyButtonView";
    private static final boolean DEBUG = false;

    public FreemeKeyButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTintType("navigationBarType");
    }

    public FreemeKeyButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTintType("navigationBarType");
    }

    @Override
    protected void setTint(Drawable drawable) {
        if (drawable != null) {
            if (mIsResever) {
                if (TintManager.DEF_TINT_WHITE != TintManager.getInstance().getIconColorByType(mTintType)) {
                    drawable.setTintList(null);
                    drawable.setTint(getContext().getColor(R.color.freeme_navigation_bar_icon_color_black));
                } else if (TintManager.getInstance().isLuncherStyle()) {
                    drawable.setTint(getContext().getColor(R.color.freeme_navigation_bar_icon_luncher_color));
                } else {
                    drawable.setTint(getContext().getColor(R.color.navigation_bar_icon_color));
                }
                invalidate();
                return;
            }
            if (DEBUG) {
                Log.i(TAG, "no resever setTintList " + this);
            }
        }
    }
}
