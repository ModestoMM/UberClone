package com.modesto.uberclone.models;

import java.util.Map;

public class FCMBody {
    //El campo to es el token del usuario al que vamos a mandar la notificacion
    //El campo priority es la prioridad que tendra la notificacion
    //Y EL CAMPO DATA ES LA INFORMACION QUE MANDAREMOS EN ESA NOTIFICACION
    private String to;
    //En ligar de to si usamos un arreglo para llamarlo en el constructor podemos mandar de parametro la notificacion a todos
    //los conductores mas cercanos y asi ver quien es el primero en acceptar la notificacion y el que se qedara con el viaje
    //private String[] registration_ids;
    private String priority;
    Map<String,String> data;
    //Esta variable significa time to live y esto sirve para asegurarce de que la notificacion se envie tan pronto como sea posible
    private String ttl;

    public FCMBody(String to, String priority, String ttl ,Map<String, String> data) {
        this.to = to;
        this.priority = priority;
        this.data = data;
        this.ttl=ttl;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

// Getter Methods

    public String getTo() {
        return to;
    }

    public String getPriority() {
        return priority;
    }



    // Setter Methods

    public void setTo( String to ) {
        this.to = to;
    }

    public void setPriority( String priority ) {
        this.priority = priority;
    }

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }
}
