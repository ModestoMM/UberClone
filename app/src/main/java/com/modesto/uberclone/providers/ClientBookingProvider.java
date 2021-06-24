package com.modesto.uberclone.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.modesto.uberclone.models.ClientBooking;

import java.util.HashMap;
import java.util.Map;

public class ClientBookingProvider {
    private DatabaseReference mDatabase;

    //Instanciamos un nuevo child a la database
    public ClientBookingProvider() {
        mDatabase= FirebaseDatabase.getInstance().getReference().child("ClientBooking");
    }

    //Creamos este metodo para poder acceseder al id del cliente y mandar de parametro el clintbooking
    //Que si recordamos es la clase donde tendremos el providers y la referencia de los aspectos que mandaremos al driver del cliente
    public Task<Void> create(ClientBooking clientBooking){
        return mDatabase.child(clientBooking.getId_Client()).setValue(clientBooking);
    }

    //Creamos un metodo para actualizar el estado del viaje
    public Task<Void> updateStatus (String idClientBooking, String status){
        Map<String,Object> map= new HashMap<>();
        //Aqui hacemos la actualizacion del status es ai como se llama el nodo que tenemos en nuestra database
        map.put("status", status);
        return  mDatabase.child(idClientBooking).updateChildren(map);
    }

    //Creamos un metodo para actualizar el estado del viaje
    public Task<Void> updateIdHistoryBooking (String idClientBooking){
        //El metodo push.getKey genera un identificador unico en la base de datos
        String idPush = mDatabase.push().getKey();
        Map<String,Object> map= new HashMap<>();
        //Aqui hacemos la actualizacion del status es ai como se llama el nodo que tenemos en nuestra database
        map.put("idHistoryBooking", idPush);
        return  mDatabase.child(idClientBooking).updateChildren(map);
    }

    //Creamos un metodo para actualizar el precio del viaje
    public Task<Void> updatePrice (String idClientBooking, double price){
        Map<String,Object> map= new HashMap<>();
        //Aqui hacemos la actualizacion del status es ai como se llama el nodo que tenemos en nuestra database
        map.put("price", price);
        return  mDatabase.child(idClientBooking).updateChildren(map);
    }

    //Este metodo se usara como referencia para la propiedad de status la cual cambiara dependiendo si el conductor
    //Accepto o no el trabajo por asi decirlo
    public DatabaseReference getStatus(String idClientBooking){
        return  mDatabase.child(idClientBooking).child("status");
    }

    public DatabaseReference getPrice(String idClient){
        return mDatabase.child(idClient).child("price");
    }

    //Haremos un metodo para retornar toda la informacion guardada en el client booking esto es ya que trasaremos la ruta
    //del conductor hacia el cliente y esa informacion la encontramos en los datos del cliente que eestan en el nodo clientBooking
    public DatabaseReference getClientBooking(String idClientBooking){
        return mDatabase.child(idClientBooking);
    }

    //Asi eliminaremos el client Booking a la hora de usarlo en el boton de cancelar pedido del requestdriver que se encuentra
    //en el apartado de cliente es la pantalla de espera
    public Task<Void> delete(String idClient){
        return mDatabase.child(idClient).removeValue();
    }

}

