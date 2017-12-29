package com.freeme.systemui.tint;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.RemotableViewMethod;
import android.view.View;
import android.widget.ImageView;

import java.util.Observable;
import java.util.Observer;

import static com.freeme.systemui.tint.TintManager.DEF_TINT_WHITE;

public class TintImageView extends ImageView implements Observer {
    private static final String TAG = "TintImageView";
    public static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    protected boolean mIsResever = true;

    public TintImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TintImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TintImageView(Context context) {
        super(context);
    }

    public TintImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        if (DEBUG) {
            Log.d(TAG, "onAttachedToWindow:" + this);
        }
        TintManager.getInstance().addObserver(this);
        setTint();
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (DEBUG) {
            Log.d(TAG, "onDetachedFromWindow:" + this);
        }
        TintManager.getInstance().deleteObserver(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void update(Observable o, Object arg) {
        setTint();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable != null) {
            drawable.mutate();
            setTint();
        }
    }

    @Override
    @RemotableViewMethod
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (getDrawable() != null) {
            getDrawable().mutate();
            setTint();
        }
    }

    public void setTint() {
        if (getVisibility() == VISIBLE) {
            Drawable drawable = getDrawable();
            if (drawable != null) {
                setTint(drawable);
            }
        }
    }

    protected void setTint(Drawable drawable) {
        if (drawable != null) {
            if (mIsResever) {
                int color = TintManager.getInstance().getIconColorByType(mTintType);
                if (DEF_TINT_WHITE != color) {
                    drawable.setTintList(null);
                    drawable.setTint(color);
                } else {
                    drawable.setTintList(null);
                    drawable.setTint(color);
                }
                invalidate();
            }
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        if (visibility == VISIBLE && !TintUtil.isAnyParentNodeInvisible(this)) {
            setTint();
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    public void setImageTintList(ColorStateList tint) {
        if (DEBUG) {
            Log.d(TAG, "setImageTintList:" + tint + " " + this);
        }
        if (mIsResever) {
            super.setImageTintList(tint);
        }
    }

    public void setIsResever(boolean isResever) {
        if (DEBUG) {
            Log.d(TAG, "setIsResever:" + isResever + " " + this);
        }
        mIsResever = isResever;
        if (!isResever && getDrawable() != null) {
            getDrawable().setTintList(null);
        }
    }

    protected String mTintType = "statusBarType";
    public void setTintType(String tintType) {
        mTintType = tintType;
    }
}
