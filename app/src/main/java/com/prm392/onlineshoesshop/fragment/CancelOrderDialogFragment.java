package com.prm392.onlineshoesshop.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.activity.TransactionHistoryActivity;
import com.prm392.onlineshoesshop.model.Cancellation;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.model.Transaction;
import com.prm392.onlineshoesshop.model.TransactionItem;
import com.prm392.onlineshoesshop.repository.TransactionRepository;

import java.util.Arrays;
import java.util.List;

public class CancelOrderDialogFragment extends DialogFragment {

    private static final String ARG_TRANSACTION = "transaction";
    private Transaction transaction;

    public static CancelOrderDialogFragment newInstance(Transaction transaction) {
        CancelOrderDialogFragment fragment = new CancelOrderDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRANSACTION, transaction);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        transaction = getArguments().getParcelable(ARG_TRANSACTION);

        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_cancel_order, null);
        Spinner spinnerReasons = view.findViewById(R.id.spinnerReasons);
        Button btnCancel = view.findViewById(R.id.btnCancelOrder);
        Button btnDismiss = view.findViewById(R.id.btnDismiss);

        List<String> reasons = Arrays.asList(
                "Tôi đổi ý", "Thời gian giao hàng quá lâu",
                "Tìm thấy sản phẩm rẻ hơn", "Thông tin sản phẩm chưa rõ ràng",
                "Lý do khác"
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner_cancel_reason, // layout cho item
                reasons
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_cancel_reason); // áp dụng cho dropdown luôn
        spinnerReasons.setAdapter(adapter);


        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .create();

        btnCancel.setOnClickListener(v -> {
            String reason = spinnerReasons.getSelectedItem().toString();
            String cancelledBy = FirebaseAuth.getInstance().getCurrentUser().getUid(); // hoặc "user"
            long timestamp = System.currentTimeMillis();

            Cancellation cancellation = new Cancellation(cancelledBy, reason, timestamp);

            TransactionRepository repo = new TransactionRepository();
            String transId = transaction.getAppTransId();

            repo.cancelOrder(transId, cancellation, success -> {
                if (success) {
                    if ("CashOnDelivery".equalsIgnoreCase(transaction.getPaymentMethod())) {
                        increaseStock(transaction.getItems());
                        decreaseSold(transaction.getItems());
                    }
                    if ("ZaloPay".equalsIgnoreCase(transaction.getPaymentMethod())) {
                        increaseStock(transaction.getItems());
                    }
                    Toast.makeText(requireContext(), "Đã huỷ đơn hàng", Toast.LENGTH_SHORT).show();
                    dismiss();
                    Intent intent = new Intent(requireContext(), TransactionHistoryActivity.class);
                    intent.putExtra("select_cancelled_tab", true); // Truyền flag
                    startActivity(intent);
                    requireActivity().finish(); // kết thúc activity hiện tại


                } else {
                    Toast.makeText(requireContext(), "Lỗi khi huỷ đơn", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnDismiss.setOnClickListener(v -> dismiss());

        return dialog;
    }

    private void increaseStock(List<TransactionItem> items) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Items");

        for (TransactionItem item : items) {
            String itemId = item.getItemId();
            String size = item.getSize();
            int quantity = item.getQuantity();

            DatabaseReference stockRef = itemsRef.child(itemId).child("stockEntries");

            stockRef.get().addOnSuccessListener(dataSnapshot -> {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ItemModel.StockEntry entry = snapshot.getValue(ItemModel.StockEntry.class);
                    if (entry != null && size.equals(entry.getSize())) {
                        int newQty = entry.getQuantity() + quantity;
                        snapshot.getRef().child("quantity").setValue(newQty);
                        break;
                    }
                }
            });
        }
    }

    private void decreaseSold(List<TransactionItem> items) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Items");

        for (TransactionItem item : items) {
            String itemId = item.getItemId();
            int quantity = item.getQuantity();

            DatabaseReference itemRef = itemsRef.child(itemId);

            itemRef.child("sold").get().addOnSuccessListener(snap -> {
                Long currentSold = snap.getValue(Long.class);
                long newSold = Math.max((currentSold != null ? currentSold : 0) - quantity, 0);
                itemRef.child("sold").setValue(newSold);
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9); // 90% chiều rộng màn hình
            getDialog().getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

}
