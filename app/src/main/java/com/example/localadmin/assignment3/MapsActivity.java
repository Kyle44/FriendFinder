package com.example.localadmin.assignment3;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.example.localadmin.assignment3.MainActivity.getUserInfos;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    UserInfo[] userInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userInfoList = getUserInfos();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        displayMapOfUsers();
    }

    // Display all users' locations on the map with markers, including the current user.
    // Then, add each user's username to the marker
    protected void displayMapOfUsers(){
        Bundle extras = getIntent().getExtras();
        String username = extras.getString("username");

        UserInfo currentUser = findUser(username);
        Float initialLat = currentUser.getLat();
        Float initialLon = currentUser.getLon();

        getFriends(initialLat, initialLon);
    } // end function

    // Find all your friends within a km and put them on the map
    private void getFriends(Float initialLat, Float initialLon) {
        for(UserInfo user: userInfoList){
            if(user != null) {
                if (isLocationWithinRange(initialLat, initialLon, user.getLat(), user.getLon())) {
                    LatLng friendLocation = new LatLng(user.getLat(), user.getLon());
                    mMap.addMarker(new MarkerOptions().position(friendLocation).title(user.getUsername()));
                }
            }
        } // end for
    }

    // Given a username, find the user in userInfoList
    private UserInfo findUser(String username) {
        for(UserInfo user: userInfoList) {
            if(user != null) {
                if (user.getUsername().equals(username)) {
                    LatLng friendLocation = new LatLng(user.getLat(), user.getLon());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(friendLocation)); // move to current user's position
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(25));
                    return user;
                }
            }
        }
        return null;
    }

    // Check if the initial (current user's) location is within a km of the friend's location
    private boolean isLocationWithinRange(Float initialLat, Float initialLon, Float userLat, Float userLon) {
        float dLat = (float) Math.toRadians(userLat - initialLat);
        float dLon = (float)Math.toRadians(userLon - initialLon);
        float a = (float)(Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(initialLat))
                * Math.cos(Math.toRadians(userLat)) * Math.sin(dLon / 2) * Math.sin(dLon / 2));
        float c = (float)(2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        float d = 6371 * c;
        d *= 1609.34; // meters

        return d <= 1000;
    }

}
