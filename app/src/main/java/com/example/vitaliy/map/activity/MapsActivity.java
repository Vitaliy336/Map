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
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.vitaliy.map.R;
import com.example.vitaliy.map.model.Place;
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
    String location;
    String name;
    View mapView;
    ArcMenu arcMenuAndroid;
    SearchView searchView;
    List<String> rental = new ArrayList<>();
    List<String> nextBike = new ArrayList<>();
    List<String> parking = new ArrayList<>();
    List<Marker> mRental = new ArrayList<Marker>();
    List<Marker> mNextBike = new ArrayList<Marker>();
    List<Marker> mParking = new ArrayList<Marker>();
    List<Marker> mShopsRepair = new ArrayList<Marker>();
    private List<Place> respondList;
    private KmlLayer layer;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_maps);
        ListView drawerLayout;

        //Rest
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Place>> call = apiService.CathcDetail(location, name);
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

        //Search Address
        final SearchView searchView = (SearchView) findViewById(R.id.simpleSearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<Address> addresses = null;
                MarkerOptions mo = new MarkerOptions();
                if (!query.equals("")) {
                    Geocoder geocoder = new Geocoder(getBaseContext());
                    try {
                        addresses = geocoder.getFromLocationName(query, 5);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < addresses.size(); i++) {
                        Address myAdress = addresses.get(i);
                        LatLng latlng = new LatLng(myAdress.getLatitude(), myAdress.getLongitude());
                        mo.position(latlng);
                        mo.title(query);
                        mMap.addMarker(mo);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                    }
                }
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return false;
            }
        });


        //Current location fab
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

        //Fab Menu
        arcMenuAndroid = (ArcMenu) findViewById(R.id.arcmenu_android_example_layout);
        final FloatingActionButton fabEmail = (FloatingActionButton) findViewById(R.id.fab_arc_menu_Email);
        final FloatingActionButton fabMap = (FloatingActionButton) findViewById(R.id.fab_arc_menu_map);
        final FloatingActionButton fabInfo = (FloatingActionButton) findViewById(R.id.fab_arc_menu_info);
        arcMenuAndroid.setStateChangeListener(new StateChangeListener() {

            @Override
            public void onMenuOpened() {
                //TODO something when menu is opened
                fabEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent Email = new Intent(Intent.ACTION_SEND);
                        Email.setType("text/email");
                        Email.putExtra(Intent.EXTRA_EMAIL,
                                new String[]{"vitaliyshevchyk336@gmail.com"});  //developer 's email
                        Email.putExtra(Intent.EXTRA_SUBJECT,
                                "Add your Subject"); // Email 's Subject
                        Email.putExtra(Intent.EXTRA_TEXT, "Dear Vitaliy," + "");  //Email 's Greeting text
                        startActivity(Intent.createChooser(Email, "Send Feedback:"));
                    }
                });

                fabMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MapsActivity.this, PrefsActivity.class);
                        startActivity(intent);
                    }
                });

                fabInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }

            @Override
            public void onMenuClosed() {
                //TODO something when menu is closed
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        PreferenceManager.setDefaultValues(this, prefs, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());

        if (sharedPreferences.getBoolean("PS", false) == true) {
            for (Marker m : mParking) {
                m.setVisible(true);
            }
        } else {
            for (Marker m : mParking) {
                m.setVisible(false);
            }
        }
        if (sharedPreferences.getBoolean("NBS", false) == true) {
            for (Marker m : mNextBike) {
                m.setVisible(true);
            }
        } else {
            for (Marker m : mNextBike) {
                m.setVisible(false);
            }
        }
        if (sharedPreferences.getBoolean("RP", false) == true) {
            for (Marker m : mRental) {
                m.setVisible(true);
            }
        } else {
            for (Marker m : mRental) {
                m.setVisible(false);
            }
        }
        if (sharedPreferences.getBoolean("SaR", false) == true) {
            for (Marker m : mShopsRepair) {
                m.setVisible(true);
            }
        } else {
            for (Marker m : mShopsRepair) {
                m.setVisible(false);
            }
        }
    }

    // Markers from JSON
    private void MarkersFromJSON(List<Place> places) {
        for (int i = 0; i < places.size(); i++) {
            String[] ls;
            ls = places.get(i).getDetail().get(0).getLocation().split(", ");
            for (int j = 0; j < 2; j++) {

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(ls[0]), Double.parseDouble(ls[1])))
                        .title(places.get(i).getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
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
        }
        try {
            layer = new KmlLayer(mMap, R.raw.lviv_b, getApplicationContext());
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (KmlContainer c : layer.getContainers()) {
            for (KmlContainer c1 : c.getContainers()) {
                for (KmlPlacemark p : c1.getPlacemarks()) {
                    if (p.getProperty("name").equals("Rental")) {
                        KmlGeometry g = p.getGeometry();
                        rental.add(g.getGeometryObject().toString());
                    } else if (p.getProperty("name").equals("NextBike")) {
                        KmlGeometry g = p.getGeometry();
                        nextBike.add(g.getGeometryObject().toString());
                    } else if (p.getProperty("name").equals("P")) {
                        KmlGeometry g = p.getGeometry();
                        parking.add(g.getGeometryObject().toString());
                    }
                }
            }
        }
        CreateNextBikeMarkers(nextBike);
        CreateParkingMarkers(parking);
        CreateRentalMarkers(rental);
    }

    private void CreateParkingMarkers(List<String> parking) {
        for (int i = 0; i < parking.size(); i++) {
            String[] ls;
            ls = parking.get(i).substring(10, parking.get(i).length() - 1).split(",");
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(ls[0]), Double.parseDouble(ls[1])))
                    .title("Parking")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .visible(false));
            mParking.add(marker);
        }
    }

    private void CreateNextBikeMarkers(List<String> nextBike) {
        for (int i = 0; i < nextBike.size(); i++) {
            String[] ls;
            ls = nextBike.get(i).substring(10, nextBike.get(i).length() - 1).split(",");
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(ls[0]), Double.parseDouble(ls[1])))
                    .title("NextBike")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    .visible(false));
            mNextBike.add(marker);
        }
    }

    private void CreateRentalMarkers(List<String> rental) {
        for (int i = 0; i < rental.size(); i++) {
            String[] ls;
            ls = rental.get(i).substring(10, rental.get(i).length() - 1).split(",");
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(ls[0]), Double.parseDouble(ls[1])))
                    .title("Rental place")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .visible(false));
            mRental.add(marker);
        }
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
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

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
        } else {
            return true;
        }
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