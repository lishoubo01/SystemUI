/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.systemui.qs.customize;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toolbar;
import android.widget.Toolbar.OnMenuItemClickListener;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.qs.QSContainer;
import com.android.systemui.qs.QSDetailClipper;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.policy.KeyguardMonitor.Callback;

import com.freeme.util.FreemeOption;

/// M: add plugin in quicksetting @{
import com.mediatek.systemui.ext.IQuickSettingsPlugin;
import com.mediatek.systemui.PluginManager;
/// @}

import java.util.ArrayList;
import java.util.List;

/**
 * Allows full-screen customization of QS, through show() and hide().
 *
 * This adds itself to the status bar window, so it can appear on top of quick settings and
 * *someday* do fancy animations to get into/out of it.
 */
public class QSCustomizer extends LinearLayout implements OnMenuItemClickListener {

    private static final int MENU_RESET = Menu.FIRST;

    /*/ freeme.gouzhouping. 20170830. remove edit animation
    private final QSDetailClipper mClipper;
    //*/

    private PhoneStatusBar mPhoneStatusBar;

    private boolean isShown;
    private QSTileHost mHost;
    private RecyclerView mRecyclerView;
    private TileAdapter mTileAdapter;
    private Toolbar mToolbar;
    private boolean mCustomizing;
    private NotificationsQuickSettingsContainer mNotifQsContainer;
    private QSContainer mQsContainer;

    //*/ freeme.zhongkai.zhu. 20170803. fix AOSP's bug: systemui stuck.
    // bug redo step: leaving EDIT, then press edit icon again immediately.
    private boolean isCollapseAnimating;
    //*/

