package com.android.systemui.recents.model;

import android.content.Context;

public class LockTaskPreference {
    private static LockTaskPreference mPreference;
    private static final String RECENTS_LOCK = "RecentLock";

    public static LockTaskPreference getInstance(Context context) {
        if (mPreference == null) {
            mPreference = new LockTaskPreference(context);
        }
        return mPreference;
    }

    public void savePreference(Context context, String key) {
        PreferenceHelper.savePreference(context, RECENTS_LOCK, key, key);
    }

    public void removePreference(Context context, String key) {
        PreferenceHelper.removePreference(context, RECENTS_LOCK, key);
    }

    public boolean isLocked(Context context, String key) {
        return loadPreference(context, key) != null;
    }

    private String loadPreference(Context context, String key) {
        return PreferenceHelper.loadPreferenceAsString(context, RECENTS_LOCK, key);
    }

    private LockTaskPreference(Context context) {
        PreferenceHelper.getSharedPreferences(context, RECENTS_LOCK, 0);
    }
}
