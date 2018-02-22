package com.example.itai.couchbasetest;

import android.util.Log;

import com.couchbase.lite.DataSource;
import com.couchbase.lite.Expression;
import com.couchbase.lite.ListenerToken;
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

    public MyQueryListner() {
        mQuery = QueryBuilder.select(SelectResult.all()).from(DataSource.database(DBHandler.getInstance().mDb)).where(Expression.property(TIME).greaterThan(Expression.longValue(lastTime)));
        myQueryListner = new myQueryListnerImpl();
        mToken = mQuery.addChangeListener(myQueryListner);
    }

    public class myQueryListnerImpl implements QueryChangeListener {

        @Override
        public void changed(QueryChange change) {
            ResultSet rows = change.getResults();
            Result row;
            if ((row = rows.next()) != null) {
                lastTime = System.currentTimeMillis();
                mQuery.removeChangeListener(mToken);
                mQuery = QueryBuilder.select(SelectResult.all()).from(DataSource.database(DBHandler.getInstance().mDb)).where(Expression.property(TIME).greaterThan(Expression.longValue(lastTime)));
                myQueryListner = new myQueryListnerImpl();
                mToken = mQuery.addChangeListener(myQueryListner);
            }
        }
    }
}
