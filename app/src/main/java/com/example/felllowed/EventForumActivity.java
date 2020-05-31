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

public class EventForumActivity extends AppCompatActivity {
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    final String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_forum);

        final ListView lv = findViewById(R.id.events);

        DatabaseReference databaseReference = database.getReference("Events").child(currentUser);
        databaseReference.addValueEventListener(new ValueEventListener() {

         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             data = String.valueOf(dataSnapshot.getValue());
             ArrayList userList = getListData(data);
             CustomListAdapter adapter = new CustomListAdapter(EventForumActivity.this, userList);
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
                EventItem user = (EventItem) lv.getItemAtPosition(position);
            }
        });
    }
    private ArrayList getListData(String data) {
        final ArrayList<EventItem> results = new ArrayList<>();
        final EventItem user1 = new EventItem();

        Gson gson = new Gson();
        Event event = gson.fromJson(data, Event.class);
        user1.setEventName(event.name);
        user1.setEventDate(event.date);
        user1.setEventTime(event.time_s);
        user1.setEventDes(event.des);
        results.add(user1);

        return results;
    }

    class CustomListAdapter extends BaseAdapter {
        private ArrayList<EventItem> listData;
        private LayoutInflater layoutInflater;
        public CustomListAdapter(Context aContext, ArrayList<EventItem> listData) {
            //super(aContext, 0, listData);
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

                EventItem eventItem = (EventItem) getItem(position);

                TextView eventName = v.findViewById(R.id.list_name);
                TextView eventDate = v.findViewById(R.id.list_date);
                TextView eventTime = v.findViewById(R.id.list_time);
                TextView eventuser = v.findViewById(R.id.list_user);
                TextView eventdes = v.findViewById(R.id.list_des);
                int height_in_pixels = eventdes.getLineCount() * eventName.getLineHeight(); //approx height text
                eventdes.setHeight(height_in_pixels);

                eventName.setText(eventItem.getEventName());
                eventDate.setText(eventItem.getEventDate());
                eventTime.setText(eventItem.getEventTime());
                eventdes.setText(eventItem.getEventDes());

                eventuser.setText("Ash");

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
