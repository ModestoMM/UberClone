package com.modesto.uberclone.providers;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.modesto.uberclone.R;
import com.modesto.uberclone.retrofit.IGoogleApi;
import com.modesto.uberclone.retrofit.RetrofitClient;

import retrofit2.Call;

public class GoogleApiProvider {

    private Context context;

    public GoogleApiProvider(Context context){
        this.context=context;

    }

    public Call<String> getDirection(LatLng mOriginLatLng, LatLng mDestinationLatLng){
        //Esta es la base del URL que usaremos para encontrar los datos que necesitamos
        String base= "https://maps.googleapis.com";
        //Esta parte ya es la URL completa tenemos que modificarle varios aspectos como el Origen, Destino y La Api key
        String query= "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                + "origin=" + mOriginLatLng.latitude + "," + mOriginLatLng.longitude + "&"
                + "destination=" + mDestinationLatLng.latitude + "," + mDestinationLatLng.longitude + "&"
                + "key=" + context.getResources().getString(R.string.google_maps_api);
        //Analizar bien como Funciona esta linea ya que no estoy completamente seguro solo se que al final retorna Call<String>
        //Con los datos que mandamos a llamar del url
        return RetrofitClient.getClient(base).create(IGoogleApi.class).getDirection(base + query);
    }
}
