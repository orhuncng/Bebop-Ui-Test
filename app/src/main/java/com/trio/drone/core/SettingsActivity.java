package com.trio.drone.core;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.trio.drone.R;
import com.trio.drone.data.LowPassData;
import com.trio.drone.data.SensorSource;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref);

            setListener(findPreference(getString(R.string.pref_key_camera_stab_mode)));
            setListener(findPreference(getString(R.string.pref_key_home_type)));
            setListener(findPreference(getString(R.string.pref_key_smoothing_drone_sensors)));
            setListener(findPreference(getString(R.string.pref_key_smoothing_watch_sensors)));
            setListener(findPreference(getString(R.string.pref_key_smoothing_phone_sensors)));
            setListener(findPreference(getString(R.string.pref_key_max_altitude)));
            setListener(findPreference(getString(R.string.pref_key_max_vert_speed)));
            setListener(findPreference(getString(R.string.pref_key_max_pitch_roll)));
            setListener(findPreference(getString(R.string.pref_key_max_rot_speed)));
            setListener(findPreference(getString(R.string.pref_key_ui_roll_limit)));
            setListener(findPreference(getString(R.string.pref_key_ui_pitch_limit)));
            setListener(findPreference(getString(R.string.pref_key_ui_yaw_limit)));
            setListener(findPreference(getString(R.string.pref_key_ui_speed_limit)));
            setListener(findPreference(getString(R.string.pref_key_ui_accel_limit)));
            setListener(findPreference(getString(R.string.pref_key_ui_altitude_limit)));
            setListener(findPreference(getString(R.string.pref_key_ui_vert_speed_limit)));
            setListener(findPreference(getString(R.string.pref_key_ui_alert_perc)));
            setListener(findPreference(getString(R.string.pref_key_ui_hectic_alert_perc)));
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                preference.setSummary(stringValue);
            }

            if (preference.getKey().equals(getString(R.string.pref_key_smoothing_phone_sensors)))
                LowPassData.setSmoothingCoeff(
                        Float.valueOf(newValue.toString()), SensorSource.PHONE);
            else if (preference.getKey().equals(
                    getString(R.string.pref_key_smoothing_watch_sensors)))
                LowPassData.setSmoothingCoeff(
                        Float.valueOf(newValue.toString()), SensorSource.WATCH);
            else if (preference.getKey().equals(
                    getString(R.string.pref_key_smoothing_drone_sensors)))
                LowPassData.setSmoothingCoeff(
                        Float.valueOf(newValue.toString()), SensorSource.DRONE);

            return true;
        }

        private void setListener(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(
                    preference.getContext()).getString(preference.getKey(), ""));
        }
    }
}
