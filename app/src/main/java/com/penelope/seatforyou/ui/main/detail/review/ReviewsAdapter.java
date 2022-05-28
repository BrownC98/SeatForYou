package com.penelope.seatforyou.ui.main.detail.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.penelope.seatforyou.data.review.Review;
import com.penelope.seatforyou.data.user.User;
import com.penelope.seatforyou.databinding.ReviewItemBinding;
import com.penelope.seatforyou.utils.TimeUtils;

import java.time.LocalDate;
import java.util.Map;

public class ReviewsAdapter extends ListAdapter<Review, ReviewsAdapter.ReviewViewHolder> {

    class ReviewViewHolder extends RecyclerView.ViewHolder {

        private final ReviewItemBinding binding;

        public ReviewViewHolder(ReviewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemSelectedListener != null) {
                    onItemSelectedListener.onItemSelected(position);
                }
            });

            binding.imageViewDeleteReview.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemSelectedListener != null) {
                    onItemSelectedListener.onDeleteClick(position);
                }
            });

            binding.imageViewEditReview.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemSelectedListener != null) {
                    onItemSelectedListener.onEditClick(position);
                }
            });
        }

        public void bind(Review model) {

            binding.textViewReviewSummary.setText(model.getSummary());
            binding.textViewReviewDetail.setText(model.getDetail());

            LocalDate date = TimeUtils.toDate(model.getCreated());
            String strDate = TimeUtils.formatDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            binding.textViewReviewDate.setText(strDate);

            User writer = userMap.get(model.getUid());
            binding.textViewReviewWriter.setText(writer != null ? writer.getName() : "");

            binding.ratingBarReview.setRating(model.getRating());

            boolean isEditable = model.getUid().equals(uid);
            binding.imageViewEditReview.setVisibility(isEditable ? View.VISIBLE : View.INVISIBLE);
            binding.imageViewDeleteReview.setVisibility(isEditable ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    private OnItemSelectedListener onItemSelectedListener;
    private final Map<String, User> userMap;
    private final String uid;


    public ReviewsAdapter(Map<String, User> userMap, String uid) {
        super(new DiffUtilCallback());
        this.userMap = userMap;
        this.uid = uid;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ReviewItemBinding binding = ReviewItemBinding.inflate(layoutInflater, parent, false);
        return new ReviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(getItem(position));
    }


    static class DiffUtilCallback extends DiffUtil.ItemCallback<Review> {

        @Override
        public boolean areItemsTheSame(@NonNull Review oldItem, @NonNull Review newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Review oldItem, @NonNull Review newItem) {
            return oldItem.equals(newItem);
        }
    }

}