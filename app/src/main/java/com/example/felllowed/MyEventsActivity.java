package com.example.felllowed;

import android.content.Context;
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

public class MyEventsActivity extends NavActivity{
    FirebaseDatabase database;
    String currentUser;
    ListView lv;
    ArrayList userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        onCreateDrawer();
        ACTIVITY_ID = MYEVENTS_ID;

        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        lv = findViewById(R.id.myevents);

        DatabaseReference databaseReference = database.getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot myEvents) {
                userList = new ArrayList();
                for(DataSnapshot my_events: myEvents.child(currentUser+"/events/personal").getChildren()){
                    Log.e("rer",my_events.getValue().toString());
                    Event event = new Event();
                    event.date = my_events.child("date").getValue().toString();
                    event.des = my_events.child("des").getValue().toString();
                    event.name = my_events.child("eventname").getValue().toString();
                    event.user = myEvents.child(my_events.child("user").getValue().toString()).child("username").getValue().toString();
                    event.time_s = my_events.child("time_S").getValue().toString();

                    userList.add(event);
                }
                for(DataSnapshot my_events: myEvents.child(currentUser+"/events/public").getChildren()){
                    Event event = new Event();
                    event.date = my_events.child("date").getValue().toString();
                    event.des = my_events.child("des").getValue().toString();
                    event.name = my_events.child("eventname").getValue().toString();
                    event.user = myEvents.child(my_events.child("user").getValue().toString()).child("username").getValue().toString();
                    event.time_s = my_events.child("time_S").getValue().toString();

                    userList.add(event);
                }
                if(true){}
                final MyEventsActivity.CustomListAdapter adapter = new MyEventsActivity.CustomListAdapter(MyEventsActivity.this, userList);
                lv.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                //Intent intent = new Intent(MyEventsActivity.);
            }
        });
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

            eventName.setText(eventItem.getname());
            eventDate.setText(eventItem.getEventDate());
            eventTime.setText(eventItem.getEventTime());
            eventdes.setText(eventItem.getEventDes());
            eventuser.setText(eventItem.getUserName());

            v.setTag(eventItem);
            return v;
        }
    }

    class Event{
        private String name;
        private String date;
        private String time_s;
        private String time_e;
        private String des;
        private String user;

        public String getname() {
            return name;
        }
        public String getEventDate() {
            return date;
        }
        public String getEventTime() {
            return time_s;
        }
        public String getEventDes() {
            return des;
        }
        public String getUserName(){
            return user;
        }
        public void setname(String name) {
            this.name = name;
        }

        public void setdate(String date) {
            this.date = date;
        }

        public void setdes(String des) {
            this.des = des;
        }

        public void settime_s(String time_s) {
            this.time_s = time_s;
        }

        public void setuser(String user) {
            this.user = user;
        }
    }
}
