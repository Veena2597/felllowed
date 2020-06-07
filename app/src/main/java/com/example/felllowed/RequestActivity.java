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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        Intent intent = getIntent();
        String event_str = intent.getStringExtra("event");

        Gson gson = new Gson();
        final String event_key = event_str.substring(21,22);
        Event event = gson.fromJson(event_str.substring(32,event_str.length()-2),Event.class);
        final String creator = event.user;

        Button joinButton = findViewById(R.id.joinBtn);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RequestActivity.this,"JOIN : pending approval",Toast.LENGTH_SHORT).show();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference databaseReference = database.getReference("Users").child(creator).child("notif_join").child(event_key);
                Map<String, Object> temp = new HashMap<>();
                temp.put(currentUser,0);
                databaseReference.setValue(temp);
                finish();
            }
        });

        Button reqButton = findViewById(R.id.reqBtn);
        reqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RequestActivity.this,"REQUEST : pending approval",Toast.LENGTH_SHORT).show();
                EditText itemText = findViewById(R.id.reqTxt);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference databaseReference = database.getReference("Users").child(creator).child("notif_req").child(event_key);
                Map<String, Object> temp = new HashMap<>();
                temp.put(currentUser,String.valueOf(itemText.getText()));
                databaseReference.setValue(temp);
                finish();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent intent;
        switch (menuItem.getItemId()){
            case R.id.find_friends:
                intent = new Intent(RequestActivity.this, FindUsersActivity.class);
                startActivity(intent);
                break;
            case R.id.friends:
                intent = new Intent(RequestActivity.this, FriendsActivity.class);
                startActivity(intent);
                break;
            case R.id.notifcations:
                intent = new Intent(RequestActivity.this, NotificationActivity.class);
                startActivity(intent);
                break;
            case R.id.myevents:
                intent = new Intent(RequestActivity.this, MyEventsActivity.class);
                startActivity(intent);
                break;
            default:
                //if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                //    drawerLayout.closeDrawer(GravityCompat.START);
                //}
                break;
        }
        return false;
    }

    class Event{
        String user;
    }
}
