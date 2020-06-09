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

public class NotificationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ArrayList<String> notifArray;
    private ArrayAdapter adapter;

    private ListView notifList;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;
    ForumActivity.UserData userdata;
    ArrayList reqOnlyList;
    int init_flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        init_flag = 0;

        reqOnlyList = new ArrayList();
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

        Intent mydata = getIntent();
        userdata = (ForumActivity.UserData) mydata.getSerializableExtra("userdata");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
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
                            reqOnlyList.add(notifArray.size()-1);
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
                        reqOnlyList.add(notifArray.size()-1);
                    }
                }

                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        notifList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(reqOnlyList.contains(position)){
                    Toast.makeText(NotificationActivity.this,"Reward : 10 points",Toast.LENGTH_SHORT).show();
                    final DatabaseReference rewardsData = database.getReference("Rewards").child(currentUser);
                    rewardsData.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(init_flag == 0) {
                                rewardsData.setValue(Integer.parseInt(dataSnapshot.getValue().toString()) + 10);
                                init_flag = 1;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent intent;
        switch (menuItem.getItemId()){
            case R.id.home:
                intent = new Intent(NotificationActivity.this, ForumActivity.class);
                intent.putExtra("userdata", userdata);
                startActivity(intent);
                break;
            case R.id.find_friends:
                intent = new Intent(NotificationActivity.this, FindUsersActivity.class);
                intent.putExtra("userdata", userdata);
                startActivity(intent);
                break;
            case R.id.myevents:
                intent = new Intent(NotificationActivity.this, MyEventsActivity.class);
                intent.putExtra("userdata", userdata);
                startActivity(intent);
                break;
            case R.id.friends:
                intent = new Intent(NotificationActivity.this, FriendsActivity.class);
                intent.putExtra("userdata", userdata);
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
