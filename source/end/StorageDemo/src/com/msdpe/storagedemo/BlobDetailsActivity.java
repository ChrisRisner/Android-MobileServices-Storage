package com.msdpe.storagedemo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class BlobDetailsActivity extends Activity {
	private final String TAG = "BlobDetailsActivity";
	private String mContainerName;
	private String mBlobName;
	private Context mContext;
	private int mBlobPosition;
	private StorageService mStorageService;
	private ImageView mImgBlobImage;;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blob_details);
		// Show the Up button in the action bar.
		setupActionBar();
		
		mContext = this;
		StorageApplication myApp = (StorageApplication) getApplication();
		mStorageService = myApp.getStorageService();
		
		Intent launchIntent = getIntent();
		mContainerName = launchIntent.getStringExtra("ContainerName");
		mBlobName = launchIntent.getStringExtra("BlobName");
		mBlobPosition = launchIntent.getIntExtra("BlobPosition", -1);
		
		mImgBlobImage = (ImageView) findViewById(R.id.imgBlobImage);
		
		Button btnLoadWithSas = (Button) findViewById(R.id.btnLoadWithSas);
		btnLoadWithSas.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mStorageService.getBlobSas(mContainerName, mBlobName);
			}
		});
		
		
		
		//mStorageService.getBlob(mContainerName, mBlobName);
		
		Set<Entry<String, JsonElement>> set = mStorageService.getLoadedBlobObjects()[mBlobPosition].getAsJsonObject().entrySet();
		String url = "";
		String contentType = "";
		
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
				JsonElement properties = entry.getValue();
				
				contentType = properties.getAsJsonObject().getAsJsonPrimitive("Content-Type").getAsString();
				TextView lblContentTypeValue = (TextView) findViewById(R.id.lblContentTypeValue);
				lblContentTypeValue.setText(contentType);
			}					
		}
		if (contentType.equals("image/jpeg")) {
			
			(new ImageFetcherTask(url)).execute();
		}
	}
	
	class ImageFetcherTask extends AsyncTask<Void, Void, Boolean> {
	    private String mUrl;
	    private Bitmap mBitmap;

	    public ImageFetcherTask(String url) {
	        mUrl = url;
	    }

	    @Override
	    protected Boolean doInBackground(Void... params) {
	         
	    	
	        try {
			  Log.w(TAG, "Url: " + mUrl);
			  mBitmap = BitmapFactory.decodeStream((InputStream)new URL(mUrl).getContent());
			  //mImgBlobImage.setImageBitmap(bitmap); 					
	        } catch (Exception e) {
	        	Log.e(TAG, e.getMessage());
	        	return false;
	        }
	        return true;
	    	
	    }

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

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("blob.loaded");
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
			
			JsonObject blob = mStorageService.getLoadedBlob();
			String sasUrl = blob.getAsJsonPrimitive("sasUrl").toString();
			sasUrl = sasUrl.replace("\"", "");
			(new ImageFetcherTask(sasUrl)).execute();

		}
	};
	
	
}
