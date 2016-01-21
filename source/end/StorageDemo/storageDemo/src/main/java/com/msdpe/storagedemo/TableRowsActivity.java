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
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.InputFilter;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.Map.Entry;
import java.util.Set;

public class TableRowsActivity extends ListActivity {
	private Context mContext;
	private StorageService mStorageService;
	private final String TAG = "TableRowsActivity";
	private ActionMode mActionMode;
	private int mSelectedRowPosition;
	private String mTableName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show the Up button in the action bar.
		setupActionBar();
		//Get access to the storage service
		StorageApplication myApp = (StorageApplication) getApplication();
		mStorageService = myApp.getStorageService();
		
		mContext = this;
		//Get data from the intent that launched this activity
		Intent launchIntent = getIntent();
		mTableName = launchIntent.getStringExtra("TableName");
		//Get the rows for the table
		mStorageService.getTableRows(mTableName);	
		//Set the click listener for items in the list view
		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				//Launch the activity to edit the existing row
				Intent tableIntent = new Intent(getApplicationContext(), EditTableRowActivity.class);
				tableIntent.putExtra("TableName", mTableName);
				tableIntent.putExtra("IsNewRow", false);
				tableIntent.putExtra("RowPosition", position);
				startActivity(tableIntent);
			}
		});
		//Handle long clicks on an item in the list view to show the delete option
		this.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mActionMode != null) {
		            return false;
		        }
				mSelectedRowPosition = position;
		        // Start the CAB using the ActionMode.Callback defined above
		        mActionMode = ((Activity) mContext).startActionMode(mActionModeCallback);
		        view.setSelected(true);
		        return true;
			}
		});
	}
	
	/***
	 * Register for broadcasts
	 */
	@Override
	protected void onResume() {
		Log.w(TAG, "onResume");
		IntentFilter filter = new IntentFilter();
		filter.addAction("tablerows.loaded");
		registerReceiver(receiver, filter);
		super.onResume();
	}
	
	/***
	 * Unregister for broadcasts
	 */
	@Override
	protected void onPause() {
		Log.w(TAG, "onPause");
		unregisterReceiver(receiver);
		super.onPause();
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tablerows, menu);
		return true;
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
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_add_table_row:
			//Launch the intent to add a new row
			Intent tableIntent = new Intent(getApplicationContext(), EditTableRowActivity.class);
			tableIntent.putExtra("TableName", mTableName);
			tableIntent.putExtra("IsNewRow", true);
			if (mStorageService.getLoadedTableRows().length == 0) {
				//If this table doesn't have any rows, we're going to be creating it's first
				tableIntent.putExtra("IsNewTable", true);
			}
			startActivity(tableIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/***
	 * Broadcast receiver for handling when table rows are added
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, android.content.Intent intent) {
			
			TableRowArrayAdapter listAdapter = new TableRowArrayAdapter(mContext, mStorageService.getLoadedTableRows());
			setListAdapter(listAdapter);			
		}
	};
	
	/***
	 * A custom array adapter for handling table row data and tying it to the list view
	 *
	 */
	private class TableRowArrayAdapter extends ArrayAdapter<JsonElement> {
		private Context mContext;
		private JsonElement[] mTableRows;
		
		public TableRowArrayAdapter(Context context, JsonElement[] tableRows) {
			super(context, R.layout.list_item_table_row_read, tableRows);
			this.mContext = context;
			this.mTableRows = tableRows;
		}
		
		/***
		 * Creates the UI for each listview row
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.list_item_table_row_read, parent, false);
			Set<Entry<String, JsonElement>> set = mTableRows[position].getAsJsonObject().entrySet();
			LinearLayout layoutItem = (LinearLayout) view.findViewById(R.id.layoutItem);
			//Loop through each data item in the row and create a layout with the key and 
			//value displayed in TextViews within it
			for (Entry<String, JsonElement> entry : set) {
				Log.i(TAG, entry.getKey());				
				RelativeLayout rowLayout = new RelativeLayout(mContext);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);				
				rowLayout.setLayoutParams(params);				
				TextView lblKey = new TextView(mContext);
				lblKey.setText(entry.getKey());				
				RelativeLayout.LayoutParams keyParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				keyParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				keyParams.leftMargin = 20;
				lblKey.setLayoutParams(keyParams);				
				TextView lblValue = new TextView(mContext);				
				RelativeLayout.LayoutParams valueParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				valueParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				lblValue.setLayoutParams(valueParams);
				//Limit the amount of text we show so the UI isn't dirty
				InputFilter[] FilterArray = new InputFilter[1];
				FilterArray[0] = new InputFilter.LengthFilter(25);
				lblValue.setFilters(FilterArray);
				lblValue.setText(entry.getValue().getAsString());
				lblValue.setGravity(Gravity.RIGHT);
				rowLayout.addView(lblKey);
				rowLayout.addView(lblValue);
				layoutItem.addView(rowLayout);
			}			
			return view;
		}
	}
	
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
	    // Called when the action mode is created; startActionMode() was called
	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        // Inflate a menu resource providing context menu items
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.context_tables, menu);
	        return true;
	    }

	    // Called each time the action mode is shown. Always called after onCreateActionMode, but
	    // may be called multiple times if the mode is invalidated.
	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false; // Return false if nothing is done
	    }

	    // Called when the user selects a contextual menu item
	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.action_delete_table:
	            	//Delete the selected table row	            	
	            	JsonElement element = mStorageService.getLoadedTableRows()[mSelectedRowPosition];
					String partitionKey = element.getAsJsonObject().getAsJsonPrimitive("PartitionKey").getAsString();				
					String rowKey = element.getAsJsonObject().getAsJsonPrimitive("RowKey").getAsString();	            	
	            	mStorageService.deleteTableRow(mTableName, partitionKey, rowKey);
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	    	mSelectedRowPosition = -1;
	        mActionMode = null;
	    }
	};
}