    public QSCustomizer(Context context, AttributeSet attrs) {
        super(new ContextThemeWrapper(context, R.style.edit_theme), attrs);
        /*/ freeme.gouzhouping. 20170830. remove edit animation
        mClipper = new QSDetailClipper(this);
        //*/

        LayoutInflater.from(getContext()).inflate(R.layout.qs_customize_panel_content, this);

        mToolbar = (Toolbar) findViewById(com.android.internal.R.id.action_bar);
        TypedValue value = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.homeAsUpIndicator, value, true);
        mToolbar.setNavigationIcon(
                getResources().getDrawable(value.resourceId, mContext.getTheme()));
        mToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide((int) v.getX() + v.getWidth() / 2, (int) v.getY() + v.getHeight() / 2);
            }
        });
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.getMenu().add(Menu.NONE, MENU_RESET, 0,
                mContext.getString(com.android.internal.R.string.reset));
        mToolbar.setTitle(R.string.qs_edit);

        mRecyclerView = (RecyclerView) findViewById(android.R.id.list);
        mTileAdapter = new TileAdapter(getContext());
        mRecyclerView.setAdapter(mTileAdapter);
        mTileAdapter.getItemTouchHelper().attachToRecyclerView(mRecyclerView);
        /*/ freeme.gouzhouping, 20170911. quick settings and edit window.
        GridLayoutManager layout = new GridLayoutManager(getContext(), 3);
        /*/
        GridLayoutManager layout = new GridLayoutManager(getContext(), mContext.getResources().
                getInteger(R.integer.freeme_edit_num_columns));
        //*/
        layout.setSpanSizeLookup(mTileAdapter.getSizeLookup());
        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.addItemDecoration(mTileAdapter.getItemDecoration());
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setMoveDuration(TileAdapter.MOVE_DURATION);
        mRecyclerView.setItemAnimator(animator);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        View navBackdrop = findViewById(R.id.nav_bar_background);
        if (navBackdrop != null) {
            boolean shouldShow = newConfig.smallestScreenWidthDp >= 600
                    || newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE;
            navBackdrop.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }

    public void setHost(QSTileHost host) {
        mHost = host;
        mPhoneStatusBar = host.getPhoneStatusBar();
        mTileAdapter.setHost(host);
    }

    public void setContainer(NotificationsQuickSettingsContainer notificationsQsContainer) {
        mNotifQsContainer = notificationsQsContainer;
    }

    public void setQsContainer(QSContainer qsContainer) {
        mQsContainer = qsContainer;
    }

    public void show(int x, int y) {
        //*/ freeme.zhongkai.zhu. 20170803. fix AOSP's bug: systemui stuck.
        if (isCollapseAnimating) {
            return;
        }
        //*/

        if (!isShown) {
            MetricsLogger.visible(getContext(), MetricsProto.MetricsEvent.QS_EDIT);
            isShown = true;
            setTileSpecs();
            setVisibility(View.VISIBLE);
            //*/ freeme.gouzhouping. 20170830. remove edit animation
            setCustomizing(true);
            animateShow(mRecyclerView, true);
            /*/
            mClipper.animateCircularClip(x, y, true, mExpandAnimationListener);
            //*/
            new TileQueryHelper(mContext, mHost).setListener(mTileAdapter);
            //*/ freeme.gouzhouping. 20170830. remove edit animation
            mNotifQsContainer.setCustomizerAnimating(false);
            /*/
            mNotifQsContainer.setCustomizerAnimating(true);
            //*/
            mNotifQsContainer.setCustomizerShowing(true);
            announceForAccessibility(mContext.getString(
                    R.string.accessibility_desc_quick_settings_edit));
            mHost.getKeyguardMonitor().addCallback(mKeyguardCallback);
        }
    }

    public void hide(int x, int y) {
        if (isShown) {
            MetricsLogger.hidden(getContext(), MetricsProto.MetricsEvent.QS_EDIT);
            isShown = false;
            mToolbar.dismissPopupMenus();
            setCustomizing(false);
            save();
            //*/ freeme.gouzhouping. 20170830. remove edit animation
            animateHide(mRecyclerView, true);
            setVisibility(View.GONE);
            mNotifQsContainer.setCustomizerAnimating(false);
            mNotifQsContainer.setCustomizerShowing(false);
            /*/
            mClipper.animateCircularClip(x, y, false, mCollapseAnimationListener);
            mNotifQsContainer.setCustomizerAnimating(true);
            mNotifQsContainer.setCustomizerShowing(false);
            //*/
            announceForAccessibility(mContext.getString(
                    R.string.accessibility_desc_quick_settings));
            mHost.getKeyguardMonitor().removeCallback(mKeyguardCallback);
        }
    }

    private void setCustomizing(boolean customizing) {
        mCustomizing = customizing;
        mQsContainer.notifyCustomizeChanged();
    }

    public boolean isCustomizing() {
        return mCustomizing;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                MetricsLogger.action(getContext(), MetricsProto.MetricsEvent.ACTION_QS_EDIT_RESET);
                reset();
                break;
        }
        return false;
    }

    private void reset() {
        ArrayList<String> tiles = new ArrayList<>();
        String defTiles = mContext.getString(R.string.quick_settings_tiles_default);

        //*/ freeme. gouzhouping, 20170113. extra tiles.
        defTiles += "," + mContext.getString(R.string.freeme_quick_settings_tiles);
        //*/

        /// M: Customize the quick settings tile order for operator. @{
        IQuickSettingsPlugin quickSettingsPlugin = PluginManager.getQuickSettingsPlugin(mContext);
        defTiles = quickSettingsPlugin.customizeQuickSettingsTileOrder(defTiles);
        /// M: Customize the quick settings tile order for operator. @}
        for (String tile : defTiles.split(",")) {
            tiles.add(tile);
        }
        mTileAdapter.setTileSpecs(tiles);
    }

    private void setTileSpecs() {
        List<String> specs = new ArrayList<>();
        for (QSTile tile : mHost.getTiles()) {
            specs.add(tile.getTileSpec());
        }
        mTileAdapter.setTileSpecs(specs);
        mRecyclerView.setAdapter(mTileAdapter);
    }

    private void save() {
        mTileAdapter.saveSpecs(mHost);
    }

    private final Callback mKeyguardCallback = new Callback() {
        @Override
        public void onKeyguardChanged() {
            if (mHost.getKeyguardMonitor().isShowing()) {
                hide(0, 0);
            }
        }
    };

    private final AnimatorListener mExpandAnimationListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            //*/ freeme.zhongkai.zhu. 20170803. fix AOSP's bug: systemui stuck.
            // bug redo step:
            // press EDIT, then press return icon immediately.
            if (!isShown) {
                return;
            }
            //*/
            setCustomizing(true);
            mNotifQsContainer.setCustomizerAnimating(false);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mNotifQsContainer.setCustomizerAnimating(false);
        }
    };

    private final AnimatorListener mCollapseAnimationListener = new AnimatorListenerAdapter() {
        //*/ freeme.zhongkai.zhu. 20170803. fix AOSP's bug: systemui stuck.
        @Override
        public void onAnimationStart(Animator animation) {
            isCollapseAnimating = true;
        }
        //*/

        @Override
        public void onAnimationEnd(Animator animation) {
            //*/ freeme.zhongkai.zhu. 20170803. fix AOSP's bug: systemui stuck.
            isCollapseAnimating = false;
            //*/

            if (!isShown) {
                setVisibility(View.GONE);
            }
            mNotifQsContainer.setCustomizerAnimating(false);
            mRecyclerView.setAdapter(mTileAdapter);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            if (!isShown) {
                setVisibility(View.GONE);
            }
            mNotifQsContainer.setCustomizerAnimating(false);
        }
    };

    //*/ freeme.gouzhouping. 20170830. remove edit animation
    private void animateShow(View v, boolean animate) {
        v.animate().cancel();
        v.setAlpha(0.0f);
        v.setVisibility(View.VISIBLE);
        if (animate) {
            v.animate()
                    .alpha(1.0f)
                    .setDuration(200)
                    .setInterpolator(Interpolators.ALPHA_IN)
                    .setStartDelay(100)
                    .withEndAction(null);
        } else {
            v.setAlpha(1.0f);
        }
    }

    private void animateHide(final View v, boolean animate) {
        v.animate().cancel();
        if (animate) {
            v.animate()
                    .alpha(0.0f)
                    .setDuration(160)
                    .setStartDelay(0)
                    .setInterpolator(Interpolators.ALPHA_OUT)
                    .withEndAction(new Runnable() {
                public void run() {
                    v.setVisibility(View.GONE);
                }
            });
            return;
        }
        v.setAlpha(0.0f);
        v.setVisibility(View.GONE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
    //*/
}
