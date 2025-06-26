package com.prm392.onlineshoesshop.repository;

import android.os.Handler;
import android.os.Looper;

import com.prm392.onlineshoesshop.model.Address;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AddressRepository {
    private static final String BASE_URL = "https://provinces.open-api.vn/api";

    public interface CityCallback {
        void onSuccess(List<Address.City> result);
        void onFailure(Exception e);
    }

    public interface DistrictCallback {
        void onSuccess(List<Address.District> result);
        void onFailure(Exception e);
    }

    public interface WardCallback {
        void onSuccess(List<Address.Ward> result);
        void onFailure(Exception e);
    }

    private void runOnMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public void fetchCities(final CityCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/p/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }

                in.close();
                JSONArray jsonArray = new JSONArray(response.toString());
                List<Address.City> cities = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    int code = obj.getInt("code");
                    String name = obj.getString("name");
                    cities.add(new Address.City(code, name));
                }

                runOnMainThread(() -> callback.onSuccess(cities));
            } catch (Exception e) {
                runOnMainThread(() -> callback.onFailure(e));
            }
        }).start();
    }

    public void fetchDistricts(final int cityCode, final DistrictCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/p/" + cityCode + "?depth=2");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }

                in.close();
                JSONObject json = new JSONObject(response.toString());
                JSONArray districtsArray = json.getJSONArray("districts");

                List<Address.District> districts = new ArrayList<>();
                for (int i = 0; i < districtsArray.length(); i++) {
                    JSONObject obj = districtsArray.getJSONObject(i);
                    int code = obj.getInt("code");
                    String name = obj.getString("name");
                    districts.add(new Address.District(code, name));
                }

                runOnMainThread(() -> callback.onSuccess(districts));
            } catch (Exception e) {
                runOnMainThread(() -> callback.onFailure(e));
            }
        }).start();
    }

    public void fetchWards(final int districtCode, final WardCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/d/" + districtCode + "?depth=2");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    response.append(line);
                }

                in.close();
                JSONObject json = new JSONObject(response.toString());
                JSONArray wardsArray = json.getJSONArray("wards");

                List<Address.Ward> wards = new ArrayList<>();
                for (int i = 0; i < wardsArray.length(); i++) {
                    JSONObject obj = wardsArray.getJSONObject(i);
                    int code = obj.getInt("code");
                    String name = obj.getString("name");
                    wards.add(new Address.Ward(code, name));
                }

                runOnMainThread(() -> callback.onSuccess(wards));
            } catch (Exception e) {
                runOnMainThread(() -> callback.onFailure(e));
            }
        }).start();
    }
}
