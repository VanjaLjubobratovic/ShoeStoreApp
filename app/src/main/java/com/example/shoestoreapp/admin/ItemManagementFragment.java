package com.example.shoestoreapp.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.databinding.FragmentItemManagementBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ItemManagementFragment extends Fragment {
    private FragmentItemManagementBinding binding;
    private ViewFlipper flipper;
    private MaterialButton inventoryBtn, newItemBtn, confirmItem;
    private ImageButton newBack;
    private Spinner modelSpinner, colorSpinner;
    private EditText priceEt;
    private ImageView itemImage;

    private ArrayList<String> modelList;
    private ArrayList<String> colorList;

    private FirebaseFirestore database;
    private Uri itemImageUri;

    public ItemManagementFragment() {
        // Required empty public constructor
    }

    public static ItemManagementFragment newInstance(String param1, String param2) {
        ItemManagementFragment fragment = new ItemManagementFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentItemManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        flipper = binding.itemManagementFlipper;
        inventoryBtn = flipper.getRootView().findViewById(R.id.catalogueButton);
        newItemBtn = flipper.getRootView().findViewById(R.id.newItemButton);
        confirmItem = flipper.getRootView().findViewById(R.id.newItemConfirm);
        newBack = flipper.getRootView().findViewById(R.id.newItemBack);
        modelSpinner = flipper.getRootView().findViewById(R.id.newItemModel);
        colorSpinner = flipper.getRootView().findViewById(R.id.newItemColor);
        priceEt = flipper.getRootView().findViewById(R.id.newItemPrice);
        itemImage = flipper.getRootView().findViewById(R.id.newItemIcon);

        inventoryBtn.setOnClickListener(view1 ->  {
            //TODO: show inventory
        });

        newItemBtn.setOnClickListener(view1 -> {
            fetchModelColor();
            flipper.showNext();
        });

        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getSelectedItem().equals("NOVI MODEL")) {
                    newValueAlert("Dodaj novi model", modelSpinner);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getSelectedItem().equals("NOVA BOJA")) {
                    newValueAlert("Dodaj novu boju", colorSpinner);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        newBack.setOnClickListener(view1 -> {
            try {
                getActivity().onBackPressed();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });

        //Image pick listener
        ActivityResultLauncher<Intent> itemPictureResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    if (data == null)
                        return;

                    itemImageUri = data.getData();
                    itemImage.setImageURI(itemImageUri);
                }
            }
        });

        itemImage.setOnClickListener(view1 -> {
            addImage(itemPictureResultLauncher);
        });

        confirmItem.setOnClickListener(view1 -> {
            //TODO: add item to db
            if(inputsAreFilledOut()) {
                addItemToDB();
            } else Toast.makeText(getContext(), "Popunite sve podatke (model, boja, cijena, slika) prije dodavanja", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchModelColor() {
        modelList = new ArrayList<>();
        colorList = new ArrayList<>();

        modelList.add("-");
        colorList.add("-");

        database.collection("/locations/webshop/items/").get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for(DocumentSnapshot document : task.getResult()) {
                            ItemModel item = document.toObject(ItemModel.class);
                            if(item == null)
                                continue;
                            item.parseModelColor(document.getId());

                            if(!colorList.contains(item.getColor()))
                                colorList.add(item.getColor());
                            if(!modelList.contains(item.getModel()))
                                modelList.add(item.getModel());
                        }
                        modelList.add("NOVI MODEL");
                        colorList.add("NOVA BOJA");
                        fillDropdowns();
                    }
                });

    }

    private void addItemToDB() {
        //TODO: add ability to set custom sizes and amounts
        ArrayList<Integer> amounts = new ArrayList<>(Collections.nCopies(5, 0));
        ArrayList<Integer> sizes = new ArrayList<>();
        for(int i = 35; i < 40; i++)
            sizes.add(i);

        //TODO: clean strings (toLower, strip...)
        ItemModel item = new ItemModel("shoe", colorSpinner.getSelectedItem() + ".jpg", Double.parseDouble(priceEt.getText().toString()),
                5, Timestamp.now(), sizes, amounts);
        //TODO: fix this
        item.parseModelColor(modelSpinner.getSelectedItem() + "-" + colorSpinner.getSelectedItem());

        uploadImageToDB();

        database.collection("/locations").get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot document : task.getResult()) {
                            addItemToStore(document.getId(), item);
                        }
                    }
                });
    }

    private void addItemToStore(String storeID, ItemModel item) {
        Map<String, Object> newItem = new HashMap<>();
        newItem.put("added", item.getAdded());
        newItem.put("amounts", item.getAmounts());
        newItem.put("sizes", item.getSizes());
        newItem.put("image", item.getImage());
        newItem.put("price", item.getPrice());
        newItem.put("rating", item.getRating());
        newItem.put("type", item.getType());
        newItem.put("model", item.getModel());
        newItem.put("color", item.getColor());

        //TODO: more precise error check
        database.collection("/locations/" + storeID + "/items").document(item.toString())
                .set(newItem)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Toast.makeText(getContext(), "Uspješno dodavanje novog artikla", Toast.LENGTH_SHORT).show();
                        clearDataAndUI();
                    } else Toast.makeText(getContext(), "Neuspješno dodavanje novog artikla", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Predmet već postoji", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadImageToDB() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference newImage = storage.getReference().child(colorSpinner.getSelectedItem() + ".jpg");

        UploadTask uploadTask = newImage.putFile(itemImageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(getContext(), "Uspješno zapisivanje slike u bazu", Toast.LENGTH_SHORT).show();
        });
        uploadTask.addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Neuspješno zapisivanje slike u bazu", Toast.LENGTH_SHORT).show();
        });
    }

    private void clearDataAndUI() {
        itemImageUri = null;
        fillDropdowns();
        priceEt.setText("");
        itemImage.setImageResource(R.drawable.running_shoe_icon);
    }

    private boolean inputsAreFilledOut() {
        return !colorSpinner.getSelectedItem().equals("-") && !modelSpinner.getSelectedItem().equals("-") &&
                !priceEt.getText().toString().equals("") && itemImageUri != null;
    }

    private void addImage(ActivityResultLauncher<Intent> itemPictureResultLauncher) {
        Intent pictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        itemPictureResultLauncher.launch(pictureIntent);
    }

    private void fillDropdowns() {
        dropdownAddColors();
        dropdownAddModels();
    }

    private void dropdownAddColors() {
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, colorList);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);
    }

    private void dropdownAddModels() {
        ArrayAdapter<String> modelAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, modelList);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(modelAdapter);
    }

    private void newValueAlert(String message, Spinner spinner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(message);

        final View customLayout = getLayoutInflater().inflate(R.layout.custom_dialog_layout, null);
        builder.setView(customLayout);

        builder.setPositiveButton("DODAJ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO: clean strings (toLower, strip...)
                EditText et = customLayout.findViewById(R.id.editText);
                if(modelList.contains(et.getText().toString()) || colorList.contains(et.getText().toString())) {
                    Toast.makeText(getContext(), "Navedena vrijednost već postoji", Toast.LENGTH_SHORT).show();
                    spinner.setSelection(0);
                    return;
                }
                if(!et.getText().toString().equals("")) {
                    if(message.contains("model")) {
                        modelList.remove(modelList.size() - 1);
                        modelList.add(et.getText().toString());
                        modelList.add("NOVI MODEL");
                        dropdownAddModels();
                        modelSpinner.setSelection(modelList.size() - 2);
                    } else {
                        colorList.remove(colorList.size() - 1);
                        colorList.add(et.getText().toString());
                        colorList.add("NOVA BOJA");
                        dropdownAddColors();
                        colorSpinner.setSelection(colorList.size() - 2);
                    }
                } else Toast.makeText(getContext(), "Morate unijeti vrijednost", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("ODUSTANI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }
}