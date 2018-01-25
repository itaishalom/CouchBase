package com.example.itai.couchbasetest;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

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

    }


}
