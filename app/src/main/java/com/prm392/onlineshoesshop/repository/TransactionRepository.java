package com.prm392.onlineshoesshop.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prm392.onlineshoesshop.model.Transaction;
import com.prm392.onlineshoesshop.model.TransactionItem;

import java.util.List;

public class TransactionRepository {

    private final DatabaseReference transactionRef;

    public TransactionRepository() {
        transactionRef = FirebaseDatabase.getInstance().getReference("transactions");
    }

    public void createPendingTransaction(@NonNull String appTransId,
                                         @NonNull String userId,
                                         double totalAmount,
                                         double tax,
                                         double deliveryFee,
                                         @NonNull List<TransactionItem> items,
                                         @NonNull String paymentMethod) {

        Transaction transaction = new Transaction(
                appTransId,
                userId,
                System.currentTimeMillis(),
                totalAmount,
                tax,
                deliveryFee,
                items,
                Transaction.PaymentStatus.PENDING,          // 🔁 payment chưa hoàn tất
                Transaction.OrderStatus.WAITING_CONFIRMATION, // 🔁 chờ xác nhận đơn hàng
                paymentMethod
        );

        transactionRef.child(appTransId).setValue(transaction)
                .addOnSuccessListener(unused -> Log.d("TransactionRepo", "Transaction created"))
                .addOnFailureListener(e -> Log.e("TransactionRepo", "Error saving transaction", e));
    }

    public void updatePaymentStatus(@NonNull String appTransId,
                                    @NonNull Transaction.PaymentStatus paymentStatus,
                                    String zaloTransactionId) {

        transactionRef.child(appTransId).child("paymentStatus").setValue(paymentStatus);
        if (zaloTransactionId != null) {
            transactionRef.child(appTransId).child("transactionId").setValue(zaloTransactionId);
        }
    }

    public void updateOrderStatus(@NonNull String appTransId,
                                  @NonNull Transaction.OrderStatus orderStatus) {

        transactionRef.child(appTransId).child("orderStatus").setValue(orderStatus);
    }

    public void deleteTransaction(@NonNull String appTransId) {
        transactionRef.child(appTransId).removeValue()
                .addOnSuccessListener(unused -> Log.d("TransactionRepo", "Transaction deleted"))
                .addOnFailureListener(e -> Log.e("TransactionRepo", "Error deleting transaction", e));
    }
}
