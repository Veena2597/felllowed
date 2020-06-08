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

import java.util.ArrayList;

public class ForumActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;

    final String TAG = "forum";
    FirebaseDatabase database;
    String currentUser;
    String data;
    ArrayList userList;
    ArrayList eventList;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest mLocationRequest;

    //private ListView eventList;
    private static final int REQUEST_CODE = 101;
    private double Latitude = 0.0, Longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        //Veeeeennnnaaaaaa
        //Navigation drawer related parameter
        toolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        ImageView navImage = (ImageView) headerView.findViewById(R.id.imageView);
        //navImage.setImageIcon();
        final TextView navUsername = (TextView) headerView.findViewById(R.id.username_header);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        //Firebase initialization
        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

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

        //Logging the friends event forum
        DatabaseReference friend_events = database.getReference("Users");
        friend_events.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot users_parent) {
                userList = new ArrayList();

                for(DataSnapshot user_friends: users_parent.child(currentUser+"/friends").getChildren()){
                    for(DataSnapshot friend_events: users_parent.child(user_friends.getKey()+"/events/personal").getChildren()){

                        Event event = new Event();
                        event.date = friend_events.child("date").getValue().toString();
                        event.des = friend_events.child("des").getValue().toString();
                        event.name = friend_events.child("eventname").getValue().toString();
                        event.user = users_parent.child(friend_events.child("user").getValue().toString()).child("username").getValue().toString();
                        event.time_s = friend_events.child("time_S").getValue().toString();
                        Log.e(TAG, users_parent.child(friend_events.child("user").getValue().toString()).child("username").getValue().toString());

                        userList.add(event);
                    }
                    for(DataSnapshot friend_events: users_parent.child(user_friends.getKey()+"/events/public").getChildren()){
                        Event event = new Event();
                        event.date = friend_events.child("date").getValue().toString();
                        event.des = friend_events.child("des").getValue().toString();
                        event.name = friend_events.child("eventname").getValue().toString();
                        event.user = users_parent.child(friend_events.child("user").getValue().toString()).child("username").getValue().toString();
                        event.time_s = friend_events.child("time_S").getValue().toString();
                        Log.e(TAG, users_parent.child(friend_events.child("user").getValue().toString()).child("username").getValue().toString());

                        userList.add(event);
                    }
                }

                final ForumActivity.CustomListAdapter adapter = new ForumActivity.CustomListAdapter(ForumActivity.this, userList);
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
                Intent intent = new Intent(ForumActivity.this, RequestActivity.class);
                intent.putExtra("event", String.valueOf(eventList.get(position)));
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent intent;
        switch (menuItem.getItemId()){
            case R.id.find_friends:
                intent = new Intent(ForumActivity.this, FindUsersActivity.class);
                startActivity(intent);
                break;
            case R.id.friends:
                intent = new Intent(ForumActivity.this, FriendsActivity.class);
                startActivity(intent);
                break;
            case R.id.notifcations:
                intent = new Intent(ForumActivity.this, NotificationActivity.class);
                startActivity(intent);
                break;
            case R.id.myevents:
                intent = new Intent(ForumActivity.this, MyEventsActivity.class);
                startActivity(intent);
                break;
            case R.id.signout:
                intent = new Intent(ForumActivity.this, LoginActivity.class);
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

    @Override
    public void onBackPressed(){
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        toolbar.inflateMenu(R.menu.options);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener () {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(ForumActivity.this, AddEventActivity.class);
        startActivity(intent);
        return true;
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

            TextView name = v.findViewById(R.id.list_name);
            TextView eventDate = v.findViewById(R.id.list_date);
            TextView eventTime = v.findViewById(R.id.list_time);
            TextView eventuser = v.findViewById(R.id.list_user);
            TextView eventdes = v.findViewById(R.id.list_des);

            ImageView profilepic = v.findViewById(R.id.profile_pic);
            profilepic.setImageResource(R.drawable.logo_1_launcher);

            name.setText(eventItem.getname());
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




