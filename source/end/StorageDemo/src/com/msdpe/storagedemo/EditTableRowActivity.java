package com.msdpe.storagedemo;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gson.JsonElement;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.text.InputFilter;

public class EditTableRowActivity extends Activity {
	private Context mContext;
	private StorageService mStorageService;
	private final String TAG = "EditTableRowActivity";
	//private ActionMode mActionMode;
	private int mSelectedRowPosition;
	private String mTableName;
	private boolean mIsNewTable;
	private boolean mIsNewRow;
	private int mWidth;
	private boolean mIsExisting; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_table_row);
		// Show the Up button in the action bar.
		setupActionBar();
		StorageApplication myApp = (StorageApplication) getApplication();
		mStorageService = myApp.getStorageService();
		mContext = this;
		
		//Load extras from intent
		Intent launchIntent = getIntent();
		mTableName = launchIntent.getStringExtra("TableName");
		mIsNewTable = launchIntent.getBooleanExtra("IsNewTable", false);
		mIsNewRow = launchIntent.getBooleanExtra("IsNewRow", false);
		mIsExisting = !mIsNewRow && !mIsNewTable;
		mSelectedRowPosition = launchIntent.getIntExtra("RowPosition", -1);		
		
		//Get screen width
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		mWidth = displayMetrics.widthPixels;
		Log.i(TAG, "Width: " + mWidth);
		
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
	
	private void setupForNewTable() {
		TextView lblTemp = (TextView) findViewById(R.id.lblTemp);
		lblTemp.setText("NewTable");
		
		Set<Entry<String, JsonElement>> set = new HashSet<Map.Entry<String,JsonElement>>();
		
		set.add(new AbstractMap.SimpleEntry<String, JsonElement>("PartitionKey", null));
		set.add(new AbstractMap.SimpleEntry<String, JsonElement>("RowKey", null));
		for (int i = 2; i < 10; i++) 
			set.add(new AbstractMap.SimpleEntry<String, JsonElement>("Item"+i, null));
				//mStorageService.getLoadedTableRows()[0].getAsJsonObject().entrySet();
		setupInterfaceForData(set);
	}
	
	private void setupForNewRow() {
		TextView lblTemp = (TextView) findViewById(R.id.lblTemp);
		lblTemp.setText("NewRow");
		
		Set<Entry<String, JsonElement>> set = mStorageService.getLoadedTableRows()[0].getAsJsonObject().entrySet();
		setupInterfaceForData(set);
	}
	
	private void setupForExistingRow() {
		TextView lblTemp = (TextView) findViewById(R.id.lblTemp);
		lblTemp.setText("Existing");
		
		Set<Entry<String, JsonElement>> set = mStorageService.getLoadedTableRows()[mSelectedRowPosition].getAsJsonObject().entrySet();
		setupInterfaceForData(set);
	}
	
	private void setupInterfaceForData(Set<Entry<String, JsonElement>> set) {
		

		LinearLayout layoutRoot = (LinearLayout) findViewById(R.id.layoutRoot);
		
		//@"link",@"etag",@"updated",@"id"
		
		for (Entry<String, JsonElement> entry : set) {
			String key = entry.getKey();
			
			if (key.equals("link") || key.equals("etag") || key.equals("updated") 
					|| key.equals("id") || key.equals("Timestamp")) {
				continue;
			}
			
			RelativeLayout rowLayout = new RelativeLayout(mContext);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			
			rowLayout.setLayoutParams(params);				
			View lblKey = null;
			if (mIsExisting || mIsNewRow ||
					entry.getKey().equals("PartitionKey") ||
					entry.getKey().equals("RowKey")) {
				lblKey = new TextView(mContext);					
			} else {
				lblKey = new EditText(mContext);
			}
			lblKey.setTag("Key");
			((TextView) lblKey).setText(entry.getKey());
			((TextView) lblKey).setWidth(100);
			((TextView) lblKey).setTextSize(12);
			
			RelativeLayout.LayoutParams keyParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			keyParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			//keyParams.leftMargin = 20;
			lblKey.setLayoutParams(keyParams);
			
			View lblValue = null;
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
			
			InputFilter[] FilterArray = new InputFilter[1];
			//FilterArray[0] = new InputFilter.LengthFilter(25);
			//((TextView) lblValue).setFilters(FilterArray);
			if (!mIsNewRow && !mIsNewTable)
				((TextView) lblValue).setText(entry.getValue().getAsString());
			((TextView) lblValue).setGravity(Gravity.RIGHT);
			rowLayout.addView(lblKey);
			rowLayout.addView(lblValue);
			layoutRoot.addView(rowLayout);
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
//	private void setupActionBar() {
//
//		getActionBar().setDisplayHomeAsUpEnabled(true);
//
//	}

	

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
			
			//Calling onBack here becuase using the NavUtils default code doesn't restore state for some reason
			onBackPressed();
			return true;
		case R.id.action_save_row:
//			Intent tableIntent = new Intent(getApplicationContext(), EditTableRowActivity.class);
//			tableIntent.putExtra("TableName", mTableName);
//			tableIntent.putExtra("IsNewRow", true);
//			if (mStorageService.getLoadedTableRows().length == 0) {
//				tableIntent.putExtra("IsNewTable", true);
//			}
//			startActivity(tableIntent);
			if (mIsNewRow || mIsNewTable) {
				List<Pair<String,String>> rowData = getRowDataFromInterface();
				mStorageService.addTableRow(mTableName, rowData);
			} else {
				List<Pair<String,String>> rowData = getRowDataFromInterface();
			}
			finish();
			return true;
		
		}
		return super.onOptionsItemSelected(item);
	}
	
	private List<Pair<String,String>> getRowDataFromInterface() {
		List<Pair<String,String>> rowData = new ArrayList<Pair<String, String>>();
		LinearLayout layoutRoot = (LinearLayout) findViewById(R.id.layoutRoot);
		
		for (int i = 0; i < layoutRoot.getChildCount(); i++) {
			
			
			View childView = layoutRoot.getChildAt(i); 
			if (childView.getClass() == RelativeLayout.class) {
				RelativeLayout childLayout = (RelativeLayout) childView;
//				for (int j = 0; j < childLayout.getChildCount(); j++) {
//					TextView   = childLayout.getChildAt(j);
//					Log.w(TAG, "TAG: " + v2.getTag());
//				}
				TextView txtKey = (TextView) childLayout.findViewWithTag("Key");
				TextView txtValue = (TextView) childLayout.findViewWithTag("Value");
				rowData.add(new Pair<String, String>(txtKey.getText().toString(), txtValue.getText().toString()));
			}
			
		}		
		return rowData;
	}

}
