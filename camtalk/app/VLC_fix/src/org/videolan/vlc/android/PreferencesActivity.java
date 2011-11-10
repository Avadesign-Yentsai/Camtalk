package org.videolan.vlc.android;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.avadesign.camvideo.MainActivity;
import com.avadesign.camvideo.R;

public class PreferencesActivity extends PreferenceActivity {

	public final static String TAG = "VLC/PreferencesActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		addPreferencesFromResource(R.xml.preferences);
		
		// Create onClickListen
		Preference directoriesPref = (Preference) findPreference("directories");
		directoriesPref.setOnPreferenceClickListener(
				new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(MainActivity.getInstance(), 
						BrowserActivity.class);
				startActivity(intent);
				return true;
			}
		});
		
		// Create onClickListen
		Preference clearHistoryPref = (Preference) findPreference("clear_history");
		clearHistoryPref.setOnPreferenceClickListener(
				new OnPreferenceClickListener() {
			
			public boolean onPreferenceClick(Preference preference) {
				new AlertDialog.Builder(PreferencesActivity.this)
				.setTitle("Clear")
				.setMessage("Do you really want to whatever?")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

				    public void onClick(DialogInterface dialog, int whichButton) {
				    	DatabaseManager db = DatabaseManager.getInstance();
						db.clearSearchhistory();
				    }})
				    
				 .setNegativeButton(android.R.string.cancel, null).show();
				return true;
			}
		});
	}
	
	



	
	
	
}
