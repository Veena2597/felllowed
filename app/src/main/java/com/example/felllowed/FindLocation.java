package com.example.felllowed;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class FindLocation extends FragmentActivity implements OnMapReadyCallback {
    final String TAG = "FL";
    public static boolean fromSetting = false;

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationManager locationManager;
    Context context;
    Location var;

    private static final int REQUEST_CODE = 101;

    public FindLocation(LocationManager locationManager, Context context){
        this.locationManager = locationManager;
        this.context = context;
    }

    public void main() {
        //check of location is enabled
        checkLocationEnabled(locationManager);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        var = fetchLocation();
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Log.e("1", String.valueOf(currentLocation));
                }
                Log.e("2", String.valueOf(currentLocation));
            }
        });
        Log.e("3", String.valueOf(currentLocation));
        Log.e(TAG, String.valueOf(currentLocation));
        Log.e(TAG, String.valueOf(var));
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("FL","onMapReady");
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
            new AlertDialog.Builder(context)
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

    private Location fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return null;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Log.e("1", String.valueOf(currentLocation));
                }
                Log.e("2", String.valueOf(currentLocation));
            }
        });
        Log.e("3", String.valueOf(currentLocation));
        return currentLocation;
    }
}