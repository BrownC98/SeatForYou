package com.penelope.seatforyou.ui.main.home;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.naver.maps.geometry.LatLng;
import com.penelope.seatforyou.data.shop.Shop;
import com.penelope.seatforyou.data.shop.ShopRepository;

import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final MutableLiveData<Event> event = new MutableLiveData<>();

    private final LiveData<List<Shop>> recommendedShops;
    private final MutableLiveData<Integer> recommendedIndex = new MutableLiveData<>(0);
    private final LiveData<Shop> recommendedShop;

    private final LiveData<List<Shop>> realReviewShops;

    private LatLng currentLocation;

    private final ShopRepository shopRepository;


    @Inject
    public HomeViewModel(ShopRepository shopRepository) {

        recommendedShops = shopRepository.getRecommendableShops();
        recommendedShop = Transformations.switchMap(recommendedShops, shops ->
                Transformations.map(recommendedIndex, index -> {
                    if (shops == null || index == null || index > shops.size() - 1) {
                        return null;
                    }
                    return shops.get(index);
                }));

        realReviewShops = shopRepository.getRealReviewShops();

        this.shopRepository = shopRepository;
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public LiveData<Shop> getRecommendedShop() {
        return recommendedShop;
    }

    public LiveData<List<Shop>> getRealReviewShops() {
        return realReviewShops;
    }


    public void onRecommendedShopClick() {

        Shop shop = recommendedShop.getValue();
        if (shop != null) {
            event.setValue(new Event.NavigateToDetailScreen(shop, false));
        }
    }

    public void onRealReviewShopClick(Shop shop) {
        event.setValue(new Event.NavigateToDetailScreen(shop, true));
    }

    public void onNearbyClick() {

        if (currentLocation == null) {
            event.setValue(new Event.RequestLocationPermission());
            return;
        }

        event.setValue(new Event.ShowLoadingUI());

        shopRepository.findShopsNearby(currentLocation.latitude, currentLocation.longitude, 10000,
                shops -> {
                    String description = "내 주변 레스토랑";
                    event.setValue(new Event.NavigateToFilteredScreen(shops, description));
                },
                e -> event.setValue(new Event.ShowGeneralMessage("검색에 실패했습니다"))
        );
    }

    public void onRegionClick(String region) {

        event.setValue(new Event.ShowLoadingUI());

        shopRepository.findShopsByRegion(region,
                shops -> {
                    String description = region + " 소재 레스토랑";
                    event.setValue(new Event.NavigateToFilteredScreen(shops, description));
                },
                e -> event.setValue(new Event.ShowGeneralMessage("검색에 실패했습니다"))
        );
    }

    public void onLocationChange(Location location) {
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }

    public void onTick() {

        List<Shop> recommendedList = recommendedShops.getValue();
        if (recommendedList == null || recommendedList.size() == 1) {
            return;
        }

        Integer oldIndex = recommendedIndex.getValue();
        assert oldIndex != null;

        int newIndex = oldIndex;

        while (newIndex == oldIndex) {
            newIndex = new Random().nextInt(recommendedList.size());
        }

        recommendedIndex.setValue(newIndex);
    }


    public static class Event {

        public static class NavigateToDetailScreen extends Event {
            public final Shop shop;
            public final boolean showReview;

            public NavigateToDetailScreen(Shop shop, boolean showReview) {
                this.shop = shop;
                this.showReview = showReview;
            }
        }

        public static class NavigateToFilteredScreen extends Event {
            public final List<Shop> shops;
            public final String description;

            public NavigateToFilteredScreen(List<Shop> shops, String description) {
                this.shops = shops;
                this.description = description;
            }
        }

        public static class ShowGeneralMessage extends Event {
            public final String message;

            public ShowGeneralMessage(String message) {
                this.message = message;
            }
        }

        public static class ShowLoadingUI extends Event {
        }

        public static class RequestLocationPermission extends Event {
        }
    }

}