package com.penelope.seatforyou.ui.main.detail.addreview;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.seatforyou.R;
import com.penelope.seatforyou.data.review.Review;
import com.penelope.seatforyou.databinding.FragmentAddEditReviewBinding;
import com.penelope.seatforyou.utils.ui.AuthListenerFragment;
import com.penelope.seatforyou.utils.ui.OnTextChangeListener;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddEditReviewFragment extends AuthListenerFragment {

    private FragmentAddEditReviewBinding binding;
    private AddEditReviewViewModel viewModel;


    public AddEditReviewFragment() {
        super(R.layout.fragment_add_edit_review);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentAddEditReviewBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(AddEditReviewViewModel.class);

        binding.editTextReviewSummary.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                viewModel.onSummaryChange(text);
            }
        });
        binding.editTextReviewDetail.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                viewModel.onDetailChange(text);
            }
        });
        binding.ratingBarReview.setOnRatingBarChangeListener((ratingBar, rating, fromUser) ->
                viewModel.onRatingChange(rating));

        binding.fabSubmitReview.setOnClickListener(v -> viewModel.onSubmitClick());

        Review oldReview = viewModel.getOldReview();
        if (oldReview != null) {
            binding.editTextReviewSummary.setText(oldReview.getSummary());
            binding.editTextReviewDetail.setText(oldReview.getDetail());
            binding.ratingBarReview.setRating(oldReview.getRating());
        }

        viewModel.isUploadInProgress().observe(getViewLifecycleOwner(), isUploadInProgress ->
                binding.progressBar6.setVisibility(isUploadInProgress ? View.VISIBLE : View.INVISIBLE));

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof AddEditReviewViewModel.Event.NavigateBackWithResult) {
                boolean addOrEdit = ((AddEditReviewViewModel.Event.NavigateBackWithResult) event).addOrEdit;
                Bundle result = new Bundle();
                result.putBoolean("add_or_edit", addOrEdit);
                getParentFragmentManager().setFragmentResult("add_edit_review_fragment", result);
                Navigation.findNavController(requireView()).popBackStack();
            } else if (event instanceof AddEditReviewViewModel.Event.ShowGeneralMessage) {
                String message = ((AddEditReviewViewModel.Event.ShowGeneralMessage) event).message;
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