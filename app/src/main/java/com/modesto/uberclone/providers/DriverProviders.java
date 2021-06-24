package com.modesto.uberclone.providers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.modesto.uberclone.models.Driver;

import java.util.HashMap;
import java.util.Map;

public class DriverProviders {
    DatabaseReference mDatabase;
    public DriverProviders(){
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
    }

    public Task<Void> create(Driver driver ){
        Map<String, Object> map=new HashMap<>();
        map.put("name",driver.getName());
        map.put("email",driver.getEmail());
        map.put("vehicleBrand",driver.getVehicleBrand());
        map.put("vehiclePlate",driver.getVehiclePlate());
        return mDatabase.child(driver.getId()).setValue(map);
    }

    //Este metodo se utilizara para retiornar la instancia de la base de datos en la clase Driver
    //para poderla usar dentro de la clase MapClientBookingActivity y asi conosca esos datos el cliente
    public DatabaseReference getDriver(String idDriver){
        return mDatabase.child(idDriver);
    }


    //EN ESTE MEOTODO HAEMOS LA ACTUALIZACION DE LOS DATOS QUE SE MANDAN EN EL MAP RECORDAR QUE EL UPDATECHILDREN HACE UNA ACTUALIZACION
    //SOLO A LOS CAMPOS REFERENCIADOS ASI QUE LOS QUE NO SON REFEENCIAS SIMPLEMENTE SE MANTIENEN IGUALCOMO SON EL CORREO Y LA CONTRASEÑA
    public Task<Void> update(Driver driver ){
        Map <String,Object> map= new HashMap<>();
        map.put("name",driver.getName());
        map.put("image",driver.getImage());
        map.put("vehicleBrand",driver.getVehicleBrand());
        map.put("vehiclePlate",driver.getVehiclePlate());
        return mDatabase.child(driver.getId()).updateChildren(map);
    }



}
