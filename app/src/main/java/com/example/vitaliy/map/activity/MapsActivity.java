package com.example.vitaliy.map.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.vitaliy.map.Message.SendMessage;
import com.example.vitaliy.map.R;
import com.example.vitaliy.map.model.Place;
import com.example.vitaliy.map.paths.PathActivity;
import com.example.vitaliy.map.rest.ApiClient;
import com.example.vitaliy.map.rest.ApiInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlGeometry;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;
import com.sa90.materialarcmenu.ArcMenu;
import com.sa90.materialarcmenu.StateChangeListener;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.vitaliy.map.R.layout.activity_maps;
import static com.example.vitaliy.map.R.xml.prefs;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final int REQUEST_LOCATION_CODE = 99;
    String location, name;
    View mapView;
    ArcMenu arcMenuAndroid;
    SearchView searchView;
    List<Marker> mRental = new ArrayList<>();
    List<Marker> mNextBike = new ArrayList<>();
    List<Marker> mSpots = new ArrayList<>();
    List<Marker> mParking = new ArrayList<>();
    List<Marker> mShopsRepair = new ArrayList<>();
    private List<Place> respondList;
    private KmlLayer layer;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_maps);

        //Rest
        CallRest();

        //Search Address
        onMapSearch();

        //Current location fab
        CurLocFab();

        //Fab Menu
        Menu();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void onMapSearch() {
        searchView = (SearchView) findViewById(R.id.simpleSearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Address myAdress;
                LatLng latlng;
                List<Address> addresses = null;
                MarkerOptions mo = new MarkerOptions();
                if (!s.equals("")) {
                    Geocoder geocoder = new Geocoder(getBaseContext());
                    try {
                        addresses = geocoder.getFromLocationName(s, 5);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < addresses.size(); i++) {
                        myAdress = addresses.get(i);
                        latlng = new LatLng(myAdress.getLatitude(), myAdress.getLongitude());
                        mo.position(latlng);
                        mo.title(s);
                        mMap.addMarker(mo);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                    }
                }
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void CurLocFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.myLocationButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws SecurityException {
                LocationManager locationManager =
                        (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                Location selfLocation = locationManager
                        .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                LatLng selfLoc = new LatLng(selfLocation.getLatitude(), selfLocation.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(selfLoc, 14);
                mMap.moveCamera(update);
            }
        });
    }

    private void CallRest() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Place>> call = apiService.CathcDetail(location);
        call.enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                respondList = response.body();
                MarkersFromJSON(respondList);
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                Log.d("Responce Fail", t.toString());
            }
        });
    }

    public void Menu() {
        arcMenuAndroid = (ArcMenu) findViewById(R.id.arcmenu_android_example_layout);
        final FloatingActionButton fabEmail = (FloatingActionButton) findViewById(R.id.fab_arc_menu_Email);
        final FloatingActionButton fabMessage = (FloatingActionButton) findViewById(R.id.fab_arc_menu_info);
        final FloatingActionButton fabMap = (FloatingActionButton) findViewById(R.id.fab_arc_menu_map);
        final FloatingActionButton fabsm = (FloatingActionButton) findViewById(R.id.fab_arc_menu_route);
        arcMenuAndroid.setStateChangeListener(new StateChangeListener() {

            @Override
            public void onMenuOpened() {
                View.OnClickListener handler = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.fab_arc_menu_Email:
                                SendEmail();
                                break;
                            case R.id.fab_arc_menu_map:
                                startActivity(new Intent(MapsActivity.this, PrefsActivity.class));
                                break;
                            case R.id.fab_arc_menu_route:
                                startActivity(new Intent(MapsActivity.this, PathActivity.class));
                                break;
                            case R.id.fab_arc_menu_info:
                                Intent intent = new Intent(MapsActivity.this, SendMessage.class);
                                intent.putStringArrayListExtra("NB", (ArrayList<String>) coords(mNextBike));
                                intent.putStringArrayListExtra("P", (ArrayList<String>) coords(mParking));
                                intent.putStringArrayListExtra("R", (ArrayList<String>) coords(mRental));
                                intent.putStringArrayListExtra("SH", (ArrayList<String>) coords(mShopsRepair));
                                intent.putStringArrayListExtra("S", (ArrayList<String>) coords(mSpots));
                                startActivity(intent);
                        }
                    }
                };
                fabEmail.setOnClickListener(handler);
                fabMap.setOnClickListener(handler);
                fabsm.setOnClickListener(handler);
                fabMessage.setOnClickListener(handler);

            }

            @Override
            public void onMenuClosed() {
                //TODO something when menu is closed
            }
        });
    }

    private List<String> coords(List<Marker> markers){
        List<String> latLngs = new ArrayList<>();
        for(Marker m : markers){
            latLngs.add(m.getPosition().toString());
        }
        return latLngs;
    }

    private void SendEmail() {
        Intent Email = new Intent(Intent.ACTION_SEND);
        Email.setType("text/email");
        Email.putExtra(Intent.EXTRA_EMAIL,
                new String[]{"vitaliyshevchyk336@gmail.com"});  //developer 's email
        Email.putExtra(Intent.EXTRA_SUBJECT,
                "Add your Subject"); // Email 's Subject
        Email.putExtra(Intent.EXTRA_TEXT, "Dear Vitaliy," + "");  //Email 's Greeting text
        startActivity(Intent.createChooser(Email, "Send Feedback:"));
    }

    @Override
    public void onResume() {
        super.onResume();

        PreferenceManager.setDefaultValues(this, prefs, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());

        VisibleOrNot(mSpots, "Spots");
        VisibleOrNot(mShopsRepair, "ShopAndRepair");
        VisibleOrNot(mNextBike, "NextBike");
        VisibleOrNot(mRental, "Rental");
        VisibleOrNot(mParking, "Parking");
    }

    private void VisibleOrNot(List<Marker> mSpots, String spots) {
        for (Marker m : mSpots) {
            m.setVisible(sharedPreferences.getBoolean(spots, false));
        }
    }

    // Markers from JSON
    private void MarkersFromJSON(List<Place> places) {
        String[] ls;
        Marker marker;

        for (int i = 0; i < places.size(); i++) {
            ls = places.get(i).getDetail().get(0).getLocation().split(", ");
            for (int j = 0; j < 2; j++) {

                marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(ls[0]), Double.parseDouble(ls[1])))
                        .title(places.get(i).getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop)));
                mShopsRepair.add(marker);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    //denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) throws ClassCastException {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setMapToolbarEnabled(false);
        }
        try {
            layer = new KmlLayer(mMap, R.raw.lviv_b, getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (KmlContainer c : layer.getContainers()) {
            for (KmlContainer c1 : c.getContainers()) {
                for (KmlPlacemark p : c1.getPlacemarks()) {
                    KmlGeometry g = p.getGeometry();
                    if (p.getProperty("name").equals("Rental")) {
                        mRental.add( createKMLMarker( g.getGeometryObject().toString(), "rental place", R.drawable.rental ));
                    } else if (p.getProperty("name").equals("NextBike")) {
                        mNextBike.add( createKMLMarker( g.getGeometryObject().toString(), "nextBike", R.drawable.nb ));
                    } else if (p.getProperty("name").equals("P")) {
                        mParking.add( createKMLMarker( g.getGeometryObject().toString(), null, R.drawable.bicycle ));
                    } else if (p.getProperty("name").equals("Bike Spot")) {
                        mSpots.add( createKMLMarker( g.getGeometryObject().toString(), "nextBike", R.drawable.star ));
                    }
                }
            }
        }
    }

    private Marker createKMLMarker(String s, String nextBike, int nb) {
      String [] ls;
        Marker marker;
        ls = s.substring(10, s.length()-1).split(",");
       marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(Double.parseDouble(ls[0]), Double.parseDouble(ls[1])))
                .title(nextBike)
                .icon(BitmapDescriptorFactory.fromResource(nb))
                .visible(false));
        return marker;
    }

    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("You here");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        currentLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        //stop location updates
        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, 0);
            }
            return false;
        }
        return true;

    }

}