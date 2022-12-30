package com.example.fitboard;

import static com.example.fitboard.DBHelper.KEY_ID;
import static com.example.fitboard.DBHelper.TABLE_CONTACTS;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Carousel;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;

import at.favre.lib.crypto.bcrypt.BCrypt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    DBHelper dbHelper;
    SQLiteDatabase db;
    ListView list;
    Button btn_update;
    RelativeLayout relativeLayout;
    SimpleCursorAdapter userAdapter;
    EditText adminFilter;
    TextView text_retrofit;
    Cursor cursor;
    ArrayList<String> listItem;
    ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_layout);

        relativeLayout = findViewById(R.id.admin_element);
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();
        listItem = new ArrayList<>();
        list = findViewById(R.id.list_acc);
        adminFilter = findViewById(R.id.adminFilter);
        text_retrofit = findViewById(R.id.text_retrofit);

        viewData();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = list.getItemAtPosition(position).toString();
                Toast.makeText(AdminActivity.this, "" + text,Toast.LENGTH_LONG).show();
                showEditUsers(text);
            }
        });
    }

    private void showEditUsers(String text){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View edit_user = inflater.inflate(R.layout.edit_user, null);
        dialog.setView(edit_user);

        EditText old_id = edit_user.findViewById(R.id.edit_id);
        EditText old_name = edit_user.findViewById(R.id.edit_name);
        EditText old_pass = edit_user.findViewById(R.id.edit_pass);
        btn_update = edit_user.findViewById(R.id.btn_update);
        RadioButton yes_root_user = edit_user.findViewById(R.id.yes_root);
        RadioButton no_root_user = edit_user.findViewById(R.id.no_root);

        Cursor cursor_for_start = dbHelper.select(db, text );
        cursor_for_start.moveToFirst();
        int r = cursor_for_start.getInt(cursor_for_start.getColumnIndexOrThrow(DBHelper.KEY_ROOT));

         if(cursor_for_start.moveToFirst()){
            old_id.setText(cursor_for_start.getString(cursor_for_start.getColumnIndexOrThrow("_id")));
            old_name.setText(cursor_for_start.getString(cursor_for_start.getColumnIndexOrThrow("name")));
            old_pass.setText(cursor_for_start.getString(cursor_for_start.getColumnIndexOrThrow("pass")));
        }

        if(r == 0)
            no_root_user.setChecked(true);
        else
            yes_root_user.setChecked(true);

        cursor_for_start.close();
        dialog.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });


        dialog.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(dbHelper.delete(db, text) == 0){
                    Snackbar.make(relativeLayout, "Произошла ошибка, попробуйте снова", BaseTransientBottomBar.LENGTH_LONG ).show();
                }
                else{
                    Cursor cursor_for_error = db.rawQuery("select " + KEY_ID +  " from " +
                            TABLE_CONTACTS +" where _id = ? ", new String[]{text});
                    cursor_for_error.moveToFirst();
                    if(cursor_for_error.getCount() == 0)
                    {
                        Snackbar.make(relativeLayout, "Пользователь удален", BaseTransientBottomBar.LENGTH_LONG ).show();
                    }
                    else
                    {
                        Snackbar.make(relativeLayout, "Пользователь не может быть удален", BaseTransientBottomBar.LENGTH_LONG ).show();
                    }
                }

                viewData();
            }
        });
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_id = old_id.getText().toString();
                String new_pass = old_pass.getText().toString();
                String new_name = old_name.getText().toString();
                int access_root = 0;
                if(no_root_user.isChecked()){
                    access_root = 0;
                }
                else{
                    access_root = 1;
                }

                if(old_pass.getText().length() > 3){
                    if(dbHelper.update(db, new_id, new_name, new_pass, access_root) == 0){
                        Snackbar.make(relativeLayout,"Произошла ошибка", Snackbar.LENGTH_SHORT).show();
                    }
                    else{
                        Snackbar.make(relativeLayout,"Пользователь обнавлен",
                                Snackbar.LENGTH_SHORT).show();
                    }
                    viewData();
                }
               else{
                    viewData();
                }
            }
        });
        dialog.show();
    }

    @Override
    public void onResume(){
        super.onResume();
        cursor = db.rawQuery("select * from " + TABLE_CONTACTS, null);
        String [] header = new String[] {
                                        DBHelper.KEY_NAME, KEY_ID,
                                        DBHelper.KEY_PASS,
                                        DBHelper.KEY_ROOT};
        int[] to = new int[] {R.id.ViewNameUser, R.id.ViewId, R.id.ViewPass, R.id.ViewRoot};

        userAdapter = new SimpleCursorAdapter(this, R.layout.item_admin ,cursor, header, to, 0);

        list.setAdapter(userAdapter);

        if(!adminFilter.getText().toString().isEmpty())
            userAdapter.getFilter().filter(adminFilter.getText().toString());

        adminFilter.addTextChangedListener(new TextWatcher()
        {
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
                        return db.rawQuery("select * from " + TABLE_CONTACTS, null);
                    }
                    else {
                        return db.rawQuery("select * from " + TABLE_CONTACTS + " where " +
                                DBHelper.KEY_NAME + " like ? or " + KEY_ID + " like ? or " + DBHelper.KEY_ROOT +
                                " like ?", new String[]{"%" + constraint.toString() + "%", "%" + constraint.toString() + "%", "%" + constraint.toString() + "%"});
                    }
                }
            });
            list.setAdapter(userAdapter);
        }
        catch (SQLException ex){
        }

        listItem.clear();
        cursor = dbHelper.viewData();
        while (cursor.moveToNext()){
            listItem.add(cursor.getString(0));
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItem);
        list.setAdapter(adapter);
    }

    private void viewData() {
        listItem.clear();
        cursor = dbHelper.viewData();
        while (cursor.moveToNext()){
            listItem.add(cursor.getString(0));
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItem);
        list.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cart_admin:
                 Intent intent = new Intent(AdminActivity.this, AdminEvent.class);
                 startActivity(intent);
                 return true;
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_admin, menu);
        return true;
    }

    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
