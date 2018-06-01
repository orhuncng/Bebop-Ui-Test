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

public class SettingsActivity extends PreferenceActivity
{
    private static Preference.OnPreferenceChangeListener prefChanged =
            new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    String stringValue = newValue.toString();

                    if (preference instanceof ListPreference) {
                        ListPreference listPreference = (ListPreference) preference;
                        int index = listPreference.findIndexOfValue(stringValue);

                        preference.setSummary(
                                index >= 0 ? listPreference.getEntries()[index] : null);
                    }
                    else {
                        preference.setSummary(stringValue);
                    }

                    return true;
                }
            };

    private static void bindSummary(Preference preference)
    {
        preference.setOnPreferenceChangeListener(prefChanged);
        prefChanged.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref);

            // bind summaries to selected pref values
            bindSummary(findPreference(getString(R.string.pref_key_camera_stab_mode)));
            bindSummary(findPreference(getString(R.string.pref_key_home_type)));
            bindSummary(findPreference(getString(R.string.pref_key_smoothing_ui)));
            bindSummary(findPreference(getString(R.string.pref_key_smoothing_watch_sensors)));
            bindSummary(findPreference(getString(R.string.pref_key_smoothing_phone_sensors)));
            bindSummary(findPreference(getString(R.string.pref_key_max_altitude)));
            bindSummary(findPreference(getString(R.string.pref_key_max_vert_speed)));
            bindSummary(findPreference(getString(R.string.pref_key_max_pitch_roll)));
            bindSummary(findPreference(getString(R.string.pref_key_max_rot_speed)));
            bindSummary(findPreference(getString(R.string.pref_key_max_tilt)));

            // register smoothing related events to LowPassData
            findPreference(getString(R.string.pref_key_smoothing_phone_sensors))
                    .setOnPreferenceChangeListener(
                            new Preference.OnPreferenceChangeListener() {
                                @Override
                                public boolean onPreferenceChange(Preference preference,
                                                                  Object newValue) {
                                    LowPassData.setSmoothingPerc(Float.valueOf(newValue.toString()));
                                    return true;
                                }
                            });
        }
    }
}
