package com.example.fitboard;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;

public class UserEvent extends AppCompatActivity {
    String NAME_USER;
    Cursor cursor;
    int id_user;
    ArrayList<String> listItem;
    ArrayList<String> name_event = new ArrayList<>();
    ArrayAdapter<String> adapter;

    ListView listView;
    DBHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_event);
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();
        Bundle arguments = getIntent().getExtras();
        NAME_USER = arguments.get("name").toString();
        listView = findViewById(R.id.listEntryUser);
        listItem = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + DBHelper.TABLE_CONTACTS + " where name = ?", new String[]{NAME_USER});
        cursor.moveToFirst();
        id_user = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_ID));
    }

    @Override
    protected void onResume() {
        super.onResume();

        String str_id_user = String.valueOf(id_user);
        cursor = db.rawQuery("select _id_event from " + DBHelper.TABLE_ENTRY + " where _id_user = ?", new String[]{str_id_user});
        cursor.moveToFirst();
        String id_event, nameEventId;
        Cursor cursor_event;

        try {
            Collections.addAll(name_event);
            adapter = new ArrayAdapter(this, R.layout.item_entry, R.id.ViewNameEntry, name_event);
            while (cursor.isAfterLast() == false) {
                id_event = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.KEY_EVENT_ENTRY));
                cursor_event = db.rawQuery("select name from " + DBHelper.TABLE_EVENT + " where _id = ?", new String[]{id_event});
                cursor_event.moveToFirst();

                nameEventId = cursor_event.getString(cursor_event.getColumnIndexOrThrow(DBHelper.KEY_NAME));

                adapter.add(nameEventId);
                adapter.notifyDataSetChanged();

                cursor.moveToNext();
            }
        }
        catch (SQLException ex){
            Toast.makeText(UserEvent.this, "Выполните повторный вход",Toast.LENGTH_SHORT).show();
        }
        listView.setAdapter(adapter);
    }

    public void onBackPressed() {
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("name", NAME_USER);
        startActivity(intent);
        finish();
    }

}
