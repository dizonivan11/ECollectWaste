package com.ecw.ecollectwaste;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.time.LocalDate;

public class ScheduleFragment extends Fragment {
    public static final String TAG = "SCHEDULE";
    public EditText schedule_date;
    private boolean has_date = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        FragmentActivity fa = requireActivity();

        // Prevents continuing if there is no user signed in
        MainActivity.ValidateUser(fa, getContext());
        if (MainActivity.USER.getIs_collector()) MainActivity.SignOut(fa, getContext());

        MainActivity.AddBackEvent(fa, view.findViewById(R.id.schedule_back));
        MainActivity.AddOpenMainMenuEvent(fa, view.findViewById(R.id.schedule_menu));

        schedule_date = view.findViewById(R.id.schedule_date);
        CheckBox schedule_auto_trigger = view.findViewById(R.id.schedule_date_trigger);
        schedule_auto_trigger.setOnCheckedChangeListener((compoundButton, b) -> {
            has_date = b;
            schedule_date.setText("");

            if (b) {
                view.findViewById(R.id.schedule_date_section).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.schedule_date_section).setVisibility(View.GONE);
            }
        });

        view.findViewById(R.id.schedule_date_pick).setOnClickListener(view12 -> {
            DatePickerFragment date_picker = new DatePickerFragment(this);
            date_picker.show(fa.getSupportFragmentManager(), DatePickerFragment.TAG);
        });

        MainActivity.AddChangeFragmentEvent(fa, view.findViewById(R.id.schedule_map), new MapFragment(), MapFragment.TAG);

        view.findViewById(R.id.schedule_submit).setOnClickListener(view1 -> {
            String date_string = schedule_date.getText().toString();
            LocalDate date_selected = null;

            if (has_date) {
                if (date_string.equals("")) {
                    Toast.makeText(fa, "Please select your preferred date", Toast.LENGTH_SHORT).show();
                    return;
                }
                date_selected = LocalDate.parse(date_string);
            }

            if (MainActivity.selected_lat_lng == null) {
                Toast.makeText(fa, "Please select a location", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save as draft
            Schedule draft_schedule = new Schedule(
                    "",
                    DatabaseManager.getStringFromLatLng(MainActivity.selected_lat_lng),
                    date_selected != null ? date_selected.toString() : "",
                    MainActivity.USER.getId(),
                    "",
                    ScheduleStatus.DRAFT,
                    LocalDate.now().toString(),
                    "");

            DatabaseManager.AddSchedule(draft_schedule);

            if (!draft_schedule.getId().equals(""))
                MainActivity.ChangeFragment(fa, new ScheduleInfoFragment(draft_schedule), ScheduleInfoFragment.TAG);
            else
                Toast.makeText(fa, "Encountered some error saving draft schedule", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}