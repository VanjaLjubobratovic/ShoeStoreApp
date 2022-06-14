package com.example.shoestoreapp.employee;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.example.shoestoreapp.R;
import com.google.android.material.button.MaterialButton;

public class QrScannerFragment extends Fragment {

    private CodeScanner qrScanner;
    private static final int CAMERA_REQUEST_CODE = 100;
    ImageButton backBtn;


    public QrScannerFragment() {
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
        return inflater.inflate(R.layout.layout_delivery_qr_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CodeScannerView scannerView = view.findViewById(R.id.scannerView);
        qrScanner = new CodeScanner(getActivity(), scannerView);
        backBtn = view.findViewById(R.id.scanDeliveryBack);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Bundle bundle = new Bundle();
                bundle.putString("Qr code", "123");
                getParentFragmentManager().setFragmentResult("Qr code", bundle);*/
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            //TODO: deprecated
            requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            qrScanner.startPreview();
        }

        qrScanner.setDecodeCallback(result -> {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(getContext(), result.getText(), Toast.LENGTH_SHORT).show();
                    qrScanner.releaseResources();
                    /* CODE FORMAT:
                           5217183
                     */
                    try {
                        String[] lines = result.getText().split(System.getProperty("line.separator"));
                        Toast.makeText(getContext(), result.getText(), Toast.LENGTH_SHORT).show();
                        Bundle bundle = new Bundle();
                        bundle.putStringArray("Qr code", lines);
                        getParentFragmentManager().setFragmentResult("Qr code", bundle);
                    } catch (IndexOutOfBoundsException | IllegalArgumentException e) {
                        Log.d("CODE READER", "invalid code ");
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Neispravan kod", Toast.LENGTH_SHORT).show();
                    }

                    getParentFragmentManager().popBackStack();
                }
            });
        });

    }

    //TODO: deprecated
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("REQUEST", "onRequestPermissionsResult: ");
                Toast.makeText(getContext(), "Dodijeljeno dopuštenje za kameru", Toast.LENGTH_SHORT).show();
                qrScanner.startPreview();
            } else {
                Toast.makeText(getContext(), "Odbijeno dopuštenje za kameru", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        }
    }
}