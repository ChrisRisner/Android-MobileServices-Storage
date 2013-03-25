package com.msdpe.storagedemo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

public class TablesActivity extends ListActivity {
	private Context mCon;
	private StorageService mStorageService;
	private final String TAG = "TabelsActivity";
	private ActionMode mActionMode;
	private int mSelectedTablePosition;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_tables);
		// Show the Up button in the action bar.
		setupActionBar();
		
		StorageApplication myApp = (StorageApplication) getApplication();
		mStorageService = myApp.getStorageService();
		
		mCon = this;
		mStorageService.getTables();	
		
		this.getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				//Launch the table rows activity for this table
				TextView lblTable = (TextView) view;
				//Toast.makeText(mCon, lblTable.getText().toString(), Toast.LENGTH_SHORT).show();
				Intent tableIntent = new Intent(getApplicationContext(), TableRowsActivity.class);
				tableIntent.putExtra("TableName", lblTable.getText().toString());
				startActivity(tableIntent);
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

				mSelectedTablePosition = position;
		        // Start the CAB using the ActionMode.Callback defined above
		        mActionMode = ((Activity) mCon).startActionMode(mActionModeCallback);
		        view.setSelected(true);
		        return true;
				
				//return false;
			}
		});
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		String text = mStorageService.getLoadedTables().get(position).get("TableName");
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
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
		case R.id.action_add_table:
		      //Show new table dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(mCon);
            // Get the layout inflater
            LayoutInflater inflater = ((Activity) mCon).getLayoutInflater();
            //Create our dialog view
            View dialogView = inflater.inflate(R.layout.dialog_new_table, null);
            final EditText txtTableName = (EditText) dialogView.findViewById(R.id.txtTableName);
            builder.setView(dialogView)
                   .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int id) {	
                    	   mStorageService.addTable(txtTableName.getText().toString());                          
                       }
                   })
                   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                    	   dialog.cancel();
                       }
                   });    
            
            builder.show();
		    break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("tables.loaded");
		registerReceiver(receiver, filter);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, android.content.Intent intent) {
			
			List<Map<String,String>> tables = mStorageService.getLoadedTables();
			
			String[] strTables = new String[tables.size()];
			for (int i = 0; i < tables.size(); i ++) {
				strTables[i] = tables.get(i).get("TableName");
			}
			
			ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(mCon,
	                android.R.layout.simple_list_item_1, strTables);
			setListAdapter(listAdapter);	
			
		}
	};
	
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
	            	String tableName = mStorageService.getLoadedTables().get(mSelectedTablePosition).get("TableName");
	            	//Toast.makeText(mCon, "table:" + tableName, Toast.LENGTH_SHORT).show();
	            	mStorageService.deleteTable(tableName);
	            	//mStorageService.deleteTable(tableName)
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	    	mSelectedTablePosition = -1;
	        mActionMode = null;
	    }
	};
}
