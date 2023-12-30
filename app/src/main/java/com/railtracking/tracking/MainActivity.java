package com.railtracking.tracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;//this explains that the class came from the google api
import com.google.android.gms.location.LocationCallback;//this imports the location call back constructors to call the notification  of the fused client location request
import com.google.android.gms.location.LocationRequest;//with this we can request the current location
import com.google.android.gms.location.LocationResult;//it gives the lon and lat
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;//it helps for upadation of the currnt location

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int Default_update_Interval = 30;
    public static final int Fast_upadate_Interval = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;
    //This code is for app components
    TextView tv_lat, tv_lon, tv_altitude, tv_sensor, tv_updates, tv_address,tv_area,tv_locality;//these are all the refrence variables

    Switch sw_locationupdates, sw_gps;//the refrence variables.

    //variable to remember if we are tracking location or not.
    boolean updateon = false;
    //we are declaring the location request to conf the all setting related to the class.
    //we can also say that the location request is a datatype to find the location.
    LocationRequest locationRequest;
    //location callback is a constructor and the call back for receiving notificatons from the Fusedlocationclient.
    LocationCallback locationCallback;
    //Googles API for location services. the majority of the app functions using "FusedlocationProviderClient" class name.
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //give each ui variable a value
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        tv_area = findViewById(R.id.tv_area);
        tv_locality=findViewById(R.id.tv_locality);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);


        //set all properties of locationRequest
        //creating the obj
        locationRequest = new LocationRequest();
        //how often does the default location check occur?
        locationRequest.setInterval(1000 * Default_update_Interval);
        //how often does the location check occur when set to the most frequent update?
        locationRequest.setInterval(1000 * Fast_upadate_Interval);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    upadateUI(location);
                    // Update UI with location data
                    // ...
                }
            }
        };
        //the setOnclickListener means when click on the btn it will take our responses.



        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //this part of code is to explain whether the gps is on/of
                if (sw_gps.isChecked()) {
                    //most accurate -use gps
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("using Gps sensors");
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("using the cell tower + wifi");
                }
            }
        });
        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_locationupdates.isChecked()) {
                    //turn on location tracking
                    startLocationupdates();
                } else {
                    //turn off tracking
                    stopLocationUpadates();
                }
            }
        });

        updategps();
    }

    private void stopLocationUpadates() {
        tv_altitude.setText("Not tracking the location");
        tv_updates.setText("Location is not being tracked");
        tv_lat.setText("Not tracking the location ");
        tv_lon.setText("Not tracking the location ");
        tv_address.setText("Not tracking the location ");
        tv_sensor.setText("Not tracking the location ");
        tv_area.setText("Not tracking the location");
        tv_locality.setText("Not tracking the location");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationupdates() {
        tv_updates.setText("Location is being tracked");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    updategps();
                }
                else {
                    Toast.makeText(this,"this app requires permission to be granted in order to work properly",Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }



    private void updategps(){
        //get permissions from the users to track GPS
        //get the current location from the fused client
        //update the UI set all properties in their associate text view items
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            //user provided the permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //we got permissions.put the values of location.xxx into the UI components
                    upadateUI(location);

                }
            });
        }
        else{
            //permissions are not granted
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_FINE_LOCATION);

            }
        }
    }

    private void upadateUI(Location location) {
        //upadate all of the text view objects with a new location
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else{
            tv_altitude.setText("not available");
        }
        //the code to convert the lon & lat into the address.
        //the geocoder will help us to convert the following.
        Geocoder geocoder = new Geocoder(MainActivity.this);
        try{
            List<Address>addresses=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            tv_area.setText(addresses.get(0).getAdminArea());
            tv_locality.setText(addresses.get(0).getLocality());
            tv_address.setText(addresses.get(0).getAddressLine(0));
        } catch (IOException e) {
            tv_address.setText("unable to track area");
        }
    }

}
