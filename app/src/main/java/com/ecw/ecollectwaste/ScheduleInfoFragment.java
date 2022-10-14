package com.ecw.ecollectwaste;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.concurrent.Task;

public class ScheduleInfoFragment extends Fragment {
    public static final String TAG = "SCHEDULE_INFO";
    private GoogleMap map;
    private final Schedule schedule;
    private User nearest_collector = null;
    private JSONObject nearest_path = null;

    private Button schedule_info_submit;
    private Button schedule_info_delete;
    private Button schedule_info_active;
    private Button schedule_info_done;

    public ScheduleInfoFragment(Schedule schedule) {
        this.schedule = schedule;
    }

    private final OnMapReadyCallback callback = googleMap -> {
        map = googleMap;
        LatLng marawoy = new LatLng(13.961627,121.1563466);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(marawoy, 13));
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule_info, container, false);
        FragmentActivity fa = requireActivity();

        MainActivity.AddChangeFragmentEvent(fa, view.findViewById(R.id.schedule_info_back), new HomeFragment(), HomeFragment.TAG);
        MainActivity.AddOpenMainMenuEvent(fa, view.findViewById(R.id.schedule_info_menu));

        LinearLayout schedule_info_draft_buttons = view.findViewById(R.id.schedule_info_draft_buttons);
        schedule_info_submit = view.findViewById(R.id.schedule_info_submit);
        schedule_info_delete = view.findViewById(R.id.schedule_info_delete);
        schedule_info_active = view.findViewById(R.id.schedule_info_active);
        schedule_info_done = view.findViewById(R.id.schedule_info_done);

        if (!MainActivity.USER.getIs_collector()) {
            if (schedule.getStatus() == ScheduleStatus.DRAFT) {
                schedule_info_draft_buttons.setVisibility(View.VISIBLE);
            }
        } else {
            if (MainActivity.USER.getId().equals(schedule.getCollector_id())) {
                if (schedule.getStatus() == ScheduleStatus.SUBMITTED) {
                    schedule_info_active.setVisibility(View.VISIBLE);
                } else if (schedule.getStatus() == ScheduleStatus.ACTIVE) {
                    schedule_info_done.setVisibility(View.VISIBLE);
                }
            }
        }

        schedule_info_submit.setOnClickListener(view1 -> MainActivity.AddDialogEvent("Submit", "Are you sure to submit this schedule?", getContext(), (dialogInterface, i) -> {
            schedule.setCollector_id(nearest_collector.getId());
            schedule.setStatus(ScheduleStatus.SUBMITTED);
            DatabaseManager.UpdateSchedule(schedule.getId(), schedule);

            Toast.makeText(fa, "Schedule submitted, check out the schedule info from time to time for its status!", Toast.LENGTH_LONG).show();
            MainActivity.ChangeFragment(fa, new HomeFragment(), HomeFragment.TAG);
        }));

        schedule_info_delete.setOnClickListener(view1 -> MainActivity.AddDialogEvent("Delete", "Delete this draft schedule?", getContext(), (dialogInterface, i) -> {
            MainActivity.DB
                    .child(DatabaseManager.SCHEDULE_TABLE_NAME)
                    .orderByChild(DatabaseManager.SCHEDULE_COLUMN_ID)
                    .equalTo(schedule.getId()).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot schedule_snapshot : snapshot.getChildren()) {
                        schedule_snapshot.getRef().removeValue();
                    }
                    Toast.makeText(fa, "Draft schedule deleted!", Toast.LENGTH_SHORT).show();
                    MainActivity.ChangeFragment(fa, new HomeFragment(), HomeFragment.TAG);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(fa, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }));

        schedule_info_active.setOnClickListener(view1 -> MainActivity.AddDialogEvent("Active", "Start working on this one?", getContext(), (dialogInterface, i) -> {
            schedule.setStatus(ScheduleStatus.ACTIVE);
            DatabaseManager.UpdateSchedule(schedule.getId(), schedule);

            Toast.makeText(fa, "This schedule is now active", Toast.LENGTH_SHORT).show();

            // Disable the current button
            view1.setVisibility(View.GONE);
            view1.setEnabled(false);
            view1.setFocusable(false);

            // Enable the button for marking Done
            Button schedule_info_done = view.findViewById(R.id.schedule_info_done);
            schedule_info_done.setVisibility(View.VISIBLE);
            schedule_info_done.setEnabled(true);
            schedule_info_done.setFocusable(true);
        }));

        schedule_info_done.setOnClickListener(view1 -> MainActivity.AddDialogEvent("Done", "Mark as completed?", getContext(), (dialogInterface, i) -> {
            schedule.setStatus(ScheduleStatus.DONE);
            DatabaseManager.UpdateSchedule(schedule.getId(), schedule);

            Toast.makeText(fa, "This schedule is now marked as completed", Toast.LENGTH_SHORT).show();

            // Disable the current button
            view1.setVisibility(View.GONE);
            view1.setEnabled(false);
            view1.setFocusable(false);
        }));

        GetDirectionData(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.schedule_info_map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void GetDirectionData(View view) {
        if (schedule.getStatus() == ScheduleStatus.DRAFT) {
            // If the schedule is a draft
            MainActivity.DB
                    .child(DatabaseManager.USER_TABLE_NAME)
                    .orderByChild(DatabaseManager.USER_COLUMN_IS_COLLECTOR)
                    .equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Thread f = new Thread(() -> {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            User collector = ds.getValue(User.class);

                            // Check if there is a preferred date on the schedule. If null, let the system decide based on nearest location only
                            if (schedule.getPreferred_date() != null && !schedule.getPreferred_date().equals("")) {
                                // Check if the collector is available on the day of preferred date using Bitwise AND, zero means not available
                                int day_of_preferred_date = LocalDate.parse(schedule.getPreferred_date()).getDayOfWeek().getValue();
                                if ((collector.getDays_available() & Weekdays.DAYS[day_of_preferred_date - 1]) == 0) continue;
                            }

                            try {
                                String url = "https://graphhopper.com/api/1/route?point=" +
                                        schedule.getLocation() +
                                        "&point=" +
                                        collector.getLocation() +
                                        "&profile=car&locale=en&elevation=false&key=6af901a8-4654-464a-aefc-6bbc573e29f6";

                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder().url(url).get().build();
                                Response response = client.newCall(request).execute();
                                String result = response.body().string();
                                JSONObject directionInfo = new JSONObject(result);
                                JSONObject path = directionInfo.getJSONArray("paths").getJSONObject(0);

                                // If this is the first collector qualified, assign first for now then proceed to next collector
                                boolean first_collector = false;
                                if (nearest_collector == null || nearest_path == null) {
                                    nearest_collector = collector;
                                    nearest_path = path;
                                    first_collector = true;
                                }

                                // Compare the previous and current collector route distance, update the nearest collector and path if the current collector is nearer
                                double distance = path.getDouble("distance");
                                if (!first_collector && distance < nearest_path.getDouble("distance")) {
                                    nearest_collector = collector;
                                    nearest_path = path;
                                }
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    f.start();

                    try { f.join(); } catch (InterruptedException e) { e.printStackTrace(); }

                    getActivity().runOnUiThread(() -> {
                        try {
                            GetDirectionDataCallback(view, nearest_path);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // If schedule is not a draft with an existing collector assigned
            MainActivity.DB
                    .child(DatabaseManager.USER_TABLE_NAME)
                    .orderByChild(DatabaseManager.USER_COLUMN_ID)
                    .equalTo(schedule.getCollector_id()).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Thread f = new Thread(() -> {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            nearest_collector = ds.getValue(User.class);

                            try {
                                String url = "https://graphhopper.com/api/1/route?point=" +
                                        schedule.getLocation() +
                                        "&point=" +
                                        nearest_collector.getLocation() +
                                        "&profile=car&locale=en&elevation=false&key=6af901a8-4654-464a-aefc-6bbc573e29f6";

                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder().url(url).get().build();
                                Response response = client.newCall(request).execute();
                                String result = response.body().string();
                                JSONObject directionInfo = new JSONObject(result);
                                nearest_path = directionInfo.getJSONArray("paths").getJSONObject(0);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    f.start();

                    try { f.join(); } catch (InterruptedException e) { e.printStackTrace(); }

                    getActivity().runOnUiThread(() -> {
                        try {
                            GetDirectionDataCallback(view, nearest_path);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @SuppressLint("DefaultLocale")
    public void GetDirectionDataCallback(View view, JSONObject nearest_path) throws JSONException {
        // Found a collector available for the schedule, don't assign the nearest collector yet until the client submitted it
        MainActivity.DB
                .child(DatabaseManager.USER_TABLE_NAME)
                .orderByChild(DatabaseManager.USER_COLUMN_ID)
                .equalTo(schedule.getClient_id()).addListenerForSingleValueEvent(new ValueEventListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder sb = new StringBuilder();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        User client = ds.getValue(User.class);
                        TextView client_details = view.findViewById(R.id.schedule_info_client_details);
                        sb.append("First Name: ");
                        sb.append(client.getFirst_name());
                        sb.append('\n');
                        sb.append("Last Name: ");
                        sb.append(client.getLast_name());
                        sb.append('\n');
                        sb.append("Phone Number: ");
                        sb.append(client.getPhone_number());
                        client_details.setText(sb.toString());
                        sb = new StringBuilder();

                        if (nearest_collector != null) {
                            List<LatLng> points = PolyUtil.decode(nearest_path.getString("points"));

                            TextView collector_details = view.findViewById(R.id.schedule_info_collector_details);
                            sb.append("First Name: ");
                            sb.append(nearest_collector.getFirst_name());
                            sb.append('\n');
                            sb.append("Last Name: ");
                            sb.append(nearest_collector.getLast_name());
                            sb.append('\n');
                            sb.append("Phone Number: ");
                            sb.append(nearest_collector.getPhone_number());

                            // sb.append('\n');
                            // sb.append("Earliest Date Available: ");
                            // String earliest_date_string = "Today";
                            // if (!schedule.getPreferred_date().equals("")) {
                            //     LocalDate now = LocalDate.now();
                            //     int day_of_preferred_date = LocalDate.parse(schedule.getPreferred_date()).getDayOfWeek().getValue();
                            //     int day_today = now.getDayOfWeek().getValue();
                            //     int day_diff = day_of_preferred_date - day_today;
                            //     day_diff = day_diff < 0 ? day_diff + 7 : day_diff;
                            //     earliest_date_string = now.plusDays(day_diff).toString();
                            // }
                            // sb.append(earliest_date_string);

                            collector_details.setText(sb.toString());
                            sb = new StringBuilder();

                            TextView route_details = view.findViewById(R.id.schedule_info_route_details);
                            map.addPolyline(new PolylineOptions()
                                    .clickable(true)
                                    .color(R.color.accent_color)
                                    .addAll(points));

                            MarkerOptions client_marker = new MarkerOptions();
                            client_marker.title("Client");
                            client_marker.position(DatabaseManager.getLatLngFromString(schedule.getLocation()));
                            map.addMarker(client_marker);

                            MarkerOptions collector_marker = new MarkerOptions();
                            collector_marker.title("Collector");
                            collector_marker.position(DatabaseManager.getLatLngFromString(nearest_collector.getLocation()));
                            map.addMarker(collector_marker);

                            sb.append("Preferred Date: ");
                            if (!schedule.getPreferred_date().equals("")) {
                                sb.append(schedule.getPreferred_date());
                            } else {
                                sb.append("Anytime");
                            }
                            sb.append('\n');

                            double route_distance = Math.round(nearest_path.getDouble("distance") / 1000);
                            double price = Math.round(nearest_path.getDouble("distance") * DatabaseManager.SERVICE_FEE_MULTIPLIER);
                            double time = Math.round((route_distance / DatabaseManager.AVERAGE_CAR_KMH) * 60);
                            // double time = Math.round(nearest_path.getDouble("time"));
                            if (price < DatabaseManager.SERVICE_FEE_MINIMUM) price = DatabaseManager.SERVICE_FEE_MINIMUM;
                            sb.append("Distance: ");
                            sb.append(route_distance);
                            sb.append(" km\n");
                            sb.append("Estimated Time (at 30 km/h): ");
                            if (time > 60) {
                                int hour = (int) time / 60;
                                int min = (int) time % 60;
                                sb.append(hour);
                                sb.append(" hrs & ");
                                sb.append(min);
                            } else {
                                sb.append(time);
                            }
                            sb.append(" mins\n");
                            sb.append("Service Fee (₱ 50 min): ₱ ");
                            sb.append(price);
                            route_details.setText(sb.toString());

                            if (schedule.getStatus() == ScheduleStatus.DRAFT) {
                                schedule_info_submit.setEnabled(true);
                                schedule_info_submit.setFocusable(true);
                                schedule_info_delete.setEnabled(true);
                                schedule_info_delete.setFocusable(true);
                            } else if (schedule.getStatus() == ScheduleStatus.SUBMITTED) {
                                schedule_info_active.setEnabled(true);
                                schedule_info_active.setFocusable(true);
                            } else if (schedule.getStatus() == ScheduleStatus.ACTIVE) {
                                schedule_info_done.setEnabled(true);
                                schedule_info_done.setFocusable(true);
                            }
                        } else {
                            Toast.makeText(getContext(), "No available collector found at this moment", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}