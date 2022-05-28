package com.penelope.seatforyou.ui.main.search.pickcategory;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.penelope.seatforyou.databinding.CheckItemBinding;

public class ChecksAdapter extends ListAdapter<Pair<String, Boolean>, ChecksAdapter.CheckViewHolder> {

    class CheckViewHolder extends RecyclerView.ViewHolder {

        private final CheckItemBinding binding;

        public CheckViewHolder(CheckItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Pair<String, Boolean> model) {

            binding.checkbox.setText(model.first);

            binding.checkbox.setOnCheckedChangeListener(null);
            binding.checkbox.setChecked(model.second);
            binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemSelectedListener != null) {
                    onItemSelectedListener.onItemChecked(position, isChecked);
                }
            });
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
        void onItemChecked(int position, boolean isChecked);
    }

    private OnItemSelectedListener onItemSelectedListener;


    public ChecksAdapter() {
        super(new DiffUtilCallback());
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    @NonNull
    @Override
    public CheckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        CheckItemBinding binding = CheckItemBinding.inflate(layoutInflater, parent, false);
        return new CheckViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckViewHolder holder, int position) {
        holder.bind(getItem(position));
    }


    static class DiffUtilCallback extends DiffUtil.ItemCallback<Pair<String, Boolean>> {

        @Override
        public boolean areItemsTheSame(@NonNull Pair<String, Boolean> oldItem, @NonNull Pair<String, Boolean> newItem) {
            return oldItem.first.equals(newItem.first);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Pair<String, Boolean> oldItem, @NonNull Pair<String, Boolean> newItem) {
            return oldItem.equals(newItem);
        }
    }

}




