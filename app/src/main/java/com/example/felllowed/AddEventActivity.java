package com.example.felllowed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class AddEventActivity extends AppCompatActivity {
    final String TAG = "AEA";
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText event_name = findViewById(R.id.event_name);
                EditText event_date = findViewById(R.id.event_date);
                EditText event_start_t = findViewById(R.id.event_start_t);
                EditText event_end_t = findViewById(R.id.event_end_t);
                EditText event_des = findViewById(R.id.event_des);

                Event event = new Event();
                event.name = String.valueOf(event_name.getText());
                event.date = String.valueOf(event_date.getText());
                event.time_s = String.valueOf(event_start_t.getText());
                event.time_e = String.valueOf(event_end_t.getText());
                event.des = String.valueOf(event_des.getText());
                event.user = currentUser;

                Gson gson = new Gson();
                String json = gson.toJson(event);

                DatabaseReference databaseReference = database.getReference("Events").child(currentUser);
                databaseReference.setValue(json);
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });
    }

    class Event{
        private String name;
        private String date;
        private String time_s;
        private String time_e;
        private String des;
        private String user;
    }
}
