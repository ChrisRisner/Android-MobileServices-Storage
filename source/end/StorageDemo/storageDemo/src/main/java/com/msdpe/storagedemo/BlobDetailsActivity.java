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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Set;

public class BlobDetailsActivity extends Activity {
	private final String TAG = "BlobDetailsActivity";
	private String mContainerName;
	private String mBlobName;
	private int mBlobPosition;
	private StorageService mStorageService;
	private ImageView mImgBlobImage;;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blob_details);
		// Show the Up button in the action bar.
		setupActionBar();
		//Get access to the storage service
		StorageApplication myApp = (StorageApplication) getApplication();
		mStorageService = myApp.getStorageService();
		//Get data from the intent that launched this activity
		Intent launchIntent = getIntent();
		mContainerName = launchIntent.getStringExtra("ContainerName");
		mBlobName = launchIntent.getStringExtra("BlobName");
		mBlobPosition = launchIntent.getIntExtra("BlobPosition", -1);		
		
		//Get UI controls
		mImgBlobImage = (ImageView) findViewById(R.id.imgBlobImage);
		
		Button btnLoadWithSas = (Button) findViewById(R.id.btnLoadWithSas);
		btnLoadWithSas.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//Start the process of getting a SAS to read the blob
				//and then display it
				mStorageService.getBlobSas(mContainerName, mBlobName);
			}
		});

		Set<Entry<String, JsonElement>> set = mStorageService.getLoadedBlobObjects()[mBlobPosition].getAsJsonObject().entrySet();
		String url = "";
		String contentType = "";
		
		//Loop through each entry in the blob and handle it depending on it's key
		for (Entry<String, JsonElement> entry : set) {
			String key = entry.getKey();			
			if (key.equals("name")) {
				TextView lblBlobNameValue = (TextView) findViewById(R.id.lblBlobNameValue);
				lblBlobNameValue.setText(entry.getValue().getAsString());
			} else if (key.equals("url")) {
				url = entry.getValue().getAsString();
				TextView lblUrlValue = (TextView) findViewById(R.id.lblUrlValue);
				lblUrlValue.setText(url);
			} else if (key.equals("properties")) {
				//Pull the content-type out of the properties element
				JsonElement properties = entry.getValue();				
				contentType = properties.getAsJsonObject().getAsJsonPrimitive("Content-Type").getAsString();
				TextView lblContentTypeValue = (TextView) findViewById(R.id.lblContentTypeValue);
				lblContentTypeValue.setText(contentType);
			}					
		}
		//If it's an image, attempt to grab it
		if (contentType.equals("image/jpeg")) {			
			(new ImageFetcherTask(url)).execute();
		}
	}
	
	//This class specifically handles fetching an image from a URL and setting
	//the image view source on the screen
	private class ImageFetcherTask extends AsyncTask<Void, Void, Boolean> {
	    private String mUrl;
	    private Bitmap mBitmap;

	    public ImageFetcherTask(String url) {
	        mUrl = url;
	    }

	    @Override
	    protected Boolean doInBackground(Void... params) {
	        try {
			  mBitmap = BitmapFactory.decodeStream((InputStream)new URL(mUrl).getContent()); 					
	        } catch (Exception e) {
	        	Log.e(TAG, e.getMessage());
	        	return false;
	        }
	        return true;	    	
	    }

	    /***
	     * If the image was loaded successfully, set the image view
	     */
	    @Override
	    protected void onPostExecute(Boolean loaded) {
	        if (loaded) {
	        	mImgBlobImage.setImageBitmap(mBitmap);
	        }
	    }
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.blob_details, menu);
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
			//NavUtils.navigateUpFromSameTask(this);
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/***
	 * Handle registering for the broadcast action
	 */
	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("blob.loaded");
		registerReceiver(receiver, filter);
		super.onResume();
	}
	
	/***
	 * Handle unregistering for broadcast action
	 */
	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}
	
	/***
	 * This broadcast receiver handles things after the blob's SAS URL is fetched
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context context, android.content.Intent intent) {
			//Load the image using the SAS URL
			JsonObject blob = mStorageService.getLoadedBlob();
			String sasUrl = blob.getAsJsonPrimitive("sasUrl").toString();
			sasUrl = sasUrl.replace("\"", "");
			(new ImageFetcherTask(sasUrl)).execute();
		}
	};
}