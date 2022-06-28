package com.example.shoestoreapp.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.employee.EmployeeOrderRecyclerViewAdapter;
import com.example.shoestoreapp.employee.OrderModel;
import com.example.shoestoreapp.notifications.FcmNotificationsSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.StructuredQuery;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminOrdersFragment extends Fragment implements AdminOrdersRecyclerAdapter.onAdminOrders{

    private FirebaseFirestore database;
    private ArrayList<OrderModel> ordersList = new ArrayList<>();
    private RecyclerView orderRecycler;
    private CollectionReference ordersRef;
    private View view;
    private boolean init = true;
    private ArrayList<String> locationList = new ArrayList<>();
    public AdminOrdersFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;
        database = FirebaseFirestore.getInstance();
        ordersRef = database.collection("/locations/webshop/orders");
        fetchOrders();
        fetchLocations();
        super.onViewCreated(view, savedInstanceState);
    }

    public void initOrderRecycler(){
        orderRecycler = view.findViewById(R.id.adminOrdersRecycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false);
        orderRecycler.setLayoutManager(layoutManager);
        AdminOrdersRecyclerAdapter adapter = new AdminOrdersRecyclerAdapter(getActivity(), ordersList, AdminOrdersFragment.this);
        orderRecycler.setAdapter(adapter);
        noOrders();

    }

    private void fetchOrders() {
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
                        if(order == null || order.isPickedUp() || order.isInStore())
                            continue;

                        order.setTotal(0);
                        order.setReceiptID(document.getId());
                        fetchItems(document.getId(), order);
                        Log.d("FIRESTORE Single", order.toString());
                    }
                    noOrders();
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
                                Log.d("fetchItems query", item.toString());
                                if(item == null)
                                    continue;

                                item.parseModelColor(document.getId());
                                order.addItem(item);
                            }
                            order.unpackItems();
                            ordersList.add(order);
                            initOrderRecycler();
                        } else {
                            Log.d("fetchItems query", "onFailure: ");
                        }
                    }
                });
    }

    @Override
    public void onResume() {
        if(!init){
            ordersList.clear();
            fetchOrders();
            orderRecycler.getAdapter().notifyDataSetChanged();
        }
        init = false;
        super.onResume();
    }

    private void sendStatusNotification(String orderCode, String title, String body) {
        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(
                "/topics/" + orderCode + "-status",
                title,
                body,
                getContext(),
                getActivity()
        );

        notificationsSender.SendNotifications();
    }

    @Override
    public void orderConfirmed(OrderModel orderItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Jeste li sigurni da želite potvrdidi narudžbu " + orderItem.getOrderCode());
        builder.setPositiveButton("Da", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(locationList.contains(orderItem.getDeliveryAddress())){
                    relocateOrder(orderItem);
                }
                else{
                    orderInStoreSet(orderItem);
                }

                sendStatusNotification(String.valueOf(orderItem.getOrderCode()), "Vaša narudžba je PRIHVAĆENA!", parseOrderItems(orderItem));
            }
        });
        builder.setNegativeButton("Ne", null);
        builder.show();

        Toast.makeText(getActivity(), "Confirmed", Toast.LENGTH_SHORT).show();
    }

    public String parseOrderItems(OrderModel orderItem) {
        //TODO: exceptions
        StringBuilder sb = new StringBuilder();
        for(ItemModel item : orderItem.getItems()) {
            int size = item.getSizes().get(item.getAmounts().indexOf(1));
            sb.append(item);
            sb.append(" ");
            sb.append(size);
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }

    public void relocateOrder(OrderModel order){
        deleteOrder(order);
        addOrderToDB(order);
        Log.d("orderInStore", "Order store up");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Narudžba uspješno prihvaćena");
        builder.setPositiveButton("OK", null);
        builder.show();
    }
    public void orderInStoreSet(OrderModel order){
        DocumentReference orderRef = database.collection("/locations/webshop/orders").document(order.getReceiptID());
        orderRef.update("inStore", true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("orderInStore", "Order store up");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Narudžba uspješno prihvaćena");
                builder.setPositiveButton("OK", null);
                builder.show();
            }
        });
        ordersList.remove(order);
        orderRecycler.getAdapter().notifyDataSetChanged();
        noOrders();

    }

    private void addOrderToDB(OrderModel order) {
        Map<String, Object> newOrder = new HashMap<>();
        newOrder.put("dateCreated", order.getDateCreated());
        newOrder.put("inStore", order.isInStore());
        newOrder.put("orderCode", order.getOrderCode());
        newOrder.put("pickedUp", order.isPickedUp());
        newOrder.put("reviewEnabled", order.isReviewEnabled());
        newOrder.put("user", order.getUser());
        newOrder.put("deliveryAddress", order.getDeliveryAddress());

        DocumentReference newOrderRef = database.collection("locations/" + order.getDeliveryAddress() + "/orders").document();

        newOrderRef.set(newOrder)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("addOrderToDB", "onSuccess: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("addOrderToDB", "onFailure: ");
                    }
                });

        //TODO: make this ItemModelMethod
        for(ItemModel item : order.getItems()) {
            Map<String, Object> newReceiptItem = new HashMap<>();
            newReceiptItem.put("added", item.getAdded());
            newReceiptItem.put("image", item.getImage());
            newReceiptItem.put("price", item.getPrice());
            newReceiptItem.put("rating", item.getRating());
            newReceiptItem.put("type", item.getType());
            newReceiptItem.put("sizes", item.getSizes());
            newReceiptItem.put("amounts", item.getAmounts());

            newOrderRef.collection("items").document(item.toString())
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
    }

    public void deleteOrder(OrderModel orderToDelete){
        DocumentReference deleteRef = database.collection("/locations/webshop/orders").document(orderToDelete.getReceiptID());
        deleteRef.delete();
        ordersList.remove(orderToDelete);
        orderRecycler.getAdapter().notifyDataSetChanged();
        noOrders();
    }

    @Override
    public void orderDenied(OrderModel orderItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Jeste li sigurni da želite odbiti narudžbu " + orderItem.getOrderCode());
        builder.setPositiveButton("Da", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteOrder(orderItem);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Narudžba uspješno odbijena");
                builder.setPositiveButton("OK", null);
                builder.show();

                sendStatusNotification(String.valueOf(orderItem.getOrderCode()), "Vaša narudžba je ODBIJENA!", parseOrderItems(orderItem));
            }
        });
        builder.setNegativeButton("Ne", null);
        builder.show();
    }

    public void fetchLocations(){
        CollectionReference locRef = database.collection("/locations");
        locRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("fetchItems query", "onComplete: ");

                            for(DocumentSnapshot document : task.getResult()) {
                                if(document == null){
                                    continue;
                                }
                                locationList.add(document.getId());
                            }
                        } else {
                            Log.d("fetchItems query", "onFailure: ");
                        }
                    }
                });
    }

    public void noOrders(){
        Log.d("No Orders", "noorders");
        ConstraintLayout noOrder = view.findViewById(R.id.noOrdersLayout);
        if(ordersList.size() == 0){
            noOrder.setVisibility(View.VISIBLE);
        }
        else{
            noOrder.setVisibility(View.GONE);
        }

    }
}