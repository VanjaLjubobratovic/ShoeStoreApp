package com.example.shoestoreapp.employee;

import android.app.AlertDialog;
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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.customer.ItemModel;
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
import com.google.firebase.firestore.model.mutation.ArrayTransformOperation;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrdersFragment extends Fragment {

    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private CollectionReference ordersRef, itemsRef;
    private StorageReference storageRef;
    private ArrayList<ItemModel> itemsList = new ArrayList<>();
    private String storeID, m_Text;
    private UserModel user;
    private ArrayList<OrderModel> orderList = new ArrayList<>();
    private MaterialButton qrButton, pickUpDelivery;
    private RecyclerView ordersRecycler;
    private String qrCode = "";
    private View view;

    public OrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseFirestore.getInstance();

        user = getActivity().getIntent().getParcelableExtra("userData");

        //TODO: exception management
        storeID = getActivity().getIntent().getStringExtra("storeID");
        String collection = "/locations/" + storeID + "/orders";
        ordersRef = database.collection(collection);
        String itemCollection = "/locations/" + storeID + "/items";
        itemsRef = database.collection(itemCollection);

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
                    removeScanned();
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
        clearData();
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        qrButton = view.findViewById(R.id.orderFragmentQrButton);
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
                    removeOrder(m_Text);
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

        fetchOrders(view);

        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setReorderingAllowed(true);

                fragmentTransaction.replace(R.id.employeeDrawerLayout, QrScannerFragment.class, null);
                fragmentTransaction.addToBackStack("name").commit();
            }
        });

    }

    private void fetchOrders(View view) {
        ordersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().size() == 0) {
                        Log.d("FIRESTORE", "0 Results");
                        return;
                    }
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        OrderModel order = document.toObject(OrderModel.class);
                        if(order == null || order.isPickedUp())
                            continue;

                        order.setTotal(0);
                        order.setReceiptID(document.getId());
                        fetchItems(document.getId(), order);
                        Log.d("FIRESTORE Single", order.toString());
                    }
                } else Log.d("FIRESTORE Single", "fetch failed");
            }
        });
    }

    private void fetchItems(String documentID, OrderModel order) {
        ordersRef.document(documentID).collection("items").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("fetchItems query", "onComplete: ");

                            for(DocumentSnapshot document : task.getResult()) {
                                ItemModel item = document.toObject(ItemModel.class);
                                if(item == null)
                                    continue;

                                item.parseModelColor(document.getId());
                                order.addItem(item);
                            }
                            order.unpackItems();
                            orderList.add(order);
                            InitRecyclerView(view);
                            removeScanned();
                        } else {
                            Log.d("fetchItems query", "onFailure: ");
                        }
                    }
                });
    }

    public void confirmOrder(OrderModel order){
        DocumentReference orderRef = database.collection("/locations/" + storeID + "/orders").document(order.getReceiptID());
        orderRef.update("pickedUp", true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("orderPickedUp", "Order picked up");
            }
        });
        orderRef.update("reviewEnabled", true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("Review enabled", "rev enabled");
            }
        });
    }

    public void InitRecyclerView(View view){
        ordersRecycler = view.findViewById(R.id.orderFragmentOrderRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false);
        ordersRecycler.setLayoutManager(layoutManager);
        EmployeeOrderRecyclerViewAdapter adapter = new EmployeeOrderRecyclerViewAdapter(getActivity(), orderList, new EmployeeOrderRecyclerViewAdapter.MyAdapterListener() {
            @Override
            public void deleteButtonOnClick(View v, int position) {
                OrderModel clickedOrder = orderList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setTitle("Unesi broj narudžbe");
                final View customLayout = getLayoutInflater().inflate(R.layout.custom_dialog_layout,null);
                builder.setView(customLayout);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = customLayout.findViewById(R.id.editText);
                        m_Text = editText.getText().toString();
                        if (m_Text.equals(clickedOrder.getOrderCode().toString())) {
                            deleteOrderFromDB(clickedOrder);
                            ((EmployeeOrderRecyclerViewAdapter) ordersRecycler.getAdapter()).deleteItem(position);
                            Log.d("TAG", "Order removed at " + position);
                            AlertDialog.Builder okAlert = new AlertDialog.Builder(getContext());
                            okAlert.setMessage("Narudžba " + clickedOrder.getOrderCode().toString() + " uspješno izbrisana");
                            okAlert.setPositiveButton("OK", null);
                            okAlert.show();

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
        newReceipt.put("employee", user.getEmail());
        newReceipt.put("storeID", storeID);
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
            removeOrder(qrCode);
        }
    }

    public void removeOrder(String orderCode){
        boolean orderFound = false;
        OrderModel orderToRemove = null;
        for(OrderModel order : orderList){
            if(orderCode.equals(order.getOrderCode().toString())){
                orderFound = true;
                orderToRemove = order;
            }
        }
        if(!orderFound){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Nepostojeći kod!")
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
            AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(getContext());
            confirmBuilder.setTitle("Potvrdite preuzimanje narudžbe " + orderToRemove.getUser() + " Kod: " +orderToRemove.getOrderCode());
            confirmBuilder.setMessage("Narudžba sadrži: \n" + orderToRemove.getItemContents());
            OrderModel finalOrderToRemove = orderToRemove;
            confirmBuilder.setPositiveButton("Potvrdi", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ReceiptModel receipt = (ReceiptModel) finalOrderToRemove;
                    receipt.packItems();
                    receipt.setTime();
                    addReceiptToDB(receipt);
                    adjustInventory(receipt);
                    confirmOrder(finalOrderToRemove);
                    orderList.remove(finalOrderToRemove);
                    ordersRecycler.getAdapter().notifyDataSetChanged();
                    dialogInterface.dismiss();

                    AlertDialog.Builder okAlert = new AlertDialog.Builder(getContext());
                    okAlert.setMessage("Narudžba " + finalOrderToRemove.getOrderCode().toString() + " uspješno izbrisana");
                    okAlert.setPositiveButton("OK", null);
                    okAlert.show();
                }
            });
            confirmBuilder.setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            confirmBuilder.show();
        }

    }

    public void clearData(){
        orderList.clear();
        itemsList.clear();
    }

    public void deleteOrderFromDB(OrderModel orderToDelete){
        DocumentReference orderRef = database.collection("/locations/" + storeID + "/orders").document(orderToDelete.getReceiptID());
        orderRef.delete();
    }

}