package com.prm392.onlineshoesshop.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Transaction implements Parcelable {

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED
    }

    public enum OrderStatus {
        WAITING_CONFIRMATION, // Chờ xác nhận
        WAITING_FOR_PICKUP,   // Chờ lấy hàng
        DELIVERING,           // Chờ giao hàng
        DELIVERED,            // Đã giao
        CANCELLED             // Đã hủy
    }

    private String transactionId;
    private String appTransId;
    private String userId;
    private long createdAt;
    private double totalAmount;
    private double tax;
    private double deliveryFee;
    private List<TransactionItem> items;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;
    private String paymentMethod;
    private Long paidAt; // nullable - chỉ có khi thanh toán thành công

    public Transaction() {}

    public Transaction(String appTransId, String userId, long createdAt,
                       double totalAmount, double tax, double deliveryFee,
                       List<TransactionItem> items,
                       PaymentStatus paymentStatus,
                       OrderStatus orderStatus,
                       String paymentMethod, long paidAt) {
        this.appTransId = appTransId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.totalAmount = totalAmount;
        this.tax = tax;
        this.deliveryFee = deliveryFee;
        this.items = items;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
    }

    // Getter & Setter
    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public Long getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Long paidAt) {
        this.paidAt = paidAt;
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

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }
    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
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
        paymentStatus = PaymentStatus.valueOf(in.readString());
        orderStatus = OrderStatus.valueOf(in.readString());
        paymentMethod = in.readString();
        paidAt = (Long) in.readValue(Long.class.getClassLoader());

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
        dest.writeString(paymentStatus.name());
        dest.writeString(orderStatus.name());
        dest.writeString(paymentMethod);
        dest.writeValue(paidAt); // dùng writeValue vì có thể null

    }
}
