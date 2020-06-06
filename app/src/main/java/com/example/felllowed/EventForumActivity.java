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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

public class EventForumActivity extends AppCompatActivity {
    final String TAG = "EFA";
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String data;
    ArrayList userList;
    int init_flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_forum);

        final ListView lv = findViewById(R.id.events);
        DatabaseReference databaseReference = database.getReference("Events");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                for(DataSnapshot snap : dataSnapshot.getChildren())
                    if(snap.getKey().equals(currentUser))
                        for(DataSnapshot snap1 : snap.getChildren()) {
                            data = String.valueOf(snap1.getValue());
                            if(init_flag == 0) {
                                userList = getListData(data, new ArrayList<EventItem>());
                                init_flag = 1;
                            }
                            else
                                userList = getListData(data, userList);
                        }
                final CustomListAdapter adapter = new CustomListAdapter(EventForumActivity.this, userList);
                lv.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                DatabaseReference friend_events = database.getReference("Users");
                friend_events.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                        for (DataSnapshot snapshot : dataSnapshot1.getChildren()) {
                            for (DataSnapshot snap : snapshot.child("friends").getChildren()) {
                                String friends = snap.getKey();
                                if (friends.equals(currentUser)) {
                                    Log.e(TAG, snapshot.getKey());
                                    data = String.valueOf(dataSnapshot.child(snapshot.getKey()).getValue());
                                    if (data.equals("null")) {
                                    } else {
                                        userList = getListData(data, userList);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                EventItem user = (EventItem) lv.getItemAtPosition(position);
            }
        });
    }
    private ArrayList getListData(String data, ArrayList<EventItem> arrayList) {
        ArrayList<EventItem> results = arrayList;//new ArrayList<>();
        EventItem user1 = new EventItem();
        Gson gson = new Gson();
        Event event = gson.fromJson(data, Event.class);
        user1.setEventName(event.name);
        user1.setEventDate(event.date);
        user1.setEventTime(event.time_s);
        user1.setEventDes(event.des);
        user1.setUserName(event.user);
        results.add(user1);

        return results;
    }

    class CustomListAdapter extends BaseAdapter {
        private ArrayList<EventItem> listData;
        private LayoutInflater layoutInflater;
        public CustomListAdapter(Context aContext, ArrayList<EventItem> listData) {
            this.listData = listData;
            Log.e(TAG, String.valueOf(listData));
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
            EventItem eventItem = (EventItem) getItem(position);

            TextView eventName = v.findViewById(R.id.list_name);
            TextView eventDate = v.findViewById(R.id.list_date);
            TextView eventTime = v.findViewById(R.id.list_time);
            TextView eventuser = v.findViewById(R.id.list_user);
            final TextView eventdes = v.findViewById(R.id.list_des);

            /*eventName.setText(eventItem.getEventName());
            eventDate.setText(eventItem.getEventDate());
            eventTime.setText(eventItem.getEventTime());
            //eventdes.setText(eventItem.getEventDes());
            eventdes.setText("Hello. This is Ash. Im just testing\nHow about you? What you upto");
            eventdes.post(new Runnable() {
                @Override
                public void run() {
                    Log.e("Descount", String.valueOf(eventdes.getLineCount()));
                    Log.e("Desheight", String.valueOf(eventdes.getLineHeight()));
                    int height_in_pixels = eventdes.getLineCount() * eventdes.getLineHeight(); //approx height text
                    eventdes.setHeight(height_in_pixels);

                }
            });
            eventuser.setText("Ash");*/

            eventName.setText(eventItem.getEventName());
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
    }
}
