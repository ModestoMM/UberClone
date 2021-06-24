package com.modesto.uberclone.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

//En esta interface vamos hacer una peticion al servicio de google map
public interface IGoogleApi {

    @GET
    Call<String> getDirection (@Url String url);
}
