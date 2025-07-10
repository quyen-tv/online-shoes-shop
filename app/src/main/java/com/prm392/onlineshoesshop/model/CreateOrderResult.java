package com.prm392.onlineshoesshop.model;

import org.json.JSONObject;

public class CreateOrderResult {
    public String appTransId;
    public String zpTransToken;
    public JSONObject rawData;

    public CreateOrderResult(String appTransId, String zpTransToken, JSONObject rawData) {
        this.appTransId = appTransId;
        this.zpTransToken = zpTransToken;
        this.rawData = rawData;
    }
}
