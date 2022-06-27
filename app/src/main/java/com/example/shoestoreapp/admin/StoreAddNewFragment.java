package com.example.shoestoreapp.admin;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ClusterMarker;
import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.customer.MyClusterManagerRenderer;
import com.example.shoestoreapp.databinding.FragmentStoreAddNewBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.WriteBatch;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;

public class StoreAddNewFragment extends Fragment implements OnMapReadyCallback {
    private FragmentStoreAddNewBinding binding;

    private String storeID;
    private String storeType = "storefront";
    private String storeAddress;
    private LatLng storeLocation;
    private HashMap<String, String> typeTranslation;

    //Google maps stuff
    private GoogleMap mGoogleMap;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ClusterMarker storeMarker;
    private FusedLocationProviderClient fusedLocationClient;

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final int FINE_REQUEST_CODE = 10;
    private static final int COARSE_REQUEST_CODE = 20;

    private MapView mMapView;
    private TextView coordTv, addressTv;
    private EditText newAddressEt, storeIDEt;
    private ImageButton searchAddress;
    private MaterialButton confirm;
    private Spinner typeSpinner;

    private FirebaseFirestore database;

    public StoreAddNewFragment() {
        // Required empty public constructor
    }

    public static StoreAddNewFragment newInstance() {
        StoreAddNewFragment fragment = new StoreAddNewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        database = FirebaseFirestore.getInstance();

        typeTranslation = new HashMap<>();
        typeTranslation.put("Štand", "storefront");
        typeTranslation.put("Trgovina", "store");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStoreAddNewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        confirm = binding.relocationConfirm;
        searchAddress = binding.mapSearch;
        newAddressEt = binding.addressToSearch;
        storeIDEt = binding.newStoreID;
        typeSpinner = binding.typeSpinner;

        initGoogleMap(savedInstanceState);
        spinnerAddTypes();

        searchAddress.setOnClickListener(view1 -> {
            try {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Greška", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!newAddressEt.getText().toString().isEmpty()) {
                Geocoder geocoder = new Geocoder(getContext());
                ArrayList<Address> addressList;
                try {
                    addressList = new ArrayList<>(geocoder.getFromLocationName(newAddressEt.getText().toString(), 6));
                    if(addressList.isEmpty()) {
                        Toast.makeText(getContext(), "Adresa nije pronađena", Toast.LENGTH_SHORT).show();
                        storeAddress = null;
                        storeLocation = null;
                        return;
                    }

                    storeLocation = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
                    storeAddress = addressList.get(0).getAddressLine(0);
                    onMapReady(mGoogleMap);
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Greška", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else Toast.makeText(getContext(), "Greška", Toast.LENGTH_SHORT).show();

            checkInputs();
        });

        confirm.setOnClickListener(view1 -> {
            if(!checkInputs())
                return;

            addStoreToDB();
        });
    }

    private void spinnerAddTypes() {
        ArrayList<String> types = new ArrayList<>();
        types.add("Štand");
        types.add("Trgovina");


        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
    }

    private boolean checkInputs() {
        storeID = storeIDEt.getText().toString();
        if(storeAddress != null && !storeID.isEmpty()) {
            return true;
        }
        if (storeAddress == null) {
            Toast.makeText(getContext(), "Postavite adresu trgovine", Toast.LENGTH_SHORT).show();
        }
        if (storeID.isEmpty()) {
            Toast.makeText(getContext(), "Postavite identifikator trgovine", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void addStoreToDB() {
        database = FirebaseFirestore.getInstance();
        HashMap<String, Object> newStore = new HashMap<>();
        newStore.put("address", storeAddress);
        newStore.put("employees", new ArrayList<String>());
        newStore.put("enabled", true);
        newStore.put("location", new GeoPoint(storeLocation.latitude, storeLocation.longitude));
        newStore.put("type", typeTranslation.get(typeSpinner.getSelectedItem()));

        //TODO: check if store exists
        database.collection("/locations").document(storeID).set(newStore)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        addItemsToDB();
                    } else Toast.makeText(getContext(), "Greška pri dodavanju trgovine", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(task -> {
                    Toast.makeText(getContext(), "Greška pri dodavanju trgovine", Toast.LENGTH_SHORT).show();
                });
    }

    private void addItemsToDB() {
        //TODO: no other collections besides items, probably isn't a problem
        database = FirebaseFirestore.getInstance();
        WriteBatch batch = database.batch();

        database.collection("/locations/webshop/items").get()
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       for(DocumentSnapshot document : task.getResult()) {
                           ItemModel item = document.toObject(ItemModel.class);
                           if(item == null)
                               continue;

                           DocumentReference storeItemsRef = database.collection("/locations/" + storeID + "/items")
                                   .document(document.getId());

                           item.parseModelColor(document.getId());
                           HashMap<String, Object> newItem = new HashMap<>();
                           newItem.put("added", item.getAdded());
                           newItem.put("amounts", item.getAmounts());
                           newItem.put("sizes", item.getSizes());
                           newItem.put("image", item.getImage());
                           newItem.put("price", item.getPrice());
                           newItem.put("rating", item.getRating());
                           newItem.put("type", item.getType());
                           newItem.put("model", item.getModel());
                           newItem.put("color", item.getColor());

                           Log.d("ADDITEMS", item.toString());

                           batch.set(storeItemsRef, newItem);
                       }
                   }

                   batch.commit().addOnCompleteListener(task1 -> {
                      if (task.isSuccessful()) {
                          Toast.makeText(getContext(), "Uspješno dodavanje trgovine", Toast.LENGTH_SHORT).show();
                          try {
                              getActivity().onBackPressed();
                          } catch (NullPointerException e) {
                              e.printStackTrace();
                          }
                      } else Toast.makeText(getContext(), "Greška u zapisivanju", Toast.LENGTH_SHORT).show();
                   });
                });
    }

    private void showLocationOnMap() {
        try {
            mGoogleMap.setMyLocationEnabled(true);
            //TODO: hardcoded location
            LatLng myLocation = new LatLng(45.333773, 14.4287187);
            if(storeLocation != null)
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(storeLocation.latitude, storeLocation.longitude),
                        13));
            else mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));
        } catch (SecurityException e) {
            Toast.makeText(getContext(), "Potrebno dopuštenje za lokaciju", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void addMapMarkers() {
        if(mGoogleMap == null)
            return;

        mClusterManager = new ClusterManager<>(getActivity(), mGoogleMap);
        mGoogleMap.clear();

        if (mClusterManagerRenderer == null) {
            mClusterManagerRenderer = new MyClusterManagerRenderer(
                    getContext(),
                    mGoogleMap,
                    mClusterManager
            );
            mClusterManager.setRenderer(mClusterManagerRenderer);
        }

        int markerIcon = R.drawable.launchericon1;
        if(storeLocation != null) {
            storeMarker = new ClusterMarker(
                    new LatLng(storeLocation.latitude, storeLocation.longitude),
                    storeID,
                    storeAddress,
                    markerIcon
            );
            mClusterManager.addItem(storeMarker);
        }
        try {
            mClusterManager.cluster();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = binding.newStoreMap;
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            //TODO: deprecated
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_REQUEST_CODE);
            return;
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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        addMapMarkers();
        checkPermissions();
        //showLocationOnMap();
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