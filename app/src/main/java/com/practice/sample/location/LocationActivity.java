package com.practice.sample.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Circle;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap.OnCircleClickListener;

//추가필요
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LocationActivity extends AppCompatActivity {
    private static final int REQUEST_USED_PERMISSION = 200;

    private static final String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permissionToLocationAccepted = true;

        switch (requestCode){
            case REQUEST_USED_PERMISSION:

                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        permissionToLocationAccepted = false;
                        break;
                    }
                }

                break;
        }

        if (permissionToLocationAccepted == false){
            finish();
        } else {
            getMyLocation();
        }
    }

    private static final String MAP_BUNDLE_KEY = "MapBundleKey";
    private static final LatLng DEFAULT_LOCATION
            = new LatLng(37.56641923090, 126.9778741551);
    private static final int DEFAULT_ZOOM = 15;

    private static final long INTERVAL_TIME = 5000;
    private static final long FASTEST_INTERVAL_TIME = 2000;

    private GoogleMap map;
    private MapView mapView;
    private Location lastKnownLocation;
    private Circle circle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (String permission : needPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, needPermissions, REQUEST_USED_PERMISSION);
                break;
            }
        }

        setContentView(R.layout.activity_location);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_BUNDLE_KEY);
        }

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(new OnMapReadyCallback() {




            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                getMyLocation();


                boolean success = googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(getApplication(), R.raw.style_json));
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapBundle = outState.getBundle(MAP_BUNDLE_KEY);

        if (mapBundle == null) {
            mapBundle = new Bundle();
            outState.putBundle(MAP_BUNDLE_KEY, mapBundle);
        }

        mapView.onSaveInstanceState(mapBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        FusedLocationProviderClient fusedLocationProviderClient
                = new FusedLocationProviderClient(this);

        Task<Location> task = fusedLocationProviderClient.getLastLocation();

        task.addOnSuccessListener(new OnSuccessListener<Location>() {

            @Override
            public void onSuccess(Location location) {
                lastKnownLocation = location;

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude()),
                        DEFAULT_ZOOM));

                updateMyLocation();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {
                if (ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(LocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));

                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
            }
        });
    }

    private String GetTime(){
        //현재 시간 날짜
        long msec = System.currentTimeMillis();//ms로 받음
        Date date = new Date(msec);
        SimpleDateFormat custom_format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date_str = custom_format.format(date);

        return date_str;
    }

    //not yet
    private double GetRSquare(double lat_cur, double lng_cur){

        double lat_pre = lastKnownLocation.getLatitude();
        double lng_pre = lastKnownLocation.getLongitude();

        double r_square = (lat_pre - lat_cur)*(lat_pre - lat_cur) +
                (lng_pre - lng_cur)*(lng_pre - lng_cur);

        return r_square;
    }

    private void updateMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL_TIME);
        locationRequest.setFastestInterval(FASTEST_INTERVAL_TIME);

        FusedLocationProviderClient fusedLocationProviderClient
                = new FusedLocationProviderClient(this);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                new LocationCallback() {

                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);

                        Location location = locationResult.getLastLocation();

                        //move camera
                        //map.moveCamera(CameraUpdateFactory.newLatLng(
                        //        new LatLng(location.getLatitude(), location.getLongitude())));



                        double lat_d = location.getLatitude();
                        double lng_d = location.getLongitude();
                        double r_square = GetRSquare(lat_d, lng_d);

                        if(r_square > 5E-8){

                            String time = GetTime();
                            String accuracy = "" + location.getAccuracy();
                            String lat = "" + lat_d;
                            String lng = "" + lng_d;

                            Toast.makeText(LocationActivity.this, "정확도 : " + accuracy + "\n위치 : " + lat + ", " + lng
                                    + "\n시간 : " + time, Toast.LENGTH_LONG).show();
                            circle = map.addCircle(new CircleOptions()
                                    .center(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .radius(7)
                                    .strokeWidth(2)
                                    .strokeColor(Color.GREEN)
                                    .fillColor(Color.argb(145, 0, 255, 55))
                                    .clickable(true));

                            map.setOnCircleClickListener(new OnCircleClickListener() {

                                @Override
                                public void onCircleClick(Circle circle) {
                                    // Flip the r, g and b components of the circle's
                                    // stroke color.
                                    int strokeColor = circle.getStrokeColor() ^ 0x00ffffff;
                                    circle.setStrokeColor(strokeColor);
                                }
                            });

                            lastKnownLocation = location;
                        }
                        else{
                            Toast.makeText(LocationActivity.this, "R square : "+r_square, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, null);
    }
}
