package com.example.felllowed;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddEventActivity extends AppCompatActivity {
    final String TAG = "AEA";
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private EditText event_name;
    private TextView event_date;
    private TextView event_start_t;
    private TextView event_end_t;
    private EditText event_des;
    private Spinner spinner;
    private Spinner visibility;
    private DatePickerDialog.OnDateSetListener onDateSetListener;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        toolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(toolbar);

        spinner = findViewById(R.id.category);

        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(this, R.array.catergory, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(catAdapter);
        //spinner.setPromptId(0);

        visibility = findViewById(R.id.visibility);
        ArrayAdapter<CharSequence> visAdapter = ArrayAdapter.createFromResource(this, R.array.visibility, android.R.layout.simple_spinner_item);
        visAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visibility.setAdapter(visAdapter);
        //visibility.setPromptId(0);

        event_name = findViewById(R.id.event_name);
        event_date = findViewById(R.id.event_date);
        setDate(event_date);

        final Calendar cal = Calendar.getInstance();
        final int hour = cal.get(Calendar.HOUR_OF_DAY);
        final int min = cal.get(Calendar.MINUTE);
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        final int day = cal.get(Calendar.DAY_OF_MONTH);

        event_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this, R.style.Theme_AppCompat_DayNight_Dialog_MinWidth, onDateSetListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                datePickerDialog.show();
            }
        });

        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                event_date.setText(String.valueOf(month+1)+"/"+String.valueOf(dayOfMonth)+'/'+String.valueOf(year));
            }
        };

        event_start_t = findViewById(R.id.event_start_t);
        event_start_t.setText(String.valueOf(hour)+":"+String.valueOf(min));
        event_start_t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        event_start_t.setText(String.valueOf(hourOfDay)+":"+String.valueOf(minute));
                    }
                },hour,min,false);
                        timePickerDialog.show();
            }
        });

        event_end_t = findViewById(R.id.event_end_t);
        event_end_t.setText(String.valueOf(hour+1)+":"+String.valueOf(min));
        event_end_t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int min = cal.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        event_end_t.setText(String.valueOf(hourOfDay)+":"+String.valueOf(minute));
                    }
                },hour+1,min,false);
                timePickerDialog.show();
            }
        });
        event_des = findViewById(R.id.event_des);

    }

    public void setDate (TextView view){

        Date today = Calendar.getInstance().getTime();//getting date
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");//formating according to my need
        String date = formatter.format(today);
        view.setText(date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolbar.inflateMenu(R.menu.save);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener () {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e(TAG,"save event");
        Event event = new Event();
        event.name = String.valueOf(event_name.getText());
        event.date = String.valueOf(event_date.getText());
        event.time_s = String.valueOf(event_start_t.getText());
        event.time_e = String.valueOf(event_end_t.getText());
        event.visibility = String.valueOf(visibility.getSelectedItem());
        event.category = String.valueOf(spinner.getSelectedItem());
        event.des = String.valueOf(event_des.getText());
        event.user = currentUser;

        Gson gson = new Gson();
        String json = gson.toJson(event);

        DatabaseReference databaseReference = database.getReference("Events").child(currentUser);
        databaseReference.setValue(json);
        startActivity(new Intent(getApplicationContext(),ForumActivity.class));
        finish();
        return true;
    }

    class Event{
        private String name;
        private String date;
        private String time_s;
        private String time_e;
        private String des;
        private String user;
        private String visibility;
        private String category;
    }
}
