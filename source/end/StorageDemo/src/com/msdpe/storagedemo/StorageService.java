package com.msdpe.storagedemo;

import static com.microsoft.windowsazure.mobileservices.MobileServiceQueryOperations.val;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;
import android.widget.ArrayAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceJsonTable;
import com.microsoft.windowsazure.mobileservices.MobileServiceQuery;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableDeleteCallback;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;

public class StorageService {
	private MobileServiceClient mClient;
	//private MobileServiceTable<PlayerRecord> mPlayerRecordsTable;
	private MobileServiceJsonTable mTableTables;
	private MobileServiceJsonTable mTableTableRows;
	private Context mContext;
	private final String TAG = "StorageService";
	private List<Map<String, String>> mTables;
	private ArrayList<JsonElement> mTableRows;

	public StorageService(Context context) {
		mContext = context;
		
		try {
			mClient = new MobileServiceClient("https://storagedemo.azure-mobile.net/", "oZaSIwBYgHrBiCApdCVcatyDxHQRCT23", mContext);
			mTableTables = mClient.getTable("Tables");		
			mTableTableRows = mClient.getTable("TableRows");
		} catch (MalformedURLException e) {
			Log.e(TAG, "There was an error creating the Mobile Service. Verify the URL");
		}
	}
	
	public List<Map<String, String>> getLoadedTables() {
		return this.mTables;
	}
	
	public JsonElement[] getLoadedTableRows() {
		return this.mTableRows.toArray(new JsonElement[this.mTableRows.size()]);
	}
	
	public void getTables() {
		mTableTables.where().execute(new TableJsonQueryCallback() {
			
			@Override
			public void onCompleted(JsonElement result, int count, Exception exception,
					ServiceFilterResponse response) {
				if (exception != null) {
					Log.e(TAG, exception.getCause().getMessage());
					return;
				}
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
	
	public void addTable(String tableName) {
		JsonObject newTable = new JsonObject();
		newTable.addProperty("tableName", tableName);
		
		mTableTables.insert(newTable, new TableJsonOperationCallback() {			
			@Override
			public void onCompleted(JsonObject jsonObject, Exception exception,
					ServiceFilterResponse response) {
				if (exception != null) {
					Log.e(TAG, exception.getCause().getMessage());
					return;
				}
				getTables();
			}
		});
	}
	
	public void deleteTable(String tableName) {
		JsonObject table = new JsonObject();		
		table.addProperty("id", 0);
		
		List<Pair<String,String>> parameters = new ArrayList<Pair<String, String>>();
		parameters.add(new Pair<String, String>("tableName", tableName));
		
		mTableTables.delete(table, parameters, new TableDeleteCallback() {			
			@Override
			public void onCompleted(Exception exception, ServiceFilterResponse response) {
				if (exception != null) {
					Log.e(TAG, exception.getCause().getMessage());
					return;
				}
				getTables();
			}
		});
	}
	
	public void getTableRows(String tableName) {
		Log.w(TAG, "Tablename: " + tableName);
		
		
		mTableTableRows.execute(mTableTableRows.parameter("table", tableName), new TableJsonQueryCallback() {
		//mTableTableRows.where().field("table").eq(tableName).execute(new TableJsonQueryCallback() {
			
			@Override
			public void onCompleted(JsonElement result, int count, Exception exception,
					ServiceFilterResponse response) {
				if (exception != null) {
					Log.e(TAG, exception.getCause().getMessage());
					return;
				}
				JsonArray results = result.getAsJsonArray();				
				mTableRows = new ArrayList<JsonElement>();				
				for (int i = 0; i < results.size(); i ++) {
					JsonElement item = results.get(i);
					mTableRows.add(item);
				}

				Intent broadcast = new Intent();
				broadcast.setAction("tablerows.loaded");
				mContext.sendBroadcast(broadcast);
			}
		});		
	}
	
	public void deleteTableRow(final String tableName, String partitionKey, String rowKey) {
		JsonObject row = new JsonObject();		
		row.addProperty("id", 0);
		
		List<Pair<String,String>> parameters = new ArrayList<Pair<String, String>>();
		parameters.add(new Pair<String, String>("tableName", tableName));
		parameters.add(new Pair<String, String>("rowKey", rowKey));
		parameters.add(new Pair<String, String>("partitionKey", partitionKey));
		
		mTableTableRows.delete(row, parameters, new TableDeleteCallback() {			
			@Override
			public void onCompleted(Exception exception, ServiceFilterResponse response) {
				if (exception != null) {
					Log.e(TAG, exception.getCause().getMessage());
					return;
				}
				getTableRows(tableName);
			}
		});
	}
	
	public void addTableRow(final String tableName, List<Pair<String,String>> tableRowData) {
		JsonObject newRow = new JsonObject();
		for (Pair<String,String> pair : tableRowData) {
			newRow.addProperty(pair.first, pair.second);
		}
		
		List<Pair<String,String>> parameters = new ArrayList<Pair<String, String>>();
		parameters.add(new Pair<String, String>("table", tableName));
		
		mTableTableRows.insert(newRow, parameters, new TableJsonOperationCallback() {			
			@Override
			public void onCompleted(JsonObject jsonObject, Exception exception,
					ServiceFilterResponse response) {
				if (exception != null) {
					Log.e(TAG, exception.getCause().getMessage());
					return;
				}
				getTableRows(tableName);
			}
		});
	}
}
