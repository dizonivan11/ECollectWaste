package com.ecw.ecollectwaste;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDate;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    public static final String TAG = "DATE_PICKER";
    public final ScheduleFragment sf;

    public DatePickerFragment(ScheduleFragment sf) {
        this.sf = sf;
    }

    @NonNull
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

    // onDateSet has zero-based month
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        sf.schedule_date.setText(LocalDate.of(year, month + 1, day).toString());
    }
}
