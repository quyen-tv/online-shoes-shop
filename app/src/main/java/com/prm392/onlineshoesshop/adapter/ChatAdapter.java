package com.prm392.onlineshoesshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.activity.ChatbotActivity;
import com.bumptech.glide.Glide;
import com.prm392.onlineshoesshop.model.ItemModel;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import androidx.core.content.ContextCompat;
import io.noties.markwon.Markwon;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatbotActivity.ChatMessage> messages;
    private static final int TYPE_USER = 0;
    private static final int TYPE_BOT = 1;
    private static final int TYPE_PRODUCT = 2;
    private static final int TYPE_TYPING = 3;

    public ChatAdapter(List<ChatbotActivity.ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatbotActivity.ChatMessage msg = messages.get(position);
        if (msg.type == ChatbotActivity.ChatMessage.Type.USER)
            return TYPE_USER;
        if (msg.type == ChatbotActivity.ChatMessage.Type.BOT)
            return TYPE_BOT;
        if (msg.type == ChatbotActivity.ChatMessage.Type.PRODUCT)
            return TYPE_PRODUCT;
        return TYPE_TYPING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else if (viewType == TYPE_BOT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_bot, parent, false);
            return new BotViewHolder(view);
        } else if (viewType == TYPE_PRODUCT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_product, parent, false);
            return new ProductViewHolder(view);
        } else if (viewType == TYPE_TYPING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_typing_indicator, parent, false);
            return new TypingViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_bot, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatbotActivity.ChatMessage msg = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvMessageUser.setText(msg.message);
            Context context = holder.itemView.getContext();
            if (msg.status == ChatbotActivity.ChatMessage.Status.SENT) {
                ((UserViewHolder) holder).tvStatus.setText("Đã gửi");
                ((UserViewHolder) holder).tvStatus
                        .setTextColor(ContextCompat.getColor(context, R.color.chat_status_sending));
                ((UserViewHolder) holder).tvStatus.setVisibility(View.VISIBLE);
            } else if (msg.status == ChatbotActivity.ChatMessage.Status.FAILED) {
                ((UserViewHolder) holder).tvStatus.setText("❗ Gửi thất bại");
                ((UserViewHolder) holder).tvStatus
                        .setTextColor(ContextCompat.getColor(context, R.color.chat_status_failed));
                ((UserViewHolder) holder).tvStatus.setVisibility(View.VISIBLE);
            } else {
                ((UserViewHolder) holder).tvStatus.setVisibility(View.GONE);
            }
        } else if (holder instanceof BotViewHolder) {
            // Hiển thị markdown cho tin nhắn bot
            Markwon markwon = Markwon.create(holder.itemView.getContext());
            markwon.setMarkdown(((BotViewHolder) holder).tvMessageBot, msg.message);
        } else if (holder instanceof ProductViewHolder) {
            ItemModel product = msg.product;
            ProductViewHolder vh = (ProductViewHolder) holder;
            vh.tvProductName.setText(product.getTitle());
            try {
                String priceStr = String.valueOf(product.getPrice());
                double price = Double.parseDouble(priceStr);
                java.text.NumberFormat format = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
                vh.tvProductPrice.setText(String.format("₫%s", format.format(price)));
            } catch (Exception e) {
                vh.tvProductPrice.setText(String.format("₫%s", product.getPrice()));
            }
            vh.tvProductRating.setText(String.valueOf(product.getRating()));
            if (product.getPicUrl() != null && !product.getPicUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(product.getPicUrl().get(0))
                        .placeholder(R.drawable.placeholder)
                        .into(vh.imgProduct);
            } else {
                vh.imgProduct.setImageResource(R.drawable.placeholder);
            }
            // Thêm sự kiện click để mở DetailActivity
            holder.itemView.setOnClickListener(v -> {
                Context context = holder.itemView.getContext();
                android.content.Intent intent = new android.content.Intent(context,
                        com.prm392.onlineshoesshop.activity.DetailActivity.class);
                intent.putExtra("object", product);
                context.startActivity(intent);
            });
        } else if (holder instanceof TypingViewHolder) {
            TypingViewHolder vh = (TypingViewHolder) holder;
            animateDot(vh.dot1, 0);
            animateDot(vh.dot2, 300);
            animateDot(vh.dot3, 600);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void animateDot(View dot, long delay) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(dot, "translationY", 0, -16, 0);
        animator.setDuration(600);
        animator.setStartDelay(delay);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageUser;
        TextView tvStatus;

        UserViewHolder(View itemView) {
            super(itemView);
            tvMessageUser = itemView.findViewById(R.id.tvMessageUser);
            tvStatus = itemView.findViewById(R.id.tvStatus); // Thêm TextView này vào layout item_chat_user.xml
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageBot;

        BotViewHolder(View itemView) {
            super(itemView);
            tvMessageBot = itemView.findViewById(R.id.tvMessageBot);
        }
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, imgStar;
        TextView tvProductName, tvProductPrice, tvProductRating;
        android.widget.Button btnAddToCart;

        ProductViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductRating = itemView.findViewById(R.id.tvProductRating);
            imgStar = itemView.findViewById(R.id.imgStar);
        }
    }

    static class TypingViewHolder extends RecyclerView.ViewHolder {
        View dot1, dot2, dot3;

        TypingViewHolder(View itemView) {
            super(itemView);
            dot1 = itemView.findViewById(R.id.dot1);
            dot2 = itemView.findViewById(R.id.dot2);
            dot3 = itemView.findViewById(R.id.dot3);
        }
    }
}
