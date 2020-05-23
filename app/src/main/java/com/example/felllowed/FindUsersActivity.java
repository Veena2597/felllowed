package com.example.felllowed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FindUsersActivity extends FragmentActivity implements OnMapReadyCallback {
    final String TAG = "FUA";
    public static boolean fromSetting = false;

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

        Log.e(TAG, "after location setting");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current User");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, (float) 15.5));
        googleMap.addMarker(markerOptions);

        Circle circle = googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(400)
                .strokeWidth(1)
                .strokeColor(0x000033CC)
                .fillColor(0x88CCDDFF));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /*DatabaseReference myRef = database.getReference("Location").child(currentuser);
        Map<String, Double> loc = new HashMap<>();
        loc.put("Latitude", currentLocation.getLatitude());
        loc.put("Longitude", currentLocation.getLongitude());
        myRef.setValue(loc);*/
        DatabaseReference myRef = database.getReference("LocationGeo");
        GeoFire geoFire = new GeoFire(myRef);
        geoFire.setLocation("firebase-hq", new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));

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
}