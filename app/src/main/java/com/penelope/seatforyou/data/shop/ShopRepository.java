package com.penelope.seatforyou.data.shop;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.penelope.seatforyou.data.review.Review;
import com.penelope.seatforyou.data.review.ReviewRepository;
import com.penelope.seatforyou.utils.BaseRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class ShopRepository extends BaseRepository<Shop> {

    private final CollectionReference shopCollection;
    private final ReviewRepository reviewRepository;


    @Inject
    public ShopRepository(FirebaseFirestore firestore, ReviewRepository reviewRepository) {
        shopCollection = firestore.collection("shops");
        this.reviewRepository = reviewRepository;
    }

    public void addShop(Shop shop, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        shopCollection.document(shop.getUid())
                .set(shop)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }

    public void getShop(String uid, OnSuccessListener<Shop> onSuccessListener, OnFailureListener onFailureListener) {
        shopCollection.document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot ->
                        onSuccessListener.onSuccess(documentSnapshot == null ?
                                null :
                                documentSnapshot.toObject(Shop.class))
                )
                .addOnFailureListener(onFailureListener);
    }

    public void getAllShops(OnSuccessListener<List<Shop>> onSuccessListener, OnFailureListener onFailureListener) {
        shopCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Shop> shops = new ArrayList<>();
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        onSuccessListener.onSuccess(shops);
                        return;
                    }
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Shop shop = snapshot.toObject(Shop.class);
                        if (shop != null) {
                            shops.add(shop);
                        }
                    }
                    onSuccessListener.onSuccess(shops);
                })
                .addOnFailureListener(onFailureListener);
    }

    public void getShopMap(OnSuccessListener<Map<String, Shop>> onSuccessListener, OnFailureListener onFailureListener) {

        getAllShops(
                shops -> {
                    Map<String, Shop> map = new HashMap<>();
                    for (Shop shop : shops) {
                        map.put(shop.getUid(), shop);
                    }
                    onSuccessListener.onSuccess(map);
                },
                onFailureListener
        );
    }

    public void getShops(List<String> ids, OnSuccessListener<List<Shop>> onSuccessListener, OnFailureListener onFailureListener) {

        getShopMap(
                map -> {
                    List<Shop> shopList = new ArrayList<>();
                    for (String id : ids) {
                        Shop shop = map.get(id);
                        if (shop != null) {
                            shopList.add(shop);
                        }
                    }
                    onSuccessListener.onSuccess(shopList);
                },
                onFailureListener
        );
    }

    public void getAllCategories(OnSuccessListener<List<String>> onSuccessListener, OnFailureListener onFailureListener) {

        getAllShops(
                shops -> {
                    List<String> categories = new ArrayList<>();
                    for (Shop shop : shops) {
                        categories.addAll(shop.getCategories());
                    }
                    onSuccessListener.onSuccess(categories);
                },
                onFailureListener
        );
    }

    public LiveData<Shop> getNewestShopLive() {

        MutableLiveData<Shop> shop = new MutableLiveData<>();

        shopCollection.orderBy("created", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || value.isEmpty()) {
                        shop.setValue(null);
                        return;
                    }
                    DocumentSnapshot snapshot = value.getDocuments().get(0);
                    shop.setValue(snapshot.toObject(Shop.class));
                });

        return shop;
    }

    public LiveData<List<Shop>> getRecommendableShops() {

        MutableLiveData<List<Shop>> shops = new MutableLiveData<>();

        getAllShops(
                shopList -> {
                    for (Shop shop : shopList) {
                        reviewRepository.getAverageRating(shop.getUid(),
                                rating -> {
                                    if (rating >= 3.5) {
                                        List<Shop> oldList = shops.getValue();
                                        if (oldList == null) {
                                            oldList = new ArrayList<>();
                                        }
                                        List<Shop> newList = new ArrayList<>(oldList);
                                        newList.add(shop);
                                        shops.setValue(newList);
                                    }
                                },
                                Throwable::printStackTrace);
                    }
                },
                e -> {
                    e.printStackTrace();
                    shops.setValue(null);
                }
        );

        return shops;
    }

    public LiveData<List<Shop>> getRealReviewShops() {

        LiveData<List<Review>> allReviews = reviewRepository.getAllReviewsLive();
        MutableLiveData<Map<String, Shop>> shopMap = new MutableLiveData<>();

        getShopMap(shopMap::setValue, e -> {
            e.printStackTrace();
            shopMap.setValue(null);
        });

        return Transformations.switchMap(allReviews, reviews ->
                Transformations.map(shopMap, map -> {
                    if (reviews == null || map == null) {
                        return null;
                    }

                    List<Shop> shops = new ArrayList<>();

                    for (Review review : reviews) {
                        String shopId = review.getShopId();
                        if (shops.stream().noneMatch(shop -> shop.getUid().equals(shopId))) {
                            Shop shop = map.get(shopId);
                            if (shop != null) {
                                shops.add(shop);
                                if (shops.size() >= 5) {
                                    break;
                                }
                            }
                        }
                    }

                    return shops;
                }));
    }

    public void findShopsByRegion(String region,
                                  OnSuccessListener<List<Shop>> onSuccessListener, OnFailureListener onFailureListener) {

        getAllShops(
                shops -> {
                    List<Shop> filtered = new ArrayList<>();
                    for (Shop shop : shops) {
                        if (shop.getAddress().getLoadAddress().contains(region)) {
                            filtered.add(shop);
                        }
                    }
                    onSuccessListener.onSuccess(filtered);
                },
                onFailureListener
        );
    }

    public void findShopsByName(String name,
                                OnSuccessListener<List<Shop>> onSuccessListener, OnFailureListener onFailureListener) {

        getAllShops(
                shops -> {
                    List<Shop> filtered = new ArrayList<>();
                    for (Shop shop : shops) {
                        if (shop.getName().contains(name)) {
                            filtered.add(shop);
                        }
                    }
                    onSuccessListener.onSuccess(filtered);
                },
                onFailureListener
        );
    }

    public void findShopsByNameOrRegion(String query,
                                        OnSuccessListener<List<Shop>> onSuccessListener, OnFailureListener onFailureListener) {

        getAllShops(
                shops -> {
                    List<Shop> filtered = new ArrayList<>();
                    for (Shop shop : shops) {
                        if (shop.getName().contains(query)
                                || shop.getAddress().getLoadAddress().contains(query)) {
                            filtered.add(shop);
                        }
                    }
                    onSuccessListener.onSuccess(filtered);
                },
                onFailureListener
        );
    }

    public void findShopsNearby(double latitude, double longitude, double radius,
                                OnSuccessListener<List<Shop>> onSuccessListener, OnFailureListener onFailureListener) {

        getAllShops(
                shops -> {
                    List<Shop> filtered = new ArrayList<>();
                    for (Shop shop : shops) {
                        double latitudeShop = shop.getAddress().getLatitude();
                        double longitudeShop = shop.getAddress().getLongitude();
                        float[] distant = new float[1];
                        Location.distanceBetween(latitude, longitude, latitudeShop, longitudeShop, distant);
                        if (distant[0] <= radius) {
                            filtered.add(shop);
                        }
                    }
                    onSuccessListener.onSuccess(filtered);
                },
                onFailureListener
        );
    }

}
