package com.ecw.ecollectwaste;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutFragment extends Fragment {
    public static final String TAG = "ABOUT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        FragmentActivity fa = requireActivity();

        // Prevents continuing if there is no user signed in
        MainActivity.ValidateUser(fa, getContext());

        MainActivity.AddBackEvent(fa, view.findViewById(R.id.about_back));
        MainActivity.AddOpenMainMenuEvent(fa, view.findViewById(R.id.about_menu));

        return view;
    }
}