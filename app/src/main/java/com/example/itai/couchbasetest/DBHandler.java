package com.example.itai.couchbasetest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Dictionary;
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.couchbase.lite.internal.support.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.itai.couchbasetest.MainActivity.HASH_MAP;
import static com.example.itai.couchbasetest.MainActivity.NAME;
import static com.example.itai.couchbasetest.MainActivity.TABLE;

/**
 * Proudly written by Itai on 25/01/2018.
 */

public class DBHandler {
    public Database mDb;
    static MyQueryListner lisnter;
    private static DBHandler instance = null;
    public static final String TIME= "TIME";
    public static int Counter = 0;
    public static DBHandler getInstance(Context context) {
        if (instance == null) {
            synchronized (DBHandler.class) {
                if (instance == null) {
                    instance = new DBHandler(context);
                    lisnter = new MyQueryListner();

                }
            }
        }
        return instance;
    }

    public static DBHandler getInstance() {
        return instance;
    }

    private DBHandler(Context context) {

        DatabaseConfiguration configuration = new DatabaseConfiguration(context);
        try {
            mDb = new Database("testDB", configuration);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    long insert(String tableName, ContentValues cv) {
        try {
            JSONObject js = new JSONObject(cv.getAsString(HASH_MAP));
            MutableDocument newDoc = new MutableDocument();
            newDoc.setString(TABLE, tableName);



            HashMap map = new HashMap();
            map.put(NAME, js.optString(NAME));
            newDoc.setLong(TIME, System.currentTimeMillis());
            newDoc.setValue(HASH_MAP, map);
         /*   Set<Map.Entry<String, Object>> valsSet = cv.valueSet();

            for (Object val : valsSet) {
                Map.Entry me = (Map.Entry) val;
                newDoc.setValue(me.getKey().toString(), me.getValue());
            }*/

            mDb.save(newDoc);
            Counter++;
            android.util.Log.w("DBHANDLER", "insert: notifications: " + Counter);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return 0;
        } catch (JSONException e) {
            e.printStackTrace();

        }
        return 1;
    }

    public Cursor query(Expression where) {
        SelectResult[] cols = new SelectResult[2];
        cols[0] = SelectResult.expression(Expression.property(TABLE));
        cols[1] = SelectResult.expression(Expression.property(HASH_MAP));
        Query query = QueryBuilder.select(cols).from(DataSource.database(mDb)).where(where);
        try {

            ResultSet resultSet = query.execute();

            MatrixCursor cursor = new MatrixCursor(new String[]{TABLE});

            Bundle bundle = new Bundle();
            Result row;
            ArrayList<Map> allHashMaps = new ArrayList<>();

            while ((row = resultSet.next()) != null) {
                Object[] allCols = new Object[1];
                allCols[0] = row.getValue(TABLE);
                Dictionary dictionary = (Dictionary) row.getValue(HASH_MAP);
                HashMap map = new HashMap(dictionary.toMap());
                allHashMaps.add(map);
                cursor.addRow(allCols);
            }
            bundle.putSerializable(HASH_MAP, allHashMaps);
            cursor.setExtras(bundle);
            return cursor;

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return null;
    }


    int updateMap(ContentValues values) {
        Document doc;

        JSONObject js = null;
        try {
            js = new JSONObject(values.getAsString(HASH_MAP));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HashMap map = new HashMap();
        map.put(NAME, js.optString(NAME));
        doc = mDb.getDocument(map.get(NAME).toString());
        if (doc == null)
            return 0;
        map.put(TIME, System.currentTimeMillis());
        MutableDocument newDoc = doc.toMutable();

        newDoc.setValue(HASH_MAP, map);
        newDoc.setLong(TIME, System.currentTimeMillis());
        try {
            android.util.Log.d("update", "updateMap: "+js.optString(NAME));
            mDb.save(newDoc);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            Log.e("DBHANDLER", "DB is not responding");
            return 0;
        }
        return 1;
    }

}
