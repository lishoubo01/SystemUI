package com.android.systemui.recents.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleProgress extends View {

    private int width;
    private int height;
    private int radius;
    private Paint paint = new Paint();
    private Paint paint2 = new Paint();
    private Paint paint3 = new Paint();
    private Rect rec = new Rect();
    private int lastValue = 0;
    private int currentvalue = 0;
    @Deprecated
    private int progressColor = Color.parseColor("#00CED1");
    private int CircleColor = Color.parseColor("#C0C0C0");
    private int LineColor = Color.parseColor("#F8F8FF");
    private int startAngle = 270;
    private float paddingscale = 0.8f;
    RectF rectf = new RectF();
    private float mStartX1;
    private float mStartY1;
    private float mEndX1;
    private float mEndY1;
    private float mStartX2;
    private float mStartY2;
    private float mEndX2;
    private float mEndY2;
    public CircleProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        width = getWidth();
        int size = height = getHeight();
        if (height > width)
            size = width;
        radius = (int) (size * paddingscale / 2f);
        paint.setAntiAlias(true);
        paint.setColor(CircleColor);
        paint.setStyle (Paint.Style.STROKE);
        canvas.drawCircle(width / 2, height / 2, radius, paint);
        mStartX1 = (width - radius ) / 2f+radius/4f;
        mStartY1 = (height - radius ) / 2f+radius/4f;
        mEndX1 = (width - radius ) / 2f+ radius/4f*3f;
        mEndY1 = (height - radius ) / 2f+ radius/4f*3f;;
        mStartX2 = (width - radius ) / 2f+radius/4f;
        mStartY2 = (height - radius ) / 2f+ radius/4f*3f;;
        mEndX2 = (width - radius ) / 2f+ radius/4f*3f;
        mEndY2 = (width - radius ) / 2f+radius/4f;
        paint2.setAntiAlias(true);
        paint2.setColor(LineColor);
        paint2.setStrokeWidth(3f);
        canvas.drawLine (mStartX1,mStartY1,mEndX1,mEndY1,paint2);
        canvas.drawLine (mStartX2,mStartY2,mEndX2,mEndY2,paint2);
        rectf.set((width - radius * 2) / 2f, (height - radius * 2) / 2f,
                ((width - radius * 2) / 2f) + (2 * radius),
                ((height - radius * 2) / 2f) + (2 * radius));
        paint3.setAntiAlias(true);
        paint3.setColor(progressColor);
        paint3.setStrokeWidth(4f);
        paint3.setStyle (Paint.Style.STROKE);
        canvas.drawArc(rectf, startAngle, currentvalue * 3.6f, false, paint3);
        super.onDraw(canvas);
    }

    public void setValue(int value) {
        if (value > 100)
            return;
        this.currentvalue = value;

        invalidate();
    }

    public int getValue( ) {
        return  currentvalue;
    }
}
