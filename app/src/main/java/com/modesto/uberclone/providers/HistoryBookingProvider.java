package com.modesto.uberclone.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.modesto.uberclone.models.ClientBooking;
import com.modesto.uberclone.models.HistoryBooking;

import java.util.HashMap;
import java.util.Map;

//ESTE PROVIDER ES SIMILAR AL QUE USARAMOS EN EL CLIENTBOOKINGPROVIDER
public class HistoryBookingProvider {

    private DatabaseReference mDatabase;

    //Instanciamos un nuevo child a la database
    public HistoryBookingProvider() {
        mDatabase= FirebaseDatabase.getInstance().getReference().child("HistoryBooking");
    }

    //Creamos este metodo para poder acceseder al id del cliente y mandar de parametro el clintbooking
    //Que si recordamos es la clase donde tendremos el providers y la referencia de los aspectos que mandaremos al driver del cliente
    public Task<Void> create(HistoryBooking historyBooking){
        return mDatabase.child(historyBooking.getIdHistoryBooking()).setValue(historyBooking);
    }

    //Metodo para actualizar la calificacion
    public Task<Void> updateCalificationClient (String idHistoryBooking,float calificacionClient){
        Map<String,Object> map = new HashMap<>();
        map.put("calificationClient", calificacionClient);
        return mDatabase.child(idHistoryBooking).updateChildren(map);

    }

    //Metodo para actualizar la calificacion
    public Task<Void> updateCalificationDriver (String idHistoryBooking,float calificacionDriver){
        Map<String,Object> map = new HashMap<>();
        map.put("calificationDriver", calificacionDriver);
        return mDatabase.child(idHistoryBooking).updateChildren(map);

    }

    //El siguiente metodo es par saber si se creo ya esta informacion o no
    public DatabaseReference getHistoryBooking(String idHistoryBooking){
        return mDatabase.child(idHistoryBooking);
    }

}
