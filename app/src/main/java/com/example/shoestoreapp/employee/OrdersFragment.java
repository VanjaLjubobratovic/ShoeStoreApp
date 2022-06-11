package com.example.shoestoreapp.employee;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ClipData;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.customer.ReviewModel;
import com.example.shoestoreapp.customer.ReviewsRecycleViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersFragment extends Fragment {

    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private CollectionReference ordersRef, itemsRef;
    private StorageReference storageRef;
    private String storeID, m_Text;
    private UserModel user;
    private ArrayList<OrderModel> orderList = new ArrayList<>();
    private ArrayList<ItemModel> itemsList = new ArrayList<>(), orderItems = new ArrayList<>();
    private ArrayList<String> userList = new ArrayList<>();
    private ArrayList<LocalDate> dateList = new ArrayList<>();
    private ArrayList<Integer> codesList = new ArrayList<>();
    private MaterialButton newDelivery, pickUpDelivery;
    private RecyclerView ordersRecycler;
    private String qrCode = "";

    public OrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseFirestore.getInstance();
        //TODO fetch orders once implemented
        //ordersRef = database.collection("/orders");

        orderList = new ArrayList<>();

        user = getActivity().getIntent().getParcelableExtra("userData");

        //TODO:fetch
        storeID = "TestShop1";
        String collection = "/locations/" + "TestShop1" + "/items";
        itemsRef = database.collection(collection);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        getParentFragmentManager().setFragmentResultListener("Qr code", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                //TODO: fix this hack
                try {
                    qrCode = result.getStringArray("Qr code")[0];
                    Toast.makeText(getContext(), qrCode, Toast.LENGTH_SHORT).show();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newDelivery = view.findViewById(R.id.orderFragmentNewOrderButton);
        pickUpDelivery = view.findViewById(R.id.orderFragmentPickupButton);

        pickUpDelivery.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Unesi broj narudžbe");

            final View customLayout = getLayoutInflater().inflate(R.layout.custom_dialog_layout,null);
            builder.setView(customLayout);


            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    EditText editText = customLayout.findViewById(R.id.editText);
                    m_Text = editText.getText().toString();
                    boolean orderFound = false;
                    OrderModel orderToRemove = null;
                    for(OrderModel order : orderList){
                        if(m_Text.equals(order.getOrderCode().toString())){
                            orderFound = true;
                            orderToRemove = order;

                        }
                    }
                    if(!orderFound){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Ne postojeći kod!")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else{
                        AlertDialog.Builder confirmBulider = new AlertDialog.Builder(getContext());
                        confirmBulider.setTitle("Potvrdite preuzimanje narudžbe " + orderToRemove.getOrderCode());
                        confirmBulider.setMessage("Narudžba sadrži: \n" + orderToRemove.getItemContents());
                        OrderModel finalOrderToRemove = orderToRemove;
                        confirmBulider.setPositiveButton("Potvrdi", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ReceiptModel receipt = (ReceiptModel) finalOrderToRemove;
                                receipt.packItems();
                                receipt.setTime();
                                addReceiptToDB(receipt);
                                adjustInventory(receipt);
                                orderList.remove(finalOrderToRemove);
                                parseOrderData();
                                ordersRecycler.getAdapter().notifyDataSetChanged();
                                dialogInterface.dismiss();
                            }
                        });
                        confirmBulider.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        confirmBulider.show();
                    }
                }
            });
            builder.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        });

        fetchItems(view);

        newDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setReorderingAllowed(true);

                fragmentTransaction.replace(R.id.employeeActivityLayout, QrScannerFragment.class, null);
                fragmentTransaction.addToBackStack("name").commit();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void initDummyRecyclerData(){
        orderList.clear();

        OrderModel order = new OrderModel();
        order.addItem(itemsList.get(9));
        ArrayList<Integer> amounts = new ArrayList<>();
        amounts.add(2);
        for(int i = 0; i<5; i++)
            amounts.add(0);
        order.getItems().get(0).setAmounts(amounts);
        order.unpackItems();
        order.setOrderCode(123);
        order.setUser("Karlo Katalinic");
        order.setDateCreated(LocalDate.now());
        order.setEmployee(user.getEmail());
        order.setStoreID(storeID);
        orderList.add(order);
    }

    public void parseOrderData(){
        if(!orderItems.isEmpty()) {
            int size = orderItems.size();
            orderItems.clear();
            userList.clear();
            dateList.clear();
            codesList.clear();
            ordersRecycler.getAdapter().notifyItemRangeRemoved(0, size);
        }
        for(OrderModel order : orderList){
            ArrayList<ItemModel> tmpItems = order.getItems();
            for(ItemModel item : tmpItems){
                orderItems.add(item);
                userList.add(order.getUser());
                dateList.add(order.getDateCreated());
                codesList.add(order.getOrderCode());
            }
        }
    }


    private void fetchItems(View view) {
        itemsRef = database.collection("locations/TestShop1/items");
        itemsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().size() == 0) {
                        Log.d("FIRESTORE", "0 Results");
                        return;
                    }
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        ItemModel newItem = document.toObject(ItemModel.class);
                        newItem.parseModelColor(document.getId());
                        itemsList.add(newItem);
                        Log.d("FIRESTORE Single", newItem.toString());
                    }
                    initDummyRecyclerData();
                    parseOrderData();
                    InitRecyclerView(view);
                    removeScanned();
                } else Log.d("FIRESTORE Single", "fetch failed");
            }
        });
    }


    public void InitRecyclerView(View view){
        ordersRecycler = view.findViewById(R.id.orderFragmentOrderRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false);
        ordersRecycler.setLayoutManager(layoutManager);
        EmployeeOrderRecyclerViewAdapter adapter = new EmployeeOrderRecyclerViewAdapter(getActivity(), orderItems, userList, dateList, new EmployeeOrderRecyclerViewAdapter.MyAdapterListener() {
            @Override
            public void deleteButtonOnClick(View v, int position) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Unesi broj narudžbe");

                final View customLayout = getLayoutInflater().inflate(R.layout.custom_dialog_layout,null);
                builder.setView(customLayout);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = customLayout.findViewById(R.id.editText);
                        m_Text = editText.getText().toString();
                        if(m_Text.equals(codesList.get(position).toString())){
                            int counter = 0;
                            for(OrderModel order : orderList){
                                if((counter + order.getItems().size()) >= position){
                                    order.removeAt(position - counter);
                                    parseOrderData();
                                    ((EmployeeOrderRecyclerViewAdapter)ordersRecycler.getAdapter()).deleteItem(position);
                                    Log.d("TAG", "Item removed at " + position);
                                }
                                else{
                                    counter += order.getItems().size();
                                }
                            }
                        }
                        else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setMessage("Krivi unos!")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do things
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            }

            @Override
            public void editButtonOnClick(View v, int position) {
                Log.d("TAG", "iconImageViewOnClick at position "+position);
            }
        });
        ordersRecycler.setAdapter(adapter);
    }
    private void adjustInventory(ReceiptModel receipt) {
        ArrayList<ItemModel> itemsToRemove = receipt.getItems();

        //TODO: find if there's a better method
        for(ItemModel item : itemsToRemove) {
            DocumentReference itemDocumentRef = itemsRef.document(item.toString());
            ArrayList<Integer> adjustedAmountsList = new ArrayList<>();

            //TODO: Generalise this boilerplate code
            itemDocumentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()) {
                        Log.d("FIRESTORE", task.getResult().toString());
                        ItemModel itemToEdit = task.getResult().toObject(ItemModel.class);

                        for(int i = 0; i < itemToEdit.getAmounts().size(); i++) {
                            int adjustedAmount = itemToEdit.getAmounts().get(i) - item.getAmounts().get(i);
                            adjustedAmountsList.add(adjustedAmount);

                            //TODO:Merge this with fetchItem method

                            itemDocumentRef.update("amounts", adjustedAmountsList)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d("adjustInventory", "Succesful amount update");
                                        }
                                    });
                        }
                    }
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("FIRESTORE", "task not successful");
                        }
                    });

        }
    }

    private void addReceiptToDB(ReceiptModel receipt) {
        Map<String, Object> newReceipt = new HashMap<>();
        newReceipt.put("time", receipt.getTime());
        newReceipt.put("user", receipt.getUser());
        newReceipt.put("employee", receipt.getEmployee());
        newReceipt.put("storeID", receipt.getStoreID());
        newReceipt.put("total", receipt.getTotal());
        newReceipt.put("annulled", false);

        DocumentReference newReceiptRef = database.collection("receipts").document();

        newReceiptRef.set(newReceipt)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("addReceiptToDB", "onSuccess: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("addReceiptToDB", "onFailure: ");
                    }
                });

        //TODO: make this ItemModelMethod
        for(ItemModel item : receipt.getItems()) {
            Map<String, Object> newReceiptItem = new HashMap<>();
            newReceiptItem.put("added", item.getAdded());
            newReceiptItem.put("image", item.getImage());
            newReceiptItem.put("price", item.getPrice());
            newReceiptItem.put("rating", item.getRating());
            newReceiptItem.put("type", item.getType());
            newReceiptItem.put("sizes", item.getSizes());
            newReceiptItem.put("amounts", item.getAmounts());

            newReceiptRef.collection("items").document(item.toString())
                    .set(newReceiptItem)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("addItemToReceipt", "onSuccess: ");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("addItemToReceipt", "onFailure: ");
                        }
                    });
        }
    }
    public void removeScanned(){
        if(!qrCode.isEmpty()){
            boolean orderFound = false;
            OrderModel orderToRemove = null;
            for(OrderModel order : orderList){
                if(qrCode.equals(order.getOrderCode().toString())){
                    orderFound = true;
                    orderToRemove = order;

                }
            }
            if(!orderFound){
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Ne postojeći kod!")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
            else{
                AlertDialog.Builder confirmBulider = new AlertDialog.Builder(getContext());
                confirmBulider.setTitle("Potvrdite preuzimanje narudžbe " + orderToRemove.getOrderCode());
                confirmBulider.setMessage("Narudžba sadrži: \n" + orderToRemove.getItemContents());
                OrderModel finalOrderToRemove = orderToRemove;
                confirmBulider.setPositiveButton("Potvrdi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ReceiptModel receipt = (ReceiptModel) finalOrderToRemove;
                        receipt.packItems();
                        receipt.setTime();
                        addReceiptToDB(receipt);
                        adjustInventory(receipt);
                        orderList.remove(finalOrderToRemove);
                        parseOrderData();
                        ordersRecycler.getAdapter().notifyDataSetChanged();
                        dialogInterface.dismiss();
                    }
                });
                confirmBulider.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                confirmBulider.show();
            }
        }
    }

}