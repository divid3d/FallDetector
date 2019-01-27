package com.example.divided.falldetector;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, newValue) -> {
        String stringValue = newValue.toString();

        if (preference instanceof ListPreference) {

            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);
        } else if (preference instanceof EditTextPreference) {
            switch (preference.getKey()) {
                case "key_phone_number":
                    if (stringValue.trim().length() > 0) {
                        preference.setSummary(stringValue);
                    } else {
                        preference.setSummary("Enter phone number");
                    }
                    break;
                case "key_username":
                    if (stringValue.trim().length() > 0) {
                        preference.setSummary(stringValue);
                    } else {
                        preference.setSummary("Username");
                    }
                    break;
                case "key_email_address":
                    if (stringValue.trim().length() > 0) {
                        preference.setSummary(stringValue);
                    } else {
                        preference.setSummary("E-mail address");
                    }
                    break;
                case "key_email_login":
                    if (stringValue.trim().length() > 0) {
                        preference.setSummary(stringValue);
                    } else {
                        preference.setSummary("E-mail login");
                    }
                    break;
                case "key_email_password":
                    final int passwordLength = stringValue.length();
                    if (passwordLength > 0) {
                        StringBuilder summary = new StringBuilder();
                        for (int i = 0; i < passwordLength; i++) {
                            summary.append("*");
                        }
                        preference.setSummary(summary);
                    } else {
                        preference.setSummary("E-mail password");
                    }
                    break;
            }
        }
        return true;
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            bindPreferenceSummaryToValue(findPreference("key_username"));
            bindPreferenceSummaryToValue(findPreference("key_phone_number"));
            bindPreferenceSummaryToValue(findPreference("key_email_address"));
            bindPreferenceSummaryToValue(findPreference("key_email_login"));
            bindPreferenceSummaryToValue(findPreference("key_email_password"));
        }
    }
}
