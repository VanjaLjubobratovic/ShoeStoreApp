package com.example.shoestoreapp.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.ClusterMarker;
import com.example.shoestoreapp.customer.MyClusterManagerRenderer;
import com.example.shoestoreapp.databinding.FragmentStoreRelocationBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.WriteBatch;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.util.ArrayList;

public class StoreRelocationFragment extends Fragment implements OnMapReadyCallback {
    private FragmentStoreRelocationBinding binding;
    private String storeID, address, newAddress;
    private Double lat, lng;
    private LatLng newLocation;
    private EditText addressEt;
    private ImageButton search;
    private MaterialButton confirmBtn;
    private TextView addressTv, coordinatesTv;

    private MapView mMapView;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int FINE_REQUEST_CODE = 10;
    private static final int COARSE_REQUEST_CODE = 20;

    private GoogleMap mGoogleMap;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ClusterMarker storeMarker;
    private FusedLocationProviderClient fusedLocationClient;

    private FirebaseFirestore database;


    public StoreRelocationFragment() {
        // Required empty public constructor
    }
    public static StoreRelocationFragment newInstance(String storeID, GeoPoint location, String address) {
        StoreRelocationFragment fragment = new StoreRelocationFragment();
        Bundle args = new Bundle();
        args.putString("storeID", storeID);
        args.putString("address", address);
        args.putDouble("lat", location.getLatitude());
        args.putDouble("lng", location.getLongitude());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null && getArguments().containsKey("storeID")) {
            this.storeID = getArguments().getString("storeID");
            this.lat = getArguments().getDouble("lat");
            this.lng = getArguments().getDouble("lng");
            this.address = getArguments().getString("address");
        } else Toast.makeText(getContext(), "No Arguments", Toast.LENGTH_SHORT).show();


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        database = FirebaseFirestore.getInstance();
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = binding.dialogMapView;
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStoreRelocationBinding.inflate(inflater, container, false);
        return  binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initGoogleMap(savedInstanceState);
        addressTv = binding.dialogAddress;
        coordinatesTv = binding.dialogCoordinates;
        search = binding.mapSearch;
        addressEt = binding.addressToSearch;
        confirmBtn = binding.relocationConfirm;

        confirmBtn.setEnabled(false);

        addressTv.setText(address);
        coordinatesTv.setText("Koordinate: " + lat + " | " + lng);

        search.setOnClickListener(view1 -> {
            try {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                addressEt.clearFocus();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Greška", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!addressEt.getText().toString().isEmpty()) {
                Geocoder geocoder = new Geocoder(getContext());
                ArrayList<Address> addresList;
                try {
                    addresList = new ArrayList<>(geocoder.getFromLocationName(addressEt.getText().toString(), 6));
                    if(addresList.isEmpty()) {
                        Toast.makeText(getContext(), "Adresa nije pronađena", Toast.LENGTH_SHORT).show();
                        newLocation = null;
                        newAddress = null;
                        return;
                    }
                    newLocation = new LatLng(addresList.get(0).getLatitude(), addresList.get(0).getLongitude());
                    newAddress = addresList.get(0).getAddressLine(0);
                    onMapReady(mGoogleMap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else Toast.makeText(getContext(), "Polje za adresu je prazno", Toast.LENGTH_SHORT).show();

            if (newLocation == null)
                confirmBtn.setEnabled(false);
            else confirmBtn.setEnabled(true);
        });

        confirmBtn.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Jeste li sigurni?");
            builder.setMessage("Želite li promijeniti adresu trgovine na: " + newAddress);

            builder.setPositiveButton("DA", (dialogInterface, i) -> {
                updateStoreLocation();
            });
            builder.setNegativeButton("ODUSTANI", (dialogInterface, i) -> {
                dialogInterface.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void updateStoreLocation() {
        database = FirebaseFirestore.getInstance();

        WriteBatch batch = database.batch();
        DocumentReference locationRef = database.document("/locations/" + storeID);

        batch.update(locationRef, "address", newAddress);
        batch.update(locationRef, "location", new GeoPoint(newLocation.latitude, newLocation.longitude));

        batch.commit().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(getContext(), "Uspješna promjena lokacije", Toast.LENGTH_SHORT).show();
                try {
                    getActivity().onBackPressed();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            } else Toast.makeText(getContext(), "Nešto je pošlo po zlu", Toast.LENGTH_SHORT).show();
        });
    }

    //TODO: check permissions just in case
    @SuppressLint("MissingPermission")
    public void showLocationOnMap() {
        mGoogleMap.setMyLocationEnabled(true);
        LatLng myLocation = new LatLng(lat, lng);
        if(newLocation != null) {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(newLocation.latitude, newLocation.longitude),
                    13));
        } else mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));

    }


    private void addMapMarkers() {
        if (mGoogleMap != null) {
            mClusterManager = new ClusterManager<ClusterMarker>(getActivity(), mGoogleMap);
            mGoogleMap.clear();

            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        getContext(),
                        mGoogleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            int markerIcon = R.drawable.launchericon1; // set the default avatar
            ClusterMarker oldStore = new ClusterMarker(
                    new LatLng(lat, lng),
                    storeID,
                    address,
                    markerIcon
            );

            mClusterManager.addItem(oldStore);

            if(newLocation != null) {
                ClusterMarker newStore = new ClusterMarker(
                        new LatLng(newLocation.latitude, newLocation.longitude),
                        storeID,
                        newAddress,
                        markerIcon
                );
                mClusterManager.addItem(newStore);
            }

            mClusterManager.cluster();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        addMapMarkers();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            //TODO: deprecated
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_REQUEST_CODE);
            return;
        }
        else {
            showLocationOnMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("REQUEST", "onRequestPermissionsResult: ");
                Toast.makeText(getContext(), "Dodijeljeno dopuštenje za fine", Toast.LENGTH_SHORT).show();
                showLocationOnMap();
            } else {
                Toast.makeText(getContext(), "Odbijeno dopuštenje za fine", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}