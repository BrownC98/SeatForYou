package com.penelope.seatforyou.ui.main.detail.review;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.seatforyou.data.review.Review;
import com.penelope.seatforyou.data.review.ReviewRepository;
import com.penelope.seatforyou.data.shop.Shop;
import com.penelope.seatforyou.data.user.User;
import com.penelope.seatforyou.data.user.UserRepository;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ReviewViewModel extends ViewModel implements FirebaseAuth.AuthStateListener {

    private final MutableLiveData<Event> event = new MutableLiveData<>();

    private final Shop shop;
    private final MutableLiveData<String> uid = new MutableLiveData<>();

    private final LiveData<List<Review>> reviews;
    private final LiveData<Map<String, User>> userMap;

    private final LiveData<Integer> reviewNumber;
    private final LiveData<Float> averageRating;

    private final ReviewRepository reviewRepository;


    @Inject
    public ReviewViewModel(SavedStateHandle savedStateHandle, ReviewRepository reviewRepository, UserRepository userRepository) {

        shop = savedStateHandle.get("shop");
        assert shop != null;

        reviews = reviewRepository.getReviewsLive(shop.getUid());
        userMap = userRepository.getUserMap();

        reviewNumber = Transformations.map(reviews, list -> list != null ? list.size() : null);
        averageRating = Transformations.map(reviews, list -> {
            if (list != null && !list.isEmpty()) {
                float sum = 0;
                for (Review review : list) {
                    sum += review.getRating();
                }
                return sum / list.size();
            } else {
                return null;
            }
        });

        this.reviewRepository = reviewRepository;
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public Shop getShop() {
        return shop;
    }

    public LiveData<List<Review>> getReviews() {
        return reviews;
    }

    public LiveData<Map<String, User>> getUserMap() {
        return userMap;
    }

    public LiveData<Integer> getReviewNumber() {
        return reviewNumber;
    }

    public LiveData<Float> getAverageRating() {
        return averageRating;
    }

    public LiveData<String> getUid() {
        return uid;
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        uid.setValue(firebaseAuth.getUid());
    }

    public void onAddReviewClick() {

        if (uid.getValue() == null) {
            event.setValue(new Event.ShowGeneralMessage("비회원은 리뷰 작성이 제한됩니다"));
            return;
        }

        reviewRepository.findReview(shop.getUid(), uid.getValue(),
                review -> {
                    if (review == null) {
                        event.setValue(new Event.NavigateToAddEditReviewScreen(null));
                    } else {
                        event.setValue(new Event.ShowGeneralMessage("이미 작성한 리뷰가 있습니다"));
                    }
                },
                e -> {
                    e.printStackTrace();
                    event.setValue(new Event.ShowGeneralMessage("네트워크를 확인해주세요"));
                });

    }

    public void onAddEditReviewResult(boolean addOrEdit) {
        if (addOrEdit) {
            event.setValue(new Event.ShowGeneralMessage("리뷰가 작성되었습니다"));
        } else {
            event.setValue(new Event.ShowGeneralMessage("리뷰가 수정되었습니다"));
        }
    }

    public void onEditReviewClick(Review review) {
        if (review.getUid().equals(uid.getValue())) {
            event.setValue(new Event.NavigateToAddEditReviewScreen(review));
        }
    }

    public void onDeleteReviewClick(Review review) {
        if (review.getUid().equals(uid.getValue())) {
            event.setValue(new Event.ConfirmDeleteReview(review));
        }
    }

    public void onDeleteReviewConfirm(Review review) {
        if (review.getUid().equals(uid.getValue())) {
            reviewRepository.deleteReview(review,
                    unused -> event.setValue(new Event.ShowGeneralMessage("리뷰가 삭제되었습니다")),
                    e -> {
                        e.printStackTrace();
                        event.setValue(new Event.ShowGeneralMessage("리뷰 삭제에 실패했습니다"));
                    });
        }
    }


    public static class Event {

        public static class ShowGeneralMessage extends Event {
            public final String message;
            public ShowGeneralMessage(String message) {
                this.message = message;
            }
        }

        public static class NavigateToAddEditReviewScreen extends Event {
            public final Review review;
            public NavigateToAddEditReviewScreen(Review review) {
                this.review = review;
            }
        }

        public static class ConfirmDeleteReview extends Event {
            public final Review review;
            public ConfirmDeleteReview(Review review) {
                this.review = review;
            }
        }
    }

}







