package com.penelope.seatforyou.ui.main.reserve;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.penelope.seatforyou.R;
import com.penelope.seatforyou.databinding.ButtonItemBinding;

public class ButtonsAdapter extends ListAdapter<Pair<String, Object>, ButtonsAdapter.ButtonViewHolder> {

    class ButtonViewHolder extends RecyclerView.ViewHolder {

        public final ButtonItemBinding binding;

        public ButtonViewHolder(ButtonItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemSelectedListener != null) {
                    onItemSelectedListener.onItemSelected(position);
                }
            });
        }

        public void bind(Pair<String, Object> model) {

            binding.textView41.setText(model.first);

            final int colorBkgUnchecked = 0xFFFFFFFF;
            final int colorTextUnchecked = context.getColor(R.color.colorBlueDark);
            final int colorBkgChecked = context.getColor(R.color.colorBlue);
            final int colorTextChecked = 0xFFFFFFFF;

            boolean isSelected = (getAdapterPosition() == selected);
            binding.getRoot().setCardBackgroundColor(isSelected ? colorBkgChecked : colorBkgUnchecked);
            binding.textView41.setTextColor(isSelected ? colorTextChecked : colorTextUnchecked);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
    }

    private final Context context;
    private OnItemSelectedListener onItemSelectedListener;
    private int selected = -1;


    public ButtonsAdapter(Context context) {
        super(new DiffUtilCallback());
        this.context = context;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    public void setSelected(int index) {
        selected = index;
    }


    @NonNull
    @Override
    public ButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ButtonItemBinding binding = ButtonItemBinding.inflate(layoutInflater, parent, false);
        return new ButtonViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ButtonViewHolder holder, int position) {
        holder.bind(getItem(position));
    }


    static class DiffUtilCallback extends DiffUtil.ItemCallback<Pair<String, Object>> {

        @Override
        public boolean areItemsTheSame(@NonNull Pair<String, Object> oldItem, @NonNull Pair<String, Object> newItem) {
            return oldItem.second.equals(newItem.second);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Pair<String, Object> oldItem, @NonNull Pair<String, Object> newItem) {
            return oldItem.equals(newItem);
        }
    }

}



