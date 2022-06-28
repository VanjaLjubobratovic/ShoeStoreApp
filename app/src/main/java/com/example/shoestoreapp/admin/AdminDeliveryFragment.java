package com.example.shoestoreapp.admin;

import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.databinding.FragmentDeliveryBinding;
import com.example.shoestoreapp.employee.DeliveryRecyclerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AdminDeliveryFragment extends Fragment implements DeliveryRecyclerViewAdapter.OnDeliveryItemListener{
    public AdminDeliveryFragment() {
        // Required empty public constructor
    }

    private UserModel user;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private CollectionReference itemsRef;
    private StorageReference storageRef;
    private String storeID = "webshop";
    private ItemModel selectedItem;
    private ItemModel itemToEdit = new ItemModel();

    private ArrayList<ItemModel> fetchedItemsList = new ArrayList<>();
    private ArrayList<ItemModel> deliveredItems = new ArrayList<>();
    private ArrayList<String> selectedColorList = new ArrayList<>();
    private ArrayList<EditText> selectedAmountsList = new ArrayList<>();

    private MaterialButton confirmBtn, addItemBtn;
    private ImageButton manualBackBtn;
    private EditText modelEt;
    private Spinner colorDropdown;
    private ProgressBar loading;
    private ImageView itemImage;
    private LinearLayout specificItemLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        database = FirebaseFirestore.getInstance();
        //TODO:exception handling
        String collection = "/locations/" + storeID + "/items";
        itemsRef = database.collection(collection);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        user = getActivity().getIntent().getParcelableExtra("userData");
        itemToEdit.parseModelColor(" - ");

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_delivery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        confirmBtn = getActivity().findViewById(R.id.adminDeliveryConfirm);
        addItemBtn = getActivity().findViewById(R.id.adminDeliveryAddBtn);
        manualBackBtn = getActivity().findViewById(R.id.adminDeliveryBack);
        modelEt = getActivity().findViewById(R.id.adminDeliveryModelEditText);
        colorDropdown = getActivity().findViewById(R.id.adminDeliveryColorDropdown);
        itemImage = getActivity().findViewById(R.id.adminDeliveryItemImage);
        specificItemLayout = getActivity().findViewById(R.id.adminSpecificItemLayout);
        loading = getActivity().findViewById(R.id.adminDeliveryProgress);

        addItemBtn.setEnabled(false);
        confirmBtn.setEnabled(false);

        modelEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                clearItemPreview();
                loading.setVisibility(View.VISIBLE);

                fetchItems(modelEt.getText().toString());
                if(modelEt.getText().toString().equals(""))
                    loading.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        manualBackBtn.setOnClickListener(manBack -> {
            getParentFragmentManager().popBackStackImmediate();

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

                addAmountsInput();
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

            //If item is already present in list, merge amounts
            if(deliveredItems.contains(deliveredItem)) {
                int index = deliveredItems.indexOf(deliveredItem);
                mergeSameItems(deliveredItems.get(index), deliveredItem);
            } else {
                deliveredItems.add(deliveredItem);
            }

            initRecyclerView();
            clearItemPreview();
            clearInputs();
            confirmBtn.setEnabled(true);
            modelEt.setEnabled(true);

            //TODO: fix this hack
            itemToEdit.parseModelColor(" - ");
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickStore();

                clearInputs();
            }
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
                    clearItemPreview();
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

    private void mergeSameItems(ItemModel addedItem, ItemModel itemToAdd) {
        for(int i = 0; i < addedItem.getAmounts().size(); i++) {
            int mergedAmount = addedItem.getAmounts().get(i) + itemToAdd.getAmounts().get(i);
            addedItem.getAmounts().set(i, mergedAmount);
        }
    }

    private void clearItemPreview(){
        fetchedItemsList = new ArrayList<>();
        selectedColorList = new ArrayList<>();
        selectedAmountsList = new ArrayList<>();
        itemImage.setImageResource(R.drawable.running_shoe_icon);
        selectedItem = null;
        addItemBtn.setEnabled(false);

        specificItemLayout.removeAllViews();
    }

    private void clearInputs() {
        modelEt.setText("");
        colorDropdown.setAdapter(null);
    }

    private void dropdownAddColors() {
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, selectedColorList);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorDropdown.setAdapter(dropdownAdapter);

        if (!itemToEdit.getModel().equals(" ")) {
            //Log.d("DELIVERY RECYCLER", "item: " + itemToEdit.toString());
            colorDropdown.setSelection(selectedColorList.indexOf(itemToEdit.getColor()));
        }
    }

    private void addAmountsInput() {
        specificItemLayout.removeAllViews();
        selectedAmountsList = new ArrayList<>();
        int numOfElements = 0;

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

            //If item editing is taking place
            if(!itemToEdit.getModel().equals(" ") && itemToEdit.getAmounts().get(i) != 0)
                et.setText(itemToEdit.getAmounts().get(i).toString());

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
        loading.setVisibility(View.GONE);
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = this.getView().findViewById(R.id.adminDeliveryRecycler);
        recyclerView.setLayoutManager(layoutManager);
        DeliveryRecyclerViewAdapter adapter = new DeliveryRecyclerViewAdapter(getContext(), deliveredItems, this, confirmBtn,
                itemToEdit, modelEt, addItemBtn);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDeliveryItemClick(int position) {

    }

    private void addDeliveryToDB(String dest) {
        Map<String, Object> newDelivery= new HashMap<>();
        Random rand = new Random();
        short s = (short) rand.nextInt(Short.MAX_VALUE + 1);
        String docName = "Delivery" + s;
        newDelivery.put("accepted", false);
        newDelivery.put("accepted_by", null);
        newDelivery.put("code", (int) s);
        newDelivery.put("time_added", Timestamp.now());
        newDelivery.put("time_accepted", null);

        DocumentReference newDeliveryRef = database.collection("locations/" + dest + "/deliveries").document(docName);
        newDeliveryRef.set(newDelivery);
        //TODO: make this ItemModelMethod
        for(ItemModel item : deliveredItems) {
            Map<String, Object> newReceiptItem = new HashMap<>();
            newReceiptItem.put("added", item.getAdded());
            newReceiptItem.put("image", item.getImage());
            newReceiptItem.put("price", item.getPrice());
            newReceiptItem.put("rating", item.getRating());
            newReceiptItem.put("type", item.getType());
            newReceiptItem.put("sizes", item.getSizes());
            newReceiptItem.put("amounts", item.getAmounts());

            newDeliveryRef.collection("items").document(item.toString())
                    .set(newReceiptItem)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("addItemToOrder", "onSuccess: ");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("addItemToOrder", "onFailure: ");
                        }
                    });
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Dostava uspješno zapisana");
        builder.setPositiveButton("Ok",null);
        builder.show();
        clearData();

    }

    private void pickStore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Odaberite trgovinu");

        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_store_picker, null);
        Spinner storeDropdown = customLayout.findViewById(R.id.storeSpinner);

        builder.setView(customLayout);
        builder.setPositiveButton("Ok", null)
                .setNegativeButton("Odustani", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        ArrayList<String> stores = new ArrayList<>();
        database.collection("/locations")
                .get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null) {
                for (DocumentSnapshot document : task.getResult()) {
                    if(!document.getId().equals("webshop")) {
                        stores.add(document.getId());
                    }
                }
                dropdownAddStores(stores, storeDropdown);
            } else {
                Toast.makeText(getActivity(), "Nije pronađena niti jedna trgovina", Toast.LENGTH_SHORT).show();
            }
        });

        positiveButton.setOnClickListener(view -> {
            if(storeDropdown.getSelectedItem() != null) {
                //TODO actually use the storeId
                String destStore = storeDropdown.getSelectedItem().toString();
                dialog.dismiss();
                addDeliveryToDB(destStore);
            }
        });

        negativeButton.setOnClickListener(view -> {

            dialog.dismiss();
        });
    }

    private void dropdownAddStores(ArrayList<String> stores, Spinner storeDropdown) {
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, stores);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        storeDropdown.setAdapter(dropdownAdapter);
    }

    private void clearData() {
        deliveredItems = new ArrayList<>();
        confirmBtn.setEnabled(false);

        //TODO: instead of this make some kind of global recycler adapter and call notify methods
        initRecyclerView();
        clearItemPreview();
    }

}