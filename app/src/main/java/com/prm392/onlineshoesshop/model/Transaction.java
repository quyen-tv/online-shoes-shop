package com.prm392.onlineshoesshop.model;

import java.util.List;

public class Transaction {
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
    private List<ItemModel> items;   // Danh sách sản phẩm trong đơn hàng
    private Status status;           // Trạng thái giao dịch
    private String paymentMethod;    // "ZaloPay" hoặc sau này bạn có thể thêm "COD", "Momo"...

    public Transaction() {}

    public Transaction(String appTransId, String userId, long createdAt,
                       double totalAmount, double tax, double deliveryFee,
                       List<ItemModel> items, Status status, String paymentMethod) {
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

    public List<ItemModel> getItems() {
        return items;
    }
    public void setItems(List<ItemModel> items) {
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
}
