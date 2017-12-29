/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.MathUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;
import libcore.util.Objects;

/** View that represents a standard quick settings tile. **/
public class QSTileView extends QSTileBaseView {
    protected final Context mContext;
    private final int mTileSpacingPx;
    private int mTilePaddingTopPx;

    protected TextView mLabel;
    private ImageView mPadLock;

    //*/ freeme.gouzhouping, 20170912. quick settings tile.
    protected LayoutParams dividerParam;
    protected View mDivider;
    //*/

    public QSTileView(Context context, QSIconView icon) {
        this(context, icon, false);
    }

    public QSTileView(Context context, QSIconView icon, boolean collapsedView) {
        super(context, icon, collapsedView);

        mContext = context;
        final Resources res = context.getResources();
        mTileSpacingPx = res.getDimensionPixelSize(R.dimen.qs_tile_spacing);
        //*/ freeme.gouzhouping, 20170912. quick settings tile.
        dividerParam = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //*/

        setClipChildren(false);

        setClickable(true);
        updateTopPadding();
        setId(View.generateViewId());
        //*/ freeme.gouzhouping, 20170912. quick settings tile.
        createDivider();
        //*/
        createLabel();
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
    }

    TextView getLabel() {
        return mLabel;
    }

    //*/ freeme.gouzhouping, 20170912. quick settings tile.
    View getDivider() {
        return mDivider;
    }

    protected void updateRippleSize(int width, int height) {
        View icon = getIcon();
        updateRippleSize(width, icon.getBottom(), (int) (icon.getHeight() * 0.46f));
    }
    //*/

    private void updateTopPadding() {
        Resources res = getResources();
        int padding = res.getDimensionPixelSize(R.dimen.qs_tile_padding_top);
        int largePadding = res.getDimensionPixelSize(R.dimen.qs_tile_padding_top_large_text);
        float largeFactor = (MathUtils.constrain(getResources().getConfiguration().fontScale,
                1.0f, FontSizeUtils.LARGE_TEXT_SCALE) - 1f) / (FontSizeUtils.LARGE_TEXT_SCALE - 1f);
        mTilePaddingTopPx = Math.round((1 - largeFactor) * padding + largeFactor * largePadding);
        setPadding(mTileSpacingPx, mTilePaddingTopPx + mTileSpacingPx, mTileSpacingPx,
                mTileSpacingPx);
        //*/ freeme.gouzhouping, 20170912. quick settings tile.
        dividerParam.topMargin = (int) mContext.getResources().getDimension(R.dimen.qs_tile_between_icon_divider_margintop);
        dividerParam.bottomMargin = (int) mContext.getResources().getDimension(R.dimen.qs_tile_between_icon_divider_marginbottom);
        //*/
        requestLayout();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateTopPadding();
        FontSizeUtils.updateFontSize(mLabel, R.dimen.qs_tile_text_size);
    }

    protected void createLabel() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.qs_tile_label, null);
        mLabel = (TextView) view.findViewById(R.id.tile_label);
        mPadLock = (ImageView) view.findViewById(R.id.restricted_padlock);
        //*/ freeme.gouzhouping, 20170912. quick settings tile.
        updateShowButtonBackground();
        //*/
        addView(view);
    }

    //*/ freeme.gouzhouping, 20170912. quick settings tile.
    public void updateShowButtonBackground() {
        Context context = getContext();
        mLabel.setBackground(context.getDrawable(R.drawable.btn_borderless_rect));
    }
    //*/

    @Override
    protected void handleStateChanged(QSTile.State state) {
        super.handleStateChanged(state);
        if (!Objects.equal(mLabel.getText(), state.label)) {
            mLabel.setText(state.label);
        }
        mLabel.setEnabled(!state.disabledByPolicy);
        mPadLock.setVisibility(state.disabledByPolicy ? View.VISIBLE : View.GONE);
    }

    //*/ freeme.gouzhouping, 20170912. quick settings tile.
    protected void createDivider() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.freeme_qs_tile_divider, null);
        mDivider = view.findViewById(R.id.tile_divider);
        mDivider.setVisibility(View.VISIBLE);
        dividerParam.topMargin = (int) mContext.getResources().getDimension(R.dimen.qs_tile_between_icon_divider_margintop);
        dividerParam.bottomMargin = (int) mContext.getResources().getDimension(R.dimen.qs_tile_between_icon_divider_marginbottom);
        view.setLayoutParams(dividerParam);
        addView(view);
    }

    public void init(OnClickListener click, OnClickListener secondary, OnLongClickListener longClick) {
        init(click, longClick);
        getLabel().setOnClickListener(secondary);
        getLabel().setOnLongClickListener(longClick);
    }
    //*/
}