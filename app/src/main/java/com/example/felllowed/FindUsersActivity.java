package com.example.felllowed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FindUsersActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    final String TAG = "FUA";
    public static boolean fromSetting = false;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    Dialog addFriend;
    Dialog addedFriend;

    private ArrayList<String> userArray;
    private ArrayList<String> uidArray;
    private ArrayAdapter adapter;
    private ListView userList;

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users);

        //check of location is enabled
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkLocationEnabled(locationManager);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();

        //Create new list of users
        userArray = new ArrayList<String>();
        uidArray = new ArrayList<String>();
        userList = findViewById(R.id.userlist);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userArray);
        userList.setAdapter(adapter);

        final DatabaseReference myfrndsRef = database.getReference("Users").child(currentuser).child("friends");
        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                addFriend = new Dialog(FindUsersActivity.this);
                addFriend.setContentView(R.layout.activity_custom_alert);

                addedFriend = new Dialog(FindUsersActivity.this);
                addedFriend.setContentView(R.layout.activity_custom_dialog);

                Button add = addFriend.findViewById(R.id.add_friend_button);
                TextView username = addFriend.findViewById(R.id.added_username);
                username.setText(userArray.get(position));

                TextView username_dialog = addedFriend.findViewById(R.id.added_username);
                username_dialog.setText(userArray.get(position));

                ImageView cancel = addFriend.findViewById(R.id.cancel_dialog);
                cancel.setImageDrawable(getDrawable(R.drawable.ic_findfriends));

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addFriend.dismiss();
                    }
                });

                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, Object> userUpdates = new HashMap<>();
                        userUpdates.put(uidArray.get(position), userArray.get(position));
                        myfrndsRef.updateChildren(userUpdates);

                        addFriend.dismiss();
                        addedFriend.show();

                        final Timer t = new Timer();
                        t.schedule(new TimerTask() {
                            public void run() {
                                addedFriend.dismiss(); // when the task active then close the dialog
                                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
                            }
                        }, 2000);
                    }
                });

                addFriend.show();
                /*
                new AlertDialog.Builder(FindUsersActivity.this)
                        .setTitle("Add Friend")
                        .setMessage("Want to add "+userArray.get(position)+" as friend?")
                        .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //update friends
                                Toast.makeText(FindUsersActivity.this, "Added "+userArray.get(position)+" as friend",Toast.LENGTH_SHORT).show();
                                //register.user.updateFriendsList(userArray.get(position));
                                Map<String, Object> userUpdates = new HashMap<>();
                                userUpdates.put(uidArray.get(position), userArray.get(position));
                                myfrndsRef.updateChildren(userUpdates);
                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .show(); */
            }
        });
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        //Set Marker
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current User").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, (float) 15));
        googleMap.addMarker(markerOptions);

        googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(400)
                .strokeWidth(1)
                .strokeColor(0x000033CC)
                .fillColor(0x88CCDDFF));


        DatabaseReference myRef = database.getReference("LocationGeo");
        GeoFire geoFire = new GeoFire(myRef);
        geoFire.setLocation(currentuser, new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));

        /*//Dummy
        //geoFire.setLocation("test0", new GeoLocation(34.417772, -119.859786));
        geoFire.setLocation("test1", new GeoLocation(34.421737, -119.859442));//Outside
        geoFire.setLocation("test2", new GeoLocation(34.418798, -119.855837));
        geoFire.setLocation("test3", new GeoLocation(34.417736, -119.856739));
        geoFire.setLocation("test4", new GeoLocation(34.414794, -119.858198));
        geoFire.setLocation("test5", new GeoLocation(34.415300, -119.860832));//Border
        geoFire.setLocation("test6", new GeoLocation(34.421342, -119.852796));//Outside
        geoFire.setLocation("test7", new GeoLocation(34.416802, -119.860064));
        geoFire.setLocation("test8", new GeoLocation(34.416901, -119.854561));
        geoFire.setLocation("test9", new GeoLocation(34.419122, -119.859077));*/

        // creates a new query around [currentLatitude, currentLongitude] with a radius of 0.25mil
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()), 0.405);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {
                //Get Username from database
                DatabaseReference myRef = database.getReference("Users").child(key).child("user");
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Get user and add marker in map
                        final String user = dataSnapshot.getValue(String.class);

                        LatLng latLng = new LatLng(location.latitude, location.longitude);
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(user);
                        googleMap.addMarker(markerOptions);

                        //Add user to list
                        if(user != null) {
                            userArray.add(user);
                            uidArray.add(key);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
            super.onResume();
            Log.e("onResume", "entered here");
            if(fromSetting == true){
                finish();
                fromSetting = false;
                startActivity(getIntent());
            }
    }

    private void checkLocationEnabled(LocationManager locationManager){
        final Context context = getApplicationContext();
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
                            fromSetting = true;
                        }
                    })
                    .setNegativeButton("CANCEL", null)
                    .show();
        }
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(FindUsersActivity.this);
                }
            }
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        return false;
    }
}