package com.example.felllowed;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MyEventsActivity extends AppCompatActivity {

    FirebaseDatabase database;
    String currentUser;

    //Logging the user event forum
    ListView lv;

    String data;
    ArrayList userList;
    int init_flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        init_flag = 0;

        lv = findViewById(R.id.myevents);

        DatabaseReference databaseReference = database.getReference("Events");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot events_parent) {
                if(userList != null)
                    userList.clear();
                for(DataSnapshot events_uid : events_parent.getChildren()) {
                    if (events_uid.getKey().equals(currentUser)) {
                        for (DataSnapshot events_num : events_uid.getChildren()) {
                            data = String.valueOf(events_num.getValue());
                            if (init_flag == 0) {
                                userList = getListData(data, new ArrayList<EventItem>());
                                init_flag = 1;
                            } else{
                                userList = getListData(data, userList);
                            }
                        }
                    }
                }

                if(init_flag == 0){
                    String samdata = "{\"date\":\"X\",\"des\":\"Please add events to view them here\",\"name\":\"Welcome\",\"time_e\":\"00:00\",\"time_s\":\"00:00\",\"user\":\"Fellowed\"}";
                    userList = getListData(samdata, new ArrayList<EventItem>());
                    init_flag = 1;
                }
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
                EventItem user = (EventItem) lv.getItemAtPosition(position);
            }
        });
    }

    private ArrayList getListData(String data, ArrayList<EventItem> arrayList) {
        ArrayList<EventItem> results = arrayList;//new ArrayList<>();
        EventItem user1 = new EventItem();
        Gson gson = new Gson();
        MyEventsActivity.Event event = gson.fromJson(data, MyEventsActivity.Event.class);
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
