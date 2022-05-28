package com.penelope.seatforyou.ui.manager.shop.address;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.penelope.seatforyou.api.reversegeocoding.ReverseGeocodingApi;
import com.penelope.seatforyou.data.address.Address;
import com.penelope.seatforyou.data.address.AddressRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddressViewModel extends ViewModel {

    private final MutableLiveData<Event> event = new MutableLiveData<>();

    private final MutableLiveData<String> query = new MutableLiveData<>();
    private final LiveData<List<Address>> addresses;
    private final MutableLiveData<Integer> selectedIndex = new MutableLiveData<>();

    private final Address oldAddress;

    private String addressDetail = "";

    private Location savedLocation;

    private final ReverseGeocodingApi reverseGeocodingApi;


    @Inject
    public AddressViewModel(SavedStateHandle savedStateHandle, AddressRepository addressRepository,
                            ReverseGeocodingApi reverseGeocodingApi) {

        oldAddress = savedStateHandle.get("oldAddress");

        addresses = Transformations.switchMap(query, addressRepository::getAddresses);

        this.reverseGeocodingApi = reverseGeocodingApi;
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public LiveData<List<Address>> getAddresses() {
        return addresses;
    }

    public Address getOldAddress() {
        return oldAddress;
    }

    public LiveData<Integer> getSelectedIndex() {
        return selectedIndex;
    }


    public void onQueryChange(String text) {
        if (!text.trim().isEmpty()) {
            query.setValue(text.trim());
            event.setValue(new Event.ShowLoadingUI());
        }
        selectedIndex.setValue(null);
    }

    public void onAddressClick(int position) {
        selectedIndex.setValue(position);
    }

    public void onAddressDetailChange(String text) {
        addressDetail = text.trim();
    }

    public void onConfirm() {

        List<Address> addressList = addresses.getValue();
        Integer index = selectedIndex.getValue();
        if (addressList == null) {
            return;
        }

        if (index != null) {
            Address address = addressList.get(index);
            address.setDetail(addressDetail);
            event.setValue(new Event.NavigateBackWithResult(address));
        } else {
            event.setValue(new Event.ShowGeneralMessage("주소를 선택하세요"));
        }
    }

    public void onCancel() {
        event.setValue(new Event.NavigateBackWithResult(null));
    }

    public void onLocationChange(Location location) {

        if (location != null && savedLocation == null) {
            savedLocation = location;

            if (oldAddress == null || oldAddress.getLoadAddress() == null || oldAddress.getLoadAddress().isEmpty()) {
                reverseGeocodingApi.get(location.getLatitude(), location.getLongitude(),
                        address -> {
                            event.setValue(new Event.FillAddress(address));
                        },
                        e -> {
                            e.printStackTrace();
                            event.setValue(new Event.ShowGeneralMessage("현 주소 확인에 실패했습니다"));
                        });
            }
        }
    }


    public static class Event {

        public static class NavigateBackWithResult extends Event {
            public final Address address;

            public NavigateBackWithResult(Address address) {
                this.address = address;
            }
        }

        public static class ShowLoadingUI extends Event {
        }

        public static class ShowGeneralMessage extends Event {
            public final String message;
            public ShowGeneralMessage(String message) {
                this.message = message;
            }
        }

        public static class FillAddress extends Event {
            public final String address;

            public FillAddress(String address) {
                this.address = address;
            }
        }

    }

}