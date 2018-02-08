package com.example.itai.couchbasetest;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
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
import java.util.Random;

import static com.example.itai.couchbasetest.DBHandler.lock;
import static com.example.itai.couchbasetest.MyContentProvider.INSERT_TEST;
import static com.example.itai.couchbasetest.MyContentProvider.PROVIDER;
import static com.example.itai.couchbasetest.MyContentProvider.QUERY_TEST;

public class MainActivity extends AppCompatActivity {

    public static final String HASH_MAP = "HashMap";
    public static final String TABLE = "table";
    public static final String NAME = "name";
    public static boolean isSafe;

    private void startTest() {
        for (int i = 0; i < 1000; i++) {
            Random r = new Random();
            char c = (char) (r.nextInt(26) + 'a');
            String name = String.valueOf(c);
            HashMap newHashMap = new HashMap();
            newHashMap.put("name", name);
            JSONObject js = new JSONObject();
            try {
                js.put("name", name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Uri uri = Uri.parse(("content://" + PROVIDER + "/" + INSERT_TEST + "/allMaps"));
            ContentValues cv = new ContentValues();
            cv.put(HASH_MAP, js.toString());
            getContentResolver().insert(uri, cv);

            if (i == 50) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < 1000; j++) {
                            Random r = new Random();
                            char c = (char) (r.nextInt(26) + 'a');
                            String name = String.valueOf(c);
                            Uri uri = Uri.parse(("content://" + PROVIDER + "/" + QUERY_TEST + "/allMaps/" + name));
                            getContentResolver().query(uri, null, null, null, null);
                        }
                    }
                }).start();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button but = (Button) findViewById(R.id.startTestButton);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSafe = true;
                Toast.makeText(MainActivity.this, "Good test begin.. Wait for the end", Toast.LENGTH_SHORT).show();
                startTest();
                Toast.makeText(MainActivity.this, "Good test ended", Toast.LENGTH_SHORT).show();
            }
        });

        Button butBad = (Button) findViewById(R.id.startTestButton_bad);
        butBad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSafe = false;
                Toast.makeText(MainActivity.this, "Good test begin.. Wait for the end", Toast.LENGTH_SHORT).show();
                startTest();
                Toast.makeText(MainActivity.this, "Good test ended", Toast.LENGTH_SHORT).show();
            }
        });
        Button test3 = (Button) findViewById(R.id.startTestButton3);
        test3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test3();
            }
        });

    }


    private void test3() {
        DBHandler dbHand = DBHandler.getInstance(this);
        MutableDocument newDoc = new MutableDocument();
        HashMap map = new HashMap();
        ArrayList<String> values = new ArrayList<>();
        values.add("first");
        values.add("second");
        map.put("name", "map");
        map.put("array", values);

        newDoc.setValue(HASH_MAP, map);
        try {
            lock.lock();
            dbHand.mDb.save(newDoc);

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
        Expression where1 = Expression.property(HASH_MAP+"."+"array").in("first");
        Expression where1_working = Expression.property(HASH_MAP+"."+"array[0]").in("first");
        Expression where2 = Expression.property("first").in(HASH_MAP+"."+"array");
        queryTest3(where1);
        queryTest3(where1_working);
        queryTest3(where2);
    }

    private void queryTest3(Expression where){

        Query query = Query.select(SelectResult.all()).from(DataSource.database(DBHandler.getInstance().mDb)).where(where);
        lock.lock();
        ResultSet resultSet = null;
        try {
            resultSet = query.execute();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        lock.unlock();
        if(resultSet !=null){
            Result row;
        while ((row = resultSet.next()) != null) {
            Dictionary dictionary = (Dictionary) row.getValue(HASH_MAP);
            HashMap map = new HashMap(dictionary.toMap());
            Log.d("MainActivity", "queryTest3: " + map.get("name"));
        }
    }}
}