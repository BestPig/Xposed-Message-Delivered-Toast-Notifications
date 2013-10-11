package com.mohammadag.deliverytoastnotification;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
		addPreferencesFromResource(R.xml.pref_general);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onMenuItemSelected(featureId, item);
	}
}
