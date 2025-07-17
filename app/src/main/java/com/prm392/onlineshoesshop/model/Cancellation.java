package com.prm392.onlineshoesshop.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Cancellation implements Parcelable {
    private String cancelledBy;
    private String reason;
    private long timestamp;

    public Cancellation() {
    }

    public Cancellation(String cancelledBy, String reason, long timestamp) {
        this.cancelledBy = cancelledBy;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    protected Cancellation(Parcel in) {
        cancelledBy = in.readString();
        reason = in.readString();
        timestamp = in.readLong();
    }

    public static final Creator<Cancellation> CREATOR = new Creator<Cancellation>() {
        @Override
        public Cancellation createFromParcel(Parcel in) {
            return new Cancellation(in);
        }

        @Override
        public Cancellation[] newArray(int size) {
            return new Cancellation[size];
        }
    };

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(cancelledBy);
        parcel.writeString(reason);
        parcel.writeLong(timestamp);
    }
}
