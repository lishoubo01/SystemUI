package com.freeme.systemui.tint;

import java.util.Observable;
import android.util.Log;

import com.freeme.systemui.statusbar.FreemeNavigationBarTransitions;
import com.freeme.systemui.statusbar.FreemePhoneStatusBarTransitions;

public class TintManager extends Observable {
    private static final String TAG = "TintManager";
    public static final boolean DEBUG = false;

    private static TintManager sInstance = new TintManager();
    private int mLastStatusColor = DEF_TINT_WHITE;
    private int mTintColor = DEF_TINT_WHITE;
    private boolean mLightBar;

    public static final int DEF_TINT_BLACK = 0xbe000000; // 70% black
    public static final int DEF_TINT_WHITE = 0xb3ffffff; // 70% white

    private static final int FREEMESTYLE = 1;
    private static final int FREEMELIGHTSTYLE = 1;
    private static final int LAUNCHERSTYLE = -1;
    private static final int TOUCH_EXPLORATION_ENABLED = -2;

    private static FreemeNavigationBarTransitions mNavigationBarTransitions;
    private static FreemePhoneStatusBarTransitions mPhoneStatusBarTransitions;
    private static int mFreemeStyle = LAUNCHERSTYLE;
    private int mFreemeLightStyle = 0;
    private int mLastNaviColor = DEF_TINT_WHITE;

    private TintManager() {
    }

    public static TintManager getInstance() {
        return sInstance;
    }

    public void setTintColor(int color) {
        mTintColor = color;
        updateBarTint();
    }

    public int getIconColorByType(String type) {
        if ("statusBarType".equals(type)) {
            return mLastStatusColor;
        } else if ("navigationBarType".equals(type)) {
            return mLastNaviColor;
        }
        return DEF_TINT_WHITE;
    }

    private int getIconColorByTypeInner(String type) {
        if ("statusBarType".equals(type) && mTintColor != DEF_TINT_BLACK && mTintColor != DEF_TINT_WHITE) {
            return mTintColor;
        }

        return mLightBar ? DEF_TINT_BLACK : DEF_TINT_WHITE;
    }

    int getIconColorByType(String type, int color) {
        if ("statusBarType".equals(type) && mTintColor != DEF_TINT_BLACK && mTintColor != DEF_TINT_WHITE) {
            return mTintColor;
        }

        if (getIconColorByType(type) == DEF_TINT_BLACK) {
            if ((color & 0xFFFFFF) == 0xFFFFFF) {
                return DEF_TINT_BLACK;
            }
        } else if ((color & 0xFFFFFF) == 0) {
            return DEF_TINT_WHITE;
        }
        return color;
    }

    private void updateBarTint() {
        if (countObservers() > 0 && updateLocalLastIconColor()) {
            try {
                setChanged();
                notifyObservers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean updateLocalLastIconColor() {
        boolean changed = false;
        int naviColor = getIconColorByTypeInner("navigationBarType");
        if (naviColor != mLastNaviColor) {
            changed = true;
            mLastNaviColor = naviColor;
        }
        int statusColor = getIconColorByTypeInner("statusBarType");
        if (statusColor != mLastStatusColor) {
            changed = true;
            mLastStatusColor = statusColor;
        }
        return changed;
    }

    public void setLightBar(boolean light) {
        if (mLightBar != light) {
            mLightBar = light;
            updateBarTint();
        }
    }

    private void updateNavigationBarColor() {
        if (DEBUG) {
            Log.d(TAG, "updateNavigationBarColor mFreemeStyle=" + mFreemeStyle);
        }
        if (mNavigationBarTransitions != null) {
            if (TOUCH_EXPLORATION_ENABLED == mFreemeStyle) {
                mNavigationBarTransitions.setBackgroundColor(android.R.color.black);
            } else if (isLuncherStyle()
                    || (FREEMESTYLE == mFreemeStyle && (FREEMELIGHTSTYLE == mFreemeLightStyle || mFreemeLightStyle == 0))) {
                mNavigationBarTransitions.setBackgroundColor(0);
            } else {
                mNavigationBarTransitions.restoreBackgroundColor();
            }
        }
    }

    public void setPhoneStatusBarTransitions(FreemePhoneStatusBarTransitions pst) {
        if (DEBUG) {
            Log.i(TAG, "setPhoneStatusBarTransitions " + pst.toString());
        }
        mPhoneStatusBarTransitions = pst;
    }

    public void setNavigationBarTransitions(FreemeNavigationBarTransitions nbt) {
        if (DEBUG) {
            Log.i(TAG, "setNavigationBarTransitions " + nbt.toString());
        }
        mNavigationBarTransitions = nbt;
    }

    public int getSemiStatusBarBgColor() {
        return getIconColorByType("statusBarType") == DEF_TINT_WHITE ? android.R.color.black : 0x66000000;
    }

    public boolean isFreemeStyle() {
        return FREEMESTYLE == mFreemeStyle;
    }

    public boolean isFreemeLightStyle() {
        return FREEMELIGHTSTYLE == mFreemeLightStyle;
    }

    public boolean isLuncherStyle() {
        return LAUNCHERSTYLE == mFreemeStyle;
    }

    public void updateBarBgColor(int freemeStyle, int statusBarColor, int navigationBarColor, int freemeLightStyle) {
        if (DEBUG) {
            Log.i(TAG, "updateBarBgColor:freemeStyle=" + freemeStyle
                    + ", freemeLightStyle=" + freemeLightStyle
                    + ", statusBarColor=" + String.format("#%08X", statusBarColor)
                    + ", navigationBarColor=" + String.format("#%08X", navigationBarColor));
        }
        mFreemeStyle = freemeStyle;
        mFreemeLightStyle = freemeLightStyle;
        if (mPhoneStatusBarTransitions != null) {
            if (mFreemeStyle == 0) {
                mPhoneStatusBarTransitions.restoreBackgroundColor();
            } else {
                if (LAUNCHERSTYLE != mFreemeStyle) {
                    mPhoneStatusBarTransitions.setBackgroundColor(TOUCH_EXPLORATION_ENABLED == mFreemeStyle ? android.R.color.black : 0);
                }
            }
        }
        updateNavigationBarColor();

        updateBarTint();
    }
}
