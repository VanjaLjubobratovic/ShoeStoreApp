package com.example.shoestoreapp.employee;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ReceiptFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText modelEt, colorEt, sizeEt;
    private MaterialButton addButton;
    private ImageView shoeImage;
    private TextView shoePriceTextView;
    private ProgressBar loadingBar;

    private ArrayList<ItemModel> items = new ArrayList<>();
    private FirebaseFirestore database;
    private CollectionReference itemsRef;
    private ItemModel currentItem;

    private FirebaseStorage storage;
    private StorageReference storageRef;

    public ReceiptFragment() {
        // Required empty public constructor
    }


    public static ReceiptFragment newInstance(String param1, String param2) {
        ReceiptFragment fragment = new ReceiptFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        database = FirebaseFirestore.getInstance();
        //TODO:Replace this placeholder after you implement employee to store binding in login
        //String collection = "/locations/" + getIntent().getStringExtra("shopID") + "/items";
        String collection = "/locations/" + "TestShop1" + "/items";
        itemsRef = database.collection(collection);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receipt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        modelEt = view.findViewById(R.id.modelEditText);
        colorEt = view.findViewById(R.id.colorEditText);
        sizeEt = view.findViewById(R.id.sizeEditText);

        shoeImage = view.findViewById(R.id.shoeImage);
        shoePriceTextView = view.findViewById(R.id.shoePriceTextView);
        loadingBar = view.findViewById(R.id.loading);


        modelEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                loadingBar.setVisibility(View.VISIBLE);
                fetchItems(modelEt.getText().toString(), colorEt.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        colorEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                loadingBar.setVisibility(View.VISIBLE);
                fetchItems(modelEt.getText().toString(), colorEt.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }


    private void fetchItems(String model, String color) {
        itemsRef.document(model + "-" + color).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    if (task.getResult().getData() == null) {
                        Log.d("FIRESTORE", "Item not found");
                        clearItemPreview();
                        return;
                    }
                    Log.d("FIRESTORE", task.getResult().toString());
                    currentItem = task.getResult().toObject(ItemModel.class);
                    setItemPreview();
                } else {
                    Log.d("FIRESTORE", "task not successful");
                    clearItemPreview();
                }
            }
        });
    }

    private void setItemPreview() {
        StorageReference imageRef = storageRef.child(currentItem.getImage());
        Glide.with(getContext())
                .asBitmap()
                .load(imageRef)
                .into(shoeImage);

        shoePriceTextView.setText("Cijena: " + (int)currentItem.getPrice() + "kn");
        loadingBar.setVisibility(View.INVISIBLE);
    }

    private void clearItemPreview() {
        shoeImage.setImageResource(R.drawable.running_shoe_icon);
        shoePriceTextView.setText("Cijena: ");
        loadingBar.setVisibility(View.INVISIBLE);
    }
}