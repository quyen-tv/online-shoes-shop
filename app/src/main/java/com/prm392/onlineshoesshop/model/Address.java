package com.prm392.onlineshoesshop.model;

public class Address {
    private String street;
    private String ward;
    private String district;
    private String city;
    private String country;

    public Address() {
    }

    public Address(String street, String ward, String district, String city, String country) {
        this.street = street;
        this.ward = ward;
        this.district = district;
        this.city = city;
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
