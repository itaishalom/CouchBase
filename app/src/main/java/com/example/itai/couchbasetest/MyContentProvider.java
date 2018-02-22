package com.example.itai.couchbasetest;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.couchbase.lite.Expression;

import java.util.List;

import static com.example.itai.couchbasetest.MainActivity.HASH_MAP;
import static com.example.itai.couchbasetest.MainActivity.NAME;
import static com.example.itai.couchbasetest.MainActivity.TABLE;

/**
 * Proudly written by Itai on 25/01/2018.
 */


public class MyContentProvider extends ContentProvider {
    public static final int INSERT_NUM = 1;
    public static final int QUERY_NUM = 2;

    public static final String PROVIDER = "com.example.itai.couchbasetest";
    public static final String INSERT_TEST = "insert";
    public static final String QUERY_TEST = "query";
    static final UriMatcher mUriMatch;

    public MyContentProvider() {
    }

    static {
        mUriMatch = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatch.addURI(PROVIDER, INSERT_TEST + "/*", INSERT_NUM);
        mUriMatch.addURI(PROVIDER, QUERY_TEST + "/*/*", QUERY_NUM);
    }


    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        switch (mUriMatch.match(uri)) {
            case QUERY_NUM:
                DBHandler db = DBHandler.getInstance(getContext());
                List<String> list = uri.getPathSegments();
                String table = list.get(list.size() - 2);
                String name = list.get(list.size() - 1);
                Expression where = Expression.property(TABLE).equalTo(Expression.string(table).and(Expression.property(HASH_MAP + "." + NAME).equalTo(Expression.string(name))));
                Cursor answer = db.query(where);
                if (getContext() != null)
                    answer.setNotificationUri(getContext().getContentResolver(), uri);
                return answer;
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        switch (mUriMatch.match(uri)) {
            case INSERT_NUM:
                DBHandler db = DBHandler.getInstance(getContext());
                List<String> list = uri.getPathSegments();
                String table = list.get(list.size() - 1);
                long row = db.insert(table, values);
                if (row > 0)
                    return uri;
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (mUriMatch.match(uri)) {
            case INSERT_NUM:
                DBHandler db = DBHandler.getInstance(getContext());
                List<String> list = uri.getPathSegments();
                String table = list.get(list.size() - 1);
                long row = db.updateMap( values);
                if (row > 0)
                    return (int)row;
        }
        return -1;
    }
}
