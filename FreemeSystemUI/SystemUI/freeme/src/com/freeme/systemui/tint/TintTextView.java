package com.freeme.systemui.tint;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

public class TintTextView extends TextView implements Observer {
    private int mColor = TintManager.DEF_TINT_WHITE;
    private boolean mIsResever = true;

    public TintTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TintTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TintTextView(Context context) {
        super(context);
    }

    @Override
    protected void onAttachedToWindow() {
        TintManager.getInstance().addObserver(this);
        setColorByTintManager();
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        TintManager.getInstance().deleteObserver(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (getVisibility() == VISIBLE) {
            setColorByTintManager();
        }
    }

    public void setColorByTintManager() {
        if (isResever() && getVisibility() == VISIBLE) {
            int tintColor = TintManager.getInstance().getIconColorByType("statusBarType", mColor);
            super.setTextColor(tintColor);
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        if (visibility == VISIBLE && !TintUtil.isAnyParentNodeInvisible(this)) {
            setColorByTintManager();
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    public void setTextColor(int color) {
        mColor = color;
        if (isResever()) {
            color = TintManager.getInstance().getIconColorByType("statusBarType", mColor);
        }
        super.setTextColor(color);
    }

    public boolean isResever() {
        return mIsResever;
    }

    public void setIsResever(boolean isResever) {
        mIsResever = isResever;
    }
}
