package com.example.admin.model;

/**
 * Created by admin on 11/24/2017.
 */

public class ParkingView {
    private int id;
    private String name;
    private String address;
    private String image;
    private String banner;
    private double latitude;
    private double longitude;

    public ParkingView() {
    }

    public ParkingView(int id, String name, String address, String image, String banner, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.image = image;
        this.banner = banner;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
