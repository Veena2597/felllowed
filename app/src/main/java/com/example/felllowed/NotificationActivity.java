package com.example.felllowed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ArrayList<String> notifArray;
    private ArrayAdapter adapter;

    private ListView notifList;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    DataSnapshot eventDataSnap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notifArray = new ArrayList<String>();
        notifList = findViewById(R.id.notifList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notifArray);
        notifList.setAdapter(adapter);

        //Navigation drawer related parameter
        toolbar = findViewById(R.id.appToolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseReference = database.getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot events_snap) {
                for(DataSnapshot public_event : events_snap.child(currentUser+"/events").child("public").getChildren()){
                    for(DataSnapshot joins : public_event.child("join").getChildren()){
                        if(joins.getValue().toString().equals("0")){
                            notifArray.add(events_snap.child(joins.getKey()).child("username").getValue().toString() +
                            " has asked to join event: "+
                                    public_event.child("eventname").getValue().toString());
                        }
                    }
                    for(DataSnapshot joins : public_event.child("req").getChildren()){
                        //if(joins.getValue().toString().equals("0")){
                            notifArray.add(events_snap.child(joins.getKey()).child("username").getValue().toString() +
                                    " has requested for event: "+
                                    public_event.child("eventname").getValue().toString());
                        //}
                    }
                }
                for(DataSnapshot personal_event : events_snap.child(currentUser+"/events").child("personal").getChildren()){
                    for(DataSnapshot joins : personal_event.child("join").getChildren()){
                        if(joins.getValue().toString().equals("0")){
                            notifArray.add(events_snap.child(joins.getKey()).child("username").getValue().toString() +
                                    " has asked to join event: "+
                                    personal_event.child("eventname").getValue().toString());
                        }
                    }
                    for(DataSnapshot joins : personal_event.child("req").getChildren()){
                        notifArray.add(events_snap.child(joins.getKey()).child("username").getValue().toString() +
                                " has requested for event: "+
                                personal_event.child("eventname").getValue().toString());
                    }
                }

                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent intent;
        switch (menuItem.getItemId()){
            case R.id.home:
                intent = new Intent(NotificationActivity.this, ForumActivity.class);
                startActivity(intent);
                break;
            case R.id.find_friends:
                intent = new Intent(NotificationActivity.this, FindUsersActivity.class);
                startActivity(intent);
                break;
            case R.id.myevents:
                intent = new Intent(NotificationActivity.this, MyEventsActivity.class);
                startActivity(intent);
                break;
            case R.id.friends:
                intent = new Intent(NotificationActivity.this, FriendsActivity.class);
                startActivity(intent);
                break;
            default:
                if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                break;
        }
        return false;
    }

}
