package com.modesto.uberclone.providers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InfoProvider {

    DatabaseReference mDatabase;

    public InfoProvider() {
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Info");
    }

    //Retornamos solo el objeto database para optener las variables que son el km y los minutos
    public DatabaseReference getInfo(){
        return mDatabase;
    }

}
