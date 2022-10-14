package com.ecw.ecollectwaste;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class HomeFragment extends Fragment {
    public static final String TAG = "HOME";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        FragmentActivity fa = requireActivity();

        // Prevents continuing if there is no user signed in
        MainActivity.ValidateUser(fa, getContext());

        MainActivity.AddSignOutEvent(fa, getContext(), view.findViewById(R.id.home_logout));
        MainActivity.AddOpenMainMenuEvent(fa, view.findViewById(R.id.home_menu));

        if (MainActivity.USER.getIs_collector()) {
            view.findViewById(R.id.home_client_schedule).setVisibility(View.GONE);
            view.findViewById(R.id.home_client_schedule_section).setVisibility(View.GONE);
            MainActivity.AddChangeFragmentEvent(fa, view.findViewById(R.id.home_collector_history), new HistoryFragment(), HistoryFragment.TAG);

        } else {
            view.findViewById(R.id.home_collector_history).setVisibility(View.GONE);
            view.findViewById(R.id.home_collector_schedule_section).setVisibility(View.GONE);
            MainActivity.AddChangeFragmentEvent(fa, view.findViewById(R.id.home_client_schedule), new ScheduleFragment(), ScheduleFragment.TAG);
        }

        MainActivity.AddChangeFragmentEvent(fa, view.findViewById(R.id.home_user_view_schedules), new OnGoingSchedulesFragment(), OnGoingSchedulesFragment.TAG);
        MainActivity.AddChangeFragmentEvent(fa, view.findViewById(R.id.home_user_about), new AboutFragment(), AboutFragment.TAG);

        return view;
    }
}