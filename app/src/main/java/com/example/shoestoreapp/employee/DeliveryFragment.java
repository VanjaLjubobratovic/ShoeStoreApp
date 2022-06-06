package com.example.shoestoreapp.employee;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.databinding.FragmentDeliveryBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DeliveryFragment extends Fragment implements DeliveryRecyclerViewAdapter.OnDeliveryItemListener {
    private MaterialButton manualAddBtn, codeScanBtn, confirmBtn, addItemBtn;
    private EditText modelEt;
    private Spinner colorDropdown;
    private FragmentDeliveryBinding binding;
    private ViewFlipper flipper;
    private ImageView itemImage;
    private LinearLayout specificItemLayout;

    private ArrayList<ItemModel> fetchedItemsList = new ArrayList<>();
    private ArrayList<ItemModel> deliveredItems = new ArrayList<>();
    private ArrayList<String> selectedColorList = new ArrayList<>();
    private ArrayList<EditText> selectedAmountsList = new ArrayList<>();

    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private CollectionReference itemsRef;
    private StorageReference storageRef;

    private ItemModel selectedItem;
    private String editColor = "";

    public DeliveryFragment() {
    }

    public static DeliveryFragment newInstance(String param1, String param2) {
        DeliveryFragment fragment = new DeliveryFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        binding = FragmentDeliveryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        flipper = binding.deliveryUpperFlipper;
        confirmBtn = binding.deliveryConfirmButton;

        manualAddBtn = flipper.getRootView().findViewById(R.id.manualDeliveryAdd);
        codeScanBtn = flipper.getRootView().findViewById(R.id.scanDeliveryCode);

        addItemBtn = flipper.getRootView().findViewById(R.id.addBtn);
        modelEt = flipper.getRootView().findViewById(R.id.modelEditText);
        colorDropdown = flipper.getRootView().findViewById(R.id.colorDropdown);
        itemImage = flipper.getRootView().findViewById(R.id.itemImageView);
        specificItemLayout = flipper.getRootView().findViewById(R.id.specificItemLinearLayout);

        addItemBtn.setEnabled(false);
        confirmBtn.setEnabled(false);


        manualAddBtn.setOnClickListener(view1 -> {
            flipper.showNext();
        });

        codeScanBtn.setOnClickListener(view1 -> {
            //TODO: implement QR code scanning
        });

        modelEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                clearData();
                fetchItems(modelEt.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        colorDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int index = selectedColorList.indexOf(colorDropdown.getSelectedItem().toString());
                selectedItem = fetchedItemsList.get(index);

                StorageReference imageRef = storageRef.child(selectedItem.getImage());
                Glide.with(getContext())
                        .asBitmap()
                        .load(imageRef)
                        .into(itemImage);

                addAmountsInput(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        addItemBtn.setOnClickListener(view1 -> {
            //Lower keyboard on add item
            view.clearFocus();
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getRootView().getWindowToken(), 0);


            ArrayList<Integer> sizeList = new ArrayList<>();
            parseSizeInput(sizeList);

            //TODO: make dedicated constructor for this
            ItemModel deliveredItem = new ItemModel(selectedItem.getType(), selectedItem.getImage(), selectedItem.getPrice(),
                    selectedItem.getRating(), selectedItem.getAdded(), selectedItem.getSizes(), sizeList);
            deliveredItem.parseModelColor(selectedItem.toString());

            //TODO: implement item editing or amounts merge of same items
            deliveredItems.add(deliveredItem);

            initRecyclerView();
            clearData();
            clearInputs();
            confirmBtn.setEnabled(true);
        });


        confirmBtn.setOnClickListener(view1 -> {
            //TODO: code here
        });
    }

    private void fetchItems(String model) {
        itemsRef.whereEqualTo("model", model).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d("FIRESTORE", "fetch successfull");
                    for(DocumentSnapshot document : task.getResult()) {
                        ItemModel newItem = document.toObject(ItemModel.class);

                        if(newItem == null) {
                            Log.d("FIRESTORE", "null item");
                            return;
                        }
                        newItem.parseModelColor(document.getId());
                        fetchedItemsList.add(newItem);
                        selectedColorList.add(newItem.getColor());
                    }
                    dropdownAddColors();
                } else {
                    Log.d("FIRESTORE", "fetch unsuccessfull");
                    clearData();
                }
            }
        });
    }

    private void parseSizeInput(ArrayList<Integer> sizeList) {
        for(EditText et : selectedAmountsList) {
            if (et.getText().toString().equals("")) {
                sizeList.add(0);
            } else sizeList.add(Integer.valueOf(et.getText().toString()));
        }
    }

    private void clearData() {
        fetchedItemsList = new ArrayList<>();
        selectedColorList = new ArrayList<>();
        selectedAmountsList = new ArrayList<>();
        specificItemLayout.removeAllViews();
        itemImage.setImageResource(R.drawable.running_shoe_icon);
        selectedItem = null;
        addItemBtn.setEnabled(false);
    }

    private void clearInputs() {
        modelEt.setText("");
        colorDropdown.setAdapter(null);
    }

    private void dropdownAddColors() {
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, selectedColorList);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorDropdown.setAdapter(dropdownAdapter);
    }

    private void addAmountsInput(ArrayList<Integer> sizes) {
        specificItemLayout.removeAllViews();
        selectedAmountsList = new ArrayList<>();

        if (selectedItem == null)
            return;

        for(int i = 0; i < selectedItem.getSizes().size(); i++) {
            //Each size input is created as a vertical linear layout containing edit text and a label
            LinearLayout ll = new LinearLayout(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(30, 5, 30, 5);
            ll.setLayoutParams(params);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setGravity(Gravity.CENTER);

            EditText et = new EditText(getContext());
            et.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            et.setHint("0");
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            et.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            et.setGravity(Gravity.CENTER);
            et.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            if(sizes != null)
                et.setText(sizes.get(i));

            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            tv.setText(selectedItem.getSizes().get(i).toString());
            tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            //Divider
            View v = new View(getContext());
            LinearLayout.LayoutParams llDiv =  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            llDiv.width = 1;
            llDiv.height = 250;
            v.setLayoutParams(llDiv);
            v.setBackgroundColor(getResources().getColor(R.color.lightGrey));

            ll.addView(et);
            ll.addView(tv);
            specificItemLayout.addView(ll);
            specificItemLayout.addView(v);
            selectedAmountsList.add(et);
        }
        //Removing last divider to look nicer
        specificItemLayout.removeViewAt(specificItemLayout.getChildCount() - 1);
        addItemBtn.setEnabled(true);
    }

    private void adjustInventory() {
        //TODO: code here
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = this.getView().findViewById(R.id.deliveryItemRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        DeliveryRecyclerViewAdapter adapter = new DeliveryRecyclerViewAdapter(getContext(), deliveredItems, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDeliveryItemClick(int position) {
        /*String model = deliveredItems.get(position).getModel();
        String editColor = deliveredItems.get(position).getColor();
        modelEt.setText(model);*/
    }
}