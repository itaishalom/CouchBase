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
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

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
import static com.example.itai.couchbasetest.MainActivity.isSafe;

/**
 * Proudly written by Itai on 25/01/2018.
 */

public class DBHandler {
    public Database mDb;
    private static DBHandler instance = null;
    public static final ReentrantLock lock = new ReentrantLock();

    public static DBHandler getInstance(Context context) {
        if (instance == null) {
            synchronized (DBHandler.class) {
                if (instance == null) {
                    instance = new DBHandler(context);
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
            MutableDocument newDoc = new MutableDocument();
            newDoc.setString(TABLE, tableName);

            JSONObject js = new JSONObject(cv.getAsString(HASH_MAP));

            HashMap map = new HashMap();
            map.put(NAME, js.optString(NAME));
            newDoc.setValue(HASH_MAP, map);
         /*   Set<Map.Entry<String, Object>> valsSet = cv.valueSet();

            for (Object val : valsSet) {
                Map.Entry me = (Map.Entry) val;
                newDoc.setValue(me.getKey().toString(), me.getValue());
            }*/
            lock.lock();
            mDb.save(newDoc);

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return 0;
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
        return 1;
    }

    public Cursor query(Expression where) {
        SelectResult[] cols = new SelectResult[2];
        cols[0] = SelectResult.expression(Expression.property(TABLE));
        cols[1] = SelectResult.expression(Expression.property(HASH_MAP));
        Query query = Query.select(cols).from(DataSource.database(mDb)).where(where);
        try {
            if (isSafe)
                lock.lock();
            ResultSet resultSet = query.execute();
            if (isSafe)
                lock.unlock();
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
}
