package com.penelope.seatforyou.ui.main.search.setfilter;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.penelope.seatforyou.R;
import com.penelope.seatforyou.databinding.DialogPriceBinding;
import com.penelope.seatforyou.databinding.DialogRegionBinding;
import com.penelope.seatforyou.databinding.FragmentSetFilterBinding;
import com.penelope.seatforyou.ui.manager.shop.category.CardsAdapter;
import com.penelope.seatforyou.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SetFilterFragment extends Fragment {

    private FragmentSetFilterBinding binding;
    private SetFilterViewModel viewModel;


    public SetFilterFragment() {
        super(R.layout.fragment_set_filter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentSetFilterBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(SetFilterViewModel.class);

        binding.buttonClose.setOnClickListener(v -> viewModel.onCloseClick());
        binding.buttonSetPrice.setOnClickListener(v -> viewModel.onSetPriceClick());
        binding.buttonSetRegion.setOnClickListener(v -> viewModel.onSetRegionClick());
        binding.buttonSetCategory.setOnClickListener(v -> viewModel.onSetCategoryClick());
        binding.buttonClearAll.setOnClickListener(v -> viewModel.onClearClick());
        binding.buttonApply.setOnClickListener(v -> viewModel.onApplyClick());

        CardsAdapter categoryAdapter = new CardsAdapter(true);
        binding.recyclerCategories.setAdapter(categoryAdapter);
        binding.recyclerCategories.setHasFixedSize(true);

        viewModel.getMinPrice().observe(getViewLifecycleOwner(), minPrice ->
                viewModel.getMaxPrice().observe(getViewLifecycleOwner(), maxPrice -> {
                    StringBuilder sb = new StringBuilder();
                    if (minPrice != null) {
                        String strPrice = StringUtils.price(minPrice);
                        sb.append("최소 ").append(strPrice);
                        if (maxPrice != null) {
                            sb.append(" ~ ");
                        }
                    }
                    if (maxPrice != null) {
                        String strPrice = StringUtils.price(maxPrice);
                        sb.append("최대 ").append(strPrice);
                        if (maxPrice == 200000) {
                            sb.append("+");
                        }
                    }
                    binding.textViewPrice.setText(sb.toString());
                }));

        viewModel.getRegion().observe(getViewLifecycleOwner(), region -> {
            if (region != null) {
                binding.textViewRegion.setText(region);
            } else {
                binding.textViewRegion.setText("");
            }
        });

        viewModel.getSelectedCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoryAdapter.submitList(categories);
            }
        });

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof SetFilterViewModel.Event.ShowGeneralMessage) {
                String message = ((SetFilterViewModel.Event.ShowGeneralMessage) event).message;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            } else if (event instanceof SetFilterViewModel.Event.NavigateBack) {
                Navigation.findNavController(requireView()).popBackStack();
            } else if (event instanceof SetFilterViewModel.Event.PromptRegion) {
                showRegionDialog();
            } else if (event instanceof SetFilterViewModel.Event.PromptPrice) {
                showPriceDialog();
            } else if (event instanceof SetFilterViewModel.Event.NavigateToPickCategoryScreen) {
                List<String> categories = ((SetFilterViewModel.Event.NavigateToPickCategoryScreen) event).selectedList;
                NavDirections navDirections = SetFilterFragmentDirections.actionSetFilterFragmentToPickCategoryFragment(new ArrayList<>(categories));
                Navigation.findNavController(requireView()).navigate(navDirections);
            } else if (event instanceof SetFilterViewModel.Event.NavigateBackWithResult) {
                String region = ((SetFilterViewModel.Event.NavigateBackWithResult) event).region;
                int minPrice = ((SetFilterViewModel.Event.NavigateBackWithResult) event).minPrice;
                int maxPrice = ((SetFilterViewModel.Event.NavigateBackWithResult) event).maxPrice;
                List<String> categories = ((SetFilterViewModel.Event.NavigateBackWithResult) event).categories;
                Bundle result = new Bundle();
                result.putString("region", region);
                result.putInt("min_price", minPrice);
                result.putInt("max_price", maxPrice);
                result.putStringArrayList("categories", new ArrayList<>(categories));
                getParentFragmentManager().setFragmentResult("set_filter_fragment", result);
                Navigation.findNavController(requireView()).popBackStack();
            }
        });

        getParentFragmentManager().setFragmentResultListener("pick_category_fragment", getViewLifecycleOwner(),
                (requestKey, result) -> {
                    List<String> categories = result.getStringArrayList("categories");
                    viewModel.onPickCategoryResult(categories);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showRegionDialog() {

        DialogRegionBinding binding = DialogRegionBinding.inflate(getLayoutInflater());

        new AlertDialog.Builder(requireContext())
                .setTitle("지역 설정")
                .setView(binding.getRoot())
                .setPositiveButton("설정", (dialog, which) ->
                        viewModel.onRegionSelected(binding.editTextRegion.getText().toString()))
                .setNegativeButton("취소", null)
                .show();
    }

    private void showPriceDialog() {

        DialogPriceBinding binding = DialogPriceBinding.inflate(getLayoutInflater());
        MutableLiveData<Integer> priceMin = new MutableLiveData<>(0);
        MutableLiveData<Integer> priceMax = new MutableLiveData<>(0);
        assert priceMax.getValue() != null && priceMin.getValue() != null;

        binding.seekBarPriceMin.setMin(0);
        binding.seekBarPriceMin.setMax(200000);
        binding.seekBarPriceMin.setProgress(0);
        binding.seekBarPriceMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int adjusted = Math.round(progress / 1000f) * 1000;
                priceMin.setValue(adjusted);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        binding.seekBarPriceMax.setMin(0);
        binding.seekBarPriceMax.setMax(200000);
        binding.seekBarPriceMax.setProgress(0);
        binding.seekBarPriceMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int adjusted = Math.round(progress / 1000f) * 1000;
                priceMax.setValue(adjusted);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        priceMax.observe(getViewLifecycleOwner(), p -> {
            String strPrice = "최대 " + StringUtils.price(p);
            binding.textViewPriceMax.setText(strPrice);
        });

        priceMin.observe(getViewLifecycleOwner(), p -> {
            String strPrice = "최소 " + StringUtils.price(p);
            binding.textViewPriceMin.setText(strPrice);
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("1인당 가격 설정")
                .setView(binding.getRoot())
                .setPositiveButton("설정", (dialog, which) ->
                        viewModel.onPriceSelected(priceMin.getValue(), priceMax.getValue()))
                .setNegativeButton("취소", null)
                .show();
    }

}




