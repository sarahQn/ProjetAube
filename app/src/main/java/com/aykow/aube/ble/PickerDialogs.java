package com.aykow.aube.ble;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sarah on 11/03/2016.
 */
public class PickerDialogs extends DialogFragment implements DatePickerDialog.OnDateSetListener{

    EditText etBirthDate;

    public PickerDialogs(View view){
        etBirthDate = (EditText) view;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Calendar calendar = Calendar.getInstance();
        int year =calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        return dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Date date = new Date((year-1900),monthOfYear,dayOfMonth);
        Log.d("date","year: "+date.getYear());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String birthDate = formatter.format(date);
        etBirthDate.setText(birthDate);
    }
}
