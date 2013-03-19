package com.msdpe.storagedemo;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceJsonTable;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;

public class StorageService {
	private MobileServiceClient mClient;
	//private MobileServiceTable<PlayerRecord> mPlayerRecordsTable;
	private MobileServiceJsonTable mTableTables;
	private Context mContext;
	private final String TAG = "StorageService";
	private List<Map<String, String>> mTables;

	public StorageService(Context context) {
		mContext = context;
		
		try {
			mClient = new MobileServiceClient("https://storagedemo.azure-mobile.net/", "oZaSIwBYgHrBiCApdCVcatyDxHQRCT23", mContext);
			mTableTables = mClient.getTable("Tables");			
		} catch (MalformedURLException e) {
			Log.e(TAG, "There was an error creating the Mobile Service. Verify the URL");
		}
	}
	
	public List<Map<String, String>> getLoadedTables() {
		return this.mTables;
	}
	
	public void getTables() {
		mTableTables.where().execute(new TableJsonQueryCallback() {
			
			@Override
			public void onCompleted(JsonElement result, int count, Exception exception,
					ServiceFilterResponse response) {
				// TODO Auto-generated method stub				
				JsonArray results = result.getAsJsonArray();
				//String[] tables = new String[results.size()];
				
				mTables = new ArrayList<Map<String, String>>();
				
				for (int i = 0; i < results.size(); i ++) {
					JsonElement item = results.get(i);
					Map<String, String> map = new HashMap<String, String>();
					map.put("TableName", item.getAsJsonObject().getAsJsonPrimitive("TableName").getAsString());
					
					mTables.add(map);
					//tables[i] = item.getAsJsonObject().getAsJsonPrimitive("TableName").getAsString();
				}
				Intent broadcast = new Intent();
				broadcast.setAction("tables.loaded");
				mContext.sendBroadcast(broadcast);
			}
		});		
	}
}
