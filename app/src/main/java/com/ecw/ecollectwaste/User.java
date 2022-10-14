package com.ecw.ecollectwaste;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String id;
    private String username;
    private String password;
    private String first_name;
    private String last_name;
    private long phone_number;
    private boolean is_collector;
    private String location;
    private int days_available;

    public User() {}
    public User(String id, String username, String password, String first_name, String last_name, long phone_number, boolean is_collector, String location, int days_available) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.first_name = first_name;
        this.last_name = last_name;
        this.phone_number = phone_number;
        this.is_collector = is_collector;
        this.location = location;
        this.days_available = days_available;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirst_name() { return first_name; }
    public void setFirst_name(String first_name) { this.first_name = first_name; }
    public String getLast_name() { return last_name; }
    public void setLast_name(String last_name) { this.last_name = last_name; }
    public long getPhone_number() { return phone_number; }
    public void setPhone_number(long phone_number) { this.phone_number = phone_number; }
    public boolean getIs_collector() { return is_collector; }
    public void setIs_collector(boolean is_collector) { this.is_collector = is_collector; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getDays_available() { return days_available; }
    public void setDays_available(int days_available) { this.days_available = days_available; }
}
