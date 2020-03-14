package com.example.gareth.androidfinder;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.gareth.androidfinder.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public final int REQUEST_CODE_ASK_PERMISSIONS = 1001;
    private Marker usermarker;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private List<User> users;
    private Map<String,Marker> markers;
    public User currentuser;
    public Marker selectedmarker;
    public User selecteduser;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final String TAG = MapsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        this.usermarker = null;
        this.currentuser = new User("username","uid");

        markers = new HashMap<>();
        users = new ArrayList<User>();

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");

        // listener added for child events
        ref.addChildEventListener(new ChildEventListener() {

            //called when the maps activity is started and also if a new user
            //registers with the application and they are added into the json file
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                User newuser = dataSnapshot.getValue(User.class);

                if(mAuth.getCurrentUser().getUid().equals(newuser.getUid())){
                    currentuser = newuser;
                }
                else{
                    users.add(newuser);
                }

                redrawMarker(newuser);
            }


            //the on child changed listener is called when there is a change
            //to one of the child elements in users on firebase
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                User changeduser = dataSnapshot.getValue(User.class);
                //System.out.println("The updated username is: " + changedUser.toString());

                for(int i = 0;i < users.size();i++){

                    if(changeduser.getUid().equals(users.get(i).getUid())){
                        users.get(i).setLatitude(changeduser.getLatitude());
                        users.get(i).setLongitude(changeduser.getLongitude());
                    }
                }

                if(selectedmarker != null){
                    setMarkerDistance(selectedmarker);
                    redrawMarker(selecteduser);
                }

                redrawMarker(changeduser);

                if(mAuth.getCurrentUser().getUid().equals(changeduser.getUid())){
                    currentuser = changeduser;
                }

                int results[];

                results = getDistance();

                if(results[0] != -1){
                    if(results[1] < 10){
                        makeNotification(results);
                    }
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    @Override
    protected  void onStart() {
        super.onStart();

    }


    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //an on click listener for all other user when a click occurs
        //is added for when the google map is ready
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                selectedmarker = marker;

                for(int i = 0;i < users.size();i++){
                    if(users.get(i).getUid().equals(marker.getTag())){
                        selecteduser = users.get(i);
                        setMarkerDistance(marker);
                    }
                }

                return false;
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.i("handle new location", "Location services not working why!!!!!.");

        } else {
            handleNewLocation(location);
            Log.i("handle new location", "it passsed the else statement.");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }


        Log.i(TAG, "Location services connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("onConnectionFailed", "Location services connection failed with code " + connectionResult.getErrorCode());
        }

        Log.i("onConnectionFailed", "Location services connection failed with code ");
    }


    //called when current user changes location it takes in the new location as an argument
    //and sets the new lat and lang in the database and sets the screen to the users new position
    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        FirebaseUser user = mAuth.getCurrentUser();

        Log.d("current user ",user.getUid());

        DatabaseReference usersRef = database.getReference("users").child(user.getUid());

        Map<String, Object> userUpdates = new HashMap<String, Object>();

        userUpdates.put("latitude", currentLatitude);
        userUpdates.put("longitude", currentLongitude);

        usersRef.updateChildren(userUpdates);

        if(selectedmarker != null){
            setMarkerDistance(selectedmarker);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }


    //draws the markers of all users
    public void drawAllUserMarkers(){

        for(int i = 0;i < this.users.size();i++){
            MarkerOptions options = new MarkerOptions()
                    .position(new LatLng(this.users.get(i).getLatitude(), this.users.get(i).getLongitude()))
                    .title(this.users.get(i).getUsername());

            markers.put(this.users.get(i).getUid(),mMap.addMarker(options));
        }
    }

    //sets the users marker which has been entered as the argument to the new location and
    //then draws onto the map at a new location and updates the hashmap with the references
    //for the users and their marker
    public void redrawMarker(User u){

        if(u != null) {
            boolean check = true;

            MarkerOptions options;
            Marker marker;

            if(u.getUid().equals(this.currentuser.getUid())){
                options = new MarkerOptions()
                        .position(new LatLng(u.getLatitude(), u.getLongitude()))
                        .title(u.getUsername())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }else{
                options = new MarkerOptions()
                        .position(new LatLng(u.getLatitude(), u.getLongitude()))
                        .title(u.getUsername())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }

            Log.d("before for loop",u.toString());

            for (Map.Entry<String, Marker> entry : markers.entrySet()) {

                Log.d("during loop",entry.toString());

                if (entry.getKey().equals(u.getUid())) {
                    entry.getValue().remove();
                    entry.setValue(marker = mMap.addMarker(options));
                    marker.setTag(u.getUid());
                    check = false;
                }
            }

            if (check) {
                markers.put(u.getUid(),marker = mMap.addMarker(options));
                marker.setTag(u.getUid());
            }
        }
    }

    public int[] getDistance(){

        float results[] = {11};
        int metres[] = {-1,-1};

        for(int i = 0;i < users.size();i++){

            Location.distanceBetween(users.get(i).getLatitude(),users.get(i).getLongitude(),currentuser.getLatitude(),currentuser.getLongitude(),results);


            if(results[0] <= 10){

                metres[0] = i;
                metres[1] = (int)results[0];

                return metres;
            }
        }

        return metres;
    }

    public int getDistance(User u){

        float results[] = {11};
        int metres = -1;

        Location.distanceBetween(u.getLatitude(),u.getLongitude(),currentuser.getLatitude(),currentuser.getLongitude(),results);

        metres = (int)results[0];

        return metres;

    }

    public void setMarkerDistance(Marker m){

        int distance = getDistance(selecteduser);

        if(distance > 1000){
            m.setSnippet(Integer.toString(distance/1000)+"km away");
        }else
            m.setSnippet(Integer.toString(distance)+"m away");
    }

    public void makeNotification(int[] results){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.application_logo)
                        .setContentTitle("Distance alert")
                        .setContentText("You are "+results[1]+"m away from "+users.get(results[0]).getUsername());
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MapsActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MapsActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        int mId = 1001;
        mNotificationManager.notify(mId, mBuilder.build());
    }

}
