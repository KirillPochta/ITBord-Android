package com.example.fitboard;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;
import static com.example.fitboard.DBHelper.KEY_ID;
import static com.example.fitboard.DBHelper.KEY_NAME;
import static com.example.fitboard.DBHelper.KEY_PASS;
import static com.example.fitboard.DBHelper.TABLE_CONTACTS;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.favre.lib.crypto.bcrypt.BCrypt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity
{
    private ArrayAdapter<User> adapter;
    private List<User> users;
    Button btnSignIn, btnRegister;
    String admin_id = "000000";
    String admin_pass = "administrator";

    RelativeLayout root;

    DBHelper dbHelper;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnRegister = findViewById(R.id.btnRegister);
        root = findViewById(R.id.root_element);
        users = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, users); // JSON
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    showRegisterWindow();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignInWindow();
            }
        });
    }

    private void showSignInWindow(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Вход");
        dialog.setMessage("Заполните поля");
        LayoutInflater inflater = LayoutInflater.from(this);
        View sign_in_window = inflater.inflate(R.layout.sign_in_window, null);
        dialog.setView(sign_in_window);
        EditText id = sign_in_window.findViewById(R.id.idTicket);
        MaterialEditText pass = sign_in_window.findViewById(R.id.passField);
        dialog.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("Войти", new DialogInterface.OnClickListener() {
            @SuppressLint("Range")
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(id.getText().toString())  ){
                    Snackbar.make(root, "Идентификатор студенческого пустой", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(pass.getText().toString().length() < 4){
                    Snackbar.make(root, "Введите пароль длинною не менее 4 символов", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                String id_string = id.getText().toString();
                String pass_string = pass.getText().toString();

                if(id_string.equals(admin_id)  && pass_string.equals(admin_pass)) {
                    Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                    startActivity(intent);
                    finish();
                }

                Cursor cursor = db.rawQuery("select " + KEY_ID + ", " + KEY_NAME + ", " + KEY_PASS + " from " +
                        TABLE_CONTACTS +" where _id = ?", new String[]{id_string});
                if(cursor.getCount() == 0){
                    Snackbar.make(root, "Такого пользователя нет", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                String pass;
                cursor.moveToFirst();
                pass = cursor.getString(cursor.getColumnIndexOrThrow("pass"));
                String name_user;
                cursor.moveToFirst();
                name_user = cursor.getString(cursor.getColumnIndex("name"));
                cursor.close();

                if(pass.equals(pass_string))
                {
                    Intent intent = new Intent(MainActivity.this, UserActivity.class);
                    intent.putExtra("name", name_user);
                    startActivity(intent);
                    finish();
                }
                else{
                    Snackbar.make(root, "Не корректный пароль", Snackbar.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        dialog.show();
    }


    private void showRegisterWindow() throws ClassNotFoundException, SQLException {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Зарегистрироваться");
        dialog.setMessage("Введите все данные для регистрации");
        LayoutInflater inflater = LayoutInflater.from(this);
        View register_window = inflater.inflate(R.layout.register_window, null);
        dialog.setView(register_window);
        EditText id = register_window.findViewById(R.id.idTicket);
        MaterialEditText name = register_window.findViewById(R.id.nameField);
        MaterialEditText pass = register_window.findViewById(R.id.passField);
        dialog.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Продолжить", new DialogInterface.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(id.getText().toString())){
                    Snackbar.make(root, "Номер идентификатора не может быть пустым", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                else if(id.getText().toString() == admin_id){
                    Snackbar.make(root, "Выберите другой идентификатор", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(name.getText().toString())){
                    Snackbar.make(root, "Имя не может быть пустым", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(pass.getText().toString().length() < 4){
                    Snackbar.make(root, "Введите пароль длинною более 4 символов", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                String id_string = id.getText().toString();
                int id_int = Integer.parseInt(id_string);
                String name_string = name.getText().toString();
                String pass_string = pass.getText().toString();
                Cursor cursor = db.rawQuery("select " + KEY_ID +  " from " + TABLE_CONTACTS +" where _id = ? ", new String[]{id_string});
                if(cursor.getCount() != 0){
                    Snackbar.make(root, "Выберите другой идентификатор", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(DBHelper.KEY_ID, id_int);
                contentValues.put(DBHelper.KEY_PASS, pass_string);
                contentValues.put(DBHelper.KEY_NAME, name.getText().toString());
                db.insert(TABLE_CONTACTS,null, contentValues);

                Snackbar.make(root,"Успешная регистрация!", Snackbar.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}