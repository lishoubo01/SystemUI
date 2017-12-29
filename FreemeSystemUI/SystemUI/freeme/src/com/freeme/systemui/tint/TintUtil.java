package com.freeme.systemui.tint;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

class TintUtil {
    static boolean isAnyParentNodeInvisible(View view) {
        ViewParent parent;
        for (parent = view.getParent(); parent != null && parent instanceof ViewGroup; parent = parent.getParent()) {
            if (((ViewGroup) parent).getVisibility() != View.VISIBLE) {
                return true;
            }
        }
        return false;
    }
}
