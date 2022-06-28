package com.example.shoestoreapp.customer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.DataModels.ComplaintModel;
import com.example.shoestoreapp.DataModels.ItemModel;
import com.example.shoestoreapp.DataModels.ReviewModel;
import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.UserModel;
import com.example.shoestoreapp.DataModels.OrderModel;
import com.example.shoestoreapp.customer.adapters.CustomerOrderHistoryRecyclerAdapter;
import com.example.shoestoreapp.notifications.FcmNotificationsSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomerOrderHistoryActivity extends AppCompatActivity implements CustomerOrderHistoryRecyclerAdapter.onItemReviewGet{

    private FirebaseAuth firebaseAuth;
    private UserModel user;
    private ImageButton exit;
    private MaterialButton reviewConfirm, reviewCancel, complaintConfirm, complaintCancel;
    private ArrayList<ItemModel> orderItems = new ArrayList<>();
    private ArrayList<OrderModel> orderList = new ArrayList<>();
    private ViewFlipper flipper;
    private static final int ORDER_MENU_SCREEN = 0;
    private static final int REVIEW_ITEM_SCREEN = 1;
    private static final int COMPLAINT_MENU_SCREEN = 2;
    private ImageView reviewModelImage;
    private TextView reviewModelColor, complaintModelColor, complaintItemSize;
    private EditText reviewText, complaintText, customComplaintType;
    private Spinner complaintTypeSpinner;
    private CheckBox complaintResend;
    private RatingBar reviewRating;
    private String storeID, reviewedItem;
    private FirebaseFirestore database;
    private CollectionReference ordersRef;
    private RecyclerView orderRecyclerView;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ArrayList<String> userReviewed;
    private String complaintOrderCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order_history);

        user=getIntent().getParcelableExtra("userData");
        userReviewed = (ArrayList<String>) getIntent().getSerializableExtra("userReviews");
        firebaseAuth = FirebaseAuth.getInstance();

        checkUser();

        storeID = "webshop";
        database = FirebaseFirestore.getInstance();


        //TODO change once webshop gets some orders
        String collection = "/locations/" + storeID + "/orders";
        ordersRef = database.collection(collection);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        exit = findViewById(R.id.orderBackImageButton);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        flipper = findViewById(R.id.customerOrderFlipper);
        initOrderRecycler();
        reviewCancel = flipper.getRootView().findViewById(R.id.itemReviewCancelButton);
        reviewConfirm = flipper.getRootView().findViewById(R.id.itemReviewConfirmButton);
        reviewModelImage = flipper.getRootView().findViewById(R.id.itemReviewImage);
        reviewModelColor = flipper.getRootView().findViewById(R.id.itemReviewModelColor);
        reviewRating = flipper.getRootView().findViewById(R.id.itemReviewRatingBar);
        reviewText = flipper.getRootView().findViewById(R.id.itemReviewReviewEditText);


        complaintItemSize = flipper.getRootView().findViewById(R.id.itemComplaintHiddenSize);
        complaintModelColor = flipper.getRootView().findViewById(R.id.itemComplaintModelTextView);
        complaintText = flipper.getRootView().findViewById(R.id.itemComplaintEditText);
        complaintCancel = flipper.getRootView().findViewById(R.id.itemComplaintCancelButton);
        complaintConfirm = flipper.getRootView().findViewById(R.id.itemComplaintSendButton);
        complaintTypeSpinner = flipper.getRootView().findViewById(R.id.itemComplaintTypeSpinner);
        initComplaintTypeSpinner();
        customComplaintType = flipper.getRootView().findViewById(R.id.itemComplaintCustomReasonEditText);
        complaintResend = flipper.getRootView().findViewById(R.id.itemComplaintNewItemCheckbox);

        complaintCancel.setOnClickListener(view ->{
            clearComplaintData();
            flipper.setDisplayedChild(ORDER_MENU_SCREEN);
        });

        complaintConfirm.setOnClickListener(view ->{
                ComplaintModel complaint = new ComplaintModel();
                complaint.setEmail(user.getEmail());
                complaint.setComplaint(complaintText.getText().toString());
                complaint.setModel(complaintModelColor.getText().toString());
                complaint.setResend(complaintResend.isChecked());
                complaint.setResolved("U razradi");
                complaint.setSize(Integer.parseInt(complaintItemSize.getText().toString()));
                String complaintType;
                if(complaintTypeSpinner.getSelectedItemPosition() == complaintTypeSpinner.getAdapter().getCount() - 1){
                    complaintType = customComplaintType.getText().toString();
                }
                else{
                    complaintType = complaintTypeSpinner.getSelectedItem().toString();
                }
                complaint.setComplaintType(complaintType);
                //TODO add code reading one orders are fetched
                complaint.setOrderCode(complaintOrderCode);
                addComplaintDB(complaint);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Prigovor uspješno zapisan");
                builder.setPositiveButton("OK",null);
                builder.show();

                sendComplaintNotification();
                subscribeToComplaintTopic();
                clearComplaintData();
                flipper.setDisplayedChild(ORDER_MENU_SCREEN);
        });

        reviewCancel.setOnClickListener(view -> {
            clearReviewData();
            flipper.setDisplayedChild(ORDER_MENU_SCREEN);
        });

        reviewConfirm.setOnClickListener(view -> {
            ReviewModel review = new ReviewModel();
            review.setReview(reviewText.getText().toString());
            review.setEmail(user.getEmail());
            review.setRating((double)reviewRating.getRating());
            reviewedItem = reviewModelColor.getText().toString();
            addReviewToDB(review);
            addReviewedItemUser();
            userReviewed.add(reviewedItem.toString());
            orderRecyclerView.getAdapter().notifyDataSetChanged();
            clearReviewData();
            flipper.setDisplayedChild(ORDER_MENU_SCREEN);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Recenzija uspješno zapisana");
            builder.setPositiveButton("OK",null);
            builder.show();

        });

        complaintTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(complaintTypeSpinner.getSelectedItemPosition() == complaintTypeSpinner.getAdapter().getCount() - 1){
                    customComplaintType.setVisibility(View.VISIBLE);
                }
                else{
                    customComplaintType.setVisibility(View.GONE);
                    customComplaintType.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        fetchLocations();
    }

    private void subscribeToComplaintTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(complaintOrderCode + "-status")
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        Log.d("COMPLAINT-SUB", "subscribeToComplaintTopic: SUCCESS");
                    } else Log.d("COMPLAINT-SUB", "subscribeToComplaintTopic: FAILURE");
                });
    }

    private void sendComplaintNotification() {
        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(
                "/topics/complaints",
                "Korisnik je uložio pritužbu!",
                user.getEmail() + " za narudžbu:\n" + complaintOrderCode,
                getApplicationContext(),
                this
        );

        notificationsSender.SendNotifications();
        Log.d("COMPLAINT", "sendComplaintNotification: ");
    }

    private void addReviewToDB(ReviewModel review) {
        Map<String, Object> newReview = new HashMap<>();
        newReview.put("email", review.getEmail());
        newReview.put("rating", review.getRating());
        newReview.put("review", review.getReview());

        DocumentReference newReviewRef = database.collection("/locations/" + storeID + "/items/" + reviewedItem + "/reviews").document();

        newReviewRef.set(newReview)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("addReviewToDB", "onSuccess: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("addReviewToDB", "onFailure: ");
                    }
                });

        adjustReviewSums(review.getRating());
    }

    private void adjustReviewSums(Double rating) {
        DocumentReference itemRef = database.document("/locations/webshop/items/" + reviewedItem);
        itemRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                ItemModel item = task.getResult().toObject(ItemModel.class);
                if(item == null)
                    return;

                WriteBatch batch = database.batch();
                batch.update(itemRef, "numberOfRatings", item.getNumberOfRatings() + 1);
                batch.update(itemRef, "ratingSum", item.getRatingSum() + rating);

                batch.commit().addOnCompleteListener(task1 -> {
                    Toast.makeText(this, "Recenzija uspješno dodana", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null) {
            //user not logged in
            startActivity(new Intent(CustomerOrderHistoryActivity.this, LoginActivity.class));
            finish();
        } else {
            //TODO: replace this placeholder with actual UI changes
        }
    }

    private void fetchLocations(){
        CollectionReference locationsRef = database.collection("locations");
        ArrayList<String> locations = new ArrayList<>();
        locationsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().size() == 0) {
                        Log.d("FIRESTORE", "0 Results");
                        return;
                    }
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        locations.add(document.getId());
                    }
                    fetchOrders(locations);
                }else Log.d("FIRESTORE Single", "fetch failed");
            }
        });

    }

    private void fetchOrders(ArrayList<String> locations) {
        for(String location : locations) {
            ordersRef = database.collection("/locations/" + location + "/orders");
            ordersRef.whereEqualTo("user", user.getEmail()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() == 0) {
                            Log.d("FIRESTORE", "0 Results");
                            return;
                        }
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            OrderModel order = document.toObject(OrderModel.class);
                            if (order == null)
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
                                Log.d("item fetched", item.toString());
                            }
                            order.unpackItems();
                            orderList.add(order);
                            Log.d("ORDER-STATUS", String.valueOf(order.isReviewEnabled()));
                            initOrderRecycler();
                        } else {
                            Log.d("fetchItems query", "onFailure: ");
                        }
                    }
                });
    }

    private void initOrderRecycler(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        orderRecyclerView = flipper.getRootView().findViewById(R.id.userOrdersRecyclerView);
        orderRecyclerView.setLayoutManager(layoutManager);
        CustomerOrderHistoryRecyclerAdapter adapter = new CustomerOrderHistoryRecyclerAdapter(this, orderList, this, userReviewed);
        orderRecyclerView.setAdapter(adapter);
    }


    @Override
    public void itemReviewGet(ItemModel reviewItem) {
        Toast.makeText(this, reviewItem.toString(), Toast.LENGTH_SHORT).show();

        StorageReference imageReference = storageRef.child(reviewItem.getImage());

        Glide.with(this)
                .asBitmap()
                .load(imageReference)
                .into(reviewModelImage);
        reviewModelColor.setText(reviewItem.toString());
        flipper.setDisplayedChild(REVIEW_ITEM_SCREEN);
    }

    @Override
    public void itemComplaintGet(ItemModel reviewItem, String orderCode) {
        complaintOrderCode = orderCode;
        complaintModelColor.setText(reviewItem.toString());
        complaintItemSize.setText(reviewItem.getSizes().get(reviewItem.getAmounts().indexOf(1)).toString());
        flipper.setDisplayedChild(COMPLAINT_MENU_SCREEN);
    }

    @Override
    public void onConfirmClick(OrderModel confOrder) {
        OrderModel clickedOrder = confOrder;

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Unesite broj narudžbe");
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_dialog_layout,null);
        builder.setView(customLayout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editText = customLayout.findViewById(R.id.editText);
                String m_Text = editText.getText().toString();
                if (m_Text.equals(clickedOrder.getOrderCode().toString())) {
                    confirmOrder(clickedOrder);
                    android.app.AlertDialog.Builder okAlert = new android.app.AlertDialog.Builder(CustomerOrderHistoryActivity.this);
                    okAlert.setMessage("Uspješno potvrđena dostava narudžbe " + clickedOrder.getOrderCode().toString());
                    okAlert.setPositiveButton("OK", null);
                    okAlert.show();
                }
                else{
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CustomerOrderHistoryActivity.this);
                    builder.setMessage("Krivi unos!")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do things
                                }
                            });
                    android.app.AlertDialog alert = builder.create();
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

    public void clearReviewData(){
        reviewText.setText("");
        reviewModelColor.setText("");
        reviewModelImage.setImageResource(0);
    }

    public void clearComplaintData(){
        complaintText.setText("");
        complaintResend.setChecked(false);
        complaintTypeSpinner.setSelection(0);
    }

    public void initComplaintTypeSpinner(){
        ArrayList<String> types = new ArrayList<>();
        types.add("Oštećena roba");
        types.add("Neispravna roba");
        types.add("Nedostajući artikl");
        types.add("Artikl drukčiji od reklamiranog");
        types.add("Ništa od navedenog");
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, types);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        complaintTypeSpinner.setAdapter(spinnerArrayAdapter);
    }

    public void addComplaintDB(ComplaintModel complaint){
        Map<String, Object> newComplaint = new HashMap<>();
        newComplaint.put("email", complaint.getEmail());
        newComplaint.put("complaintType", complaint.getComplaintType());
        newComplaint.put("orderCode", complaint.getOrderCode());
        newComplaint.put("model", complaint.getModel());
        newComplaint.put("resend", complaint.isResend());
        newComplaint.put("complaint", complaint.getComplaint());
        newComplaint.put("resolved", complaint.getResolved());
        newComplaint.put("size", complaint.getSize());

        DocumentReference newReviewRef = database.collection("/complaints").document();

        newReviewRef.set(newComplaint)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("addComplaintToDB", "onSuccess: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("addComplaintToDB", "onFailure: ");
                    }
                });

    }

    public void confirmOrder(OrderModel order){
        order.setPickedUp(true);
        order.setReviewEnabled(true);
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

        orderRecyclerView.getAdapter().notifyDataSetChanged();
    }

    public void addReviewedItemUser(){
        DocumentReference userRef = database.collection("users").document(user.getEmail());
        userRef.update("reviewedItems", FieldValue.arrayUnion(reviewedItem.toString()));
    }
}