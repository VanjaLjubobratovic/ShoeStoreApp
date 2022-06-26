package com.example.shoestoreapp.employee;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.shoestoreapp.R;
import com.example.shoestoreapp.UserModel;
import com.example.shoestoreapp.customer.ItemModel;
import com.example.shoestoreapp.databinding.FragmentInventoryAdjustmentBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InventoryAdjustmentFragment extends Fragment {
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private CollectionReference itemsRef;
    private StorageReference storageRef;

    private UserModel user;
    private ItemModel itemToEdit;
    private InventoryLogModel logModel;

    private FragmentInventoryAdjustmentBinding binding;

    private MaterialButton addOneBtn, removeOneBtn, groupEditBtn;
    private ImageView itemIcon, backBtn;
    private TextView itemName, itemPrice;
    private LinearLayout sizesLayout;
    private ProgressBar progressBar;

    private ArrayList<EditText> amountsEtList;

    public InventoryAdjustmentFragment() {
        // Required empty public constructor
    }

    public static InventoryAdjustmentFragment newInstance(ItemModel item, String collectionPath) {
        InventoryAdjustmentFragment fragment = new InventoryAdjustmentFragment();
        Bundle args = new Bundle();
        args.putParcelable("editItem", item);
        args.putString("collectionPath", collectionPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO:exception handling
        try {
            user = (UserModel) getActivity().getIntent().getParcelableExtra("userData");
            itemToEdit = (ItemModel) getArguments().getParcelable("editItem");
            String collection = getArguments().getString("collectionPath");

            database = FirebaseFirestore.getInstance();
            itemsRef = database.collection(collection);
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInventoryAdjustmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        backBtn = binding.inventoryEditBack;
        addOneBtn = binding.inventoryEditAddOne;
        removeOneBtn = binding.inventoryEditRemoveOne;
        groupEditBtn = binding.inventoryEditBatchEdit;
        itemIcon = binding.itemEditImage;
        itemName = binding.editItemName;
        itemPrice = binding.itemEditPrice;
        progressBar = binding.inventoryAdjustmentProgress;

        sizesLayout = binding.itemEditSizesLayout;

        backBtn.setOnClickListener(view1 -> {
            requireActivity().onBackPressed();
        });

        removeOneBtn.setOnClickListener(view1 -> {
            showCustomDialog("Uklonite artikal iz inventara", true);
        });

        addOneBtn.setOnClickListener(view1 -> {
            showCustomDialog("Dodajte artikal u inventar", false);
        });

        groupEditBtn.setOnClickListener(view1 -> {
            if(!user.getRole().equals("admin"))
                Toast.makeText(getContext(), "Dopušteno samo voditelju poslovanja", Toast.LENGTH_SHORT).show();
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Jeste li sigurni?");
                builder.setMessage("Ovime ćete postaviti količine za svaku veličinu predmeta na 0");
                AlertDialog dialog = builder.create();

                builder.setPositiveButton("OK", (dialogInterface, i) -> {
                    ArrayList<Integer> amountDiff = new ArrayList<>();
                    for(Integer amount : itemToEdit.getAmounts())
                        amountDiff.add(-amount);

                    adjustItemAmounts(amountDiff);
                    updateInventory();
                    fetchAmounts();
                    dialogInterface.dismiss();
                });

                builder.setNegativeButton("Odustani", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
            }
        });

        progressBar.setVisibility(View.VISIBLE);

        setItemPreview();
        fetchAmounts();
    }

    private void setItemPreview() {
        if(itemToEdit == null)
            return;

        StorageReference imageRef = storageRef.child(itemToEdit.getImage());
        Glide.with(getContext())
                .asBitmap()
                .load(imageRef)
                .into(itemIcon);

        itemName.setText(itemToEdit.toString());
        itemPrice.setText("Cijena: " + itemToEdit.getPrice() + "kn");
    }

    private void fetchAmounts() {
        itemsRef.document(itemToEdit.toString()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        ItemModel item = task.getResult().toObject(ItemModel.class);

                        if(item != null) {
                            itemToEdit.getAmounts().clear();
                            itemToEdit.getAmounts().addAll(item.getAmounts());
                        }

                        generateSizesTable();
                    }
                });
    }

    private void generateSizesTable() {
        sizesLayout.removeAllViews();
        amountsEtList = new ArrayList<>();

        for(int i = 0; i < itemToEdit.getSizes().size(); i++) {
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
            et.setText(itemToEdit.getAmounts().get(i).toString());

            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            tv.setText(itemToEdit.getSizes().get(i).toString());
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
            sizesLayout.addView(ll);
            sizesLayout.addView(v);
            amountsEtList.add(et);
        }

        sizesLayout.removeViewAt(sizesLayout.getChildCount() - 1);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showCustomDialog(String message, boolean isRemoving) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.edit_dialog);

        final EditText descEt = dialog.findViewById(R.id.additionalInfo);
        final TextView title = dialog.findViewById(R.id.dialogTitle);
        final Spinner reasonSpinner = dialog.findViewById(R.id.reasonSpinner);
        final Spinner sizeSpinner = dialog.findViewById(R.id.sizeSpinner);
        ImageButton cancelBtn = dialog.findViewById(R.id.dialogCancel);
        MaterialButton confirmBtn = dialog.findViewById(R.id.dialogConfirm);

        title.setText(message);

        setDialogDropdowns(reasonSpinner, sizeSpinner, isRemoving);

       cancelBtn.setOnClickListener(view -> {
            dialog.dismiss();
        });

        confirmBtn.setOnClickListener(view -> {
            ArrayList<Integer> amountDiff = new ArrayList<>(Collections.nCopies(itemToEdit.getSizes().size(), 0));
            int amountIndex = itemToEdit.getSizes().indexOf(Integer.parseInt(sizeSpinner.getSelectedItem().toString()));
            int diff = 1;

            if(isRemoving)
                diff = -diff;

            amountDiff.set(amountIndex, diff);

            adjustItemAmounts(amountDiff);
            updateInventory();

            writeLog(new InventoryLogModel(itemToEdit.getModel(), itemToEdit.getColor(), reasonSpinner.getSelectedItem().toString(),
                    descEt.getText().toString(), user.getEmail(), itemToEdit.getSizes(), amountDiff, Timestamp.now()));

            dialog.dismiss();

            //TODO: or just go to inventory fragment
            fetchAmounts();
        });

        dialog.show();
    }

    private void adjustItemAmounts(ArrayList<Integer> amountDiff) {
        for(int i = 0; i < amountDiff.size(); i++) {
            int newAmount = itemToEdit.getAmounts().get(i) + amountDiff.get(i);
            itemToEdit.getAmounts().set(i, newAmount);
        }
    }

    private void setDialogDropdowns(Spinner reasonSpinner, Spinner sizeSpinner, boolean isRemoving) {
        reasonSpinner.setAdapter(null);
        sizeSpinner.setAdapter(null);

        ArrayList<String> sizes = new ArrayList<>();
        for(int i = 0; i < itemToEdit.getSizes().size(); i++) {
            if(isRemoving && itemToEdit.getAmounts().get(i) == 0)
                continue;

            sizes.add(itemToEdit.getSizes().get(i).toString());
        }

        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sizes);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(sizeAdapter);

        ArrayList<String> reasonList = new ArrayList<>();
        reasonList.add("OSTECENO");
        reasonList.add("GRESKA");
        reasonList.add("OSTALO");

        if(!isRemoving)
            reasonList.remove("OSTECENO");

        ArrayAdapter<String> reasonAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, reasonList);
        reasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reasonSpinner.setAdapter(reasonAdapter);
    }

    private void updateInventory() {
        itemsRef.document(itemToEdit.toString()).update("amounts", itemToEdit.getAmounts())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                        Toast.makeText(getContext(), "Uspješno ažuriranje", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ažuriranje neuspjelo", Toast.LENGTH_SHORT).show();
                });
    }

    private void writeLog(InventoryLogModel logModel) {
        Map<String, Object> newLog = new HashMap<>();
        newLog.put("model", logModel.getModel());
        newLog.put("color", logModel.getColor());
        newLog.put("sizes", logModel.getSizes());
        newLog.put("amounts", logModel.getAmounts());
        newLog.put("type", logModel.getType());
        newLog.put("description", logModel.getDescription());
        newLog.put("time", logModel.getTime());
        newLog.put("employee", logModel.getEmployee());

        DocumentReference newLogRef;
        try {
            newLogRef = itemsRef.getParent().collection("logs").document();
        }catch (NullPointerException e) {
            Toast.makeText(getContext(), "Neuspješno dodavanje zapisnika o akciji", Toast.LENGTH_SHORT).show();
            return;
        }

        newLogRef.set(newLog)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        Toast.makeText(getContext(), "Uspješno dodavanje zapisnika", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Neuspješno dodavanje zapisnika o akciji", Toast.LENGTH_SHORT).show();
                });
    }

}