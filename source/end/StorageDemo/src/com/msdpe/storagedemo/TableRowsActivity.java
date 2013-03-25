package com.msdpe.storagedemo;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.GradientDrawable.Orientation;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

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
		
		StorageApplication myApp = (StorageApplication) getApplication();
		mStorageService = myApp.getStorageService();		
		mContext = this;
		
		Intent launchIntent = getIntent();
		mTableName = launchIntent.getStringExtra("TableName");
		Log.i(TAG, "TABLE: " + mTableName);
		
		mStorageService.getTableRows(mTableName);	
		
		this.getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				//This was tested against the Users table
				JsonElement element = mStorageService.getLoadedTableRows()[position];
				String name = element.getAsJsonObject().getAsJsonPrimitive("Name").getAsString();				
				Toast.makeText(mContext, name, Toast.LENGTH_SHORT).show();
			}
		});
		
		this.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				//Toast.makeText(mCon, "long click", Toast.LENGTH_SHORT).show();
				
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
	
	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("tablerows.loaded");
		registerReceiver(receiver, filter);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
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
		getMenuInflater().inflate(R.menu.tables, menu);
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
		}
		return super.onOptionsItemSelected(item);
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, android.content.Intent intent) {
			
			TableRowArrayAdapter listAdapter = new TableRowArrayAdapter(mContext, mStorageService.getLoadedTableRows());
			setListAdapter(listAdapter);
			
		}
	};
	
	private class TableRowArrayAdapter extends ArrayAdapter<JsonElement> {
		private Context mContext;
		private JsonElement[] mTableRows;
		
		public TableRowArrayAdapter(Context context, JsonElement[] tableRows) {
			super(context, R.layout.list_item_table_row_read, tableRows);
			this.mContext = context;
			this.mTableRows = tableRows;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

//			return super.getView(position, convertView, parent);
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.list_item_table_row_read, parent, false);
//			TextView lblName = (TextView) view.findViewById(R.id.lblName);
//			lblName.setText(mTableRows[position].getAsJsonObject().toString());
//			
			Set<Entry<String, JsonElement>> set = mTableRows[position].getAsJsonObject().entrySet();
//			lblName.setText("size: " + set.size());
			
			//TextView lblPartitionKeyValue = (TextView) view.findViewById(R.id.lblPartitionKeyValue);
			//TextView lblRowKeyValue = (TextView) view.findViewById(R.id.lblRowKeyValue);
			LinearLayout layoutItem = (LinearLayout) view.findViewById(R.id.layoutItem);
			
			for (Entry<String, JsonElement> entry : set) {
				Log.i(TAG, entry.getKey());
//				if (entry.getKey().equals("PartitionKey"))
//					lblPartitionKeyValue.setText(entry.getValue().getAsString());
//				if (entry.getKey().equals("RowKey"))
//					lblRowKeyValue.setText(entry.getValue().getAsString());
				
//				LinearLayout rowLayout = new LinearLayout(mContext);
//				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//				params.leftMargin = 20;
//				rowLayout.setLayoutParams(params);				
//				rowLayout.setOrientation(LinearLayout.HORIZONTAL);
//				TextView lblKey = new TextView(mContext);
//				lblKey.setText(entry.getKey());
//				//lblKey.setPadding(20, 0, 50, 0);
//				TextView lblValue = new TextView(mContext);
//				InputFilter[] FilterArray = new InputFilter[1];
//				FilterArray[0] = new InputFilter.LengthFilter(8);
//				lblValue.setFilters(FilterArray);
//				lblValue.setText(entry.getValue().getAsString());
//				lblValue.setGravity(Gravity.RIGHT);
//				rowLayout.addView(lblKey);
//				rowLayout.addView(lblValue);
//				layoutItem.addView(rowLayout);
				
				
				RelativeLayout rowLayout = new RelativeLayout(mContext);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				
				rowLayout.setLayoutParams(params);				
				//rowLayout.setOrientation(LinearLayout.HORIZONTAL);
				TextView lblKey = new TextView(mContext);
				lblKey.setText(entry.getKey());
				//lblKey.setPadding(20, 0, 50, 0);
				
				RelativeLayout.LayoutParams keyParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				keyParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				keyParams.leftMargin = 20;
				lblKey.setLayoutParams(keyParams);
				
				TextView lblValue = new TextView(mContext);
				
				RelativeLayout.LayoutParams valueParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				valueParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				lblValue.setLayoutParams(valueParams);
				
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
	            	//Delete the selected table
	            	//String tableName = mStorageService.getLoadedTables().get(mSelectedTablePosition).get("TableName");
	            	//Toast.makeText(mCon, "table:" + tableName, Toast.LENGTH_SHORT).show();
	            	//mStorageService.deleteTable(tableName);
	            	//mStorageService.deleteTable(tableName)
	            	
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
