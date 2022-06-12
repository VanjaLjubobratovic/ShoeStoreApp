package com.example.shoestoreapp.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.LoginActivity;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private ArrayList<TestOrderModel> fillerOrders = new ArrayList<>();
    private ArrayList<ItemModel> orderItems = new ArrayList<>();
    private ViewFlipper flipper;
    private static final int ORDER_MENU_SCREEN = 0;
    private static final int REVIEW_ITEM_SCREEN = 1;
    private static final int COMPLAINT_MENU_SCREEN = 2;
    private ImageView reviewModelImage;
    private TextView reviewModelColor, complaintModelColor;
    private EditText reviewText, complaintText, customComplaintType;
    private Spinner complaintTypeSpinner;
    private CheckBox complaintResend;
    private RatingBar reviewRating;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String storeID, reviewedItem;
    private FirebaseFirestore database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order_history);

        user=getIntent().getParcelableExtra("userData");
        firebaseAuth = FirebaseAuth.getInstance();

        checkUser();

        storeID = "webshop";
        database = FirebaseFirestore.getInstance();

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

        complaintModelColor = flipper.getRootView().findViewById(R.id.itemComplaintModelTextView);
        complaintText = flipper.getRootView().findViewById(R.id.itemComplaintEditText);
        complaintCancel = flipper.getRootView().findViewById(R.id.itemComplaintCancelButton);
        complaintConfirm = flipper.getRootView().findViewById(R.id.itemComplaintSendButton);
        complaintTypeSpinner = flipper.getRootView().findViewById(R.id.itemComplaintTypeSpinner);
        initComplaintTypeSpinner();
        customComplaintType = flipper.getRootView().findViewById(R.id.itemComplaintCustomReasonEditText);
        complaintResend = flipper.getRootView().findViewById(R.id.itemComplaintNewItemCheckbox);

        complaintCancel.setOnClickListener(view -> flipper.setDisplayedChild(ORDER_MENU_SCREEN));

        complaintConfirm.setOnClickListener(view ->{
                ComplaintModel complaint = new ComplaintModel();
                complaint.setUser(user.getEmail());
                complaint.setComplaint(complaintText.getText().toString());
                complaint.setModel(complaintModelColor.getText().toString());
                complaint.setResend(complaintResend.isChecked());
                String complaintType;
                if(complaintTypeSpinner.getSelectedItemPosition() == 4){
                    complaintType = customComplaintType.getText().toString();
                }
                else{
                    complaintType = complaintTypeSpinner.getSelectedItem().toString();
                }
                complaint.setComplaintType(complaintType);

                //TODO add code reading one orders are fetched
                complaint.setOrderCode("random filler code");

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
            clearReviewData();
            flipper.setDisplayedChild(ORDER_MENU_SCREEN);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Recenzija uspješno zapisana");
            builder.setPositiveButton("OK",null);
            builder.show();

        });

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

    private void initDummyData(){
        ArrayList<Integer> sizes = new ArrayList<>(), amounts = new ArrayList<>();
        sizes.add(39);
        sizes.add(40);
        sizes.add(41);
        amounts.add(2);
        amounts.add(1);
        amounts.add(5);
        ItemModel fillerItem = new ItemModel("shoe","imageLink",200.0,4.0, Timestamp.now(),sizes, amounts);
        fillerItem.parseModelColor("501-crna");
        orderItems.add(fillerItem);
        orderItems.add(fillerItem);
        TestOrderModel fillerOrder = new TestOrderModel("Vanja Ljubobratovic","12.17.2028","200.2", orderItems);
        fillerOrders.add(fillerOrder);
        fillerOrders.add(fillerOrder);

    }

    private void initOrderRecycler(){
        initDummyData();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        RecyclerView orderRecyclerView = flipper.getRootView().findViewById(R.id.userOrdersRecyclerView);
        orderRecyclerView.setLayoutManager(layoutManager);
        CustomerOrderHistoryRecyclerAdapter adapter = new CustomerOrderHistoryRecyclerAdapter(this, fillerOrders, this);
        orderRecyclerView.setAdapter(adapter);
    }


    @Override
    public void itemReviewGet(ItemModel reviewItem) {
        Toast.makeText(this, reviewItem.toString(), Toast.LENGTH_SHORT).show();

        /*StorageReference imageReference = storageRef.child(reviewItem.getImage());

        Glide.with(this)
                .asBitmap()
                .load(imageReference)
                .into(reviewModelImage);*/
        reviewModelColor.setText(reviewItem.toString());

        flipper.setDisplayedChild(REVIEW_ITEM_SCREEN);
    }

    @Override
    public void itemComplaintGet(ItemModel reviewItem) {
        complaintModelColor.setText(reviewItem.toString());

        flipper.setDisplayedChild(COMPLAINT_MENU_SCREEN);
    }

    public void clearReviewData(){
        reviewText.setText("");
        reviewModelColor.setText("");
        reviewModelImage.setImageResource(0);
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
}