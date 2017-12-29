package com.android.systemui.recents.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceHelper {
    public static SharedPreferences getSharedPreferences(Context context, String name, int mode) {
        return context.getSharedPreferences(name, mode);
    }

    public static void savePreference(Context context, String prefName, String key, String value) {
        Editor editor = getSharedPreferences(context, prefName, 0).edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String loadPreferenceAsString(Context context, String prefName, String key) {
        return getSharedPreferences(context, prefName, 0).getString(key, null);
    }

    public static void removePreference(Context context, String prefName, String key) {
        Editor editor = getSharedPreferences(context, prefName, 0).edit();
        editor.remove(key);
        editor.commit();
    }
}
