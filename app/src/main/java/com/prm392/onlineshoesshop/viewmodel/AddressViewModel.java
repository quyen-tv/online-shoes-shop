package com.prm392.onlineshoesshop.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.prm392.onlineshoesshop.model.Address;
import com.prm392.onlineshoesshop.repository.AddressRepository;

import java.util.List;

public class AddressViewModel extends ViewModel {
    private final AddressRepository repository = new AddressRepository();

    private final MutableLiveData<List<Address.City>> cities = new MutableLiveData<>();
    private final MutableLiveData<List<Address.District>> districts = new MutableLiveData<>();
    private final MutableLiveData<List<Address.Ward>> wards = new MutableLiveData<>();

    private final MutableLiveData<Address.City> selectedCity = new MutableLiveData<>();
    private final MutableLiveData<Address.District> selectedDistrict = new MutableLiveData<>();
    private final MutableLiveData<Address.Ward> selectedWard = new MutableLiveData<>();
    private final MutableLiveData<String> country = new MutableLiveData<>("Việt Nam");

    // === Expose ===
    public LiveData<List<Address.City>> getCities() {
        return cities;
    }

    public LiveData<List<Address.District>> getDistricts() {
        return districts;
    }

    public LiveData<List<Address.Ward>> getWards() {
        return wards;
    }

    public MutableLiveData<Address.City> getSelectedCity() {
        return selectedCity;
    }

    public MutableLiveData<Address.District> getSelectedDistrict() {
        return selectedDistrict;
    }

    public MutableLiveData<Address.Ward> getSelectedWard() {
        return selectedWard;
    }

    public MutableLiveData<String> getCountry() {
        return country;
    }

    // === Actions ===
    public void fetchCities() {
        repository.fetchCities(new AddressRepository.CityCallback() {
            @Override
            public void onSuccess(List<Address.City> result) {
                cities.setValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                // log nếu cần
            }
        });
    }

    public void fetchDistricts(int cityCode) {
        repository.fetchDistricts(cityCode, new AddressRepository.DistrictCallback() {
            @Override
            public void onSuccess(List<Address.District> result) {
                districts.setValue(result);
            }

            @Override
            public void onFailure(Exception e) {}
        });
    }

    public void fetchWards(int districtCode) {
        repository.fetchWards(districtCode, new AddressRepository.WardCallback() {
            @Override
            public void onSuccess(List<Address.Ward> result) {
                wards.setValue(result);
            }

            @Override
            public void onFailure(Exception e) {}
        });
    }

    public void setSelectedCity(String name, int code) {
        Address.City city = new Address.City(code, name);
        selectedCity.setValue(city);
    }

    public void setSelectedDistrict(String name, int code) {
        Address.District district = new Address.District(code, name);
        selectedDistrict.setValue(district);
    }

    public void setSelectedWard(String name, int code) {
        Address.Ward ward = new Address.Ward(code, name);
        selectedWard.setValue(ward);
    }

    // Khôi phục dữ liệu gốc (nếu cần)
    public void restoreAddress(Address address) {
        if (address != null) {
            selectedCity.setValue(address.getCity());
            selectedDistrict.setValue(address.getDistrict());
            selectedWard.setValue(address.getWard());
            country.setValue(address.getCountry());
        }
    }
}
