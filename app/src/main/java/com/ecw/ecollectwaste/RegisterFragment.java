package com.ecw.ecollectwaste;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {
    public static final String TAG = "REGISTER";
    Map<Integer, Boolean> dayValues = new LinkedHashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        FragmentActivity fa = requireActivity();

        MainActivity.AddBackEvent(fa, view.findViewById(R.id.register_back));
        CheckBox collector_trigger = view.findViewById(R.id.register_collector_trigger);
        LinearLayout collector_section = view.findViewById(R.id.register_collector_section);
        Button collector_location = view.findViewById(R.id.register_map);

        collector_trigger.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                collector_section.setVisibility(View.VISIBLE);
            } else {
                collector_section.setVisibility(View.GONE);
            }
        });

        MainActivity.AddChangeFragmentEvent(fa, collector_location, new MapFragment(), MapFragment.TAG);

        dayValues.put(R.id.register_mon, false);
        dayValues.put(R.id.register_tue, false);
        dayValues.put(R.id.register_wed, false);
        dayValues.put(R.id.register_thu, false);
        dayValues.put(R.id.register_fri, false);
        dayValues.put(R.id.register_sat, false);
        dayValues.put(R.id.register_sun, false);

        Resources res = getResources();
        for (int dayButtonID : dayValues.keySet()) {
            view.findViewById(dayButtonID).setOnClickListener(view1 -> {
                TextView dayButton = (TextView) view1;
                int id = dayButton.getId();

                if (dayValues.get(id)) {
                    dayButton.setBackgroundColor(res.getColor(R.color.days_available_default_color));
                    dayButton.setTextColor(res.getColor(R.color.text_color));
                    dayValues.put(id, false);
                } else {
                    dayButton.setBackgroundColor(res.getColor(R.color.accent_color));
                    dayButton.setTextColor(res.getColor(R.color.white));
                    dayValues.put(id, true);
                }
            });
        }

        view.findViewById(R.id.register_submit).setOnClickListener(view13 -> {
            EditText register_user = view.findViewById(R.id.register_user);
            EditText register_pass = view.findViewById(R.id.register_pass);
            EditText register_first = view.findViewById(R.id.register_first);
            EditText register_last = view.findViewById(R.id.register_last);
            EditText register_phone = view.findViewById(R.id.register_phone);

            if (InputValidator.IfEmpty(fa, register_user, "Input valid username")) return;
            if (InputValidator.IfEmpty(fa, register_pass, "Input valid password")) return;
            if (InputValidator.IfEmpty(fa, register_first, "Input your first name")) return;
            if (InputValidator.IfEmpty(fa, register_last, "Input your last name")) return;
            if (InputValidator.IfPhoneEmpty(fa, register_phone, 11, "Input valid phone number")) return;

            MainActivity.DB
                    .child(DatabaseManager.USER_TABLE_NAME)
                    .orderByChild(DatabaseManager.USER_COLUMN_USERNAME)
                    .equalTo(register_user.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        Toast.makeText(fa, "Username already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        if (collector_section.getVisibility() == View.VISIBLE) { // Save as collector
                            if (MainActivity.selected_lat_lng == null) {
                                Toast.makeText(fa, "Please select a location", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            int i = 0;
                            int days_available = 0;
                            for (Boolean day_value : dayValues.values()) {
                                if (day_value) days_available += Weekdays.DAYS[i];
                                i++;
                            }

                            if (days_available == 0) {
                                Toast.makeText(fa, "Please select at least one day availability", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            DatabaseManager.AddUser(new User(
                                    "",
                                    register_user.getText().toString(),
                                    register_pass.getText().toString(),
                                    register_first.getText().toString(),
                                    register_last.getText().toString(),
                                    Long.parseLong(register_phone.getText().toString()),
                                    true,
                                    DatabaseManager.getStringFromLatLng(MainActivity.selected_lat_lng),
                                    days_available));

                            MainActivity.selected_lat_lng = null;
                        } else { // Save as client
                            DatabaseManager.AddUser(new User(
                                    "",
                                    register_user.getText().toString(),
                                    register_pass.getText().toString(),
                                    register_first.getText().toString(),
                                    register_last.getText().toString(),
                                    Long.parseLong(register_phone.getText().toString()),
                                    false,
                                    "",
                                    0));
                        }
                        MainActivity.PreviousFragment(fa);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(fa, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }
}