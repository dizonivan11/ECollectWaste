package com.ecw.ecollectwaste;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OnGoingSchedulesFragment extends Fragment {
    public static final String TAG = "ON_GOING_SCHEDULES";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_on_going_schedules, container, false);
        FragmentActivity fa = requireActivity();

        // Prevents continuing if there is no user signed in
        MainActivity.ValidateUser(fa, getContext());

        LinearLayout list_layout = view.findViewById(R.id.on_going_schedules_list_layout);

        Query on_going_query;
        if (MainActivity.USER.getIs_collector())
            on_going_query = MainActivity.DB.child(DatabaseManager.SCHEDULE_TABLE_NAME)
                    .orderByChild(DatabaseManager.SCHEDULE_COLUMN_COLLECTOR_ID)
                    .equalTo(MainActivity.USER.getId());
        else
            on_going_query = MainActivity.DB.child(DatabaseManager.SCHEDULE_TABLE_NAME)
                    .orderByChild(DatabaseManager.SCHEDULE_COLUMN_CLIENT_ID)
                    .equalTo(MainActivity.USER.getId());

        on_going_query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean has_record = false;
                for (DataSnapshot schedule_snapshot : snapshot.getChildren()) {
                    Schedule schedule = schedule_snapshot.getValue(Schedule.class);

                    // Only display schedule without DONE status
                    if (schedule.getStatus() != ScheduleStatus.DONE) {
                        MainActivity.AddRecord(fa, getResources(), list_layout, schedule);
                        has_record = true;
                    }
                }

                if (has_record)
                    view.findViewById(R.id.on_going_schedules_empty).setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(fa, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        MainActivity.AddBackEvent(fa, view.findViewById(R.id.on_going_schedules_back));
        MainActivity.AddOpenMainMenuEvent(fa, view.findViewById(R.id.on_going_schedules_menu));

        return view;
    }
}