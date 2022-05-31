package com.example.shoestoreapp.employee;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatePicker extends DialogFragment implements  DatePickerDialog.OnDateSetListener{
    private int year, month, day;
    private Button btn;

    public void setObject(Button v) {
        this.btn = v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(android.widget.DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.month = monthOfYear + 1;
        this.day = dayOfMonth;

        btn.setText(day + "-" + month + "-" + year);
    }
}
