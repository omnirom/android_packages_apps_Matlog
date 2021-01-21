package com.pluscubed.logcat.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.pluscubed.logcat.data.ColorScheme;
import com.pluscubed.logcat.util.StringUtil;
import com.pluscubed.logcat.util.UtilLogger;

import org.omnirom.logcat.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PreferenceHelper {

    private static final String WIDGET_EXISTS_PREFIX = "widget_";
    private static float textSize = -1;
    private static Character defaultLogLevel = null;
    private static Boolean showTimestampAndPid = null;
    private static ColorScheme colorScheme = null;
    private static int displayLimit = -1;
    private static UtilLogger log = new UtilLogger(PreferenceHelper.class);
    public static final String DELIMITER = ",";

    public static void clearCache() {
        defaultLogLevel = null;
        textSize = -1;
        showTimestampAndPid = null;
        colorScheme = null;
        displayLimit = -1;
    }

    /**
     * Record that we managed to get root in JellyBean.
     *
     * @param context
     * @return
     */
    public static void setJellybeanRootRan(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPrefs.edit();
        editor.putBoolean(context.getString(R.string.pref_ran_jellybean_su_update), true);
        editor.commit();
    }

    /**
     * Return true if we have root in jelly bean.
     *
     * @param context
     * @return
     */
    public static boolean getJellybeanRootRan(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(context.getString(R.string.pref_ran_jellybean_su_update), false);
    }

    public static boolean getWidgetExistsPreference(Context context, int appWidgetId) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String widgetExists = WIDGET_EXISTS_PREFIX.concat(Integer.toString(appWidgetId));

        return sharedPrefs.getBoolean(widgetExists, false);
    }

    public static void setWidgetExistsPreference(Context context, int[] appWidgetIds) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Editor editor = sharedPrefs.edit();

        for (int appWidgetId : appWidgetIds) {
            String widgetExists = WIDGET_EXISTS_PREFIX.concat(Integer.toString(appWidgetId));
            editor.putBoolean(widgetExists, true);
        }

        editor.commit();
    }

    public static void clearWidgetExistsPreference(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(WIDGET_EXISTS_PREFIX.concat(Integer.toString(appWidgetId))).commit();
    }

    public static void remapWidgetExistsPreference(Context context, int oldId, int newId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(WIDGET_EXISTS_PREFIX.concat(Integer.toString(newId)), true);
        prefs.edit().remove(WIDGET_EXISTS_PREFIX.concat(Integer.toString(oldId))).commit();
    }

    public static int getDisplayLimitPreference(Context context) {

        if (displayLimit == -1) {

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            String defaultValue = context.getText(R.string.pref_display_limit_default).toString();

            String intAsString = sharedPrefs.getString(context.getText(R.string.pref_display_limit).toString(), defaultValue);

            try {
                displayLimit = Integer.parseInt(intAsString);
            } catch (NumberFormatException e) {
                displayLimit = Integer.parseInt(defaultValue);
            }
        }

        return displayLimit;
    }

    public static int getLogLinePeriodPreference(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        String defaultValue = context.getText(R.string.pref_log_line_period_default).toString();

        String intAsString = sharedPrefs.getString(context.getText(R.string.pref_log_line_period).toString(), defaultValue);

        try {
            return Integer.parseInt(intAsString);
        } catch (NumberFormatException e) {
            return Integer.parseInt(defaultValue);
        }
    }

    public static void setDisplayLimitPreference(Context context, int value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPrefs.edit();

        editor.putString(context.getText(R.string.pref_display_limit).toString(), Integer.toString(value));

        editor.commit();
    }

    public static void setLogLinePeriodPreference(Context context, int value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPrefs.edit();

        editor.putString(context.getText(R.string.pref_log_line_period).toString(), Integer.toString(value));

        editor.commit();
    }

    public static char getDefaultLogLevelPreference(Context context) {

        if (defaultLogLevel == null) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            String logLevelPref = sharedPrefs.getString(
                    context.getText(R.string.pref_default_log_level).toString(),
                    context.getText(R.string.log_level_value_verbose).toString());

            defaultLogLevel = logLevelPref.charAt(0);
        }

        return defaultLogLevel;


    }

    public static float getTextSizePreference(Context context) {

        if (textSize == -1) {

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            String textSizePref = sharedPrefs.getString(
                    context.getText(R.string.pref_text_size).toString(),
                    context.getText(R.string.text_size_medium_value).toString());

            if (textSizePref.equals(context.getText(R.string.text_size_xsmall_value))) {
                cacheTextsize(context, R.dimen.text_size_xsmall);
            } else if (textSizePref.equals(context.getText(R.string.text_size_small_value))) {
                cacheTextsize(context, R.dimen.text_size_small);
            } else if (textSizePref.equals(context.getText(R.string.text_size_medium_value))) {
                cacheTextsize(context, R.dimen.text_size_medium);
            } else if (textSizePref.equals(context.getText(R.string.text_size_large_value))) {
                cacheTextsize(context, R.dimen.text_size_large);
            } else { // xlarge
                cacheTextsize(context, R.dimen.text_size_xlarge);
            }
        }

        return textSize;

    }

    private static void cacheTextsize(Context context, int dimenId) {

        float unscaledSize = context.getResources().getDimension(dimenId);

        log.d("unscaledSize is %g", unscaledSize);

        textSize = unscaledSize;
    }

    public static boolean getShowTimestampAndPidPreference(Context context) {

        if (showTimestampAndPid == null) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            showTimestampAndPid = sharedPrefs.getBoolean(context.getText(R.string.pref_show_timestamp).toString(), true);
        }

        return showTimestampAndPid;

    }

    public static boolean getHidePartialSelectHelpPreference(Context context) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPrefs.getBoolean(
                context.getText(R.string.pref_hide_partial_select_help).toString(), false);
    }

    public static void setHidePartialSelectHelpPreference(Context context, boolean bool) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPrefs.edit();

        editor.putBoolean(context.getString(R.string.pref_hide_partial_select_help), bool);

        editor.apply();

    }

    public static boolean getExpandedByDefaultPreference(Context context) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPrefs.getBoolean(
                context.getText(R.string.pref_expanded_by_default).toString(), false);
    }

    public static ColorScheme getColorScheme(Context context) {
        return ColorScheme.Default;

    }

    public static Set<String> getBuffers(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Set<String> defaultValue = new HashSet<String>();
        defaultValue.add(context.getString(R.string.pref_buffer_choice_main_value));
        String key = context.getString(R.string.pref_buffer);

        return sharedPrefs.getStringSet(key, defaultValue);
    }

    public static boolean getIncludeDeviceInfoPreference(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPrefs.getBoolean(context.getString(R.string.pref_include_device_info), true);
    }

    public static void setIncludeDeviceInfoPreference(Context context, boolean value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Editor editor = sharedPrefs.edit();
        editor.putBoolean(context.getString(R.string.pref_include_device_info), value);
        editor.apply();
    }

    public static boolean isScrubberEnabled(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean("scrubber", false);
    }

    public static boolean getIncludeDmesgPreference(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPrefs.getBoolean(context.getString(R.string.pref_include_dmesg), true);
    }

    public static void setIncludeDmesgPreference(Context context, boolean value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        Editor editor = sharedPrefs.edit();
        editor.putBoolean(context.getString(R.string.pref_include_dmesg), value);
        editor.apply();
    }
}
