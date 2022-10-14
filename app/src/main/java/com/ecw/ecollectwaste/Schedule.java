package com.ecw.ecollectwaste;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Schedule {
    private String id;
    private String location;
    private String preferred_date;
    private String client_id;
    private String collector_id;
    private int status;
    private String date_created;
    private String date_done;

    public Schedule() {}
    public Schedule(String id, String location, String preferred_date, String client_id, String collector_id, int status, String date_created, String date_done) {
        this.id = id;
        this.location = location;
        this.preferred_date = preferred_date;
        this.client_id = client_id;
        this.collector_id = collector_id;
        this.status = status;
        this.date_created = date_created;
        this.date_done = date_done;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPreferred_date() { return preferred_date; }
    public void setPreferred_date(String preferred_date) { this.preferred_date = preferred_date; }
    public String getClient_id() { return client_id; }
    public void setClient_id(String client_id) { this.client_id = client_id; }
    public String getCollector_id() { return collector_id; }
    public void setCollector_id(String collector_id) { this.collector_id = collector_id; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getDate_created() { return date_created; }
    public void setDate_created(String date_created) { this.date_created = date_created; }
    public String getDate_done() { return date_done; }
    public void setDate_done(String date_done) { this.date_done = date_done; }
}