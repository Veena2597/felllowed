package com.example.felllowed;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/* MainActivity handles:
* 1. Check if location service is accessible
* 2. Sign out of app
* 3. Send current location
* 4. List of all in-app activities:
*       a. Find Users
*       b. ToDo: Create events --create requests in this event or a separate activity
*       c. ToDo: Display friends
*       d. ToDo: Create list of shopping items
* */

public class MainActivity extends AppCompatActivity{
    final String TAG = "MA";
    private ArrayList<String> eventArray;
    private ArrayAdapter adapter;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest mLocationRequest;

    private ListView eventList;
    private static final int REQUEST_CODE = 101;
    private double Latitude = 0.0, Longitude = 0.0;
    public static boolean fromSetting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);

        //Get current location
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                        Log.e(TAG, String.valueOf(Latitude));

                        DatabaseReference mygeoRef = database.getReference("LocationGeo");
                        GeoFire geoFire = new GeoFire(mygeoRef);
                        geoFire.setLocation(currentuser, new GeoLocation(Latitude, Longitude));
                    }
                }
            }
        }, getMainLooper());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Toolbar myToolbar = findViewById(R.id.appToolbar);
        myToolbar.inflateMenu(R.menu.options);
        myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener () {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item); //ToDo: Why this?
            }
        });

        //Display list of in-app activities
        eventArray = new ArrayList<String>();
        eventList = findViewById(R.id.eventlist);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventArray);
        eventList.setAdapter(adapter);
        eventArray.add("Find Users");
        adapter.notifyDataSetChanged();

        eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int posID = position;
                Intent intent;
                switch (posID){
                    case 0:
                        intent = new Intent(MainActivity.this, FindUsersActivity.class);
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, AddEventActivity.class);
                        break;
                    default:
                        intent = new Intent(MainActivity.this, MainActivity.class);
                }
                startActivity(intent);
            }
        });
        return true;
    }

    //Handler function that determines what happens when an option is pressed in the Action Bar.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();//logout
                startActivity(new Intent(getApplicationContext(),login.class));
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        //Return home when back button pressed
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
}
