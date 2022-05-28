package com.penelope.seatforyou.ui.main.reserve;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.penelope.seatforyou.data.reservation.Reservation;
import com.penelope.seatforyou.data.reservation.ReservationRepository;
import com.penelope.seatforyou.data.shop.Shop;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ReserveViewModel extends ViewModel implements FirebaseAuth.AuthStateListener {

    private final MutableLiveData<Event> event = new MutableLiveData<>();

    private String uid;
    private final Shop shop;

    private final MutableLiveData<LocalDate> date = new MutableLiveData<>(LocalDate.now());
    private final MutableLiveData<LocalTime> time = new MutableLiveData<>();
    private final MutableLiveData<Integer> personNumber = new MutableLiveData<>();

    private final ReservationRepository reservationRepository;


    @Inject
    public ReserveViewModel(SavedStateHandle savedStateHandle, ReservationRepository reservationRepository) {
        shop = savedStateHandle.get("shop");
        this.reservationRepository = reservationRepository;
    }

    public LiveData<Event> getEvent() {
        event.setValue(null);
        return event;
    }

    public LiveData<Integer> getPersonNumber() {
        return personNumber;
    }

    public LiveData<LocalTime> getTime() {
        return time;
    }

    public LiveData<LocalDate> getDate() {
        return date;
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if (firebaseAuth.getCurrentUser() != null) {
            uid = firebaseAuth.getUid();
        }
    }

    public void onPersonNumberClick(int number) {
        personNumber.setValue(number);
    }

    public void onDateClick(int year, int month, int dayOfMonth) {
        date.setValue(LocalDate.of(year, month, dayOfMonth));
    }

    public void onTimeClick(int hour, int minute) {
        time.setValue(LocalTime.of(hour, minute));
    }

    public void onOkClick() {

        if (uid == null) {
            event.setValue(new Event.ShowGeneralMessage("비회원은 예약할 수 없습니다"));
            return;
        }

        LocalDate dateValue = date.getValue();
        LocalTime timeValue = time.getValue();
        Integer personNumberValue = personNumber.getValue();

        if (dateValue == null || timeValue == null || personNumberValue == null) {
            event.setValue(new Event.ShowGeneralMessage("모두 입력해주세요"));
            return;
        }

        LocalDateTime dateTime = LocalDateTime.of(dateValue, timeValue);

        LocalTime openTime = LocalTime.of(shop.getOpenHour(), shop.getOpenMinute());
        LocalTime closeTime = LocalTime.of(shop.getCloseHour(), shop.getCloseMinute());
        if (timeValue.isBefore(openTime) || timeValue.isAfter(closeTime) || dateTime.isBefore(LocalDateTime.now())) {
            event.setValue(new Event.ShowGeneralMessage("예약 가능 시간이 아닙니다"));
            return;
        }

        reservationRepository.getShopReservation(shop.getUid(), dateTime,
                reservation -> {
                    if (reservation == null) {
                        addReservation();
                    } else {
                        event.setValue(new Event.ShowGeneralMessage("이미 예약된 시간대입니다"));
                    }
                },
                e -> {
                    e.printStackTrace();
                    event.setValue(new Event.ShowGeneralMessage("네트워크를 확인해주세요"));
                }
        );
    }

    public void onBackClick() {
        event.setValue(new Event.NavigateBack());
    }

    public void onCloseClick() {
        event.setValue(new Event.NavigateBack());
    }


    private void addReservation() {

        LocalTime timeValue = time.getValue();
        LocalDate dateValue = date.getValue();
        Integer personNumberValue = personNumber.getValue();
        if (timeValue == null || dateValue == null || personNumberValue == null) {
            return;
        }

        Reservation newReservation = new Reservation(
                uid, shop.getUid(), personNumberValue,
                dateValue.getYear(), dateValue.getMonthValue(), dateValue.getDayOfMonth(),
                timeValue.getHour(), timeValue.getMinute()
        );

        reservationRepository.addReservation(newReservation,
                unused -> event.setValue(new Event.NavigateBackWithResult(true)),
                e -> {
                    e.printStackTrace();
                    event.setValue(new Event.ShowGeneralMessage("네트워크를 확인해주세요"));
                });
    }


    public static class Event {

        public static class NavigateBack extends Event {
        }

        public static class ShowGeneralMessage extends Event {
            public final String message;

            public ShowGeneralMessage(String message) {
                this.message = message;
            }
        }

        public static class NavigateBackWithResult extends Event {
            public final boolean success;

            public NavigateBackWithResult(boolean success) {
                this.success = success;
            }
        }
    }

}