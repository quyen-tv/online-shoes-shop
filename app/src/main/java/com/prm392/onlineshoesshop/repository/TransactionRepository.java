package com.prm392.onlineshoesshop.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prm392.onlineshoesshop.model.ItemModel;
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
                Transaction.Status.PENDING,
                paymentMethod
        );

        transactionRef.child(appTransId).setValue(transaction)
                .addOnSuccessListener(unused -> Log.d("TransactionRepo", "Pending transaction saved"))
                .addOnFailureListener(e -> Log.e("TransactionRepo", "Error saving transaction", e));
    }

    public void updateTransactionStatus(@NonNull String appTransId,
                                        @NonNull Transaction.Status status,
                                        String transactionId) {
        transactionRef.child(appTransId).child("status").setValue(status);
        if (transactionId != null) {
            transactionRef.child(appTransId).child("transactionId").setValue(transactionId);
        }
    }
    public void deleteTransaction(@NonNull String appTransId) {
        transactionRef.child(appTransId).removeValue()
                .addOnSuccessListener(unused -> Log.d("TransactionRepo", "Pending transaction deleted"))
                .addOnFailureListener(e -> Log.e("TransactionRepo", "Error deleting transaction", e));
    }

}
