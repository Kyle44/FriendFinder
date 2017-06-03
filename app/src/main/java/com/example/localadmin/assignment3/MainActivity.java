package com.example.localadmin.assignment3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {

    private DatabaseReference userInfoReference;

    private Button button;
    private EditText etUsername, etPassword;
    private boolean isDataReady = false;
    protected static UserInfo[] userInfoList;
    boolean isLocationFound = false;

    DataSnapshot data;

    LocationManager locationmanager;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        etUsername = (EditText) findViewById(R.id.editTextUsername);
        etPassword = (EditText) findViewById(R.id.editTextPassword);

        userInfoReference = FirebaseDatabase.getInstance().getReference().child("UserInfo");


        locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        userInfoReference.addValueEventListener(userInfoListener);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            return;
        }
        locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationmanager.removeUpdates(this);
    }

    ValueEventListener userInfoListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            data = dataSnapshot;
            userInfoList = setUserInfo(data);
            isDataReady = true;
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Toast.makeText(MainActivity.this, "Error retrieving User Data", Toast.LENGTH_SHORT).show();
            Log.w("Firebase", "loadPost:onCancelled", databaseError.toException());
        }
    };

    private void updateUserPosition(String username) {
        userInfoReference.child(username).child("lat").setValue(location.getLatitude());
        userInfoReference.child(username).child("lon").setValue(location.getLongitude());
    }

    // Write a new user to the database with their username, password, lat, lon
    // This will cause onDataChange to occur
    protected void addNewUserToDatabase(String username, String password) {
        Toast.makeText(this, "Registering new user...", Toast.LENGTH_SHORT).show();
        userInfoReference.push().setValue(username);
        userInfoReference.child(username).child("username").setValue(username);
        userInfoReference.child(username).child("password").setValue(password);
        userInfoReference.child(username).child("lat").setValue(location.getLatitude());
        userInfoReference.child(username).child("lon").setValue(location.getLongitude());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                if (!isDataReady) { // data hasn't been gathered yet
                    Toast.makeText(this, "Loading database... please wait...", Toast.LENGTH_LONG).show();
                    return;
                }
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "No Location Permission Granted", Toast.LENGTH_SHORT).show();
                    return;
                }
                location = locationmanager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(!username.equals("") && !password.equals("") && isLocationFound){
                    if(checkDatabaseForUser(username)){
                        UserInfo userInfo = getUserInfo(username);
                        if(password.equals(userInfo.getPassword())){ // go to next activity if password is correct
                            updateUserPosition(username);
                            startMapsActivity(username);
                        }
                        else{
                            Toast.makeText(this, "Incorrect Password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{ // Register new user
                        addNewUserToDatabase(username, password); // Add username, password to database
                        startMapsActivity(username);
                    }
                } // end outer if
        } // end switch
    } // end onClick

    // Uses DB info and the current user's username
    private void startMapsActivity(String username) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("username", username);
        Toast.makeText(this, "Finding your friends...", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    private UserInfo getUserInfo(String username) {
        for(UserInfo user: userInfoList){
            if(username.equals(user.getUsername())){
                return user;
            }
        }
        return null;
    }

    // Checks if the user is in the database
    private boolean checkDatabaseForUser(String username) {
        for(UserInfo user: userInfoList){
            if(user != null) {
                if (username.equals(user.getUsername())) {
                    return true;
                }
            }
        }
        return false;
    }

    // Returns a list of all of the user information
    private UserInfo[] setUserInfo(DataSnapshot dataSnapshot){
        Integer count = 0;
        UserInfo[] userInfoList = new UserInfo[(int) dataSnapshot.getChildrenCount()];
        for(DataSnapshot ds: dataSnapshot.getChildren()){
            if (!ds.hasChild("username")) {
                continue;
            }
            else{
                UserInfo userInfo = new UserInfo();
                userInfo.setUsername(ds.getValue(UserInfo.class).getUsername());
                userInfo.setPassword(ds.getValue(UserInfo.class).getPassword());
                userInfo.setLat(ds.getValue(UserInfo.class).getLat());
                userInfo.setLon(ds.getValue(UserInfo.class).getLon());

                userInfoList[count] = userInfo;
                count += 1;
            }

        }
        return userInfoList;
    } // end setUserInfo


    @Override
    public void onLocationChanged(Location location) {
        isLocationFound = true;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    protected static UserInfo[] getUserInfos(){
        return userInfoList;
    }
}