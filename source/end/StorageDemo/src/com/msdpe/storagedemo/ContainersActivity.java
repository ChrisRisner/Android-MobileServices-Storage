package com.msdpe.storagedemo;

import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;
import android.view.View;
import android.widget.TextView;

public class ContainersActivity extends ListActivity {
	private Context mContext;
	private StorageService mStorageService;
	private final String TAG = "ContainersActivity";
	private ActionMode mActionMode;
	private int mSelectedContainerPosition;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show the Up button in the action bar.
		setupActionBar();
				
		StorageApplication myApp = (StorageApplication) getApplication();
		mStorageService = myApp.getStorageService();
				
		mContext = this;
		mStorageService.getContainers();
		
		this.getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				//Launch the table rows activity for this table
				TextView lblTable = (TextView) view;
				//Toast.makeText(mCon, lblTable.getText().toString(), Toast.LENGTH_SHORT).show();
//				Intent tableIntent = new Intent(getApplicationContext(), BlobsActivity.class);
//				tableIntent.putExtra("TableName", lblTable.getText().toString());
//				startActivity(tableIntent);
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

				mSelectedContainerPosition = position;
		        // Start the CAB using the ActionMode.Callback defined above
		        mActionMode = ((Activity) mContext).startActionMode(mActionModeCallback);
		        view.setSelected(true);
		        return true;
				
				//return false;
			}
		});
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
		getMenuInflater().inflate(R.menu.containers, menu);
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
		case R.id.action_add_container:
		      //Show new table dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            // Get the layout inflater
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            //Create our dialog view
            View dialogView = inflater.inflate(R.layout.dialog_new_container, null);
            final EditText txtContainerName = (EditText) dialogView.findViewById(R.id.txtContainerName);

            final ToggleButton btnIsPublic = (ToggleButton) dialogView.findViewById(R.id.btnIsPublic);
            builder.setView(dialogView)
                   .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int id) {
                    	   mStorageService.addContainer(txtContainerName.getText().toString(), btnIsPublic.isChecked());                          
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
		filter.addAction("containers.loaded");
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
			
			List<Map<String,String>> containers = mStorageService.getLoadedContainers();
			
			String[] strContainers = new String[containers.size()];
			for (int i = 0; i < containers.size(); i ++) {
				strContainers[i] = containers.get(i).get("ContainerName");
			}
			
			ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(mContext,
	                android.R.layout.simple_list_item_1, strContainers);
			setListAdapter(listAdapter);	
			
		}
	};
	
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

	    // Called when the action mode is created; startActionMode() was called
	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        // Inflate a menu resource providing context menu items
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.context_containers, menu);
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
	            	String tableName = mStorageService.getLoadedContainers().get(mSelectedContainerPosition).get("ContainerName");
	            	//Toast.makeText(mCon, "table:" + tableName, Toast.LENGTH_SHORT).show();
	            	//mStorageService.deleteTable(tableName);
	            	
	            	//delete the container
	            	
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	    	mSelectedContainerPosition = -1;
	        mActionMode = null;
	    }
	};
}
