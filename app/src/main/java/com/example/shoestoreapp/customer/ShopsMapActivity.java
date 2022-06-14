package com.example.shoestoreapp.customer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.shoestoreapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.android.clustering.ClusterManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class ShopsMapActivity extends AppCompatActivity implements OnMapReadyCallback{

    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int FINE_REQUEST_CODE = 10;
    private static final int COARSE_REQUEST_CODE = 20;
    private static final String TAG = "ShopsMapActivity";
    GoogleMap mGoogleMap;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private LatLngBounds mMapBoundary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shops_map);
        initGoogleMap(savedInstanceState);
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.shopsMapView);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    public void showLocationOnMap(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            //TODO: deprecated
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_REQUEST_CODE);
            return;
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            //TODO: deprecated
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_REQUEST_CODE);
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mGoogleMap = map;
        addMapMarkers();
        showLocationOnMap();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("REQUEST", "onRequestPermissionsResult: ");
                Toast.makeText(this, "Dodijeljeno dopuštenje za fine", Toast.LENGTH_SHORT).show();
                showLocationOnMap();
            } else {
                Toast.makeText(this, "Odbijeno dopuštenje za fine", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == COARSE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("REQUEST", "onRequestPermissionsResult: ");
                Toast.makeText(this, "Dodijeljeno dopuštenje za coarse", Toast.LENGTH_SHORT).show();
                showLocationOnMap();
            } else {
                Toast.makeText(this, "Odbijeno dopuštenje za coarse", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addMapMarkers(){

        if(mGoogleMap != null){

            if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(this, mGoogleMap);
            }
            if(mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        this,
                        mGoogleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }


            Log.d(TAG, "addMapMarkers: location: " + "store location");
            try{
                String snippet = "Store address";
                int avatar = R.drawable.ic_paymet_method; // set the default avatar
                ClusterMarker newClusterMarker = new ClusterMarker(
                        new LatLng(45.303988, 14.711322),
                        "Ducan 1",
                        snippet,
                        avatar
                );
                mClusterManager.addItem(newClusterMarker);
                mClusterMarkers.add(newClusterMarker);

            }catch (NullPointerException e){
                Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage() );
            }
            mClusterManager.cluster();

            mMapBoundary = new LatLngBounds(
                    new LatLng(44.803988, 15.011322),
                    new LatLng(45.503988, 15.311322)
            );

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
        }
    }
}

