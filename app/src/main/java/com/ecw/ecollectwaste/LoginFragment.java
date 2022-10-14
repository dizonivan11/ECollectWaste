package com.ecw.ecollectwaste;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginFragment extends Fragment {
    public static final String TAG = "LOGIN";

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        FragmentActivity fa = requireActivity();

        // Add touch events to main menu for first time load
        if (!MainActivity.FIRST_LOAD) {
            MainActivity.AddHideMainMenuEvent(fa, fa.findViewById(R.id.menu_back));
            MainActivity.AddChangeFragmentEvent(fa, fa.findViewById(R.id.nav_home_button), new HomeFragment(), HomeFragment.TAG);
            MainActivity.AddChangeFragmentEvent(fa, fa.findViewById(R.id.nav_schedule_button), new ScheduleFragment(), ScheduleFragment.TAG);
            MainActivity.AddChangeFragmentEvent(fa, fa.findViewById(R.id.nav_history_button), new HistoryFragment(), HistoryFragment.TAG);
            MainActivity.AddChangeFragmentEvent(fa, fa.findViewById(R.id.nav_on_going_schedules_button), new OnGoingSchedulesFragment(), OnGoingSchedulesFragment.TAG);
            MainActivity.AddChangeFragmentEvent(fa, fa.findViewById(R.id.nav_about_button), new AboutFragment(), AboutFragment.TAG);
            MainActivity.FIRST_LOAD = true;
        }

        MainActivity.AddCloseEvent(fa, getContext(), view.findViewById(R.id.login_exit));

        EditText login_user = view.findViewById(R.id.login_user);
        EditText login_pass = view.findViewById(R.id.login_pass);

        view.findViewById(R.id.login_sign_up).setOnClickListener(view12 -> MainActivity.ChangeFragment(fa, new RegisterFragment(), RegisterFragment.TAG));

        view.findViewById(R.id.login_sign_in).setOnClickListener(view1 -> {
            String user_value = login_user.getText().toString();
            String pass_value = login_pass.getText().toString();

            if (InputValidator.IfEmpty(fa, view.findViewById(R.id.login_user), "Input valid username")) return;
            if (InputValidator.IfEmpty(fa, view.findViewById(R.id.login_pass), "Input valid password")) return;

            // CREDENTIAL VALIDATION
            MainActivity.DB
                    .child(DatabaseManager.USER_TABLE_NAME)
                    .orderByChild(DatabaseManager.USER_COLUMN_USERNAME)
                    .equalTo(user_value).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.getValue(User.class);

                        if (user == null || !user.getPassword().equals(pass_value)) {
                            Toast.makeText(fa, "Incorrect username or password", Toast.LENGTH_SHORT).show();
                        } else {
                            MainActivity.USER = user;

                            if (MainActivity.USER.getIs_collector()) {
                                fa.findViewById(R.id.nav_schedule_button).setVisibility(View.GONE);
                                fa.findViewById(R.id.nav_history_button).setVisibility(View.VISIBLE);
                            } else {
                                fa.findViewById(R.id.nav_history_button).setVisibility(View.GONE);
                                fa.findViewById(R.id.nav_schedule_button).setVisibility(View.VISIBLE);
                            }

                            MainActivity.ChangeFragment(fa, new HomeFragment(), HomeFragment.TAG);
                        }
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