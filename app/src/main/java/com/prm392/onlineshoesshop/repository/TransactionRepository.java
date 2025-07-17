package com.prm392.onlineshoesshop.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prm392.onlineshoesshop.model.Cancellation;
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

        transactionRef.child(appTransId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Log.d("TransactionRepo", "Transaction already exists, skipping creation to preserve createdAt");
                return; // ❌ Không ghi đè nếu đã tồn tại
            }

            // ✅ Giao dịch mới → tạo mới với thời gian hiện tại
            Transaction transaction = new Transaction(
                    appTransId,
                    userId,
                    System.currentTimeMillis(),
                    totalAmount,
                    tax,
                    deliveryFee,
                    items,
                    Transaction.PaymentStatus.PENDING,
                    Transaction.OrderStatus.WAITING_CONFIRMATION,
                    paymentMethod,
                    0
            );

            transactionRef.child(appTransId).setValue(transaction)
                    .addOnSuccessListener(unused -> Log.d("TransactionRepo", "Transaction created"))
                    .addOnFailureListener(e -> Log.e("TransactionRepo", "Error saving transaction", e));
        }).addOnFailureListener(e -> {
            Log.e("TransactionRepo", "Error checking existing transaction: " + e.getMessage());
        });
    }


    public void updatePaymentStatus(@NonNull String appTransId,
                                    @NonNull Transaction.PaymentStatus paymentStatus,
                                    String zaloTransactionId) {

        transactionRef.child(appTransId).child("paymentStatus").setValue(paymentStatus);

        if (zaloTransactionId != null) {
            transactionRef.child(appTransId).child("transactionId").setValue(zaloTransactionId);
        }

        if (paymentStatus == Transaction.PaymentStatus.SUCCESS) {
            transactionRef.child(appTransId).child("paidAt").setValue(System.currentTimeMillis());
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
    public void cancelOrder(@NonNull String appTransId,
                            @NonNull Cancellation cancellation,
                            @NonNull OnResultListener<Boolean> listener) {

        transactionRef.child(appTransId).get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Log.w("TransactionRepo", "Transaction not found: " + appTransId);
                listener.onResult(false);
                return;
            }

            transactionRef.child(appTransId).child("orderStatus")
                    .setValue(Transaction.OrderStatus.CANCELLED)
                    .addOnSuccessListener(unused -> {
                        // Ghi thông tin huỷ vào "cancellation"
                        transactionRef.child(appTransId).child("cancellation")
                                .setValue(cancellation)
                                .addOnSuccessListener(unused2 -> {
                                    Log.d("TransactionRepo", "Transaction cancelled with reason: " + cancellation.getReason());
                                    listener.onResult(true);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("TransactionRepo", "Failed to write cancellation reason", e);
                                    listener.onResult(false);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TransactionRepo", "Failed to cancel order", e);
                        listener.onResult(false);
                    });

        }).addOnFailureListener(e -> {
            Log.e("TransactionRepo", "Failed to fetch transaction", e);
            listener.onResult(false);
        });
    }
    public interface OnResultListener<T> {
        void onResult(T result);
    }


}
