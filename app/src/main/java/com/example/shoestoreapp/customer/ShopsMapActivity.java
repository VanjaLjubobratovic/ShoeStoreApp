package com.example.shoestoreapp.customer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.shoestoreapp.DataModels.ClusterMarker;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.CustomerStoreModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class ShopsMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int FINE_REQUEST_CODE = 10;
    private static final int COARSE_REQUEST_CODE = 20;
    private static final String TAG = "ShopsMapActivity";
    private GoogleMap mGoogleMap;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private LatLngBounds mMapBoundary;
    private FirebaseFirestore database;
    private ArrayList<CustomerStoreModel> storesList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_shops_map);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        database = FirebaseFirestore.getInstance();
        initGoogleMap(savedInstanceState);
        super.onCreate(savedInstanceState);

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

    @SuppressLint("MissingPermission")
    public void showLocationOnMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            //TODO: deprecated
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_REQUEST_CODE);
        }
        else {
            mGoogleMap.setMyLocationEnabled(true);
            LatLng myLocation = new LatLng(45.333781614474205, 14.425587816563203);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),
                                        10));
                            } else {
                                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                                        10));
                            }
                        }
                    });
        }

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
        mGoogleMap = map;
        fetchLocations();
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
                LatLng myLocation = new LatLng(45.333781614474205, 14.425587816563203);
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                        10));
            }
        }
    }

    private void addMapMarkers() {
        if (mGoogleMap != null && !storesList.isEmpty()) {

            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<ClusterMarker>(this, mGoogleMap);
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        this,
                        mGoogleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            for (CustomerStoreModel store : storesList) {
                Log.d(TAG, "addMapMarkers: location: " + "store location");
                try {
                    int avatar = R.drawable.launchericon1; // set the default avatar
                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(store.getLocation().getLatitude(), store.getLocation().getLongitude()),
                            store.getName(),
                            store.getAddress(),
                            avatar
                    );
                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);

                } catch (NullPointerException e) {
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                }
                mClusterManager.cluster();
            }

        }
    }

    private void fetchLocations() {
        CollectionReference locationsRef = database.collection("locations");
        locationsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().size() == 0) {
                        Log.d("FIRESTORE", "0 Results");
                        return;
                    }
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        CustomerStoreModel location = document.toObject(CustomerStoreModel.class);
                        if(location == null || location.getType().equals("webshop"))
                            continue;

                        location.setName(document.getId());
                        storesList.add(location);
                        Log.d("FIRESTORE Single", location.toString());
                    }
                } else Log.d("FIRESTORE Single", "fetch failed");
                addMapMarkers();
            }
        });
    }
}

