package com.prm392.onlineshoesshop.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Address implements Parcelable {
    private String street;
    private City city;
    private District district;
    private Ward ward;
    private String country;

    public Address() {}

    public Address(Address other) {
        this.street = other.street;
        this.city = other.city;
        this.district = other.district;
        this.ward = other.ward;
        this.country = other.country;
    }
    public Address(String street, String country, City city, District district, Ward ward) {
        this.street = street;
        this.country = country;
        this.city = city;
        this.district = district;
        this.ward = ward;
    }

    protected Address(Parcel in) {
        street = in.readString();
        city = in.readParcelable(City.class.getClassLoader());
        district = in.readParcelable(District.class.getClassLoader());
        ward = in.readParcelable(Ward.class.getClassLoader());
        country = in.readString();
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override public Address[] newArray(int size) {
            return new Address[size];
        }
    };

    @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(street);
        dest.writeParcelable(city, flags);
        dest.writeParcelable(district, flags);
        dest.writeParcelable(ward, flags);
        dest.writeString(country);
    }

    @Override public int describeContents() { return 0; }

    // Getters & Setters
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public District getDistrict() { return district; }
    public void setDistrict(District district) { this.district = district; }

    public Ward getWard() { return ward; }
    public void setWard(Ward ward) { this.ward = ward; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    // ========== LỚP LỒNG ==========

    public static class City implements Parcelable {
        private int code;
        private String name;

        public City() {}

        public City(int code, String name) {
            this.code = code;
            this.name = name;
        }

        protected City(Parcel in) {
            code = in.readInt();
            name = in.readString();
        }

        public static final Creator<City> CREATOR = new Creator<City>() {
            @Override public City createFromParcel(Parcel in) {
                return new City(in);
            }
            @Override public City[] newArray(int size) {
                return new City[size];
            }
        };

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(code);
            dest.writeString(name);
        }

        @Override public int describeContents() { return 0; }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }

    public static class District implements Parcelable {
        private int code;
        private String name;

        public District() {}

        public District(int code, String name) {
            this.code = code;
            this.name = name;
        }

        protected District(Parcel in) {
            code = in.readInt();
            name = in.readString();
        }

        public static final Creator<District> CREATOR = new Creator<District>() {
            @Override public District createFromParcel(Parcel in) {
                return new District(in);
            }

            @Override public District[] newArray(int size) {
                return new District[size];
            }
        };

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(code);
            dest.writeString(name);
        }

        @Override public int describeContents() { return 0; }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }

    public static class Ward implements Parcelable {
        private int code;
        private String name;

        public Ward() {}

        public Ward(int code, String name) {
            this.code = code;
            this.name = name;
        }

        protected Ward(Parcel in) {
            code = in.readInt();
            name = in.readString();
        }

        public static final Creator<Ward> CREATOR = new Creator<Ward>() {
            @Override public Ward createFromParcel(Parcel in) {
                return new Ward(in);
            }

            @Override public Ward[] newArray(int size) {
                return new Ward[size];
            }
        };

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(code);
            dest.writeString(name);
        }

        @Override public int describeContents() { return 0; }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }
}
