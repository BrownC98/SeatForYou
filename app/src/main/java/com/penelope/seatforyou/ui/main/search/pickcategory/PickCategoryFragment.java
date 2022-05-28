package com.penelope.seatforyou.ui.main.search.pickcategory;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.penelope.seatforyou.R;
import com.penelope.seatforyou.databinding.FragmentPickCategoryBinding;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PickCategoryFragment extends DialogFragment {

    private FragmentPickCategoryBinding binding;
    private PickCategoryViewModel viewModel;


    public PickCategoryFragment() {
        super(R.layout.fragment_pick_category);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentPickCategoryBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(PickCategoryViewModel.class);

        binding.textViewConfirm.setOnClickListener(v -> viewModel.onConfirm());
        binding.textViewCancel.setOnClickListener(v -> viewModel.onCancel());

        ChecksAdapter adapter = new ChecksAdapter();
        binding.recyclerViewCategories.setAdapter(adapter);
        binding.recyclerViewCategories.setHasFixedSize(true);

        adapter.setOnItemSelectedListener(new ChecksAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
            }

            @Override
            public void onItemChecked(int position, boolean isChecked) {
                String category = adapter.getCurrentList().get(position).first;
                viewModel.onCategoryCheck(category, isChecked);
            }
        });

        viewModel.getAllCategories().observe(getViewLifecycleOwner(), allCategories ->
                viewModel.getSelectedCategories().observe(getViewLifecycleOwner(), selectedCategories -> {
                    List<Pair<String, Boolean>> list = new ArrayList<>();
                    for (String category : allCategories) {
                        list.add(new Pair<>(category, selectedCategories.contains(category)));
                    }
                    adapter.submitList(list);
                }));

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof PickCategoryViewModel.Event.NavigateBackWithResult) {
                List<String> categories = ((PickCategoryViewModel.Event.NavigateBackWithResult) event).categories;
                Bundle result = new Bundle();
                result.putStringArrayList("categories", categories != null ? new ArrayList<>(categories) : null);
                getParentFragmentManager().setFragmentResult("pick_category_fragment", result);
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}