package com.penelope.seatforyou.ui.main.reserve;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.seatforyou.R;
import com.penelope.seatforyou.databinding.FragmentReserveBinding;
import com.penelope.seatforyou.utils.ui.AuthListenerFragment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReserveFragment extends AuthListenerFragment {

    private FragmentReserveBinding binding;
    private ReserveViewModel viewModel;


    public ReserveFragment() {
        super(R.layout.fragment_reserve);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentReserveBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(ReserveViewModel.class);

        ButtonsAdapter adapter = new ButtonsAdapter(requireContext());
        binding.recyclerViewPersonNumber.setAdapter(adapter);
        binding.recyclerViewPersonNumber.setHasFixedSize(true);

        adapter.setOnItemSelectedListener(position -> {
            int personNumber = (int) adapter.getCurrentList().get(position).second;
            viewModel.onPersonNumberClick(personNumber);
        });

        List<Pair<String, Object>> entries = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            entries.add(new Pair<>(i + "명", i));
        }
        adapter.submitList(entries);

        binding.button630pm.setOnClickListener(v -> viewModel.onTimeClick(18, 30));
        binding.button700pm.setOnClickListener(v -> viewModel.onTimeClick(19, 0));
        binding.button730pm.setOnClickListener(v -> viewModel.onTimeClick(19, 30));
        binding.button800pm.setOnClickListener(v -> viewModel.onTimeClick(20, 0));
        binding.button900pm.setOnClickListener(v -> viewModel.onTimeClick(21, 0));

        binding.datePicker.setOnDateChangedListener((view1, year, monthOfYear, dayOfMonth) ->
                viewModel.onDateClick(year, monthOfYear + 1, dayOfMonth));

        binding.buttonOk.setOnClickListener(v -> viewModel.onOkClick());
        binding.buttonBack.setOnClickListener(v -> viewModel.onBackClick());
        binding.buttonClose.setOnClickListener(v -> viewModel.onCloseClick());

        final Button[] buttonsTime = {
                binding.button630pm, binding.button700pm, binding.button730pm,
                binding.button800pm, binding.button900pm
        };
        final int colorBkgUnchecked = 0xFFFFFFFF;
        final int colorTextUnchecked = getResources().getColor(R.color.colorBlueDark, null);
        final int colorBkgChecked = getResources().getColor(R.color.colorBlue, null);
        final int colorTextChecked = 0xFFFFFFFF;

        viewModel.getPersonNumber().observe(getViewLifecycleOwner(), personNumber -> {
            RecyclerView.LayoutManager layoutManager = binding.recyclerViewPersonNumber.getLayoutManager();
            assert layoutManager != null;
            for (int i = 0; i < adapter.getItemCount(); i++) {
                View itemView = layoutManager.findViewByPosition(i);
                if (itemView != null) {
                    ButtonsAdapter.ButtonViewHolder viewHolder = (ButtonsAdapter.ButtonViewHolder)
                            binding.recyclerViewPersonNumber.getChildViewHolder(itemView);
                    if (i == personNumber - 1) {
                        viewHolder.binding.getRoot().setCardBackgroundColor(colorBkgChecked);
                        viewHolder.binding.textView41.setTextColor(colorTextChecked);
                    } else {
                        viewHolder.binding.getRoot().setCardBackgroundColor(colorBkgUnchecked);
                        viewHolder.binding.textView41.setTextColor(colorTextUnchecked);
                    }
                }
            }
        });

        viewModel.getTime().observe(getViewLifecycleOwner(), time -> {
            for (Button button : buttonsTime) {
                button.setBackgroundColor(colorBkgUnchecked);
                button.setTextColor(colorTextUnchecked);
            }
            int index = -1;
            if (time.getHour() == 18 && time.getMinute() == 30) {
                index = 0;
            } else if (time.getHour() == 19 && time.getMinute() == 0) {
                index = 1;
            } else if (time.getHour() == 19 && time.getMinute() == 30) {
                index = 2;
            } else if (time.getHour() == 20 && time.getMinute() == 0) {
                index = 3;
            } else if (time.getHour() == 21 && time.getMinute() == 0) {
                index = 4;
            }
            if (index != -1) {
                buttonsTime[index].setBackgroundColor(colorBkgChecked);
                buttonsTime[index].setTextColor(colorTextChecked);
            }
        });

        viewModel.getDate().observe(getViewLifecycleOwner(), date -> {

            binding.button630pm.setEnabled(true);
            binding.button700pm.setEnabled(true);
            binding.button730pm.setEnabled(true);
            binding.button800pm.setEnabled(true);
            binding.button900pm.setEnabled(true);

            if (date.isBefore(LocalDate.now())) {
                binding.button630pm.setEnabled(false);
                binding.button700pm.setEnabled(false);
                binding.button730pm.setEnabled(false);
                binding.button800pm.setEnabled(false);
                binding.button900pm.setEnabled(false);
            } else if (date.isEqual(LocalDate.now())) {
                LocalTime now = LocalTime.now();
                if (now.isAfter(LocalTime.of(21, 0))) {
                    binding.button900pm.setEnabled(false);
                }
                if (now.isAfter(LocalTime.of(20, 0))) {
                    binding.button800pm.setEnabled(false);
                }
                if (now.isAfter(LocalTime.of(19, 30))) {
                    binding.button730pm.setEnabled(false);
                }
                if (now.isAfter(LocalTime.of(19, 0))) {
                    binding.button700pm.setEnabled(false);
                }
                if (now.isAfter(LocalTime.of(18, 30))) {
                    binding.button630pm.setEnabled(false);
                }
            }

            binding.button900pm.setAlpha(binding.button900pm.isEnabled() ? 1f : 0.5f);
            binding.button800pm.setAlpha(binding.button800pm.isEnabled() ? 1f : 0.5f);
            binding.button730pm.setAlpha(binding.button730pm.isEnabled() ? 1f : 0.5f);
            binding.button700pm.setAlpha(binding.button700pm.isEnabled() ? 1f : 0.5f);
            binding.button630pm.setAlpha(binding.button630pm.isEnabled() ? 1f : 0.5f);
        });

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof ReserveViewModel.Event.NavigateBack) {
                Navigation.findNavController(requireView()).popBackStack();
            } else if (event instanceof ReserveViewModel.Event.NavigateBackWithResult) {
                boolean success = ((ReserveViewModel.Event.NavigateBackWithResult) event).success;
                Bundle result = new Bundle();
                result.putBoolean("success", success);
                getParentFragmentManager().setFragmentResult("reserve_fragment", result);
                Navigation.findNavController(requireView()).popBackStack();
            } else if (event instanceof ReserveViewModel.Event.ShowGeneralMessage) {
                String message = ((ReserveViewModel.Event.ShowGeneralMessage) event).message;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        viewModel.onAuthStateChanged(firebaseAuth);
    }

}