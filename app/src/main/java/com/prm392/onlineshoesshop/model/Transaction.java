package com.prm392.onlineshoesshop.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Transaction implements Parcelable {
    public enum Status {
        PENDING, SUCCESS, FAILED
    }

    private String transactionId;    // Mã giao dịch từ ZaloPay (có sau khi thành công)
    private String appTransId;       // Mã giao dịch nội bộ (từ createOrder)
    private String userId;           // ID của người dùng
    private long createdAt;          // Thời gian tạo giao dịch (timestamp)
    private double totalAmount;      // Tổng tiền đơn hàng (USD)
    private double tax;              // Thuế
    private double deliveryFee;      // Phí giao hàng
    private List<TransactionItem> items;// Danh sách sản phẩm trong đơn hàng
    private Status status;           // Trạng thái giao dịch
    private String paymentMethod;    // "ZaloPay" hoặc sau này bạn có thể thêm "COD", "Momo"...

    public Transaction() {}

    public Transaction(String appTransId, String userId, long createdAt,
                       double totalAmount, double tax, double deliveryFee,
                       List<TransactionItem> items, Status status, String paymentMethod) {
        this.appTransId = appTransId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.totalAmount = totalAmount;
        this.tax = tax;
        this.deliveryFee = deliveryFee;
        this.items = items;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }


    // Getter/setter

    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAppTransId() {
        return appTransId;
    }
    public void setAppTransId(String appTransId) {
        this.appTransId = appTransId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getTax() {
        return tax;
    }
    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }
    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public List<TransactionItem> getItems() {
        return items;
    }
    public void setItems(List<TransactionItem> items) {
        this.items = items;
    }
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Parcelable implementation
    protected Transaction(Parcel in) {
        transactionId = in.readString();
        appTransId = in.readString();
        userId = in.readString();
        createdAt = in.readLong();
        totalAmount = in.readDouble();
        tax = in.readDouble();
        deliveryFee = in.readDouble();
        items = new ArrayList<>();
        in.readTypedList(items, TransactionItem.CREATOR);
        status = Status.valueOf(in.readString());
        paymentMethod = in.readString();
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(transactionId);
        dest.writeString(appTransId);
        dest.writeString(userId);
        dest.writeLong(createdAt);
        dest.writeDouble(totalAmount);
        dest.writeDouble(tax);
        dest.writeDouble(deliveryFee);
        dest.writeTypedList(items);
        dest.writeString(status.name());
        dest.writeString(paymentMethod);
    }
}
