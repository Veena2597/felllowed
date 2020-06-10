package com.example.felllowed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.DefaultDatabaseErrorHandler;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ForumActivity extends NavActivity {

    final String TAG = "forum";
    FirebaseDatabase database;
    String currentUser;
    ArrayList userList;
    ArrayList creatorList;
    ArrayList userFriendsList;
    ArrayList userFriendsUidList;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest mLocationRequest;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //private ListView eventList;
    private static final int REQUEST_CODE = 101;
    private double Latitude = 0.0, Longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);
        onCreateDrawer();
        ACTIVITY_ID = FORUM_ID;

        sharedPreferences = getSharedPreferences("FELLOWED", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //Firebase initialization
        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        editor.putString("uid", currentUser);
        editor.commit();
        editor.apply();

        //Location updates
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkLocationEnabled(locationManager);
        checkPermission();

        fusedLocationProviderClient = new FusedLocationProviderClient(this);
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(10*1000);
        mLocationRequest.setFastestInterval(4*1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Latitude = location.getLatitude();
                        Longitude = location.getLongitude();

                        DatabaseReference mygeoRef = database.getReference("LocationGeo");
                        GeoFire geoFire = new GeoFire(mygeoRef);
                        geoFire.setLocation(currentUser, new GeoLocation(Latitude, Longitude));
                    }
                }
            }
        }, getMainLooper());

        //Logging the user event forum
        final ListView lv = findViewById(R.id.events);

        DatabaseReference profileReference = database.getReference("Profiles");
        profileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String dp = String.valueOf(dataSnapshot.child(currentUser).getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Logging the friends event forum
        final DatabaseReference friend_events = database.getReference("Users");
        friend_events.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot users_parent) {
                userList = new ArrayList();
                creatorList = new ArrayList();
                userFriendsList = new ArrayList();
                userFriendsUidList = new ArrayList();

                editor.putString("username",users_parent.child(currentUser).child("username").getValue().toString());
                editor.commit();
                editor.apply();
                setNavHeader(users_parent.child(currentUser).child("username").getValue().toString());
                for(DataSnapshot user_friends: users_parent.child(currentUser+"/friends").getChildren()){
                    userFriendsList.add(user_friends.getValue().toString());
                    userFriendsUidList.add(user_friends.getKey().toString());
                    for(DataSnapshot friends_event_type : users_parent.child(user_friends.getKey()).child("events").getChildren()){
                        for(DataSnapshot friend_events: friends_event_type.getChildren()){
                            Event event = new Event(
                                    friend_events.child("eventname").getValue().toString(),
                                    friend_events.child("date").getValue().toString(),
                                    friend_events.child("time_S").getValue().toString(),
                                    friend_events.child("time_E").getValue().toString(),
                                    friend_events.child("des").getValue().toString(),
                                    users_parent.child(friend_events.child("user").getValue().toString()).child("username").getValue().toString(),
                                    friend_events.child("category").getValue().toString(),
                                    friend_events.child("visibility").getValue().toString()
                            );
                            creatorList.add(users_parent.child(friend_events.child("user").getValue().toString()).getKey());
                            userList.add(event);
                        }
                    }
                }

                final ForumActivity.CustomListAdapter adapter = new ForumActivity.CustomListAdapter(ForumActivity.this, userList);
                lv.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                Gson gson = new Gson();
                String frnds = gson.toJson(userFriendsList);
                editor.putString("friendslist", frnds);
                String frndsuid = gson.toJson(userFriendsUidList);
                editor.putString("friendsuidlist", frndsuid);
                editor.commit();
                editor.apply();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Intent intent = new Intent(ForumActivity.this, RequestActivity.class);
                Event event= (Event) userList.get(position);
                intent.putExtra("event_name",event.name);
                intent.putExtra("event_des",event.des);
                intent.putExtra("event_date",event.date);
                intent.putExtra("event_time_s",event.time_s);
                //intent.putExtra("event_category",event.);
                intent.putExtra("event_visibility",event.visibility);
                intent.putExtra("event_creator",String.valueOf(creatorList.get(position)));
                startActivity(intent);
            }
        });
    }

    private void checkPermission(){
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void checkLocationEnabled(LocationManager locationManager){
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager. GPS_PROVIDER ) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager. NETWORK_PROVIDER ) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }
        if(!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(this)
                    .setTitle("Enable Location")  // GPS not found
                    .setMessage("For better results, turn on your device location service.") // Want to enable?
                    .setPositiveButton("LOCATION SETTINGS", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        }
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
            Event eventItem = (Event) getItem(position);

            TextView eventName = v.findViewById(R.id.list_name);
            TextView eventDate = v.findViewById(R.id.list_date);
            TextView eventTime = v.findViewById(R.id.list_time);
            TextView eventuser = v.findViewById(R.id.list_user);
            TextView eventdes = v.findViewById(R.id.list_des);

            ImageView profilepic = v.findViewById(R.id.profile_pic);
            profilepic.setImageResource(R.drawable.logo_1_launcher);

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




