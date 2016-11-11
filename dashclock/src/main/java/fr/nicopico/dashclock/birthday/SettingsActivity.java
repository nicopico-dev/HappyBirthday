/*
 * Copyright 2016 Nicolas Picon <nicopico.dev@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.nicopico.dashclock.birthday;

import android.Manifest.permission;
import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.view.MenuItem;

import timber.log.Timber;

public class SettingsActivity extends PreferenceActivity {

    private static final int REQUEST_READ_CONTACTS = 1;

    public static final String PREF_DAYS_LIMIT_KEY = "pref_days_limit";
    public static final String PREF_SHOW_QUICK_CONTACT = "pref_show_quickcontact";
    public static final String PREF_DISABLE_LOCALIZATION = "pref_disable_localization";
    public static final String PREF_CONTACT_GROUP = "pref_contact_group";

    public static final String NO_CONTACT_GROUP_SELECTED = "-1";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setIcon(R.drawable.ic_extension_white);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadContactGroups();
        }
        else {
            // Fallback (no groups)
            @SuppressWarnings("deprecation")
            ListPreference listPreference = (ListPreference) findPreference(PREF_CONTACT_GROUP);
            listPreference.setEntries(new CharSequence[] { getString(R.string.pref_no_contact_group_selected) });
            listPreference.setEntryValues(new CharSequence[] { NO_CONTACT_GROUP_SELECTED });
            bindPreferenceSummaryToValue(listPreference);
        }
    }

    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference(PREF_DAYS_LIMIT_KEY));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { permission.READ_CONTACTS }, REQUEST_READ_CONTACTS);
        }
        else {
            loadContactGroups();
        }
    }

    @RequiresPermission(permission.READ_CONTACTS)
    private void loadContactGroups() {
        // Contact group preference
        Cursor groupCursor = null;
        try {
            groupCursor = getContentResolver().query(
                    ContactsContract.Groups.CONTENT_URI,
                    new String[] {
                            ContactsContract.Groups._ID,
                            ContactsContract.Groups.TITLE
                    }, null, null, null
            );
            assert groupCursor != null;
            int nbGroups = groupCursor.getCount();
            CharSequence[] groupNames = new CharSequence[nbGroups + 1];
            CharSequence[] groupIds = new CharSequence[nbGroups + 1];
            int i = 0;
            groupNames[i] = getString(R.string.pref_no_contact_group_selected);
            groupIds[i] = NO_CONTACT_GROUP_SELECTED;
            while (groupCursor.moveToNext()) {
                groupNames[++i] = groupCursor.getString(1);
                groupIds[i] = groupCursor.getString(0);
            }

            @SuppressWarnings("deprecation")
            ListPreference listPreference = (ListPreference) findPreference(PREF_CONTACT_GROUP);
            listPreference.setEntries(groupNames);
            listPreference.setEntryValues(groupIds);
            bindPreferenceSummaryToValue(listPreference);
        }
        catch (IndexOutOfBoundsException e) {
            Timber.e(e, "Error while building contact group list");
        }
        finally {
            if (groupCursor != null) groupCursor.close();
        }
    }

    /**
     * A preference value change listener that updates the preference's summary to reflect its new
     * value.
     */
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener
            = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                //noinspection ConstantConditions
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null
                );

            }
            else if (PREF_DAYS_LIMIT_KEY.equals(preference.getKey())) {
                final Resources res = preference.getContext().getResources();
                int intValue;

                try {
                    intValue = Integer.valueOf(stringValue);
                }
                catch (NumberFormatException e) {
                    Timber.e(e, "Unable to retrieve days limit preference. Restore default");
                    intValue = 7;
                }

                String summary;
                if (intValue == 0) {
                    summary = res.getString(R.string.pref_days_limit_0_summary_format);
                }
                else if (intValue == 1) {
                    summary = res.getString(R.string.pref_days_limit_1_summary_format);
                }
                else {
                    summary = res.getQuantityString(
                            R.plurals.pref_days_limit_summary_format,
                            intValue,
                            intValue
                    );
                }
                preference.setSummary(summary);
            }
            else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the preference's value is
     * changed, its summary (line of text below the preference title) is updated to reflect the
     * value. The summary is also immediately updated upon calling this method. The exact display
     * format is dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "")
        );
    }
}