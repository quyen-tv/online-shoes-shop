package com.prm392.onlineshoesshop.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.prm392.onlineshoesshop.model.Address;
import com.prm392.onlineshoesshop.repository.AddressRepository;

import java.util.List;

public class AddressViewModel extends ViewModel {

    private final AddressRepository addressRepository = AddressRepository.getInstance();

    private final MutableLiveData<String> _country = new MutableLiveData<>("Việt Nam");
    private final MutableLiveData<String> _street = new MutableLiveData<>();
    private final MutableLiveData<String> _selectedCity = new MutableLiveData<>();
    private final MutableLiveData<String> _selectedDistrict = new MutableLiveData<>();
    private final MutableLiveData<String> _selectedWard = new MutableLiveData<>();

    public LiveData<String> getCountry() { return _country; }
    public LiveData<String> getStreet() { return _street; }
    public LiveData<String> getSelectedCity() { return _selectedCity; }
    public LiveData<String> getSelectedDistrict() { return _selectedDistrict; }
    public LiveData<String> getSelectedWard() { return _selectedWard; }

    public void setStreet(String street) { _street.setValue(street); }
    public void setCountry(String country) { _country.setValue(country); }

    public LiveData<List<String>> getCities() { return addressRepository.getCities(); }
    public LiveData<List<String>> getDistricts() { return addressRepository.getDistricts(); }
    public LiveData<List<String>> getWards() { return addressRepository.getWards(); }

    public void fetchCities() { addressRepository.fetchCities(); }

    public void setSelectedCity(String city) {
        _selectedCity.setValue(city);
        _selectedDistrict.setValue(null);  // reset các cấp thấp hơn
        _selectedWard.setValue(null);
        addressRepository.fetchDistricts(city); // tự động fetch quận
    }

    public void setSelectedDistrict(String district) {
        _selectedDistrict.setValue(district);
        _selectedWard.setValue(null);
        addressRepository.fetchWards(district); // tự động fetch phường
    }

    public void setSelectedWard(String ward) {
        _selectedWard.setValue(ward);
    }

    public Address buildAddress() {
        return new Address(
                _street.getValue(),
                _selectedWard.getValue(),
                _selectedDistrict.getValue(),
                _selectedCity.getValue(),
                _country.getValue()
        );
    }


    // Gọi API để lấy danh sách quận/huyện theo thành phố
    public void fetchDistricts(String city) {
        addressRepository.fetchDistricts(city);
    }

    // Gọi API để lấy danh sách phường/xã theo quận
    public void fetchWards(String district) {
        addressRepository.fetchWards(district);
    }

}
