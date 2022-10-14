package com.ecw.ecollectwaste;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ecw.ecollectwaste.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static boolean FIRST_LOAD = false;
    public static User USER = null;
    public static DatabaseReference DB;
    public static LatLng selected_lat_lng = null;
    public static String selected_collector = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DB = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_main);
    }

    public static void ChangeFragment(FragmentActivity fa , Fragment fragment, String tag) {
        FragmentManager fm = fa.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(R.id.fcv, fragment, tag);

        if (fm.findFragmentByTag(tag) == null || !fragment.isAdded())
            ft.addToBackStack(tag);

        ft.commit();
        HideMainMenu(fa);
    }

    public static void PreviousFragment(FragmentActivity fa) {
        FragmentManager fm = fa.getSupportFragmentManager();

        if (fm.getBackStackEntryCount() > 0)
            fm.popBackStack();
        else if (USER != null)
            ChangeFragment(fa, new HomeFragment(), HomeFragment.TAG);
        else
            ChangeFragment(fa, new LoginFragment(), LoginFragment.TAG);
    }

    public static void AddChangeFragmentEvent(FragmentActivity fa, View widget, Fragment fragment, String tag) {
        widget.setOnClickListener(v -> ChangeFragment(fa, fragment, tag));
    }

    public static void OpenMainMenu(Activity a) {
        a.findViewById(R.id.mainMenu).setVisibility(View.VISIBLE);
        a.findViewById(R.id.fcv).setVisibility(View.GONE);
    }

    public static void AddOpenMainMenuEvent(Activity a, View widget) {
        widget.setOnClickListener(v -> OpenMainMenu(a));
    }

    public static void HideMainMenu(Activity a) {
        a.findViewById(R.id.mainMenu).setVisibility(View.GONE);
        a.findViewById(R.id.fcv).setVisibility(View.VISIBLE);
    }

    public static void AddHideMainMenuEvent(Activity a, View widget) {
        widget.setOnClickListener(v -> HideMainMenu(a));
    }

    public static void AddBackEvent(FragmentActivity fa, View widget) {
        widget.setOnClickListener(v -> PreviousFragment(fa));
    }

    public static void AddDialogEvent(String title, String text, Context context, DialogInterface.OnClickListener positive_callback) {
        AlertDialog.Builder closeDialog = new AlertDialog.Builder(context);
        closeDialog.setTitle(title);
        closeDialog.setMessage(text);
        closeDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        closeDialog.setPositiveButton("Yes", positive_callback);
        closeDialog.create().show();
    }

    public static void AddCloseEvent(FragmentActivity fa, Context context, Button button) {
        button.setOnClickListener(v -> AddDialogEvent("Notice", "Are you sure you want to exit the application?", context, (dialogInterface, i) -> {
            fa.finishAffinity();
            System.exit(0);
        }));
    }

    public static void SignOut(FragmentActivity fa, Context context) {
        AddDialogEvent("Notice", "Are you sure you want logout?", context, (dialogInterface, i2) -> {
            // Clear fragment backstack
            FragmentManager fm = fa.getSupportFragmentManager();
            for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
                fm.popBackStack();
            }
            USER = null;
            ChangeFragment(fa, new LoginFragment(), LoginFragment.TAG);
        });
    }

    public static void AddSignOutEvent(FragmentActivity fa, Context context, Button button) {
        button.setOnClickListener(v -> SignOut(fa, context));
    }

    public static void ValidateUser(FragmentActivity fa, Context context) {
        if (USER == null) {
            Toast.makeText(fa, "No user signed in, returning to login screen....", Toast.LENGTH_SHORT).show();
            SignOut(fa, context);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void AddRecord(FragmentActivity fa, Resources r, LinearLayout list_layout, Schedule schedule) {
        // RECORD BASE LAYOUT
        RelativeLayout base_layout = new RelativeLayout(fa);

        RelativeLayout.LayoutParams base_layout_params =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        360);

        base_layout_params.setMargins(0, 0, 0, 32);
        base_layout.setLayoutParams(base_layout_params);
        base_layout.setFocusable(View.FOCUSABLE);
        base_layout.setClickable(true);

        // RECORD BACKGROUND IMAGE
        ImageView background_image = new ImageView(fa);

        ViewGroup.LayoutParams background_image_params =
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

        background_image.setLayoutParams(background_image_params);
        background_image.setBackground(r.getDrawable(R.drawable.schedule_info_background));
        background_image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        background_image.setAdjustViewBounds(true);

        // TODO: ALIGN THE INNER ELEMENTS VERTICALLY CENTER
        // RECORD INNER LAYOUT
        LinearLayout inner_layout = new LinearLayout(fa);

        LinearLayout.LayoutParams inner_layout_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        inner_layout_params.setMargins(80, 48, 80, 48);
        inner_layout_params.gravity = Gravity.CENTER_VERTICAL;
        inner_layout.setLayoutParams(inner_layout_params);
        inner_layout.setOrientation(LinearLayout.VERTICAL);

        // HEADER TEXT
        TextView header_textview = new TextView(fa);

        ViewGroup.LayoutParams header_textview_params =
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        header_textview.setLayoutParams(header_textview_params);
        header_textview.setText(String.format("ID: %s", schedule.getId()));
        header_textview.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        header_textview.setTextSize(16);

        // DATE TEXT
        TextView date_textview = new TextView(fa);
        String date_text = schedule.getDate_created();
        if (date_text == null || date_text.equals("")) date_text = "N/A";

        ViewGroup.LayoutParams date_textview_params =
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        date_textview.setLayoutParams(date_textview_params);
        date_textview.setText(String.format("Date Created: %s", date_text));

        // STATUS COLUMN 1
        TextView status_label_textview = new TextView(fa);
        ViewGroup.LayoutParams status_label_params =
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        status_label_textview.setPadding(0, 12, 15, 12);
        status_label_textview.setLayoutParams(status_label_params);
        status_label_textview.setText("Status");
        status_label_textview.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        // STATUS COLUMN 2
        LinearLayout status_text_layout = new LinearLayout(fa);
        LinearLayout.LayoutParams status_text_layout_params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        status_text_layout.setPadding(32, 12, 32, 12);
        status_text_layout.setLayoutParams(status_text_layout_params);
        status_text_layout.setOrientation(LinearLayout.VERTICAL);

        TextView status_text_textview = new TextView(fa);
        ViewGroup.LayoutParams status_text_params =
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        String status_text = "N/A";
        int status_text_color = -1;
        Drawable status_text_background = null;
        switch (schedule.getStatus()) {
            case ScheduleStatus.DRAFT:
                status_text = "Draft";
                status_text_color = r.getColor(R.color.black);
                status_text_background = r.getDrawable(R.drawable.draft_status);
                break;
            case ScheduleStatus.SUBMITTED:
                status_text = "Submitted";
                status_text_color = r.getColor(R.color.black);
                status_text_background = r.getDrawable(R.drawable.submitted_status);
                break;
            case ScheduleStatus.ACTIVE:
                status_text = "Active";
                status_text_color = r.getColor(R.color.white);
                status_text_background = r.getDrawable(R.drawable.active_status);
                break;
            case ScheduleStatus.DONE:
                status_text = "Done";
                status_text_color = r.getColor(R.color.white);
                status_text_background = r.getDrawable(R.drawable.done_status);
                break;
        }

        status_text_textview.setLayoutParams(status_text_params);
        status_text_textview.setText(status_text);
        status_text_textview.setTextColor(status_text_color);
        status_text_layout.setBackground(status_text_background);

        base_layout.setOnClickListener(view -> MainActivity.ChangeFragment(fa, new ScheduleInfoFragment(schedule), ScheduleInfoFragment.TAG));

        list_layout.addView(base_layout);
        /**/base_layout.addView(background_image);
        /**/base_layout.addView(inner_layout);
        /*    */inner_layout.addView(header_textview);
        /*    */inner_layout.addView(date_textview);
        /*    */inner_layout.addView(status_label_textview);
        /*    */inner_layout.addView(status_text_layout);
        /*        */status_text_layout.addView(status_text_textview);
    }
}