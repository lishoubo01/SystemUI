package com.freeme.systemui.statusbar;

import android.util.Log;
import android.graphics.drawable.Drawable;
import com.android.systemui.statusbar.phone.PhoneStatusBarTransitions;
import com.android.systemui.statusbar.phone.PhoneStatusBarView;
import com.freeme.systemui.tint.TintManager;

public class FreemePhoneStatusBarTransitions extends PhoneStatusBarTransitions {
    private static final String TAG = "FreemePSTransitions";
    private static final boolean DEBUG = false;

    private int mColor;
    private float mLastAlpha = 0.5f;
    private PhoneStatusBarView mView;

    public FreemePhoneStatusBarTransitions(PhoneStatusBarView view) {
        super(view);
        mView = view;
        TintManager.getInstance().setPhoneStatusBarTransitions(this);
    }

    public void setBackgroundColor(int color) {
        if (DEBUG) {
            Log.i(TAG, "setBackgroundColor:new=" + color + ", old=" + mBarBackground.mColor);
        }
        mView.setBackgroundColor(color);
        mColor = color;
    }

    public void restoreBackgroundColor() {
        if (DEBUG) {
            Log.i(TAG, "restoreBackgroundColor:new=" + mBarBackground.mColor + ", old=" + mColor);
        }
        mView.setBackground(mBarBackground);
    }

    public void setBackgroundDrawable(Drawable drawable) {
        if (DEBUG) {
            Log.i(TAG, "setBackgroundDrawable:drawable=" + drawable);
        }
        mView.setBackgroundDrawable(drawable);
    }

    public void setBackgroundAlpha(float alpha) {
        if (isFloatChanged(mLastAlpha, alpha)) {
            if (DEBUG) {
                Log.i(TAG, "setBackgroundAlpha:alpha=" + alpha);
            }
            mView.setAlpha(alpha);
            mLastAlpha = alpha;
        }
    }

    public void transitionTo(int mode, boolean animate) {
        super.transitionTo(mode, animate);
        if (1 == mode && TintManager.getInstance().isFreemeStyle()) {
            setBackgroundColor(TintManager.getInstance().getSemiStatusBarBgColor());
        }
    }

    private boolean isFloatChanged(float data1, float data2) {
        return Math.abs(data1 - data2) > 1.0E-6f;
    }
}
