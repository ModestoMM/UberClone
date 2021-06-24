package com.modesto.uberclone.retrofit;

import com.modesto.uberclone.models.FCMBody;
import com.modesto.uberclone.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

//FIREBASE CLOUD MESSAGING
//Esta seria la interface que nos permita enviar notificaciones de dispositivo a dispositivo
public interface IFCMapi {

    //Vamos a settear los Headers Como en el Post
    @Headers({
            "Conten-Type:application/json",
            "Authorization:key=AAAA3o2JOZw:APA91bGuNMXdDPJKk7zEOCSjqtoLo7jJIZnCzCiEVUmhSAD4wB20Iy0dz2sTD61QiN98g_CwX0laK-Se80mZGRBrTexYy0oqB_va3bGyzdSHYkxwSxttXoxgEyCcSieiSYllAgtZLecL"
    })

    //Es la ruta wu nos permitira enviar notificaciones
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}
