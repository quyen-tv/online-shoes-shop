package com.prm392.onlineshoesshop.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prm392.onlineshoesshop.model.Address;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddressRepository {

    private static final String TAG = "AddressRepository";
    private static final String BASE_URL = "https://provinces.open-api.vn/api/";

    private static AddressRepository instance;
    private final OkHttpClient client = new OkHttpClient();

    private final MutableLiveData<List<String>> _cities = new MutableLiveData<>();
    private final MutableLiveData<List<String>> _districts = new MutableLiveData<>();
    private final MutableLiveData<List<String>> _wards = new MutableLiveData<>();


    // Tên -> Mã
    private final Map<String, Integer> cityCodeMap = new HashMap<>();
    private final Map<String, Integer> districtCodeMap = new HashMap<>();

    public LiveData<List<String>> getCities() {
        return _cities;
    }

    public LiveData<List<String>> getDistricts() {
        return _districts;
    }

    public LiveData<List<String>> getWards() {
        return _wards;
    }

    public static AddressRepository getInstance() {
        if (instance == null) {
            instance = new AddressRepository();
        }
        return instance;
    }

    public Integer getCityCodeByName(String name) {
        return cityCodeMap.get(name);
    }

    public Integer getDistrictCodeByName(String name) {
        return districtCodeMap.get(name);
    }

    public void fetchCities() {
        Request request = new Request.Builder()
                .url(BASE_URL + "p/")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch cities: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                List<String> result = new ArrayList<>();
                if (response.isSuccessful()) {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        cityCodeMap.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String name = obj.getString("name");
                            int code = obj.getInt("code");
                            result.add(name);
                            cityCodeMap.put(name, code);
                        }
                        _cities.postValue(result);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing cities: " + e.getMessage());
                    }
                }
            }
        });
    }

    public void fetchDistricts(String cityName) {
        Integer cityCode = getCityCodeByName(cityName);
        if (cityCode == null) {
            Log.e(TAG, "City code not found for name: " + cityName);
            return;
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "p/" + cityCode + "?depth=2")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch districts: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                List<String> result = new ArrayList<>();
                if (response.isSuccessful()) {
                    try {
                        JSONObject obj = new JSONObject(response.body().string());
                        JSONArray array = obj.getJSONArray("districts");
                        districtCodeMap.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject item = array.getJSONObject(i);
                            String name = item.getString("name");
                            int code = item.getInt("code");
                            result.add(name);
                            districtCodeMap.put(name, code);
                        }
                        _districts.postValue(result);
                        _wards.postValue(new ArrayList<>()); // reset wards
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing districts: " + e.getMessage());
                    }
                }
            }
        });
    }

    public void fetchWards(String districtName) {
        Integer districtCode = getDistrictCodeByName(districtName);
        if (districtCode == null) {
            Log.e(TAG, "District code not found for name: " + districtName);
            return;
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "d/" + districtCode + "?depth=2")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Failed to fetch wards: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                List<String> result = new ArrayList<>();
                if (response.isSuccessful()) {
                    try {
                        JSONObject obj = new JSONObject(response.body().string());
                        JSONArray array = obj.getJSONArray("wards");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject item = array.getJSONObject(i);
                            result.add(item.getString("name"));
                        }
                        _wards.postValue(result);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing wards: " + e.getMessage());
                    }
                }
            }
        });
    }
}

