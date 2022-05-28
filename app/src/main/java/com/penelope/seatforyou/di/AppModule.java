package com.penelope.seatforyou.di;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.penelope.seatforyou.api.reversegeocoding.ReverseGeocodingApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public FirebaseFirestore provideFirestore() {
        return FirebaseFirestore.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseAuth provideAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides
    @Singleton
    public FirebaseStorage provideStorage() {
        return FirebaseStorage.getInstance();
    }

    @Provides
    @Singleton
    public ReverseGeocodingApi provideReverseGeocodingApi(Application application) {
        return new ReverseGeocodingApi(application);
    }

}
