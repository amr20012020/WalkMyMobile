package com.example.walkmymobile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.Places;

public class MainActivity extends AppCompatActivity implements FetchAddressTask.OnTaskCompleted{
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_PICK_PLACE = 2;
    private static final String TRACKING_LOCATION_KEY = "tracking_location";


    private Button mLocationButton;
    private Button mPlacePickerButton;
    private TextView mLocationTextView;
    private ImageView mAndroidImageView;

    private boolean mTrackingLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mClient;

    private LocationCallback mLocationCallBack;


    private AnimatorSet mRotateAnim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.mipmap.ic_launcher);
        actionBar.setTitle("Walk My Mobile");
        actionBar.setDisplayUseLogoEnabled(true);


        mLocationButton = (Button) findViewById(R.id.btn_location);
        mLocationTextView = (TextView) findViewById(R.id.textView_location);
        mAndroidImageView = (ImageView) findViewById(R.id.imageview_android);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);





        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(TRACKING_LOCATION_KEY);
        }


        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mTrackingLocation) {
                    startTrackingLocation();
                } else {
                    stopTrackingLocation();
                }
            }
        });

        mLocationCallBack = new LocationCallback(){
            public void onLocationResult(LocationResult locationResult) {
                if (mTrackingLocation) {
                    new FetchAddressTask(MainActivity.this, MainActivity.this).execute(locationResult.getLastLocation());
                }
            }
        };
    }


    private void stopTrackingLocation() {
        if(mTrackingLocation){
            mTrackingLocation = false;
            mLocationButton.setText(R.string.start_tracking_location);
            mLocationTextView.setText(R.string.textview_hint);
            mRotateAnim.end();
        }

    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION);
        }else {
            mTrackingLocation = true;
            mFusedLocationClient.requestLocationUpdates(getLocationRequest(),mLocationCallBack,null);

            mLocationTextView.setText(getString(R.string.address_text,getString(R.string.loading),System.currentTimeMillis()));
            mLocationButton.setText(R.string.stop_tracking_location);
            mRotateAnim.start();

        }
    }

    protected void onSaveInstanceState(Bundle outState){
        outState.putBoolean(TRACKING_LOCATION_KEY, mTrackingLocation);
        super.onSaveInstanceState(outState);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startTrackingLocation();
                }else {
                    Toast.makeText(this,R.string.location_permission_denied,Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }


    @Override
    public void onTaskCompleted(String results) {
        if(mTrackingLocation){
            mLocationTextView.setText(getString(R.string.address_text,results,System.currentTimeMillis()));
        }
    }

    protected void onPause(){
        if(mTrackingLocation){
            stopTrackingLocation();
            mTrackingLocation = true;
        }
        super.onPause();
    }

    protected void onResume(){
        if (mTrackingLocation){
            startTrackingLocation();
        }
        super.onResume();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.about){
            Intent intentabout = new Intent(MainActivity.this,MainActivity2.class);
            startActivity(intentabout);
            return true;
        }
        return true;
    }



}