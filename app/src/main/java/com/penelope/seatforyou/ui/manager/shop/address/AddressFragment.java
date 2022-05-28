package com.penelope.seatforyou.ui.manager.shop.address;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;
import com.penelope.seatforyou.R;
import com.penelope.seatforyou.data.address.Address;
import com.penelope.seatforyou.databinding.FragmentAddressBinding;
import com.penelope.seatforyou.utils.ui.OnTextChangeListener;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddressFragment extends DialogFragment implements
        OnMapReadyCallback, NaverMap.OnLocationChangeListener {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private FragmentAddressBinding binding;
    private AddressViewModel viewModel;

    private NaverMap map;
    private FusedLocationSource fusedLocationSource;
    private ActivityResultLauncher<String> locationPermissionLauncher;


    public AddressFragment() {
        super(R.layout.fragment_address);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // GPS 소스를 초기화한다
        fusedLocationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        // 위치 퍼미션 런처를 정의한다
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (fusedLocationSource.onRequestPermissionsResult(
                            LOCATION_PERMISSION_REQUEST_CODE,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            new int[]{result ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED}
                    )) {
                        // 퍼미션 거절
                        if (!fusedLocationSource.isActivated()) {
                            map.setLocationTrackingMode(LocationTrackingMode.None);
                            return;
                        }
                        // 퍼미션 승인 시 네이버 맵 초기설정 진행
                        configureMap();
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentAddressBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(AddressViewModel.class);

        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.textViewConfirm.setOnClickListener(v -> viewModel.onConfirm());
        binding.textViewCancel.setOnClickListener(v -> viewModel.onCancel());

        binding.editTextAddress.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                viewModel.onQueryChange(text);
            }
        });

        binding.editTextAddressDetail.addTextChangedListener(new OnTextChangeListener() {
            @Override
            public void onTextChange(String text) {
                viewModel.onAddressDetailChange(text);
            }
        });

        if (viewModel.getOldAddress() != null) {
            binding.editTextAddress.setText(viewModel.getOldAddress().getLoadAddress());
            binding.editTextAddressDetail.setText(viewModel.getOldAddress().getDetail());
        }

        AddressAdapter addressAdapter = new AddressAdapter();
        binding.recyclerAddress.setAdapter(addressAdapter);
        binding.recyclerAddress.setHasFixedSize(true);

        addressAdapter.setOnItemSelectedListener(position ->
                viewModel.onAddressClick(position));

        viewModel.getAddresses().observe(getViewLifecycleOwner(), addresses -> {
            if (addresses != null) {
                addressAdapter.submitList(addresses);
                binding.textViewNoAddress.setVisibility(addresses.isEmpty() ? View.VISIBLE : View.INVISIBLE);
            }
            binding.progressBar.setVisibility(View.INVISIBLE);
        });

        viewModel.getSelectedIndex().observe(getViewLifecycleOwner(), index -> {
            RecyclerView.LayoutManager layoutManager = binding.recyclerAddress.getLayoutManager();
            assert layoutManager != null;
            for (int i = 0; i < addressAdapter.getItemCount(); i++) {
                View itemView = layoutManager.findViewByPosition(i);
                if (itemView != null) {
                    AddressAdapter.AddressViewHolder viewHolder = (AddressAdapter.AddressViewHolder)
                            binding.recyclerAddress.getChildViewHolder(itemView);
                    if (index != null && index == i) {
                        viewHolder.binding.textViewAddressName.setTypeface(null, Typeface.BOLD);
                    } else {
                        viewHolder.binding.textViewAddressName.setTypeface(null, Typeface.NORMAL);
                    }
                }
            }
        });

        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event instanceof AddressViewModel.Event.NavigateBackWithResult) {
                Address address = ((AddressViewModel.Event.NavigateBackWithResult) event).address;
                Bundle result = new Bundle();
                result.putSerializable("address", address);
                getParentFragmentManager().setFragmentResult("address_fragment", result);
                NavHostFragment.findNavController(this).popBackStack();
            } else if (event instanceof AddressViewModel.Event.ShowLoadingUI) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (event instanceof AddressViewModel.Event.ShowGeneralMessage) {
                String message = ((AddressViewModel.Event.ShowGeneralMessage) event).message;
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            } else if (event instanceof AddressViewModel.Event.FillAddress) {
                String address = ((AddressViewModel.Event.FillAddress) event).address;
                binding.editTextAddress.setText(address);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        map = naverMap;

        // 퍼미션 여부를 확인하고 초기설정을 진행한다
        if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            configureMap();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void configureMap() {

        // 네이버 맵 초기설정을 진행한다
        map.setLocationSource(fusedLocationSource);
        map.setLocationTrackingMode(LocationTrackingMode.Follow);
        map.getUiSettings().setLocationButtonEnabled(true);
        map.addOnLocationChangeListener(this);
    }

    @Override
    public void onLocationChange(@NonNull Location location) {
        viewModel.onLocationChange(location);
    }

}