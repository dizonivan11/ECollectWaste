package com.ecw.ecollectwaste;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    public static final double SERVICE_FEE_MULTIPLIER = 0.025;
    public static final double SERVICE_FEE_MINIMUM = 50;
    public static final int AVERAGE_CAR_KMH = 30;

    // USERS (Client or Collector)
    public static final String USER_TABLE_NAME = "users";
    public static final String USER_COLUMN_ID = "id";
    public static final String USER_COLUMN_USERNAME = "username";
    public static final String USER_COLUMN_PASSWORD = "password";
    public static final String USER_COLUMN_FIRST = "first_name";
    public static final String USER_COLUMN_LAST = "last_name";
    public static final String USER_COLUMN_PHONE = "phone_number";
    // Collector Exclusive
    public static final String USER_COLUMN_IS_COLLECTOR = "is_collector";
    public static final String USER_COLUMN_LOCATION = "location";
    public static final String USER_COLUMN_DAYS_AVAILABLE = "days_available";

    // SCHEDULES
    public static final String SCHEDULE_TABLE_NAME = "schedules";
    public static final String SCHEDULE_COLUMN_ID = "id";
    public static final String SCHEDULE_COLUMN_LOCATION = "location";
    public static final String SCHEDULE_COLUMN_PREFERRED_DATE = "preferred_date";
    public static final String SCHEDULE_COLUMN_CLIENT_ID = "client_id";
    public static final String SCHEDULE_COLUMN_COLLECTOR_ID = "collector_id";
    public static final String SCHEDULE_COLUMN_STATUS = "status";
    public static final String SCHEDULE_COLUMN_DATE_CREATED = "date_created";
    public static final String SCHEDULE_COLUMN_DATE_DONE = "date_done";

    public static LatLng getLatLngFromString(String latLng) {
        return latLng != null && !latLng.isEmpty() ?
                new LatLng(
                        Double.parseDouble(latLng.substring(0, latLng.indexOf(','))),
                        Double.parseDouble(latLng.substring(latLng.indexOf(',') + 1))) :
                null;
    }

    public static String getStringFromLatLng(LatLng latLng) { return latLng != null ? String.valueOf(latLng.latitude) + ',' + latLng.longitude : null; }

    public static void AddUser(User user) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        // Create an empty record with only its key, data will be updated later
        String key = db.child(USER_TABLE_NAME).push().getKey();
        UpdateUser(key, user);
    }

    public static void UpdateUser(String key, User user) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        user.setId(key);
        Map<String, Object> postValues = MapUser(user);
        Map<String, Object> updates = new HashMap<>();
        updates.put('/'+ USER_TABLE_NAME + '/' + key, postValues);
        db.updateChildren(updates);
    }

    public static Map<String, Object> MapUser(User user) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(USER_COLUMN_ID, user.getId());
        map.put(USER_COLUMN_USERNAME, user.getUsername());
        map.put(USER_COLUMN_PASSWORD, user.getPassword());
        map.put(USER_COLUMN_FIRST, user.getFirst_name());
        map.put(USER_COLUMN_LAST, user.getLast_name());
        map.put(USER_COLUMN_PHONE, user.getPhone_number());
        map.put(USER_COLUMN_IS_COLLECTOR, user.getIs_collector());
        map.put(USER_COLUMN_LOCATION, user.getLocation());
        map.put(USER_COLUMN_DAYS_AVAILABLE, user.getDays_available());
        return map;
    }

    public static void AddSchedule(Schedule schedule) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        // Create an empty record with only its key, data will be updated later
        String key = db.child(SCHEDULE_TABLE_NAME).push().getKey();
        UpdateSchedule(key, schedule);

        // Resets the global selected map latitude and longitude
        MainActivity.selected_lat_lng = null;
    }

    public static void UpdateSchedule(String key, Schedule schedule) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        schedule.setId(key);
        Map<String, Object> postValues = MapSchedule(schedule);
        Map<String, Object> updates = new HashMap<>();
        updates.put('/'+ SCHEDULE_TABLE_NAME + '/' + key, postValues);
        db.updateChildren(updates);
    }

    public static Map<String, Object> MapSchedule(Schedule schedule) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(SCHEDULE_COLUMN_ID, schedule.getId());
        map.put(SCHEDULE_COLUMN_LOCATION, schedule.getLocation());
        map.put(SCHEDULE_COLUMN_PREFERRED_DATE, schedule.getPreferred_date());
        map.put(SCHEDULE_COLUMN_CLIENT_ID, schedule.getClient_id());
        map.put(SCHEDULE_COLUMN_COLLECTOR_ID, schedule.getCollector_id());
        map.put(SCHEDULE_COLUMN_STATUS, schedule.getStatus());
        map.put(SCHEDULE_COLUMN_DATE_CREATED, schedule.getDate_created());
        map.put(SCHEDULE_COLUMN_DATE_DONE, schedule.getDate_done());
        return map;
    }
}
