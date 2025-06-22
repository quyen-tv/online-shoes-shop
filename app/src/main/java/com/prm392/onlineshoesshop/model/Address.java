package com.prm392.onlineshoesshop.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Address implements Parcelable {
    private String street;
    private String ward;
    private String district;
    private String city;
    private String country;

    public Address() {
    }

    public Address(Address address) {
        this.street = address.getStreet();
        this.ward = address.getWard();
        this.district = address.getDistrict();
        this.city = address.getCity();
        this.country = address.getCountry();
    }

    public Address(String street, String ward, String district, String city, String country) {
        this.street = street;
        this.ward = ward;
        this.district = district;
        this.city = city;
        this.country = country;
    }

    protected Address(Parcel in) {
        street = in.readString();
        ward = in.readString();
        district = in.readString();
        city = in.readString();
        country = in.readString();
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(street);
        dest.writeString(ward);
        dest.writeString(district);
        dest.writeString(city);
        dest.writeString(country);
    }
}
