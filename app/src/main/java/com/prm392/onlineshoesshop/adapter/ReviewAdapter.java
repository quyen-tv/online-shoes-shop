package com.prm392.onlineshoesshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.model.Review;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.graphics.PorterDuff;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviews;
    private boolean showAll;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
        this.showAll = false;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvUserName.setText(review.getUserName());
        holder.tvComment.setText(review.getComment());
        holder.tvSize.setText(review.getUserImageUrl() != null ? ("Size: " + review.getUserImageUrl()) : "");
        // Hiển thị ngày
        String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date(review.getCreatedAt()));
        holder.tvDate.setText(dateStr);
        // Hiển thị số sao động
        setStars(holder, review.getRating());
        // Avatar
        if (review.getUserAvatar() != null && !review.getUserAvatar().isEmpty()) {
            Glide.with(holder.imgAvatar.getContext())
                    .load(review.getUserAvatar())
                    .placeholder(R.drawable.default_user)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.default_user);
        }
    }

    private void setStars(ReviewViewHolder holder, double rating) {
        int fullStars = (int) rating;
        boolean hasHalf = (rating - fullStars) >= 0.25 && (rating - fullStars) < 0.75;
        int i = 0;
        for (; i < fullStars; i++) {
            holder.stars[i].setColorFilter(holder.starColor, PorterDuff.Mode.SRC_IN);
        }
        if (hasHalf && i < 5) {
            holder.stars[i].setImageResource(R.drawable.star);
            holder.stars[i].setColorFilter(holder.starColor, PorterDuff.Mode.SRC_IN);
            holder.stars[i].setAlpha(0.5f);
            i++;
        }
        for (; i < 5; i++) {
            holder.stars[i].setColorFilter(holder.starGrey, PorterDuff.Mode.SRC_IN);
            holder.stars[i].setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        if (reviews == null)
            return 0;
        if (showAll)
            return reviews.size();
        return Math.min(3, reviews.size());
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvUserName, tvSize, tvComment, tvDate;
        ImageView[] stars = new ImageView[5];
        int starColor, starGrey;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvDate = itemView.findViewById(R.id.tvDate);
            stars[0] = itemView.findViewById(R.id.star1);
            stars[1] = itemView.findViewById(R.id.star2);
            stars[2] = itemView.findViewById(R.id.star3);
            stars[3] = itemView.findViewById(R.id.star4);
            stars[4] = itemView.findViewById(R.id.star5);
            starColor = itemView.getContext().getResources().getColor(R.color.yellow);
            starGrey = itemView.getContext().getResources().getColor(R.color.grey);
        }
    }
}