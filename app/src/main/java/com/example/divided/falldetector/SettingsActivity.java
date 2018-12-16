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
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
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
        } else if (preference instanceof NumberPickerPreference) {

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


        // load settings fragment
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

            // feedback preference click listener
            /*Preference myPref = findPreference(getString(R.string.key_send_feedback));
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    return true;
                }
            });*/

        }
    }

    /*public static void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@androidhive.info"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));
    }*/
}
