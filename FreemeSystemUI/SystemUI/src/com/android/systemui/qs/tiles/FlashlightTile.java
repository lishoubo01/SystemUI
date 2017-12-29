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
 * limitations under the License
 */

package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.Switch;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.freeme.systemui.qs.FreemeFloatToolkit;

/** Quick settings tile: Control flashlight **/
public class FlashlightTile extends QSTile<QSTile.BooleanState> implements
        //*/ freeme. gouzhouping, 20170309. show flashlight mark words when low battery.
        BatteryController.BatteryStateChangeCallback,
        //*/
        FlashlightController.FlashlightListener {

    private final AnimationIcon mEnable
            = new AnimationIcon(R.drawable.ic_signal_flashlight_enable_animation,
            R.drawable.ic_signal_flashlight_disable);
    private final AnimationIcon mDisable
            = new AnimationIcon(R.drawable.ic_signal_flashlight_disable_animation,
            R.drawable.ic_signal_flashlight_enable);
    private final FlashlightController mFlashlightController;

    //*/ freeme. gouzhouping, 20170309. show flashlight mark words when low battery.
    private int mBatteryLevel;
    private int mLowBattery;
    private final BatteryController mBatteryController;
    //*/

    public FlashlightTile(Host host) {
        super(host);
        mFlashlightController = host.getFlashlightController();
        mFlashlightController.addListener(this);

        //*/ freeme. gouzhouping, 20170309. show flashlight mark words when low battery.
        mLowBattery = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_lowBatteryWarningLevel);
        mBatteryController = host.getBatteryController();
        //*/

    }

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        mFlashlightController.removeListener(this);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void setListening(boolean listening) {
        //*/ freeme. gouzhouping, 20170309. show flashlight mark words when low battery.
        if (listening) {
            mBatteryController.addStateChangedCallback(this);
        } else {
            mBatteryController.removeStateChangedCallback(this);
        }
        //*/
    }

    @Override
    protected void handleUserSwitch(int newUserId) {
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
    }

    @Override
    public boolean isAvailable() {
        return mFlashlightController.hasFlashlight();
    }

    @Override
    protected void handleClick() {
        if (ActivityManager.isUserAMonkey()) {
            return;
        }
        MetricsLogger.action(mContext, getMetricsCategory(), !mState.value);
        /*/ freeme. gouzhouping, 20170309. show flashlight mark words when low battery.
        boolean newState = !mState.value;
        refreshState(newState);
        mFlashlightController.setFlashlight(newState);
        /*/
        if (mBatteryLevel <= mLowBattery) {
            showLowBatteryDialog();
        } else {
            boolean newState = !mState.value;
            refreshState(newState);
            mFlashlightController.setFlashlight(newState);
        }
        //*/
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_flashlight_label);
    }

    @Override
    protected void handleLongClick() {
        handleClick();
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.label = mHost.getContext().getString(R.string.quick_settings_flashlight_label);
        if (!mFlashlightController.isAvailable()) {
            Drawable icon = mHost.getContext().getDrawable(R.drawable.ic_signal_flashlight_disable)
                    .mutate();
            final int disabledColor = mHost.getContext().getColor(R.color.qs_tile_tint_unavailable);
            icon.setTint(disabledColor);
            /*/ freeme.gouzhouping, 20170908. replace the resources.
            state.icon = new DrawableIcon(icon);
            state.label = new SpannableStringBuilder().append(state.label,
                    new ForegroundColorSpan(disabledColor),
                    SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);
            //*/
            state.icon = ResourceIcon.get(R.drawable.freeme_ic_qs_flashlight_off);
            //*/
            state.contentDescription = mContext.getString(
                    R.string.accessibility_quick_settings_flashlight_unavailable);
            return;
        }
        if (arg instanceof Boolean) {
            boolean value = (Boolean) arg;
            if (value == state.value) {
                return;
            }
            state.value = value;
        } else {
            state.value = mFlashlightController.isEnabled();
        }
        /*/ freeme.gouzhouping, 20170908. replace the resources.
        final AnimationIcon icon = state.value ? mEnable : mDisable;
        state.icon = icon;
        //*/
        state.icon = state.value ? ResourceIcon.get(R.drawable.freeme_ic_qs_flashlight_on)
                : ResourceIcon.get(R.drawable.freeme_ic_qs_flashlight_off);
        //*/
        state.contentDescription = mContext.getString(R.string.quick_settings_flashlight_label);
        state.minimalAccessibilityClassName = state.expandedAccessibilityClassName
                = Switch.class.getName();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_FLASHLIGHT;
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_off);
        }
    }

    @Override
    public void onFlashlightChanged(boolean enabled) {
        refreshState(enabled);
    }

    @Override
    public void onFlashlightError() {
        refreshState(false);
    }

    @Override
    public void onFlashlightAvailabilityChanged(boolean available) {
        refreshState();
    }

    //*/ freeme. gouzhouping, 20170309. show flashlight mark words when low battery.
    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        mBatteryLevel = level;
        if (mBatteryLevel <= mLowBattery) {
            refreshState(false);
            mFlashlightController.setFlashlight(false);
        }
    }

    @Override
    public void onPowerSaveChanged(boolean isPowerSave) {
        // do nothing
    }

    private void showLowBatteryDialog() {
        FreemeFloatToolkit freemeFloatToolkit = new FreemeFloatToolkit();
        freemeFloatToolkit.create(mContext, R.layout.freeme_low_battery_dialog);
        freemeFloatToolkit.show();
    }
    //*/

}
