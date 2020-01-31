package com.f19.locationexercise;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1;
    Location lastKnownLocation;

    Button btn_start, btn_stop;

    TextView latitude, longitude, accuracy, altitude, address;

    /*
     * The other way to access the location of the user with the FusedLocationProvider
     * add gradle goolgle implemenation
     * check the SDK Manager for Google Play Services
     */

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    @Override
    protected void onStart() {
        super.onStart();

        if (!checkPermission()) {
            requestPermission();
        } else {
            getLastLocation();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = findViewById(R.id.btn_update);
        btn_stop = findViewById(R.id.btn_stop);

        // Rosette: Assign the textviews to the following Rids
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        accuracy = findViewById(R.id.accuracy);
        altitude = findViewById(R.id.altitude);
        address = findViewById(R.id.address);

        // Initialize the fusedLocationProvideClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Initialize the location manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "onLocationChanged: " + location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        //Check permission instead doing something below (commented out)
        // check it via addting onstart


//        // Check if permission to access location is granted
//        // If not we are requesting for permission
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
//        } else {
//           // Commenting this out when using the fusedLocationProvider Client
//           // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//
            buildLocationRequest();
            buildLocationCallback();
//        }
//


        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                btn_start.setEnabled(!btn_start.isEnabled());
                btn_stop.setEnabled(!btn_stop.isEnabled());

            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                btn_start.setEnabled(!btn_start.isEnabled());
                btn_stop.setEnabled(!btn_stop.isEnabled());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

// Commenting this out when implementing the FusedLocationClientProvider
//        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            //if(ContextCompat.checkSelfPermission((this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {//&& ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    Activity#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for Activity#requestPermissions for more details.
//                return;
//            }
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//            //}
//        }

        //Implementing when using FusedLocationClientProvider
        if(grantResults.length <= 0) {
            Log.i(TAG, "onRequestPermissionsResult: " + "User interaction was cancelled");
        } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

            //getLocation();
            buildLocationCallback();
            buildLocationRequest();

            // This fixes the crash at first runtime but we are removing this when applying the fusedLocation
            //buildLocationCallback();
            //buildLocationRequest();
        } else {
            // If permission is denied
            showSnakBar(R.string.warning_txt, R.string.setting, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // build intent
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                }
            });

        }
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); //milli
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10); //meters

    }
    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //super.onLocationResult(locationResult);
                for (Location location: locationResult.getLocations()) {
                    // This will be called in setLocation
                    // location_textView.setText(String.valueOf(location.getLatitude()) + "/" + String.valueOf(location.getLongitude()));
                    setLocation(location);
                }
            }
        };
    }

    private boolean checkPermission() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        /*
         * Provide an additional rationale to the user ehen user has denied permission
         * */
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (shouldProvideRationale) {
            Log.i(TAG, "requestPermission: " + "Displaying the permission rationale");
            // Give user a chance to change his/her mind if the permission was previously denied
            // implementation 'com.google.android.material:material:1.0.0'

            //Provide a way so that user can grant permission

            showSnakBar(R.string.warning_txt, android.R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startLocationPermissionRequest();
                }
            });

        } else {
            startLocationPermissionRequest();
        }
    }

    private void startLocationPermissionRequest() {
        // Just request for location permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private void getLastLocation() {
        // We want to update our text view so we are adding a listener when it is completed
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    lastKnownLocation = task.getResult();
                    setLocation(lastKnownLocation);
                }
            }
        });
    }

    private void setLocation(Location location) {
        //location_textView.setText(String.valueOf(location.getLatitude()) + "/" + String.valueOf(location.getLongitude()));
        latitude.setText("Latitude\t:"+ String.valueOf(location.getLatitude()));
        longitude.setText("Longitude\t:" + String.valueOf(location.getLongitude()));
        altitude.setText("Altitude\t:" +String.valueOf(location.getAltitude()));
        accuracy.setText("Accuracy\t:"+ String.valueOf(location.getAccuracy()));


        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        //addresses;



        try {
            Log.i(TAG, "setLocation: " + "Trying to retrieve address.");
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses.size() > 0) {
                Log.i(TAG, "setLocation: " + "Address size is greater than 0");
                String addr = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                //String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
                address.setText("Address\t:" + addr + " " + city + " " + country + " " + postalCode + " " + knownName);

            } else  {
                Log.i(TAG, "setLocation: " + "No address information retrieved.");
            }
        } catch (IOException e) {
            Log.i(TAG, "setLocation: " + "Geocoder getLocation exception.");
            e.printStackTrace();
        }




    }
    private void getLocation() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //super.onLocationResult(locationResult);
                for (Location location: locationResult.getLocations()) {
                    setLocation(location);
                }
            }

        };
    }

    private void showSnakBar(final int mainStringID, final int actionStringID, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content), getString(mainStringID), Snackbar.LENGTH_INDEFINITE).setAction(actionStringID,listener).show();
    }
}
