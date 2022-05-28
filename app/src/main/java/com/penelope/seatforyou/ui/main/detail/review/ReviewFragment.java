package com.penelope.seatforyou.ui.main.detail.review;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.seatforyou.R;
import com.penelope.seatforyou.data.review.Review;
import com.penelope.seatforyou.databinding.FragmentReviewBinding;
import com.penelope.seatforyou.utils.ui.AuthListenerFragment;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReviewFragment extends AuthListenerFragment implements ReviewsAdapter.OnItemSelectedListener {

    private FragmentReviewBinding binding;
    private ReviewViewModel viewModel;
    private ReviewsAdapter reviewsAdapter;


    public ReviewFragment() {
        super(R.layout.fragment_review);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentReviewBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        binding.fabAddReview.setOnClickListener(v -> viewModel.onAddReviewClick());

        viewModel.getReviewNumber().observe(getViewLifecycleOwner(), reviewNumber -> {
            if (reviewNumber != null) {
                String strReview = String.format(Locale.getDefault(), "리뷰 (%d)", reviewNumber);
                binding.textViewReviewTitle.setText(strReview);
            } else {
                binding.textViewReviewTitle.setText("리뷰");
            }
        });

        viewModel.getAverageRating().observe(getViewLifecycleOwner(), rating -> {
            if (rating != null) {
                binding.ratingBarAverageReview.setRating(rating);
            }
            binding.ratingBarAverageReview.setVisibility(rating != null ? View.VISIBLE : View.INVISIBLE);
        });

        viewModel.getUserMap().observe(getViewLifecycleOwner(), userMap ->
                viewModel.getUid().observe(getViewLifecycleOwner(), uid -> {
                    if (userMap == null) {
                        return;
                    }
                    reviewsAdapter = new ReviewsAdapter(userMap, uid);
                    binding.recyclerReview.setAdapter(reviewsAdapter);
                    binding.recyclerReview.setHasFixedSize(true);

                    reviewsAdapter.setOnItemSelectedListener(this);

                    viewModel.getReviews().observe(getViewLifecycleOwner(), reviews -> {
                        if (reviews != null) {
                            reviewsAdapter.submitList(reviews);
                            binding.textViewNoReview.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.INVISIBLE);
                        }
                        binding.progressBar5.setVisibility(View.INVISIBLE);
                    });
                }));

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof ReviewViewModel.Event.NavigateToAddEditReviewScreen) {
                Review review = ((ReviewViewModel.Event.NavigateToAddEditReviewScreen) event).review;
                NavDirections navDirections = ReviewFragmentDirections.actionReviewFragmentToAddReviewFragment(viewModel.getShop(), review);
                Navigation.findNavController(requireView()).navigate(navDirections);
            } else if (event instanceof ReviewViewModel.Event.ShowGeneralMessage) {
                String message = ((ReviewViewModel.Event.ShowGeneralMessage) event).message;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            } else if (event instanceof ReviewViewModel.Event.ConfirmDeleteReview) {
                Review review = ((ReviewViewModel.Event.ConfirmDeleteReview) event).review;
                new AlertDialog.Builder(requireContext())
                        .setTitle("리뷰 삭제")
                        .setMessage("리뷰를 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (dialog, which) -> viewModel.onDeleteReviewConfirm(review))
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        getParentFragmentManager().setFragmentResultListener("add_edit_review_fragment", getViewLifecycleOwner(),
                (requestKey, result) -> {
                    boolean addOrEdit = result.getBoolean("add_or_edit");
                    viewModel.onAddEditReviewResult(addOrEdit);
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

    @Override
    public void onItemSelected(int position) {

    }

    @Override
    public void onEditClick(int position) {
        Review review = reviewsAdapter.getCurrentList().get(position);
        viewModel.onEditReviewClick(review);
    }

    @Override
    public void onDeleteClick(int position) {
        Review review = reviewsAdapter.getCurrentList().get(position);
        viewModel.onDeleteReviewClick(review);
    }
}















