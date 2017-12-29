package com.freeme.systemui.statusbar;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.systemui.R;
import com.freeme.provider.FreemeSettings;

public class FreemeStatusBarSettingsActivity extends PreferenceActivity implements
        Preference.OnPreferenceClickListener {

    // noti area key
    private static final String NOTI_CATEGORY = "notification_category";
    private static final String NOTI_MANAGER  = "notifications_custom";
    private static final String TILE_SORT     = "tile_sort";

    // status area key
    private static final String STATUS_CATEGORY   = "status_category";
    private static final String SHOW_NETWORKSPEED = "show_networkspeed";
    private static final String SHOW_BATTERY      = "show_battery";
    private static final String SHOW_NOTI_ICON    = "show_noti_icon";
    private static final String FORBID_SLIDE_STATUS_KEYGUARD = "forb_slide_statusbar_keyguard";

    private Preference mNotiManager;
    private Preference mTileSort;
    private PreferenceCategory mNotiCategory;

    private SwitchPreference mShowNetworkSpeed;
    private SwitchPreference mShowBatteryLevel;
    private SwitchPreference mShowNotiIcon;
    private SwitchPreference mForbiStatusSlide;
    private PreferenceCategory mStatusCategory;

    //other
    private final Intent kIntent = new Intent().setClassName(
            "com.android.settings","com.android.settings.Settings$NotificationAppListActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.freeme_statusbar_settings);
        initResources();
    }

    private void initResources() {
        // noti
        mNotiCategory = (PreferenceCategory) findPreference(NOTI_CATEGORY);
        mNotiManager  = (Preference) findPreference(NOTI_MANAGER);
        mTileSort     = (Preference) findPreference(TILE_SORT);
        mNotiManager.setOnPreferenceClickListener(this);
        mTileSort.setOnPreferenceClickListener(this);
        mNotiCategory.removePreference(mTileSort);


        // status
        mStatusCategory   = (PreferenceCategory) findPreference(STATUS_CATEGORY);
        mShowNetworkSpeed = (SwitchPreference) findPreference(SHOW_NETWORKSPEED);
        mShowBatteryLevel = (SwitchPreference) findPreference(SHOW_BATTERY);
        mShowNotiIcon     = (SwitchPreference) findPreference(SHOW_NOTI_ICON);
        mForbiStatusSlide = (SwitchPreference) findPreference(FORBID_SLIDE_STATUS_KEYGUARD);
        mShowBatteryLevel.setOnPreferenceClickListener(this);
        mShowNetworkSpeed.setOnPreferenceClickListener(this);
        mShowNotiIcon.setOnPreferenceClickListener(this);
        mForbiStatusSlide.setOnPreferenceClickListener(this);

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference == mNotiManager) {
            kIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityAsUser(kIntent, new UserHandle(UserHandle.USER_CURRENT));
            return true;
        }
        if(preference == mTileSort) {
            // edit surface
            return true;
        }

        if(preference == mShowNetworkSpeed) {
            Settings.System.putInt(getContentResolver(),
                    com.freeme.systemui.statusbar.FreemeStatusbarStateToolKit.SHOW_NETWORK_SPEED_SWITCH,
                    mShowNetworkSpeed.isChecked() ? 1 : 0);
            return true;
        }

        if(preference == mShowNotiIcon) {
            Settings.System.putInt(getContentResolver(),
                    FreemeSettings.System.FREEME_SHOW_NOTI_ICON,
                    mShowNotiIcon.isChecked() ? 1 : 0);
            return true;
        }

        if(preference == mForbiStatusSlide) {
            Settings.System.putInt(getContentResolver(),
                    com.freeme.systemui.statusbar.FreemeStatusbarStateToolKit.FORB_SLIDE_KEYGUARD_SWITCH,
                    mForbiStatusSlide.isChecked() ? 1 : 0);
            return true;
        }

        if(preference == mShowBatteryLevel) {
            Settings.System.putInt(getContentResolver(),
                    com.freeme.systemui.statusbar.FreemeStatusbarStateToolKit.SHOW_BATTERY_LEVEL_SWITCH,
                    mShowBatteryLevel.isChecked() ? 1 : 0);
            return true;
        }

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mShowNetworkSpeed.setChecked(Settings.System.getInt(getContentResolver(),
                com.freeme.systemui.statusbar.FreemeStatusbarStateToolKit.SHOW_NETWORK_SPEED_SWITCH, 0) != 0);

        mShowBatteryLevel.setChecked(Settings.System.getInt(getContentResolver(),
                com.freeme.systemui.statusbar.FreemeStatusbarStateToolKit.SHOW_BATTERY_LEVEL_SWITCH, 0) != 0);

        mShowNotiIcon.setChecked(Settings.System.getInt(getContentResolver(),
                FreemeSettings.System.FREEME_SHOW_NOTI_ICON, 0) != 0);

        mForbiStatusSlide.setChecked(Settings.System.getInt(getContentResolver(),
                com.freeme.systemui.statusbar.FreemeStatusbarStateToolKit.FORB_SLIDE_KEYGUARD_SWITCH, 0) != 0);

    }

    @Override
    public void onPause() {
        super.onPause();
        // do nothing
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // do nothing
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // do nothing
    }
}
