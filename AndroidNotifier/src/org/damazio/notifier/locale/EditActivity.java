/*
 * Copyright 2010 Rodrigo Damazio <rodrigo@damazio.org>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.damazio.notifier.locale;

import org.damazio.notifier.EditableListPreference;
import org.damazio.notifier.NotifierConstants;
import org.damazio.notifier.R;
import org.damazio.notifier.locale.LocaleSettings.OnOffKeep;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * Activity displayed when the user wants to edit the Locale plugin settings.
 *
 * @author rdamazio
 */
public class EditActivity extends PreferenceActivity implements OnPreferenceChangeListener {
  private LocaleSettings settings;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Create the UI
    addPreferencesFromResource(R.xml.locale_settings);
    String breadcrumbString = getIntent().getStringExtra(com.twofortyfouram.Intent.EXTRA_STRING_BREADCRUMB);
    if (breadcrumbString != null) {
      setTitle(String.format("%s%s%s", breadcrumbString,
          com.twofortyfouram.Intent.BREADCRUMB_SEPARATOR, getString(R.string.settings_title)));
    }

    // Parse the input bundle
    Bundle forwardedBundle = getIntent().getBundleExtra(com.twofortyfouram.Intent.EXTRA_BUNDLE);
    settings = new LocaleSettings(this, forwardedBundle);

    // Populate settings
    populateListPreference(R.string.locale_change_enabled_key, settings.getEnabledState().name());
    populateListPreference(R.string.locale_ip_enabled_key, settings.getIpEnabledState().name());
    populateListPreference(R.string.locale_bt_enabled_key, settings.getBluetoothEnabledState().name());
    populateListPreference(R.string.locale_target_ip_key, settings.getTargetIp());

    EditableListPreference customIpsPreference =
        (EditableListPreference) findPreference(getString(R.string.locale_custom_ip_key));
    customIpsPreference.setValues(settings.getCustomIps());
    customIpsPreference.setOnPreferenceChangeListener(this);
  }

  private void populateListPreference(int preferenceKey, String initialValue) {
    ListPreference enabledPreference = (ListPreference) findPreference(getString(preferenceKey));
    enabledPreference.setValue(initialValue);
    enabledPreference.setOnPreferenceChangeListener(this);
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    String key = preference.getKey();
    if (getString(R.string.locale_change_enabled_key).equals(key)) {
      settings.setEnabledState(OnOffKeep.valueOf((String) newValue));
    } else if (getString(R.string.locale_ip_enabled_key).equals(key)) {
      settings.setIpEnabledState(OnOffKeep.valueOf((String) newValue));
    } else if (getString(R.string.locale_bt_enabled_key).equals(key)) {
      settings.setBluetoothEnabledState(OnOffKeep.valueOf((String) newValue));
    } else if (getString(R.string.locale_target_ip_key).equals(key)) {
      settings.setTargetIp((String) newValue);
    } else if (getString(R.string.locale_custom_ip_key).equals(key)) {
      settings.setCustomIps((String[]) newValue);
    }

    updateLocaleResult();
    return true;
  }

  private void updateLocaleResult() {
    if (!settings.hasChanges()) {
      setResult(com.twofortyfouram.Intent.RESULT_REMOVE);
      return;
    }

    Bundle settingsBundle = settings.toBundle();
    String blurb = settings.toString();
    Log.d(NotifierConstants.LOG_TAG, "New locale plugin settings: " + blurb);
    if (blurb.length() > com.twofortyfouram.Intent.MAXIMUM_BLURB_LENGTH) {
      blurb = blurb.substring(0, com.twofortyfouram.Intent.MAXIMUM_BLURB_LENGTH - 3) + "...";
    }

    Intent returnIntent = new Intent();
    returnIntent.putExtra(com.twofortyfouram.Intent.EXTRA_BUNDLE, settingsBundle);
    returnIntent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_BLURB, blurb);
    setResult(RESULT_OK, returnIntent);
  }
}
