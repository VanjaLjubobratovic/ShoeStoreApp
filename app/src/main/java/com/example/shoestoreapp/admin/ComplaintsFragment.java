package com.example.shoestoreapp.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.shoestoreapp.R;
import com.example.shoestoreapp.DataModels.ComplaintModel;
import com.example.shoestoreapp.DataModels.ItemModel;
import com.example.shoestoreapp.admin.adapters.AdminComplaintsAdapter;
import com.example.shoestoreapp.notifications.FcmNotificationsSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ComplaintsFragment extends Fragment implements AdminComplaintsAdapter.onComplaintListener{

    private FirebaseFirestore database;
    private ArrayList<ComplaintModel> complaints = new ArrayList();
    private RecyclerView complaintRecyclerView;
    private ArrayList<ItemModel> items = new ArrayList<>();
    private LinearLayout noComplaints;
    private boolean init = true;

    public ComplaintsFragment() {
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
        return inflater.inflate(R.layout.fragment_complaints, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        database = FirebaseFirestore.getInstance();
        super.onViewCreated(view, savedInstanceState);
        noComplaints = view.findViewById(R.id.adminNoComplaintsLayout);
        fetchComplaints();
        fetchItems();

    }

    private void fetchComplaints() {
        CollectionReference compRef = database.collection("complaints");
        compRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if(task.getResult().size() == 0) {
                        Log.d("FIRESTORE", "0 Results");
                        return;
                    }
                    //Writing the results to a ArrayList
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        ComplaintModel newComplaint = document.toObject(ComplaintModel.class);
                        if(newComplaint.getResolved().equals("U razradi")) {
                            newComplaint.setComplaintId(document.getId());
                            complaints.add(newComplaint);
                            Log.d("FIRESTORE Single", newComplaint.toString());
                        }
                    }
                    initComplaintRecycler();
                    if(complaints.size() == 0){
                        noComplaints.setVisibility(View.VISIBLE);
                    }
                    } else Log.d("FIRESTORE Single", "fetch failed");
            }
        });
    }

    public void initComplaintRecycler(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false);
        complaintRecyclerView = getActivity().findViewById(R.id.customerComplaintsRecycler);
        complaintRecyclerView.setLayoutManager(layoutManager);
        AdminComplaintsAdapter adapter = new AdminComplaintsAdapter(getContext(),complaints, this);
        complaintRecyclerView.setAdapter(adapter);
    }

    private void sendComplaintReslovedNotification(String decision, ComplaintModel complaint) {
        //TODO: maybe work with complaint ID instead of order code
        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(
                "/topics/" + complaint.getOrderCode() + "-status",
                "Vaša žalba je razriješena.",
                "Odlučeno je da je vaš prigovor " + decision
                        + "\nza narudžbu: " + complaint.getOrderCode()
                        + "\nPredmet: " + complaint.getModel() + " " + complaint.getSize(),
                getContext(),
                getActivity()
        );

        notificationsSender.SendNotifications();
    }

    @Override
    public void OnComplaintResendClick(ComplaintModel complaint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Jeste li sigurni da želite odobriti ponovno slanje artikla?");
        builder.setNegativeButton("Ne", null);
        builder.setPositiveButton("Da", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                complaints.remove(complaint);
                complaintRecyclerView.getAdapter().notifyDataSetChanged();
                confirmComplaint(complaint);
                addOrderToDB(complaint);
                if(complaints.size() == 0){
                    noComplaints.setVisibility(View.VISIBLE);
                }
                sendComplaintReslovedNotification("PRIHVAĆEN", complaint);
            }
        });
        builder.show();

    }

    @Override
    public void OnComplaintDenyClick(ComplaintModel complaint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Unesite razlog odbijanja.");
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_dialog_layout,null);
        builder.setView(customLayout);
        builder.setNegativeButton("Odustani", null);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                complaints.remove(complaint);
                complaintRecyclerView.getAdapter().notifyDataSetChanged();
                EditText editText = customLayout.findViewById(R.id.editText);
                String m_Text = editText.getText().toString();
                denyComplaint(complaint, m_Text);
                if(complaints.size() == 0){
                    noComplaints.setVisibility(View.VISIBLE);
                }

                sendComplaintReslovedNotification("ODBIJEN", complaint);
            }
        });
        builder.show();
    }

    @Override
    public void OnComplaintSeenClick(ComplaintModel complaint) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Jeste li sigurni da želite žalbu označiti kao pročitanu?");
        builder.setNegativeButton("Ne", null);
        builder.setPositiveButton("Da", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                complaints.remove(complaint);
                complaintRecyclerView.getAdapter().notifyDataSetChanged();
                confirmComplaint(complaint);
                if(complaints.size() == 0){
                    noComplaints.setVisibility(View.VISIBLE);
                }
                sendComplaintReslovedNotification("PRIHVAĆEN", complaint);
            }
        });
        builder.show();
    }

    public void confirmComplaint(ComplaintModel complaint){
        DocumentReference compRef = database.collection("complaints").document(complaint.getComplaintId());
        compRef.update("resolved", "Approved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("complaint edited", "complaint edited");
            }
        });
    }

    public void denyComplaint(ComplaintModel complaint, String reason){
        DocumentReference compRef = database.collection("complaints").document(complaint.getComplaintId());
        compRef.update("resolved", "Denied").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("complaint edited", "complaint edited");
            }
        });
        compRef.update("reason", reason).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("complaint edited", "complaint edited");
            }
        });
    }

    private void fetchItems() {
        CollectionReference itemsRef = database.collection("/locations/webshop/items");
        itemsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                        items.add(newItem);
                        Log.d("FIRESTORE", newItem.toString());
                    }
                } else Log.d("FIRESTORE", "fetch failed");
            }
        });
    }

    private void addOrderToDB(ComplaintModel complaint) {
        Random rand = new Random();
        Map<String, Object> newOrder = new HashMap<>();
        newOrder.put("dateCreated", Timestamp.now());
        newOrder.put("dateCreated", Timestamp.now());
        newOrder.put("inStore", true);
        newOrder.put("orderCode", Math.abs(rand.nextInt()));
        newOrder.put("pickedUp", false);
        newOrder.put("reviewEnabled", false);
        newOrder.put("user", complaint.getEmail());

        DocumentReference newOrderRef = database.collection("locations/webshop/orders").document();

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
        String itemModel = complaint.getModel();
        for(ItemModel item : items){
            if(item.toString().equals(itemModel)){
                Map<String, Object> newOrderItem = new HashMap<>();
                newOrderItem.put("added", item.getAdded());
                newOrderItem.put("image", item.getImage());
                newOrderItem.put("price", item.getPrice());
                newOrderItem.put("rating", item.getRating());
                newOrderItem.put("type", item.getType());
                newOrderItem.put("sizes", item.getSizes());
                ArrayList<Integer> tmpAmounts = new ArrayList<>();
                for(Integer i : item.getSizes()){
                    if(i == complaint.getSize()){
                        tmpAmounts.add(1);
                    }
                    else{
                        tmpAmounts.add(0);
                    }
                }
                newOrderItem.put("amounts", tmpAmounts);

                newOrderRef.collection("items").document(item.toString())
                        .set(newOrderItem)
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
                break;
            }
        }
    }

    @Override
    public void onResume() {
        if(!init){
            complaints.clear();
            items.clear();
            complaintRecyclerView.getAdapter().notifyDataSetChanged();
            fetchComplaints();
            fetchItems();
        }
        init = false;
        super.onResume();
    }
}
