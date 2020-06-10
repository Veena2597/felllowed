package com.example.felllowed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.DialogInterface;
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

public class NotificationActivity extends NavActivity {
    private ArrayList<String> notifArray;
    private ArrayAdapter adapter;
    ArrayList reqOnlyList;
    ArrayList join_req_uid;
    ArrayList type_flag_list;
    ArrayList event_num_list;
    int init_flag = 0;

    private ListView notifList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        onCreateDrawer();
        ACTIVITY_ID = NOTIFICATIONS_ID;

        init_flag = 0;
        reqOnlyList = new ArrayList();
        join_req_uid = new ArrayList();
        type_flag_list = new ArrayList();
        event_num_list = new ArrayList();

        notifArray = new ArrayList<String>();
        notifList = findViewById(R.id.notifList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notifArray);
        notifList.setAdapter(adapter);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DatabaseReference databaseReference = database.getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot events_snap) {
                for(DataSnapshot event_type : events_snap.child(currentUser+"/events").getChildren()){
                    for(DataSnapshot event_num : event_type.getChildren()){
                        for(DataSnapshot joins : event_num.child("join").getChildren()){
                            if(joins.getValue().toString().equals("0")){
                                notifArray.add(events_snap.child(joins.getKey()).child("username").getValue().toString() +
                                        " has asked to join event: "+
                                        event_num.child("eventname").getValue().toString());
                                join_req_uid.add(joins.getKey());
                                type_flag_list.add(event_type.getKey());
                                event_num_list.add(event_num.getKey());
                            }
                            else{
                                //notifArray.add("Invitation for event "+event_num.child("eventname").getValue().toString() + " accepted");
                            }
                        }
                        for(DataSnapshot joins : event_num.child("req").getChildren()){
                            Log.e("Not", String.valueOf(joins));
                            if(joins.child("flag").getValue().toString().equals("0")) {
                                notifArray.add(events_snap.child(joins.getKey()).child("username").getValue().toString() +
                                        " has requested for event: " +
                                        event_num.child("eventname").getValue().toString());
                                reqOnlyList.add(notifArray.size() - 1);
                                join_req_uid.add(joins.getKey());
                                type_flag_list.add(event_type.getKey());
                                event_num_list.add(event_num.getKey());
                            }
                            else{
                                //notifArray.add("Accepted invitation for event: "+event_num.child("eventname").getValue().toString());
                            }
                        }
                    }
                }

                for(DataSnapshot myfriends: events_snap.child(currentUser+"/friends").getChildren()){
                    for(DataSnapshot friend_event_type: events_snap.child(myfriends.getKey()).child("events").getChildren()){
                        for(DataSnapshot friend_events : friend_event_type.getChildren()){
                            Log.e("Notif3", String.valueOf(friend_events));
                            //for (DataSnapshot event_num : friend_events.getChildren()){
                                //Log.e("Notif4", String.valueOf(event_num));
                            try{
                                if(friend_events.child("join/"+currentUser).getValue().toString().equals("1")){
                                    notifArray.add("Accepted invitation for event: "+friend_events.child("eventname").getValue().toString());
                                }
                                if(friend_events.child("req/"+currentUser+"/flag").getValue().toString().equals("1")){
                                    notifArray.add("Accepted invitation for event: "+friend_events.child("eventname").getValue().toString());
                                }
                            }
                            catch(Exception e){}
                        }
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
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirm")
                        .setMessage("Accept invitation?")
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(reqOnlyList.contains(position)){
                                    if(type_flag_list.get(position).toString().equals("personal"))
                                        databaseReference.child(currentUser+"/events/personal/"+event_num_list.get(position)+
                                                "/req/"+join_req_uid.get(position)+"/flag").setValue(1);
                                    else
                                        databaseReference.child(currentUser+"/events/public/"+event_num_list.get(position)+
                                                "/req/"+join_req_uid.get(position)+"/flag").setValue(1);

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
                                else{
                                    if(type_flag_list.get(position).toString().equals("personal"))
                                        databaseReference.child(currentUser+"/events/personal/"+event_num_list.get(position)+
                                                "/join/"+join_req_uid.get(position)).setValue(1);
                                    else
                                        databaseReference.child(currentUser+"/events/public/"+event_num_list.get(position)+
                                                "/join/"+join_req_uid.get(position)).setValue(1);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Reject", null)
                        .show();
            }
        });
    }
}
