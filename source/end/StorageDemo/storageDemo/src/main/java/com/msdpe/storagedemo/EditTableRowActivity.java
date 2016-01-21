/*
 Copyright 2013 Microsoft Corp
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.msdpe.storagedemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class EditTableRowActivity extends Activity {
	private Context mContext;
	private StorageService mStorageService;
	private int mSelectedRowPosition;
	private String mTableName;
	private boolean mIsNewTable;
	private boolean mIsNewRow;
	private boolean mIsExisting; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_table_row);
		// Show the Up button in the action bar.
		setupActionBar();
		//Get access to the storage service
		StorageApplication myApp = (StorageApplication) getApplication();
		mStorageService = myApp.getStorageService();
		
		mContext = this;		
		//Load data from the intent that launched this activity
		Intent launchIntent = getIntent();
		mTableName = launchIntent.getStringExtra("TableName");
		mIsNewTable = launchIntent.getBooleanExtra("IsNewTable", false);
		mIsNewRow = launchIntent.getBooleanExtra("IsNewRow", false);
		mIsExisting = !mIsNewRow && !mIsNewTable;
		mSelectedRowPosition = launchIntent.getIntExtra("RowPosition", -1);				
		//handle the UI depending on the type of row we're dealing with
		if (mIsNewTable) {
			setupForNewTable();
		} else if (mIsNewRow) {
			setupForNewRow();
		} else {
			setupForExistingRow();
		}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_table_row, menu);
		return true;
	}
	
	/***
	 * Set things up if we're creating the first row in a table
	 */
	private void setupForNewTable() {
		TextView lblTemp = (TextView) findViewById(R.id.lblTemp);
		lblTemp.setText("NewTable");
		
		Set<Entry<String, JsonElement>> set = new HashSet<Map.Entry<String,JsonElement>>();
		//Preset the entries so we have ones for PartitionKey and RowKey
		set.add(new AbstractMap.SimpleEntry<String, JsonElement>("PartitionKey", null));
		set.add(new AbstractMap.SimpleEntry<String, JsonElement>("RowKey", null));
		//Add some default data items in
		for (int i = 2; i < 10; i++) 
			set.add(new AbstractMap.SimpleEntry<String, JsonElement>("Item"+i, null));
		setupInterfaceForData(set);
	}
	
	/***
	 * Set things up if we're adding a row to a table
	 */
	private void setupForNewRow() {
		TextView lblTemp = (TextView) findViewById(R.id.lblTemp);
		lblTemp.setText("NewRow");		
		Set<Entry<String, JsonElement>> set = mStorageService.getLoadedTableRows()[0].getAsJsonObject().entrySet();
		setupInterfaceForData(set);
	}
	
	/***
	 * Set things up if we're editing an existing row
	 */
	private void setupForExistingRow() {
		TextView lblTemp = (TextView) findViewById(R.id.lblTemp);
		lblTemp.setText("Existing");		
		Set<Entry<String, JsonElement>> set = mStorageService.getLoadedTableRows()[mSelectedRowPosition].getAsJsonObject().entrySet();
		setupInterfaceForData(set);
	}
	
	/***
	 * Handles seting up the UI for the appropriate data
	 * @param set - The data set we should use for the UI
	 */
	private void setupInterfaceForData(Set<Entry<String, JsonElement>> set) {
		//get the root layout we'll add our new UI elements to
		LinearLayout layoutRoot = (LinearLayout) findViewById(R.id.layoutRoot);
		for (Entry<String, JsonElement> entry : set) {
			String key = entry.getKey();
			//Don't show rows for the link, etag, updated, id, or Timestamp data items
			//These are added and handled automatically by Table Storage
			if (key.equals("link") || key.equals("etag") || key.equals("updated") 
					|| key.equals("id") || key.equals("Timestamp")) {
				continue;
			}
			//Create a new relative layout for the data item
			RelativeLayout rowLayout = new RelativeLayout(mContext);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);			
			rowLayout.setLayoutParams(params);				
			View lblKey = null;
			//If we're not creating the first row in a table (or it's the partitionkey / rowkey)
			//don't show the data key as editable.  This is so we're not changing key names.
			if (mIsExisting || mIsNewRow ||
					entry.getKey().equals("PartitionKey") ||
					entry.getKey().equals("RowKey")) {
				lblKey = new TextView(mContext);					
			} else {
				//Otherwise, for new tables, let the user set the key names
				lblKey = new EditText(mContext);
			}
			lblKey.setTag("Key");
			((TextView) lblKey).setText(entry.getKey());
			((TextView) lblKey).setWidth(100);
			((TextView) lblKey).setTextSize(12);			
			RelativeLayout.LayoutParams keyParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			keyParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lblKey.setLayoutParams(keyParams);
			//Handle the value type
			View lblValue = null;
			//Don't let the user edit the partitionkey / rowkey if it's an existing entry
			if (mIsExisting && (entry.getKey().equals("PartitionKey") ||
					entry.getKey().equals("RowKey"))) {
				lblValue = new TextView(mContext);					
			} else {
				lblValue = new EditText(mContext);
			}
			lblValue.setTag("Value");
			((TextView) lblValue).setWidth(150);
			((TextView) lblValue).setTextSize(12);
			RelativeLayout.LayoutParams valueParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			valueParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			lblValue.setLayoutParams(valueParams);
			//if we're dealing with an existing row, set the value textbox's text
			if (!mIsNewRow && !mIsNewTable)
				((TextView) lblValue).setText(entry.getValue().getAsString());
			((TextView) lblValue).setGravity(Gravity.RIGHT);
			rowLayout.addView(lblKey);
			rowLayout.addView(lblValue);
			layoutRoot.addView(rowLayout);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			//NavUtils.navigateUpFromSameTask(this);
			
			//Calling onBack here because using the NavUtils default code doesn't restore state for some reason
			onBackPressed();
			return true;
		case R.id.action_save_row:
			//add or update the row depending on if it's new or existing
			if (mIsNewRow || mIsNewTable) {
				List<Pair<String,String>> rowData = getRowDataFromInterface();
				mStorageService.addTableRow(mTableName, rowData);
			} else {
				List<Pair<String,String>> rowData = getRowDataFromInterface();
				mStorageService.updateTableRow(mTableName, rowData);
			}
			finish();
			return true;		
		}
		return super.onOptionsItemSelected(item);
	}
	
	/***
	 * Handles pulling the data out of the UI when we're ready to save it
	 * @return
	 */
	private List<Pair<String,String>> getRowDataFromInterface() {
		List<Pair<String,String>> rowData = new ArrayList<Pair<String, String>>();
		LinearLayout layoutRoot = (LinearLayout) findViewById(R.id.layoutRoot);
		//Loop through each "row" in the UI
		for (int i = 0; i < layoutRoot.getChildCount(); i++) {
			View childView = layoutRoot.getChildAt(i); 
			if (childView.getClass() == RelativeLayout.class) {
				RelativeLayout childLayout = (RelativeLayout) childView;
				//Get the key and vlaue out of the textviews
				TextView txtKey = (TextView) childLayout.findViewWithTag("Key");
				TextView txtValue = (TextView) childLayout.findViewWithTag("Value");
				rowData.add(new Pair<String, String>(txtKey.getText().toString(), txtValue.getText().toString()));
			}
		}		
		return rowData;
	}
}
