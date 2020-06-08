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

import java.io.Serializable;
import java.util.ArrayList;

public class ForumActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Serializable {
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;

    final String TAG = "forum";
    FirebaseDatabase database;
    String currentUser;
    DataSnapshot events_parent;
    String data;
    ArrayList userList;
    ArrayList eventList;
    int init_flag = 0;
    UserData userData = new UserData();;

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
        userData.setUid(currentUser);

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

        //Saving dataSnapshot of Events
        final DatabaseReference events_Reference = database.getReference("Events");
        events_Reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                events_parent = dataSnapshot;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //Logging the friends event forum
        DatabaseReference friend_events = database.getReference("Users");
        friend_events.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot users_parent) {
                userList = new ArrayList();
                eventList = new ArrayList();
                String name = String.valueOf(users_parent.child(currentUser).child("username").getValue());
                userData.setUsername(name);
                navUsername.setText(name);
                for (DataSnapshot users_uid : users_parent.getChildren()) {
                    for (DataSnapshot users_frnds : users_uid.child("friends").getChildren()) {
                        if (users_frnds.getKey().equals(currentUser)) {
                            for (DataSnapshot events_frnds_num : events_parent.child(users_uid.getKey()).getChildren()){
                                eventList.add(events_frnds_num);
                                data = String.valueOf(events_frnds_num.getValue());
                                if (init_flag == 0) {
                                    userList = getListData(data, new ArrayList<EventItem>());
                                    init_flag = 1;
                                } else{
                                    userList = getListData(data, userList);
                                }
                            }
                        }
                    }
                }
                if(init_flag == 0){
                    String samdata = "{\"date\":\"X\",\"des\":\"Please add events to view them here\",\"name\":\"Welcome\",\"time_e\":\"00:00\",\"time_s\":\"00:00\",\"user\":\"Fellowed\"}";
                    userList = getListData(samdata, new ArrayList<EventItem>());
                    init_flag = 1;
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

    private ArrayList getListData(String data, ArrayList<EventItem> arrayList) {
        ArrayList<EventItem> results = arrayList;//new ArrayList<>();
        EventItem user1 = new EventItem();
        Gson gson = new Gson();
        ForumActivity.Event event = gson.fromJson(data, ForumActivity.Event.class);
        user1.setEventName(event.name);
        user1.setEventDate(event.date);
        user1.setEventTime(event.time_s);
        user1.setEventDes(event.des);
        user1.setUserName(event.user);
        results.add(user1);
        return results;
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
                //intent.putExtra("userdata", userData);
                startActivity(intent);
                finish();
                break;
            case R.id.friends:
                intent = new Intent(ForumActivity.this, FriendsActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.notifcations:
                intent = new Intent(ForumActivity.this, NotificationActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.myevents:
                intent = new Intent(ForumActivity.this, MyEventsActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.signout:
                intent = new Intent(ForumActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
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
        Log.e(TAG,"add event");
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
            EventItem eventItem = (EventItem) getItem(position);

            TextView eventName = v.findViewById(R.id.list_name);
            TextView eventDate = v.findViewById(R.id.list_date);
            TextView eventTime = v.findViewById(R.id.list_time);
            TextView eventuser = v.findViewById(R.id.list_user);
            TextView eventdes = v.findViewById(R.id.list_des);

            ImageView profilepic = v.findViewById(R.id.profile_pic);
            profilepic.setImageResource(R.drawable.logo_1_launcher);

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

    static class UserData{
        public String username;
        public String uid;
        public ArrayList friendslist;

        public ArrayList getFriendslist() {
            return friendslist;
        }

        public String getUid() {
            return uid;
        }

        public String getUsername() {
            return username;
        }

        public void setFriendslist(ArrayList friendslist) {
            this.friendslist = friendslist;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}




