package com.modesto.uberclone.retrofit;

import android.net.Uri;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {



    //El para metro que recibe es la url de donde vamos a realizar la peticion
    public static Retrofit getClient(String url){
        //Aqui verificaremos si todavia no se ha instanciado
          Retrofit retrofit=new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            return  retrofit;
    }

    //CONFIGURAR UN METODO QUE NOS PERMITA ENVIAR UNA PETICION HTTP AUN SERVICIO DE FIREBASE PARA ENVIAR
    // LAS NOTIFICACIONES DISPOSITIVO A DISPOSITIVO
    public static Retrofit getClientObject(String url){
        //Aqui verificaremos si todavia no se ha instanciado
          Retrofit  retrofit=new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        return  retrofit;
    }

}
