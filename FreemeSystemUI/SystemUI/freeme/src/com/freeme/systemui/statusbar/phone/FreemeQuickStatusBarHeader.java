package com.freeme.systemui.statusbar.phone;

import android.app.AlarmManager.AlarmClockInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.android.systemui.R;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSPanel.Callback;
import com.android.systemui.statusbar.phone.ActivityStarter;
import com.android.systemui.statusbar.phone.BaseStatusBarHeader;
import com.android.systemui.statusbar.phone.ExpandableIndicator;
import com.android.systemui.statusbar.phone.MultiUserSwitch;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.phone.SettingsButton;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.NextAlarmController.NextAlarmChangeCallback;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener;
import com.android.systemui.tuner.TunerService;

public class FreemeQuickStatusBarHeader extends BaseStatusBarHeader implements NextAlarmChangeCallback,
        OnClickListener, OnUserInfoChangedListener {
    private ActivityStarter mActivityStarter;
    private View mClockView;
    private View mDateView;
    private View mEditButton;
    protected ExpandableIndicator mExpandIndicator;
    private boolean mExpanded;
    private QSTileHost mHost;
    private boolean mListening;
    private ImageView mMultiUserAvatar;
    protected MultiUserSwitch mMultiUserSwitch;
    private NextAlarmController mNextAlarmController;
    private View mQSHeader;
    private QSPanel mQsPanel;
    private SettingsButton mSettingsButton;
    private boolean mShowEmergencyCallsOnly;

    public FreemeQuickStatusBarHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        mQSHeader = findViewById(R.id.quick_qs_header);
        mExpandIndicator = (ExpandableIndicator) findViewById(R.id.expand_indicator);
        mSettingsButton = (SettingsButton) findViewById(R.id.settings_button);
        mSettingsButton.setOnClickListener(this);
        mEditButton = findViewById(android.R.id.edit);
        mEditButton.setOnClickListener(view->
                mHost.startRunnableDismissingKeyguard(()->mQsPanel.showEdit(view)));
        mMultiUserSwitch = (MultiUserSwitch) findViewById(R.id.multi_user_switch);
        mMultiUserAvatar = (ImageView) mMultiUserSwitch.findViewById(R.id.multi_user_avatar);
        mClockView = findViewById(R.id.status_bar_header_clock);
        mDateView = findViewById(R.id.status_bar_header_date);
        mClockView.setOnClickListener(this);
        mDateView.setOnClickListener(this);
        updateResources();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateResources();
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        updateResources();
    }

    private void updateResources() {
        LayoutParams lp = (LayoutParams) mQSHeader.getLayoutParams();
        lp.height = getResources().getDimensionPixelSize(R.dimen.status_bar_header_height);
        mQSHeader.setLayoutParams(lp);
        updateDateTimePosition();
        updateSettingsAnimator();
    }

    protected void updateSettingsAnimator() {
    }

    @Override
    public int getCollapsedHeight() {
        return getHeight();
    }

    @Override
    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
        updateEverything();
    }

    @Override
    public void onNextAlarmChanged(AlarmClockInfo nextAlarm) {
    }

    @Override
    public void setExpansion(float headerExpansionFraction) {
        updateAlarmVisibilities();
        mExpandIndicator.setExpanded(headerExpansionFraction > 0.93f);
    }

    @Override
    protected void onDetachedFromWindow() {
        setListening(false);
        mHost.getUserInfoController().remListener(this);
        mHost.getNetworkController().removeEmergencyListener(this);
        super.onDetachedFromWindow();
    }

    private void updateAlarmVisibilities() {
    }

    private void updateDateTimePosition() {
    }

    @Override
    public void setListening(boolean listening) {
        if (listening != mListening) {
            mListening = listening;
            updateListeners();
        }
    }

    @Override
    public void updateEverything() {
        updateDateTimePosition();
        updateVisibilities();
    }

    protected void updateVisibilities() {
        updateAlarmVisibilities();
        refreshUserView();
    }

    private void updateListeners() {
        if (mListening) {
            mNextAlarmController.addStateChangedCallback(this);
        } else {
            mNextAlarmController.removeStateChangedCallback(this);
        }
    }

    @Override
    public void setActivityStarter(ActivityStarter activityStarter) {
        mActivityStarter = activityStarter;
    }

    @Override
    public void setQSPanel(QSPanel qsPanel) {
        if (qsPanel != null) {
            mQsPanel = qsPanel;
            setupHost(mQsPanel.getHost());
            mMultiUserSwitch.setQsPanel(mQsPanel);
        }
    }

    public void setupHost(QSTileHost host) {
        mHost = host;
        host.setHeaderView(mExpandIndicator);
        setUserInfoController(host.getUserInfoController());
        setBatteryController(host.getBatteryController());
        setNextAlarmController(host.getNextAlarmController());
        if (mHost.getNetworkController().hasVoiceCallingFeature()) {
            mHost.getNetworkController().addEmergencyListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mSettingsButton) {
            if (mSettingsButton.isTunerClick()) {
                if (TunerService.isTunerEnabled(getContext())) {
                    TunerService.setTunerEnabled(getContext(), false);
                }
                Log.w("QuickStatusBarHeader", "SettingsButton::onClick: always cancel the tuner function!");
                return;
            }
            startSettingsActivity();
        } else if (v == mClockView) {
            startDeskClockActivity();
        } else if (v == mDateView) {
            startCalendarActivity();
        }
    }

    private void startDeskClockActivity() {
        if (!SystemProperties.getBoolean("sys.super_power_save", false)) {
            Intent intent = new Intent();
            intent.setClassName("com.android.deskclock", "com.android.deskclock.DeskClock");
            try {
                mActivityStarter.startActivity(intent, true);
            } catch (ActivityNotFoundException e) {
                Log.e("QuickStatusBarHeader", "startDeskClockActivity:: activity not found, " + e);
            }
        }
    }

    private void startCalendarActivity() {
        if (!SystemProperties.getBoolean("sys.super_power_save", false)) {
            Intent intent = new Intent();
            intent.setClassName("com.android.calendar", "com.android.calendar.AllInOneActivity");
            try {
                mActivityStarter.startActivity(intent, true);
            } catch (ActivityNotFoundException e) {
                Log.e("QuickStatusBarHeader", "startCalendarActivity:: activity not found, " + e);
            }
        }
    }

    private void startSettingsActivity() {
        mActivityStarter.startActivity(new Intent("android.settings.SETTINGS"), true);
    }

    @Override
    public void setNextAlarmController(NextAlarmController nextAlarmController) {
        mNextAlarmController = nextAlarmController;
    }

    @Override
    public void setBatteryController(BatteryController batteryController) {
    }

    @Override
    public void setUserInfoController(UserInfoController userInfoController) {
        userInfoController.addListener(this);
    }

    @Override
    public void setEmergencyCallsOnly(boolean show) {
        if (show != mShowEmergencyCallsOnly) {
            mShowEmergencyCallsOnly = show;
            if (mExpanded) {
                updateEverything();
            }
        }
    }

    @Override
    public void onUserInfoChanged(String name, Drawable picture) {
        mMultiUserAvatar.setImageDrawable(picture);
    }

    @Override
    public void setCallback(Callback qsPanelCallback) {
    }

    public void refreshUserView() {
        MultiUserSwitch multiUserSwitch = mMultiUserSwitch;
        int i = (!mMultiUserSwitch.hasMultipleUsers()) ? View.GONE : View.VISIBLE;
        multiUserSwitch.setVisibility(i);
    }

    public void onDetailsAnimateStarted() {
        setVisibility(View.INVISIBLE);
    }

    @Override
    public int getExpandedHeight() {
        return getHeight();
    }
}
