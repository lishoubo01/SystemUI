package com.freeme.systemui.statusbar;

import android.util.Log;
import com.android.systemui.statusbar.phone.NavigationBarTransitions;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.freeme.systemui.tint.TintManager;

public class FreemeNavigationBarTransitions extends NavigationBarTransitions {
    private static final String TAG = "FreemeNBTransitions";
    private static final boolean DEBUG = false;

    private int mColor;
    private NavigationBarView mView;

    public FreemeNavigationBarTransitions(NavigationBarView view) {
        super(view);
        mView = view;
        TintManager.getInstance().setNavigationBarTransitions(this);
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

    public void transitionTo(int mode, boolean animate) {
        super.transitionTo(mode, animate);
        boolean isFreemeStyle = TintManager.getInstance().isFreemeStyle();
        if (DEBUG) {
            Log.i(TAG, "transitionTo:mode=" + mode + ", isFreemeStyle=" + isFreemeStyle);
        }
        if (1 == mode && isFreemeStyle) {
            setBackgroundColor(TintManager.getInstance().getSemiStatusBarBgColor());
        }
    }
}
