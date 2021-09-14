package com.example.moveonotes;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final String time;
    private final String date;





    public MyItem(LatLng position, String title, String snippet, String time, String date) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.time = time;
        this.date = date;
    }

    public MyItem(double lat, double lng, String title, String snippet, String time, String date) {
        position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
        this.time = time;
        this.date= date;
    }


    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

}

