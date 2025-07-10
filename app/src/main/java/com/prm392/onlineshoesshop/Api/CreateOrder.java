package com.prm392.onlineshoesshop.Api;

import com.prm392.onlineshoesshop.constant.AppInfo;
import com.prm392.onlineshoesshop.helper.Helpers;
import com.prm392.onlineshoesshop.model.CreateOrderResult;

import org.json.JSONObject;

import java.util.Date;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class CreateOrder {
    private class CreateOrderData {
        String AppId;
        String AppUser;
        String AppTime;
        String Amount;
        String AppTransId;
        String EmbedData;
        String Items;
        String BankCode;
        String Description;
        String Mac;

        private CreateOrderData(String amount) throws Exception {
            long appTime = new Date().getTime();
            AppId = String.valueOf(AppInfo.APP_ID);
            AppUser = "Android_Demo";
            AppTime = String.valueOf(appTime);
            Amount = amount;
            AppTransId = Helpers.getAppTransId();
            EmbedData = "{}";
            Items = "[]";
            BankCode = "zalopayapp";
            Description = "Merchant pay for order #" + AppTransId;
            String inputHMac = String.format("%s|%s|%s|%s|%s|%s|%s",
                    AppId, AppTransId, AppUser, Amount, AppTime, EmbedData, Items);
            Mac = Helpers.getMac(AppInfo.MAC_KEY, inputHMac);
        }
    }

    public CreateOrderResult createOrder(String amount) throws Exception {
        CreateOrderData input = new CreateOrderData(amount);

        RequestBody formBody = new FormBody.Builder()
                .add("app_id", input.AppId)
                .add("app_user", input.AppUser)
                .add("app_time", input.AppTime)
                .add("amount", input.Amount)
                .add("app_trans_id", input.AppTransId)
                .add("embed_data", input.EmbedData)
                .add("item", input.Items)
                .add("bank_code", input.BankCode)
                .add("description", input.Description)
                .add("mac", input.Mac)
                .build();

        JSONObject response = HttpProvider.sendPost(AppInfo.URL_CREATE_ORDER, formBody);
        String token = response.optString("zp_trans_token");

        return new CreateOrderResult(input.AppTransId, token, response);
    }
}
