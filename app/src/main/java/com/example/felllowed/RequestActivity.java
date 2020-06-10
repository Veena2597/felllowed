package com.example.felllowed;

import androidx.annotation.NonNull;


import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class RequestActivity extends NavActivity{
    DatabaseReference databaseReference;
    int join_status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        Intent intent = getIntent();

        final String eventName = intent.getStringExtra("event_name");
        final String eventDes = intent.getStringExtra("event_des");
        String eventDate = intent.getStringExtra("event_date");
        String eventTime_s = intent.getStringExtra("event_time_s");
        final String eventVisibility = intent.getStringExtra("event_visibility");
        final String eventCreator = intent.getStringExtra("event_creator");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Button requestbtn = findViewById(R.id.reqBtn);
        requestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventVisibility.equals("Everyone")) {
                    databaseReference = database.getReference("Users/" + eventCreator + "/events/public");
                } else {
                    databaseReference = database.getReference("Users/" + eventCreator + "/events/personal");
                }


                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot event_num : dataSnapshot.getChildren()) {
                                if (event_num.child("eventname").getValue().toString().equals(eventName)) {
                                    if(join_status == 1) {
                                        Toast.makeText(RequestActivity.this, "JOIN : pending approval", Toast.LENGTH_SHORT).show();
                                        databaseReference.child(event_num.getKey()).child("join/"+currentUser).setValue(0);
                                    }
                                    else if (join_status == 2) {
                                        EditText itemText = findViewById(R.id.reqTxt);
                                        databaseReference.child(event_num.getKey()).child("req/"+currentUser+"/flag").setValue(0);
                                        databaseReference.child(event_num.getKey()).child("req/"+currentUser+"/reqTxt").setValue(itemText.getText().toString());
                                        Toast.makeText(RequestActivity.this, "REQUEST : pending approval", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                finish();
            }
        });

        Button cancelbtn = findViewById(R.id.reqcancel);
        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RadioGroup radioGroup = findViewById(R.id.radiobuttons);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                if (checkedId == R.id.radio_join) {
                    join_status = 1;
                } else if (checkedId == R.id.radio_req) {
                    join_status = 2;
                }
            }
        });
    }
}
