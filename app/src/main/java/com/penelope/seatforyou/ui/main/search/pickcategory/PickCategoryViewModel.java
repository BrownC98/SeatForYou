package com.penelope.seatforyou.ui.main.search.pickcategory;

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
public class PickCategoryViewModel extends ViewModel {

    private final MutableLiveData<Event> event = new MutableLiveData<>();

    private final MutableLiveData<List<String>> allCategories = new MutableLiveData<>();

    private final MutableLiveData<List<String>> selectedCategories = new MutableLiveData<>();


    @Inject
    public PickCategoryViewModel(SavedStateHandle savedStateHandle, ShopRepository shopRepository) {

        selectedCategories.setValue(savedStateHandle.get("categories"));

        shopRepository.getAllCategories(
                allCategories::setValue,
                e -> {
                    e.printStackTrace();
                    allCategories.setValue(null);
                });
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public LiveData<List<String>> getSelectedCategories() {
        return selectedCategories;
    }

    public LiveData<List<String>> getAllCategories() {
        return allCategories;
    }


    public void onCategoryCheck(String category, boolean isChecked) {

        List<String> categories = selectedCategories.getValue();
        assert categories != null;

        List<String> newCategories = new ArrayList<>(categories);
        newCategories.remove(category);
        if (isChecked) {
            newCategories.add(category);
        }

        selectedCategories.setValue(newCategories);
    }

    public void onConfirm() {
        event.setValue(new Event.NavigateBackWithResult(selectedCategories.getValue()));
    }

    public void onCancel() {
        event.setValue(new Event.NavigateBackWithResult(null));
    }


    public static class Event {

        public static class NavigateBackWithResult extends Event {
            public final List<String> categories;

            public NavigateBackWithResult(List<String> categories) {
                this.categories = categories;
            }
        }

    }

}