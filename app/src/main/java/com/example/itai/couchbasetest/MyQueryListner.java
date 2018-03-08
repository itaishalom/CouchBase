package com.example.itai.couchbasetest;

import android.util.Log;

import com.couchbase.lite.DataSource;
import com.couchbase.lite.Expression;
import com.couchbase.lite.ListenerToken;
import com.couchbase.lite.Parameters;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.QueryChange;
import com.couchbase.lite.QueryChangeListener;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;

import java.util.HashMap;

import static com.example.itai.couchbasetest.DBHandler.TIME;
import static com.example.itai.couchbasetest.MainActivity.HASH_MAP;


/**
 * Created by Itai on 20/02/2018.
 */

public class MyQueryListner {
    private ListenerToken mToken;
    Query mQuery;
    long lastTime = 0L;
    myQueryListnerImpl myQueryListner;
    public static int counter = 0;

    public MyQueryListner() {
        mQuery = QueryBuilder.select(SelectResult.all()).from(DataSource.database(DBHandler.getInstance().mDb)).where(Expression.property(TIME).greaterThan(Expression.parameter(TIME)));
        Parameters params = new Parameters(mQuery.getParameters());
        params.setValue(TIME,lastTime);
        mQuery.setParameters(params);
        myQueryListner = new myQueryListnerImpl();
        mToken = mQuery.addChangeListener(myQueryListner);
    }

    public class myQueryListnerImpl implements QueryChangeListener {

        @Override
        public void changed(QueryChange change) {
            ResultSet rows = change.getResults();
            if(rows == null )
                return;
            Result row;
            boolean rowsExists = false;
            if ((row = rows.next()) != null) {
                counter++;
                rowsExists = true;
            }
            if (rowsExists) {
                lastTime = System.currentTimeMillis();
                Parameters params = new Parameters(mQuery.getParameters());
                params.setValue(TIME,lastTime);
                mQuery.setParameters(params);
                Log.w("MyQueryListner", "insert: notifications: "+ counter);
            }


        }
    }
}
