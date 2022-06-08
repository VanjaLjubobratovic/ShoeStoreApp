package com.example.shoestoreapp.employee;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.databinding.FragmentDeliveryBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DeliveryFragment extends Fragment implements DeliveryRecyclerViewAdapter.OnDeliveryItemListener {
    private MaterialButton manualAddBtn, codeScanBtn, confirmBtn, addItemBtn;
    private EditText modelEt;
    private Spinner colorDropdown;
    private ProgressBar loading;
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
    private ItemModel itemToEdit = new ItemModel();

    private CodeScanner qrScanner;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int INPUT_MENU_SCREEN = 0;
    private static final int MANUAL_ADD_SCREEN = 1;
    private static final int QR_SCANNER_SCREEN = 2;

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

        itemToEdit.parseModelColor(" - ");
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
        loading = binding.deliveryProgressBar;

        manualAddBtn = flipper.getRootView().findViewById(R.id.manualDeliveryAdd);
        codeScanBtn = flipper.getRootView().findViewById(R.id.scanDeliveryCode);

        addItemBtn = flipper.getRootView().findViewById(R.id.addBtn);
        modelEt = flipper.getRootView().findViewById(R.id.modelEditText);
        colorDropdown = flipper.getRootView().findViewById(R.id.colorDropdown);
        itemImage = flipper.getRootView().findViewById(R.id.itemImageView);
        specificItemLayout = flipper.getRootView().findViewById(R.id.specificItemLinearLayout);

        addItemBtn.setEnabled(false);
        confirmBtn.setEnabled(false);

        CodeScannerView scannerView = flipper.getRootView().findViewById(R.id.scannerView);
        qrScanner = new CodeScanner(getActivity(), scannerView);


        manualAddBtn.setOnClickListener(view1 -> {
            flipper.setDisplayedChild(MANUAL_ADD_SCREEN);
        });

        codeScanBtn.setOnClickListener(view1 -> {
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                //TODO: deprecated
                requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            } else {
                flipper.setDisplayedChild(QR_SCANNER_SCREEN);
                qrScanner.startPreview();
            }
        });

        qrScanner.setDecodeCallback(result -> {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(getContext(), result.getText(), Toast.LENGTH_SHORT).show();
                    qrScanner.releaseResources();
                    flipper.setDisplayedChild(MANUAL_ADD_SCREEN);

                    /* CODE FORMAT:
                           /locations/TestShop1/deliveries/delivery001
                           CODE:5217183
                     */
                    try {
                        String[] lines = result.getText().split(System.getProperty("line.separator"));
                        fetchScannedDelivery(lines[0], lines[1]);
                    } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
                        Log.d("CODE READER", "invalid code ");
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Neispravan kod", Toast.LENGTH_SHORT).show();
                        flipper.setDisplayedChild(INPUT_MENU_SCREEN);
                        clearData();
                    }
                }
            });
        });

        /*OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(flipper.getDisplayedChild() != INPUT_MENU_SCREEN) {
                    flipper.setDisplayedChild(INPUT_MENU_SCREEN);
                    clearData();
                } else {
                    if(getActivity() != null) {
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        fm.popBackStack();
                    }
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);*/


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


        confirmBtn.setOnClickListener(view1 -> {
            loading.setVisibility(View.VISIBLE);
            confirmBtn.setEnabled(false);
            adjustInventory();
            clearData();
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

    private void clearData() {
        deliveredItems = new ArrayList<>();

        //TODO: instead of this make some kind of global recycler adapter and call notify methods
        initRecyclerView();
        clearItemPreview();
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

    private void adjustInventory() {
        AtomicBoolean successfulUpdate = new AtomicBoolean(true);
        ArrayList<ItemModel> itemsToAdjust = deliveredItems;

        for (ItemModel item : itemsToAdjust) {
            DocumentReference itemDocumentRef = itemsRef.document(item.toString());
            ArrayList<Integer> adjustedAmountsList = new ArrayList<>();

            //TODO: generalise this method
            itemDocumentRef.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Log.d("FIRESTORE", task.getResult().toString());
                    ItemModel itemToAdjust = task.getResult().toObject(ItemModel.class);

                    for(int i = 0; i < itemToAdjust.getAmounts().size(); i++) {
                        int adjustedAmount = itemToAdjust.getAmounts().get(i) + item.getAmounts().get(i);
                        adjustedAmountsList.add(adjustedAmount);

                        itemDocumentRef.update("amounts", adjustedAmountsList)
                                .addOnCompleteListener(task1 -> {
                                    if(task.isSuccessful()) {
                                        Log.d("adjustInventory", "Successful amount update");
                                    } else {
                                        Log.d("adjustInventory", "Amount update failed");
                                        successfulUpdate.getAndSet(false);
                                    }
                                });
                    }
                }
            })
            .addOnFailureListener(task1 -> {
                Log.d("FIRESTORE", "AdjustInventory fetch not successful");
                successfulUpdate.getAndSet(false);
            });
        }

        //TODO: make more detailed error message detection
        if(successfulUpdate.get())
            showAlert("Uspješno ste podesili inventar");
        else showAlert("Količine nekih artikala nisu uspješno zapisane u bazu");
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerView recyclerView = this.getView().findViewById(R.id.deliveryItemRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        DeliveryRecyclerViewAdapter adapter = new DeliveryRecyclerViewAdapter(getContext(), deliveredItems, this, confirmBtn,
                itemToEdit, modelEt, addItemBtn);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDeliveryItemClick(int position) {
        /*String model = deliveredItems.get(position).getModel();
        String editColor = deliveredItems.get(position).getColor();
        modelEt.setText(model);*/
    }

    private void showAlert(String message) {
        //TODO: add icon and title
        AlertDialog.Builder deliveryAlert = new AlertDialog.Builder(getContext());
        deliveryAlert.setMessage(message);
        deliveryAlert.setCancelable(true);
        deliveryAlert.show();
        loading.setVisibility(View.GONE);
    }

    private void mergeSameItems(ItemModel addedItem, ItemModel itemToAdd) {
        for(int i = 0; i < addedItem.getAmounts().size(); i++) {
            int mergedAmount = addedItem.getAmounts().get(i) + itemToAdd.getAmounts().get(i);
            addedItem.getAmounts().set(i, mergedAmount);
        }
    }

    private void fetchScannedDelivery(String path, String code) {
        DocumentReference deliveryRef = database.document(path);

        deliveryRef.collection("items").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(getContext(), "Pretraga nije pronašla niti jedan artikl", Toast.LENGTH_SHORT).show();
                            flipper.setDisplayedChild(INPUT_MENU_SCREEN);
                            clearData();
                        } else {
                            for(DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                ItemModel deliveryItem = document.toObject(ItemModel.class);
                                if (deliveryItem != null) {
                                    deliveryItem.parseModelColor(document.getId());
                                }
                                deliveredItems.add(deliveryItem);
                            }
                            initRecyclerView();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Neuspješno dobavljanje iz baze", Toast.LENGTH_SHORT).show();
                        flipper.setDisplayedChild(INPUT_MENU_SCREEN);
                        clearData();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        qrScanner.startPreview();
    }

    @Override
    public void onPause() {
        qrScanner.releaseResources();
        super.onPause();
    }



    //TODO: deprecated
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("REQUEST", "onRequestPermissionsResult: ");
                Toast.makeText(getContext(), "Dodijeljeno dopuštenje za kameru", Toast.LENGTH_SHORT).show();
                flipper.setDisplayedChild(2);
            } else {
                Toast.makeText(getContext(), "Odbijeno dopuštenje za kameru", Toast.LENGTH_SHORT).show();
            }
        }
    }
}