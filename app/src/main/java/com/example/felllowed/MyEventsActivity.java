package com.example.felllowed;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MyEventsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    FirebaseDatabase database;
    String currentUser;
    ListView lv;
    ArrayList userList;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;

    CustomListAdapter adapter;
    DatabaseReference removeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        lv = findViewById(R.id.myevents);

        //Navigation drawer related parameter
        toolbar = findViewById(R.id.appToolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        DatabaseReference databaseReference = database.getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot myEvents) {
                userList = new ArrayList();
                for(DataSnapshot event_type: myEvents.child(currentUser+"/events").getChildren()){
                    for(DataSnapshot my_events : event_type.getChildren()) {
                        Event event = new Event(
                                my_events.child("eventname").getValue().toString(),
                                my_events.child("date").getValue().toString(),
                                my_events.child("time_S").getValue().toString(),
                                my_events.child("time_E").getValue().toString(),
                                my_events.child("des").getValue().toString(),
                                myEvents.child(my_events.child("user").getValue().toString()).child("username").getValue().toString(),
                                my_events.child("category").getValue().toString(),
                                my_events.child("visibility").getValue().toString()
                        );
                        userList.add(event);
                    }
                }
                adapter = new MyEventsActivity.CustomListAdapter(MyEventsActivity.this, userList);
                lv.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, final int position, long id) {
                new AlertDialog.Builder(v.getContext())
                .setMessage("Do you want to delete this event?")
                .setTitle("Delete Event")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Event event = (Event) userList.get(position);
                        if(event.visibility.equals("Everyone")){
                            removeData = database.getReference("Users/"+currentUser+"/events/public");
                        }
                        else{
                            removeData = database.getReference("Users/"+currentUser+"/events/personal");
                        }
                        removeData.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot event_num : dataSnapshot.getChildren()){
                                    if(event_num.child("eventname").getValue().toString().equals(event.name)){
                                        removeData.child(event_num.getKey()).removeValue();
                                        userList.remove(position);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        //userList.remove(position);
                    }
                })
                .setNegativeButton("No",null)
                .show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent intent;
        switch (menuItem.getItemId()){
            case R.id.home:
                intent = new Intent(MyEventsActivity.this, ForumActivity.class);
                startActivity(intent);
                break;
            case R.id.find_friends:
                intent = new Intent(MyEventsActivity.this, FindUsersActivity.class);
                startActivity(intent);
                break;
            case R.id.notifcations:
                intent = new Intent(MyEventsActivity.this, NotificationActivity.class);
                startActivity(intent);
                break;
            case R.id.friends:
                intent = new Intent(MyEventsActivity.this, FriendsActivity.class);
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

    class CustomListAdapter extends BaseAdapter {
        private ArrayList<EventItem> listData;
        private LayoutInflater layoutInflater;
        public CustomListAdapter(Context aContext, ArrayList<EventItem> listData) {
            this.listData = listData;
            layoutInflater = LayoutInflater.from(aContext);
        }
        @Override
        public int getCount() {
            return listData.size();
        }
        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View v, ViewGroup vg) {

            if (v == null) {
                v = layoutInflater.inflate(R.layout.list_row, null);
            }
            //EventItem eventItem = (EventItem) getItem(position);
            Event eventItem = (Event) getItem(position);

            TextView eventName = v.findViewById(R.id.list_name);
            TextView eventDate = v.findViewById(R.id.list_date);
            TextView eventTime = v.findViewById(R.id.list_time);
            TextView eventuser = v.findViewById(R.id.list_user);
            final TextView eventdes = v.findViewById(R.id.list_des);

            eventName.setText(eventItem.getEventname());
            eventDate.setText(eventItem.getDate());
            eventTime.setText(eventItem.getTime_S());
            eventdes.setText(eventItem.getDes());
            eventuser.setText(eventItem.getUser());

            v.setTag(eventItem);
            return v;
        }
    }

    public class Event{
        private String name;
        private String date;
        private String time_s;
        private String time_e;
        private String des;
        private String user;
        private String category;
        private String visibility;

        public Event(String name, String date, String time_s, String time_e, String des, String user, String category, String visibility){
            this.name = name;
            this.date = date;
            this.time_s = time_s;
            this.time_e = time_e;
            this.des = des;
            this.user = user;
            this.category = category;
            this.visibility = visibility;
        }

        public String getEventname(){return name;}
        public String getDate(){return date;}
        public String getTime_S(){return time_s;}
        public String getTime_E(){return time_e;}
        public String getDes(){return des;}
        public String getUser(){return user;}
        public String getCategory(){return category;}
        public String getVisibility(){return visibility;}

    }
}
