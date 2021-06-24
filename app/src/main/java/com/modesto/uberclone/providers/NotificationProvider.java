package com.modesto.uberclone.providers;

import com.modesto.uberclone.models.FCMBody;
import com.modesto.uberclone.models.FCMResponse;
import com.modesto.uberclone.retrofit.IFCMapi;
import com.modesto.uberclone.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {

    //Este es el servicio de fire cloud messaging
    private  String url = "https://fcm.googleapis.com";

    public NotificationProvider() {

    }

    public Call<FCMResponse> sedNotification(FCMBody body){
            return RetrofitClient.getClientObject(url).create(IFCMapi.class).send(body);

    }
}
