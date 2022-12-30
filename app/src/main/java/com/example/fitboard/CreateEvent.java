package com.example.fitboard;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Calendar;


public class CreateEvent extends AppCompatActivity {
    Button btn_time;
    TextView currentDateTime, eventTime;
    DBHelper dbHelper;
    SQLiteDatabase db;
    String UserName;
    int tHour, tMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crete_event_user);
        Bundle arguments = getIntent().getExtras();
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();
        UserName = arguments.get("name").toString();
        currentDateTime = findViewById(R.id.currentDateTime);
        eventTime = findViewById(R.id.event_time);
        btn_time = findViewById(R.id.but_time);

        Button but = findViewById(R.id.but);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Writer();
                act1();
            }
        });

        btn_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        CreateEvent.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                tHour = hourOfDay;
                                tMinute = minute;
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(0,0,0, tHour, tMinute);
                                eventTime.setText(DateFormat.format("hh:mm aa", calendar));
                            }
                        },24,0,true
                );
                timePickerDialog.updateTime(tHour, tMinute);
                timePickerDialog.show();
            }
        });
    }
    public void act1()
    {
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("name", UserName);
        startActivity(intent);
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void Writer(){
        EditText name_event = findViewById(R.id.name_event);
        EditText place_event = findViewById(R.id.place_event);
        EditText desc = findViewById(R.id.description);

        ContentValues contentValues = new ContentValues();

        String name = name_event.getText().toString();
        String place = place_event.getText().toString();
        String time = eventTime.getText().toString();
        String date = currentDateTime.getText().toString();
        String desc_str = desc.getText().toString();

        contentValues.put(DBHelper.KEY_NAME, name);
        contentValues.put(DBHelper.KEY_PLACE, place);
        contentValues.put(DBHelper.KEY_TIME, time);
        contentValues.put(DBHelper.KEY_DATE, date);
        contentValues.put(DBHelper.KEY_DESC, desc_str);
        contentValues.put(DBHelper.KEY_AUTH_EVENT, UserName);
        db.insert(DBHelper.TABLE_EVENT, null, contentValues);
    }
}