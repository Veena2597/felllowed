package com.example.felllowed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {
    private ArrayList<String> notifArray;
    private ArrayAdapter adapter;

    private ListView notifList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        notifArray = new ArrayList<String>();
        notifList = findViewById(R.id.notifList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notifArray);
        notifList.setAdapter(adapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseReference = database.getReference("Users").child(currentUser);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot user_snapshot) {
                if (user_snapshot.child("notif_join").exists()) {
                    for (DataSnapshot events_snap : user_snapshot.child("notif_join").getChildren()) {
                        for (DataSnapshot requestor_snap : events_snap.getChildren()) {
                            notifArray.add(requestor_snap.getKey() +
                                    " has requested to join event");
                        }
                    }
                }
                if (user_snapshot.child("notif_req").exists()) {
                    for (DataSnapshot events_snap : user_snapshot.child("notif_req").getChildren()) {
                        for (DataSnapshot requestor_snap : events_snap.getChildren()) {
                            notifArray.add(requestor_snap.getKey() +
                                    " has requested for event");
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private ArrayList getListData(String data, ArrayList<EventItem> arrayList) {
        ArrayList<EventItem> results = arrayList;//new ArrayList<>();
        EventItem user1 = new EventItem();
        Gson gson = new Gson();
        //MyEventsActivity.Event event = gson.fromJson(data, MyEventsActivity.Event.class);
        Event event = gson.fromJson(data, Event.class);
        user1.setEventName(event.name);
        user1.setEventDate(event.date);
        user1.setEventTime(event.time_s);
        user1.setEventDes(event.des);
        user1.setUserName(event.user);
        results.add(user1);

        return results;
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
