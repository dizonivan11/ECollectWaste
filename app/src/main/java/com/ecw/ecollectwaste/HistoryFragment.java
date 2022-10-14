package com.ecw.ecollectwaste;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    public static final String TAG = "HISTORY";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        FragmentActivity fa = requireActivity();

        // Prevents continuing if there is no user signed in
        MainActivity.ValidateUser(fa, getContext());
        if (!MainActivity.USER.getIs_collector()) MainActivity.SignOut(fa, getContext());

        LinearLayout list_layout = view.findViewById(R.id.history_list_layout);

        MainActivity.DB.child(DatabaseManager.SCHEDULE_TABLE_NAME)
                .orderByChild(DatabaseManager.SCHEDULE_COLUMN_COLLECTOR_ID)
                .equalTo(MainActivity.USER.getId()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean has_record = false;
                for (DataSnapshot schedule_snapshot : snapshot.getChildren()) {
                    Schedule schedule = schedule_snapshot.getValue(Schedule.class);

                    // Only display schedule with DONE status
                    if (schedule.getStatus() == ScheduleStatus.DONE) {
                        MainActivity.AddRecord(fa, getResources(), list_layout, schedule);
                        has_record = true;
                    }
                }

                if (has_record)
                    view.findViewById(R.id.history_empty).setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(fa, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        MainActivity.AddBackEvent(fa, view.findViewById(R.id.history_back));
        MainActivity.AddOpenMainMenuEvent(fa, view.findViewById(R.id.history_menu));

        return view;
    }
}