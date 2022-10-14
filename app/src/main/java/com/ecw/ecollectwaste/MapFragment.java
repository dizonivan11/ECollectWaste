package com.ecw.ecollectwaste;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment {
    public static final String TAG = "MAP";
    private Marker last_marker;

    private final OnMapReadyCallback callback = googleMap -> {
        LatLng marawoy = new LatLng(13.961627,121.1563466);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marawoy, 13));

        googleMap.setOnMapClickListener(latLng -> {
            // Clears the previously touched position
            if (last_marker != null)
                last_marker.remove();

            // Creating a marker
            MarkerOptions markerOptions = new MarkerOptions();

            // Setting the position for the marker
            markerOptions.position(latLng);

            // Setting the title for the marker.
            // This will be displayed on taping the marker
            markerOptions.title(latLng.latitude + " | " + latLng.longitude);

            // Animating to the touched position
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

            // Placing a marker on the touched position
            last_marker = googleMap.addMarker(markerOptions);

            AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
            adb.setCancelable(false); // Let the negative button do the job
            adb.setTitle("Notice");
            adb.setMessage("Please confirm if this is your precise location.");
            adb.setPositiveButton("Confirm", (dialogInterface, i) -> {
                // Clears the previously touched position before moving to another fragment, this prevents the temporary marker to stay on the map
                if (last_marker != null)
                    last_marker.remove();

                MainActivity.selected_lat_lng = latLng;
                Toast.makeText(getActivity(), "Successfully selected the location", Toast.LENGTH_SHORT).show();
                MainActivity.PreviousFragment(getActivity());
            });
            adb.setNegativeButton("No", (dialogInterface, i) -> {
                // Clears the previously touched position
                if (last_marker != null)
                    last_marker.remove();
            });
            adb.create().show();
        });
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        FragmentActivity fa = requireActivity();

        MainActivity.AddBackEvent(fa, view.findViewById(R.id.map_back_button));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_google_map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}