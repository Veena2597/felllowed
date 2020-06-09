package com.example.felllowed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestActivity extends AppCompatActivity{
    DatabaseReference databaseReference;
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

        Button joinButton = findViewById(R.id.joinBtn);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RequestActivity.this,"JOIN : pending approval",Toast.LENGTH_SHORT).show();
                if(eventVisibility.equals("Everyone")){
                    databaseReference = database.getReference("Users/"+eventCreator+"/events/public");
                }
                else{
                    databaseReference = database.getReference("Users/"+eventCreator+"/events/personal");
                }
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot event_num : dataSnapshot.getChildren()){
                            if(event_num.child("eventname").getValue().toString().equals(eventName)){
                                databaseReference.child(event_num.getKey()).child("join").child(currentUser).setValue(0);
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

        Button reqButton = findViewById(R.id.reqBtn);
        reqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RequestActivity.this,"REQUEST : pending approval",Toast.LENGTH_SHORT).show();
                final EditText itemText = findViewById(R.id.reqTxt);
                if(eventVisibility.equals("Everyone")){
                    databaseReference = database.getReference("Users/"+eventCreator+"/events/public");
                }
                else{
                    databaseReference = database.getReference("Users/"+eventCreator+"/events/personal");
                }
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot event_num : dataSnapshot.getChildren()){
                            if(event_num.child("eventname").getValue().toString().equals(eventName)){
                                databaseReference.child(event_num.getKey()).child("req").child(currentUser).setValue(itemText.getText().toString());
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
    }
}
