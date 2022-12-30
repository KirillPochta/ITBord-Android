package com.example.fitboard;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import static com.example.fitboard.DBHelper.KEY_ID;
import static com.example.fitboard.DBHelper.TABLE_CONTACTS;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class AdminEvent extends AppCompatActivity {
    ListView list_event;
    SimpleCursorAdapter userAdapter;
    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event);
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();
        list_event = findViewById(R.id.listEventAdmin);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewDate();
        list_event.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int text = (int)list_event.getItemIdAtPosition(position);
                showDelete(text);
            }
        });
    }

    private void viewDate() {
        cursor = db.rawQuery("select * from " + DBHelper.TABLE_EVENT, null);
        String[] header = new String[]{DBHelper.KEY_NAME, DBHelper.KEY_PLACE, DBHelper.KEY_TIME, DBHelper.KEY_DATE, DBHelper.KEY_DESC, DBHelper.KEY_AUTH_EVENT};
        int[] to = new int[]{R.id.ViewName, R.id.ViewPlace, R.id.ViewTime, R.id.ViewDate, R.id.ViewDesc, R.id.ViewAuth};
        userAdapter = new SimpleCursorAdapter(this, R.layout.item, cursor, header, to, 0);
        list_event.setAdapter(userAdapter);
    }

    private void showDelete(int text) {
        String id = String.valueOf(text);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View edit_user = inflater.inflate(R.layout.delete_activity, null);
        dialog.setView(edit_user);
        dialog.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Cursor cursor_for_error = db.rawQuery("select " + KEY_ID +  " from " +
                        TABLE_CONTACTS +" where _id = ? ", new String[]{id});
                cursor_for_error.moveToFirst();
                if(cursor_for_error.getCount() == 0){
                    db.delete(DBHelper.TABLE_EVENT, "_id = " + id, null);
                    viewDate();
                    cursor_for_error.close();
                }
            }
        });

        dialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_admin, menu);
        return true;
    }

    public void onBackPressed() {
        startActivity(new Intent(this, AdminActivity.class));
    }

}
