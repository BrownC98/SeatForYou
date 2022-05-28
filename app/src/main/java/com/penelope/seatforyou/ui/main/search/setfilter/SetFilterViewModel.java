package com.penelope.seatforyou.ui.main.search.setfilter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.penelope.seatforyou.data.shop.ShopRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SetFilterViewModel extends ViewModel {

    private final MutableLiveData<Event> event = new MutableLiveData<>();

    private final MutableLiveData<String> region = new MutableLiveData<>();
    private final MutableLiveData<Integer> minPrice = new MutableLiveData<>();
    private final MutableLiveData<Integer> maxPrice = new MutableLiveData<>();
    private final MutableLiveData<List<String>> selectedCategories = new MutableLiveData<>(new ArrayList<>());


    @Inject
    public SetFilterViewModel(SavedStateHandle savedStateHandle) {

        String regionValue = savedStateHandle.get("region");
        Integer minPriceValue = savedStateHandle.get("min_price");
        Integer maxPriceValue = savedStateHandle.get("max_price");
        List<String> categoriesValue = savedStateHandle.get("categories");

        if (regionValue != null) {
            region.setValue(regionValue);
        }
        if (minPriceValue != null) {
            minPrice.setValue(minPriceValue);
        }
        if (maxPriceValue != null && maxPriceValue != 0) {
            maxPrice.setValue(maxPriceValue);
        }
        if (categoriesValue != null) {
            selectedCategories.setValue(categoriesValue);
        }
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public LiveData<String> getRegion() {
        return region;
    }

    public LiveData<Integer> getMinPrice() {
        return minPrice;
    }

    public LiveData<Integer> getMaxPrice() {
        return maxPrice;
    }

    public LiveData<List<String>> getSelectedCategories() {
        return selectedCategories;
    }


    public void onCloseClick() {
        event.setValue(new Event.NavigateBack());
    }

    public void onSetPriceClick() {
        event.setValue(new Event.PromptPrice());
    }

    public void onSetRegionClick() {
        event.setValue(new Event.PromptRegion());
    }

    public void onSetCategoryClick() {
        event.setValue(new Event.NavigateToPickCategoryScreen(selectedCategories.getValue()));
    }

    public void onPickCategoryResult(List<String> categories) {
        if (categories != null) {
            selectedCategories.setValue(categories);
        }
    }

    public void onClearClick() {
        region.setValue(null);
        minPrice.setValue(null);
        maxPrice.setValue(null);
        selectedCategories.setValue(new ArrayList<>());
    }

    public void onRegionSelected(String value) {
        value = value.trim();
        if (!value.isEmpty()) {
            region.setValue(value);
        } else {
            region.setValue(null);
        }
    }

    public void onPriceSelected(int min, int max) {
        if (max > min) {
            maxPrice.setValue(max);
        } else {
            maxPrice.setValue(null);
        }

        minPrice.setValue(min);
    }

    public void onApplyClick() {
        event.setValue(new Event.NavigateBackWithResult(
                region.getValue(),
                minPrice.getValue() != null ? minPrice.getValue() : 0,
                maxPrice.getValue() != null ? maxPrice.getValue() : 0,
                selectedCategories.getValue()
        ));
    }


    public static class Event {

        public static class ShowGeneralMessage extends Event {
            public final String message;

            public ShowGeneralMessage(String message) {
                this.message = message;
            }
        }

        public static class NavigateBack extends Event {
        }

        public static class PromptRegion extends Event {
        }

        public static class PromptPrice extends Event {
        }

        public static class NavigateToPickCategoryScreen extends Event {
            public final List<String> selectedList;
            public NavigateToPickCategoryScreen(List<String> selectedList) {
                this.selectedList = selectedList;
            }
        }

        public static class NavigateBackWithResult extends Event {
            public final String region;
            public final int minPrice;
            public final int maxPrice;
            public final List<String> categories;

            public NavigateBackWithResult(String region, int minPrice, int maxPrice, List<String> categories) {
                this.region = region;
                this.minPrice = minPrice;
                this.maxPrice = maxPrice;
                this.categories = categories;
            }
        }
    }

}