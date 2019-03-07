package com.headspire.googlemapstest;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * MapActivity class contains the code for loading searching and get the current location of the client.
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    private GoogleMap mgoogleMap;
    private EditText address;
    private Address searchAddress;
    private static final float DEFAULT_ZOOM = 15f;
    private FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        address=findViewById(R.id.textlocation);
        //initializing the map
        initMap();
    }

    //override the enter key
    private void init()
    {
        address.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_SEARCH
                || actionId==EditorInfo.IME_ACTION_DONE
                || event.getAction()==KeyEvent.ACTION_DOWN
                || event.getAction()==KeyEvent.KEYCODE_ENTER)
                {
                    Log.e("tagg","success");
                    geoLocate();
                }
                return false;
            }
        });
    }
    //searching the string to get latitude and longitude and other information
    private void geoLocate()
    {
        String searchString=address.getText().toString();
        Geocoder geocoder=new Geocoder(MapActivity.this);
        List<Address> addresses=new ArrayList<>();
        try
        {
            addresses=geocoder.getFromLocationName(searchString,1);
            if(addresses.size()>0)
            {
                searchAddress=addresses.get(0);
                moveCamera(new LatLng(searchAddress.getLatitude(),searchAddress.getLongitude()),DEFAULT_ZOOM
                ,searchAddress.getAddressLine(0));
            }
        }
        catch (Exception e)
        {Log.e("tagg",e.getMessage());}
    }

    /**
     * initializing the google map
     */
    private void initMap() {
        getPermission();
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(MapActivity.this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mgoogleMap = googleMap;
        getCurrentLocation();
        if (!checkPermission()) {
            mgoogleMap.setMyLocationEnabled(true);
        }
        else
            getPermission();
        init();
    }

    /**
     * moveCamera will move the screen to searched location in the map
     * @param latLng object will have the longitude and latitude
     * @param zoom floating value for display map
     * @param title string that will displayed when the user click on the marker
     */
    private void moveCamera(LatLng latLng,float zoom,String title)
    {
        mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        if(!title.equals("your location")) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mgoogleMap.addMarker(markerOptions);
        }
    }

    /**
     * gives the current position of the user.
     */
    public void getCurrentLocation() {
        fusedLocationProviderClient = new FusedLocationProviderClient(this);
        LocationRequest mlocationRequest = new LocationRequest();
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if(!checkPermission())
        {
            fusedLocationProviderClient.requestLocationUpdates(mlocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    moveCamera(new LatLng(locationResult.getLastLocation().getLatitude()
                    ,locationResult.getLastLocation().getLongitude()),DEFAULT_ZOOM,"your location");
                }
            }, getMainLooper());
        }
        else
            getPermission();
    }

    /**
     * request for the required permissions
     */
    public void getPermission() {
            if(checkPermission())
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container,new PermissionFragment())
                            .addToBackStack(null)
                            .commit();
    }

    /**
     * check the required permission are given or not.
     * @return true if permission not given and false if permission are given
     */
    public boolean checkPermission()
    {
        if (ContextCompat.checkSelfPermission(getApplicationContext()
                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }
}
