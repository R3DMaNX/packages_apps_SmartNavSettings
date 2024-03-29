/*
 * Copyright (C) 2015 The Dirty Unicorns Project
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

package com.android.settings.smartnav;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceCategory;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import java.util.ArrayList;
import java.util.List;

import com.havoc.settings.preferences.SecureSettingSeekBarPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class PulseSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = PulseSettings.class.getSimpleName();
    private static final String CUSTOM_DIMEN = "pulse_custom_dimen";
    private static final String CUSTOM_DIV = "pulse_custom_div";
    private static final String PULSE_BLOCK = "pulse_filled_block_size";
    private static final String EMPTY_BLOCK = "pulse_empty_block_size";
    private static final String FUDGE_FACOR = "pulse_custom_fudge_factor";
    private static final int RENDER_STYLE_FADING_BARS = 0;
    private static final int RENDER_STYLE_SOLID_LINES = 1;
    private static final String SOLID_FUDGE = "pulse_solid_fudge_factor";
    private static final String SOLID_LAVAMP_SPEED = "lavamp_solid_speed";
    private static final String FADING_LAVAMP_SPEED = "fling_pulse_lavalamp_speed";
    private static final String PULSE_SOLID_UNITS_COUNT = "pulse_solid_units_count";
    private static final String PULSE_SOLID_UNITS_OPACITY = "pulse_solid_units_opacity";
    private static final String PULSE_CUSTOM_BUTTONS_OPACITY = "pulse_custom_buttons_opacity";
    private static final String PULSE_FADING_BLOCKS_OPACITY = "pulse_fading_blocks_opacity";

    static final int DEFAULT = 0xffffffff;
    static final int DEFAULT_TO = 0xff8080ff;
    static final int DEFAULT_FROM = 0xffff8080;
    private static final int MENU_RESET = Menu.FIRST;
    SwitchPreference mShowPulse;
    ListPreference mRenderMode;
    SwitchPreference mAutoColor;
    ColorPickerPreference mPulseColor;
    SwitchPreference mPulseAccentColorEnabled;
    ColorPickerPreference mLavaLampColorFrom;
    ColorPickerPreference mLavaLampColorTo;
    SwitchPreference mLavaLampEnabled;
    SwitchPreference mSmoothingEnabled;
    SecureSettingSeekBarPreference mCustomDimen;
    SecureSettingSeekBarPreference mCustomDiv;
    SecureSettingSeekBarPreference mFilled;
    SecureSettingSeekBarPreference mEmpty;
    SecureSettingSeekBarPreference mFudge;
    SecureSettingSeekBarPreference mSolidFudge;
    SecureSettingSeekBarPreference mSolidSpeed;
    SecureSettingSeekBarPreference mFadingSpeed;
    SecureSettingSeekBarPreference mSolidCount;
    SecureSettingSeekBarPreference mSolidOpacity;
    SecureSettingSeekBarPreference mNavButtonsOpacity;
    SecureSettingSeekBarPreference mFadingOpacity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pulse_settings);

        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.pulse_help_policy_notice_summary);

        mShowPulse = (SwitchPreference) findPreference("eos_fling_show_pulse");
        mShowPulse.setChecked(Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_PULSE_ENABLED, 0, UserHandle.USER_CURRENT) == 1);
        mShowPulse.setOnPreferenceChangeListener(this);

        int renderMode = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_RENDER_STYLE_URI, RENDER_STYLE_SOLID_LINES, UserHandle.USER_CURRENT);
        mRenderMode = (ListPreference) findPreference("pulse_render_mode");
        mRenderMode.setValue(String.valueOf(renderMode));
        mRenderMode.setSummary(mRenderMode.getEntry());
        mRenderMode.setOnPreferenceChangeListener(this);

        mAutoColor = (SwitchPreference) findPreference("pulse_auto_color");
        mAutoColor.setChecked(Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_AUTO_COLOR, 0, UserHandle.USER_CURRENT) == 1);
        mAutoColor.setOnPreferenceChangeListener(this);

        PreferenceCategory fadingBarsCat = (PreferenceCategory)findPreference("pulse_fading_bars_category");
        fadingBarsCat.setEnabled(renderMode == RENDER_STYLE_FADING_BARS);

        PreferenceCategory solidBarsCat = (PreferenceCategory) findPreference("pulse_2");
        solidBarsCat.setEnabled(renderMode == RENDER_STYLE_SOLID_LINES);

        mPulseAccentColorEnabled = (SwitchPreference) findPreference("pulse_accent_color_enabled");
        mPulseAccentColorEnabled.setChecked(Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_ACCENT_COLOR_ENABLED, 0, UserHandle.USER_CURRENT) == 1);
        mPulseAccentColorEnabled.setOnPreferenceChangeListener(this);

        int pulseColor = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_PULSE_COLOR, Color.WHITE, UserHandle.USER_CURRENT);
        mPulseColor = (ColorPickerPreference) findPreference("eos_fling_pulse_color");
        mPulseColor.setEnabled(Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_ACCENT_COLOR_ENABLED, 0, UserHandle.USER_CURRENT) == 0);
        mPulseColor.setNewPreviewColor(pulseColor);
        mPulseColor.setOnPreferenceChangeListener(this);

        int lavaLampColorFrom = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_PULSE_LAVALAMP_COLOR_FROM, 0xffff8080, UserHandle.USER_CURRENT);
        mLavaLampColorFrom = (ColorPickerPreference) findPreference("fling_lavalamp_color_from");
        mLavaLampColorFrom.setNewPreviewColor(lavaLampColorFrom);
        mLavaLampColorFrom.setOnPreferenceChangeListener(this);

         int lavaLampColorTo = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_PULSE_LAVALAMP_COLOR_TO, 0xff8080ff, UserHandle.USER_CURRENT);
        mLavaLampColorTo = (ColorPickerPreference) findPreference("fling_lavalamp_color_to");
        mLavaLampColorTo.setNewPreviewColor(lavaLampColorTo);
        mLavaLampColorTo.setOnPreferenceChangeListener(this);

        mLavaLampEnabled = (SwitchPreference) findPreference("eos_fling_lavalamp");
        mLavaLampEnabled.setEnabled(Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_ACCENT_COLOR_ENABLED, 0, UserHandle.USER_CURRENT) == 0);
        mLavaLampEnabled.setChecked(Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_PULSE_LAVALAMP_ENABLED, 1, UserHandle.USER_CURRENT) == 1);
        mLavaLampEnabled.setOnPreferenceChangeListener(this);

        mSmoothingEnabled = (SwitchPreference) findPreference("fling_smoothing_enabled");
        mSmoothingEnabled.setChecked(Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_PULSE_SMOOTHING_ENABLED, 0, UserHandle.USER_CURRENT) == 1);
        mSmoothingEnabled.setOnPreferenceChangeListener(this);

        int customdimen = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_CUSTOM_DIMEN, 14, UserHandle.USER_CURRENT);
        mCustomDimen = (SecureSettingSeekBarPreference) findPreference(CUSTOM_DIMEN);
        mCustomDimen.setValue(customdimen);
        mCustomDimen.setOnPreferenceChangeListener(this);

        int customdiv = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_CUSTOM_DIV, 16, UserHandle.USER_CURRENT);
        mCustomDiv = (SecureSettingSeekBarPreference) findPreference(CUSTOM_DIV);
        mCustomDiv.setValue(customdiv);
        mCustomDiv.setOnPreferenceChangeListener(this);

        int filled = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_FILLED_BLOCK_SIZE, 4, UserHandle.USER_CURRENT);
        mFilled = (SecureSettingSeekBarPreference) findPreference(PULSE_BLOCK);
        mFilled.setValue(filled);
        mFilled.setOnPreferenceChangeListener(this);

        int empty = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_EMPTY_BLOCK_SIZE, 1, UserHandle.USER_CURRENT);
        mEmpty = (SecureSettingSeekBarPreference) findPreference(EMPTY_BLOCK);
        mEmpty.setValue(empty);
        mEmpty.setOnPreferenceChangeListener(this);

        int fudge = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_CUSTOM_FUDGE_FACTOR, 4, UserHandle.USER_CURRENT);
        mFudge = (SecureSettingSeekBarPreference) findPreference(FUDGE_FACOR);
        mFudge.setValue(fudge);
        mFudge.setOnPreferenceChangeListener(this);

        int solidfudge = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_SOLID_FUDGE_FACTOR, 5,
                UserHandle.USER_CURRENT);
        mSolidFudge = (SecureSettingSeekBarPreference) findPreference(SOLID_FUDGE);
        mSolidFudge.setValue(solidfudge);
        mSolidFudge.setOnPreferenceChangeListener(this);

        int speed = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_LAVALAMP_SOLID_SPEED, 10000, UserHandle.USER_CURRENT);
        mSolidSpeed =
                (SecureSettingSeekBarPreference) findPreference(SOLID_LAVAMP_SPEED);
        mSolidSpeed.setValue(speed);
        mSolidSpeed.setOnPreferenceChangeListener(this);

        int fspeed = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_PULSE_LAVALAMP_SPEED, 10000, UserHandle.USER_CURRENT);
        mFadingSpeed =
                (SecureSettingSeekBarPreference) findPreference(FADING_LAVAMP_SPEED);
        mFadingSpeed.setValue(fspeed);
        mFadingSpeed.setOnPreferenceChangeListener(this);

        int count = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_SOLID_UNITS_COUNT, 64, UserHandle.USER_CURRENT);
        mSolidCount =
                (SecureSettingSeekBarPreference) findPreference(PULSE_SOLID_UNITS_COUNT);
        mSolidCount.setValue(count);
        mSolidCount.setOnPreferenceChangeListener(this);

        int opacitysolid = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_SOLID_UNITS_OPACITY, 200, UserHandle.USER_CURRENT);
        mSolidOpacity =
                (SecureSettingSeekBarPreference) findPreference(PULSE_SOLID_UNITS_OPACITY);
        mSolidOpacity.setValue(opacitysolid);
        mSolidOpacity.setOnPreferenceChangeListener(this);

        int buttonsOpacity = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_CUSTOM_BUTTONS_OPACITY, 200, UserHandle.USER_CURRENT);
        mNavButtonsOpacity =
                (SecureSettingSeekBarPreference) findPreference(PULSE_CUSTOM_BUTTONS_OPACITY);
        mNavButtonsOpacity.setValue(buttonsOpacity);
        mNavButtonsOpacity.setOnPreferenceChangeListener(this);

        int opacityblock = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_FADING_BLOCKS_OPACITY, 200, UserHandle.USER_CURRENT);
        mFadingOpacity =
                (SecureSettingSeekBarPreference) findPreference(PULSE_FADING_BLOCKS_OPACITY);
        mFadingOpacity.setValue(opacityblock);
        mFadingOpacity.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mRenderMode)) {
            int mode = Integer.valueOf((String) newValue);
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_RENDER_STYLE_URI, mode, UserHandle.USER_CURRENT);
            PreferenceCategory fadingBarsCat = (PreferenceCategory)findPreference("pulse_fading_bars_category");
            fadingBarsCat.setEnabled(mode == RENDER_STYLE_FADING_BARS);
            PreferenceCategory solidBarsCat = (PreferenceCategory)findPreference("pulse_2");
            solidBarsCat.setEnabled(mode == RENDER_STYLE_SOLID_LINES);
            mRenderMode.setSummary(mRenderMode.getEntry());
            return true;
        } else if (preference.equals(mShowPulse)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.FLING_PULSE_ENABLED, enabled ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mAutoColor)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_AUTO_COLOR, enabled ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mPulseAccentColorEnabled)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_ACCENT_COLOR_ENABLED, enabled ? 1 : 0,
                    UserHandle.USER_CURRENT);
            mPulseColor.setEnabled(!enabled);
            mLavaLampEnabled.setEnabled(!enabled);
            return true;
        } else if (preference.equals(mPulseColor)) {
            int color = ((Integer) newValue).intValue();
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.FLING_PULSE_COLOR, color, UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mLavaLampColorFrom)) {
            int color = ((Integer) newValue).intValue();
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.FLING_PULSE_LAVALAMP_COLOR_FROM, color, UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mLavaLampColorTo)) {
            int color = ((Integer) newValue).intValue();
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.FLING_PULSE_LAVALAMP_COLOR_TO, color, UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mLavaLampEnabled)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.FLING_PULSE_LAVALAMP_ENABLED, enabled ? 1 : 0,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mSmoothingEnabled)) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.FLING_PULSE_SMOOTHING_ENABLED, enabled ? 1 : 0,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mCustomDimen) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_CUSTOM_DIMEN, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mCustomDiv) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_CUSTOM_DIV, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mFilled) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_FILLED_BLOCK_SIZE, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mEmpty) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_EMPTY_BLOCK_SIZE, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mFudge) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_CUSTOM_FUDGE_FACTOR, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mSolidFudge) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_SOLID_FUDGE_FACTOR, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mSolidSpeed) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_LAVALAMP_SOLID_SPEED, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mFadingSpeed) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.FLING_PULSE_LAVALAMP_SPEED, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mSolidCount) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_SOLID_UNITS_COUNT, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mSolidOpacity) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_SOLID_UNITS_OPACITY, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mNavButtonsOpacity) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_CUSTOM_BUTTONS_OPACITY, val, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mFadingOpacity) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_FADING_BLOCKS_OPACITY, val, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_action_reset)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }
     @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

     private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.pulse_colors_reset_title);
        alertDialog.setMessage(R.string.pulse_colors_reset_message);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

     private void resetValues() {
	ContentResolver resolver = getActivity().getContentResolver();
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_PULSE_LAVALAMP_COLOR_TO, DEFAULT_TO);
        mLavaLampColorTo.setNewPreviewColor(DEFAULT_TO);
        mLavaLampColorTo.setSummary(R.string.default_string);
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_PULSE_LAVALAMP_COLOR_FROM, DEFAULT_FROM);
        mLavaLampColorFrom.setNewPreviewColor(DEFAULT_FROM);
        mLavaLampColorFrom.setSummary(R.string.default_string);
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_PULSE_COLOR, DEFAULT);
        mPulseColor.setNewPreviewColor(DEFAULT);
        mPulseColor.setSummary(R.string.default_string);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.HAVOC_SETTINGS;
    }
}
