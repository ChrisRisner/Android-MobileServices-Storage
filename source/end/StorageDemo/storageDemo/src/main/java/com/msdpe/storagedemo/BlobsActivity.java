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
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class BlobsActivity extends ListActivity {
	private Context mContext;
	private StorageService mStorageService;
	private final String TAG = "BlobsActivity";
	private ActionMode mActionMode;
	private int mSelectedBlobPosition;
	private String mContainerName;
	private Button btnPositiveDialog;
	private EditText mTxtBlobName;
	private ImageView mImgBlobImage;
	private Uri mImageUri;
	private AlertDialog mAlertDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show the Up button in the action bar.
		setupActionBar();
		//Get access to the storage service
		StorageApplication myApp = (StorageApplication) getApplication();
		mStorageService = myApp.getStorageService();
		//Get data from the intent that launched this activity
		Intent launchIntent = getIntent();
		mContainerName = launchIntent.getStringExtra("ContainerName");
				
		mContext = this;
		//Get the blobs for the selected container
		mStorageService.getBlobsForContainer(mContainerName);		
		
		//Handle clicking on an individual item in the list view
		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				//Launch the BlobDetailsActivity
				TextView lblTable = (TextView) view;
				Intent blobDetailsIntent = new Intent(getApplicationContext(), BlobDetailsActivity.class);
				blobDetailsIntent.putExtra("ContainerName", mContainerName);
				blobDetailsIntent.putExtra("BlobName", lblTable.getText().toString());
				blobDetailsIntent.putExtra("BlobPosition", position);
				startActivity(blobDetailsIntent);
			}
		});
		//Handle long clicking an individual item in the list view
		this.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {				
				if (mActionMode != null) {
		            return false;
		        }
				mSelectedBlobPosition = position;
		        // Start the CAB using the ActionMode.Callback defined above
		        mActionMode = ((Activity) mContext).startActionMode(mActionModeCallback);
		        view.setSelected(true);
		        return true;
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
		getMenuInflater().inflate(R.menu.blobs, menu);
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
		case R.id.action_add_blob:
		      //Show new table dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            // Get the layout inflater
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            //Create our dialog view
            View dialogView = inflater.inflate(R.layout.dialog_new_blob, null);
            mTxtBlobName = (EditText) dialogView.findViewById(R.id.txtBlobName);            
            final Button btnSelectImage = (Button) dialogView.findViewById(R.id.btnSelectImage);
            mImgBlobImage = (ImageView) dialogView.findViewById(R.id.imgBlobImage);            
            //Set select image handler
            btnSelectImage.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					selectImage();
				}
			});
            //Build up the dialog view and add buttons, note that the positive button
            //has no click listener here.  If hte listener is done here, we can't prevent
            //the OK button from closing the dialog when clicked if they don't 
            //have an image selected
            builder.setView(dialogView)
                   .setPositiveButton(R.string.create, null)
                   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                    	   dialog.cancel();
                       }
                   });                
            mAlertDialog = builder.show();
            //We're overriding the button's click here so it won't close if we're not ready
            btnPositiveDialog = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnPositiveDialog.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					mStorageService.getSasForNewBlob(mContainerName, mTxtBlobName.getText().toString());
				}
			});
		    break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/***
	 * Register for broadcasts
	 */
	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("blobs.loaded");
		filter.addAction("blob.created");
		registerReceiver(receiver, filter);
		super.onResume();
	}
	
	/***
	 * Unregister for broadcasts
	 */
	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}
	
	/***
	 * Broadcast receiver handles blobs being loaded or a new blob being created
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, android.content.Intent intent) {
			String intentAction = intent.getAction();
			if (intentAction.equals("blobs.loaded")) {
				//If the blobs have been loaded, wire up our list view
				List<Map<String,String>> blobs = mStorageService.getLoadedBlobNames();				
				String[] strBlobs = new String[blobs.size()];
				for (int i = 0; i < blobs.size(); i ++) {
					strBlobs[i] = blobs.get(i).get("BlobName");
				}				
				ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(mContext,
		                android.R.layout.simple_list_item_1, strBlobs);
				setListAdapter(listAdapter);	
			} else if (intentAction.equals("blob.created")) {
				//If a blob has been created, upload the image
				JsonObject blob = mStorageService.getLoadedBlob();
				String sasUrl = blob.getAsJsonPrimitive("sasUrl").toString();				
				(new ImageUploaderTask(sasUrl)).execute();
			}			
		}
	};
	
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
	    // Called when the action mode is created; startActionMode() was called
	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        // Inflate a menu resource providing context menu items
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.context_blobs, menu);
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
	            case R.id.action_delete_blob:
	            	//Delete the selected blob
	            	String blobName = mStorageService.getLoadedBlobNames().get(mSelectedBlobPosition).get("BlobName");
	            	mStorageService.deleteBlob(mContainerName, blobName);	            	
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	    	mSelectedBlobPosition = -1;
	        mActionMode = null;
	    }
	};
	
	// Fire off intent to select image from gallery
 	protected void selectImage() {
 		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 		intent.setType("image/*");
 		startActivityForResult(intent, 1111);
 	}
	 	
 	// Result handler for any intents started with startActivityForResult
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		try {
 			//handle result from gallary select
 			if (requestCode == 1111) {
 				Uri currImageURI = data.getData();
 				mImageUri = currImageURI;
 				//Set the image view's image by using imageUri
 				mImgBlobImage.setImageURI(currImageURI);
 			}
 		} catch (Exception ex) {
 			Log.e(TAG, ex.getMessage());
 		}
 	}	
 	
 	/***
 	 * Handles uploading an image to a specified url
 	 */
 	class ImageUploaderTask extends AsyncTask<Void, Void, Boolean> {
	    private String mUrl;
	    public ImageUploaderTask(String url) {
	    	mUrl = url;
	    }

	    @Override
	    protected Boolean doInBackground(Void... params) {	         
	    	try {
	    		//Get the image data
		    	Cursor cursor = getContentResolver().query(mImageUri, null,null, null, null);
				cursor.moveToFirst();
				int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
				String absoluteFilePath = cursor.getString(index);
				FileInputStream fis = new FileInputStream(absoluteFilePath);
				int bytesRead = 0;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] b = new byte[1024];
				while ((bytesRead = fis.read(b)) != -1) {
					bos.write(b, 0, bytesRead);
				}
				byte[] bytes = bos.toByteArray();
				// Post our image data (byte array) to the server
				URL url = new URL(mUrl.replace("\"", ""));
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setDoOutput(true);
				urlConnection.setRequestMethod("PUT");
				urlConnection.addRequestProperty("Content-Type", "image/jpeg");
				urlConnection.setRequestProperty("Content-Length", ""+ bytes.length);
				// Write image data to server
				DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
				wr.write(bytes);
				wr.flush();
				wr.close();
				int response = urlConnection.getResponseCode();
				//If we successfully uploaded, return true
				if (response == 201
						&& urlConnection.getResponseMessage().equals("Created")) {
					return true;
				}
	    	} catch (Exception ex) {
	    		Log.e(TAG, ex.getMessage());
	    	}
	        return false;	    	
	    }

	    @Override
	    protected void onPostExecute(Boolean uploaded) {
	        if (uploaded) {
	        	mAlertDialog.cancel();
	        	mStorageService.getBlobsForContainer(mContainerName);
	        }
	    }
	}
}
