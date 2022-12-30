package com.example.fitboard;

import static com.example.fitboard.DBHelper.TABLE_ENTRY;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UserActivity extends AppCompatActivity {
    ListView listView;
    String UserName;

    RelativeLayout relativeLayout;
    Cursor cursor;
    SimpleCursorAdapter userAdapter;

    String date_now;
    Button btn_save_entry;
    EditText userFilter;
    int ID_USER;

    DBHelper dbHelper;
    SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        relativeLayout = findViewById(R.id.user_element);
        Bundle arguments = getIntent().getExtras();
        UserName = arguments.get("name").toString();
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();
        userFilter = findViewById(R.id.userFilter);

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        date_now = df.format(c);

        listView = findViewById(R.id.listEvent);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text;
                Cursor mycursor = (Cursor) listView.getItemAtPosition(position);
                text = mycursor.getString(0);
                ID_USER = mycursor.getInt(0);
                showEventEvent(text);
            }
        });
    }

    private void showEventEvent(String text){
        ContentValues contentValues = new ContentValues();
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        int id_user;
        int id_event;
        LayoutInflater inflater = LayoutInflater.from(this);
        View entry_event = inflater.inflate(R.layout.entry_event, null);
        dialog.setView(entry_event);
        btn_save_entry = entry_event.findViewById(R.id.btn_update);
        RadioButton yes_entry =entry_event.findViewById(R.id.yes_entry);
        RadioButton no_entry = entry_event.findViewById(R.id.no_entry);
        Cursor cursor = db.rawQuery("select * from " + DBHelper.TABLE_CONTACTS + " where name = ? ", new String[]{UserName});
        cursor.moveToFirst();
        id_user = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_ID));

        Cursor cursor1 = db.rawQuery("select * from " + DBHelper.TABLE_EVENT + " where _id = ? ", new String[]{text});
        cursor1.moveToFirst();
        id_event = cursor1.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_ID));

        btn_save_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int access_root = 0;
                if(no_entry.isChecked()){
                    access_root = 0;
                }
                else if(yes_entry.isChecked())
                    access_root = 1;
                 else {
                    Snackbar.make(relativeLayout,"Произошла ошибка", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(access_root == 1){
                    Cursor cursorCheck = db.rawQuery("select * from " + TABLE_ENTRY + " where _id_user = ? and _id_event = ?"
                            ,new String[]{String.valueOf(id_user), String.valueOf(id_event)});
                    if(cursorCheck == null || cursorCheck.getCount() == 0){
                        contentValues.put(DBHelper.KEY_USER_ENTRY, id_user);
                        contentValues.put(DBHelper.KEY_EVENT_ENTRY, id_event);
                        db.insert(TABLE_ENTRY, null, contentValues);

                        Toast.makeText(UserActivity.this, "Вы подписались под событием",Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(UserActivity.this, "",Toast.LENGTH_SHORT).show();
                }
                else {
                   db.delete(TABLE_ENTRY, "_id_user = ? and _id_event = ?"
                            , new String[]{String.valueOf(id_user),String.valueOf(id_event)});
                    Toast.makeText(UserActivity.this, "Вы отписались от события",Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }
    @Override
    public void onResume(){
        super.onResume();

        cursor = db.rawQuery("select * from " + DBHelper.TABLE_EVENT, null);

        dbHelper.delete_event(db, date_now);
        String [] header = new String[] {DBHelper.KEY_ID ,DBHelper.KEY_NAME, DBHelper.KEY_PLACE, DBHelper.KEY_TIME, DBHelper.KEY_DATE, DBHelper.KEY_DESC, DBHelper.KEY_AUTH_EVENT}; //  DBHelper.KEY_TIME, DBHelper.KEY_DATE, DBHelper.KEY_DESC
        int[] to = new int[] {R.id.ViewId ,R.id.ViewName, R.id.ViewPlace, R.id.ViewTime, R.id.ViewDate, R.id.ViewDesc, R.id.ViewAuth};

        userAdapter = new SimpleCursorAdapter(this, R.layout.item ,cursor, header, to, 0);

        listView.setAdapter(userAdapter);

        if(!userFilter.getText().toString().isEmpty())
            userAdapter.getFilter().filter(userFilter.getText().toString());

        userFilter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userAdapter.getFilter().filter(s.toString());
            }
        });

        try {
            userAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {

                    if (constraint == null || constraint.length() == 0) {

                        return db.rawQuery("select * from " + DBHelper.TABLE_EVENT, null);
                    }
                    else {
                        return db.rawQuery("select * from " + DBHelper.TABLE_EVENT + " where " +
                                DBHelper.KEY_NAME + " like ? or " + DBHelper.KEY_PLACE + " like ? or " + DBHelper.KEY_DATE +
                                " like ?", new String[]{"%" + constraint.toString() + "%", "%" + constraint.toString() + "%", "%" + constraint.toString() + "%"});
                    }
                }
            });

            listView.setAdapter(userAdapter);
        }
        catch (SQLException ex){
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cart:
                Cursor cursor = db.rawQuery("select root from " + DBHelper.TABLE_CONTACTS + " where name = ? ", new String[]{UserName});
                cursor.moveToFirst();
                int r = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.KEY_ROOT));
                if( r == 1){
                    Intent intent = new Intent(UserActivity.this, CreateEvent.class);
                    intent.putExtra("name", UserName);
                    startActivity(intent);
                    return true;
                }
                else {
                    Toast.makeText(UserActivity.this, "У вас нету разрешения создавать ", Toast.LENGTH_LONG).show();
                    return false;
                }
            case R.id.action_cart_entry:
                Intent intent = new Intent(UserActivity.this, UserEvent.class);
                intent.putExtra("name", UserName);
                startActivity(intent);
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.up_menu, menu);
        return true;
    }
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
