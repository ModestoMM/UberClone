package com.modesto.uberclone.providers;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeofireProvider {

    private DatabaseReference mDatabase;
    private GeoFire mGiofire;

    public GeofireProvider(String reference){
        mDatabase= FirebaseDatabase.getInstance().getReference().child(reference);
        //Recibe de parametro la referencia de nuestra base de datos que en este caso esta en mDatabase
        mGiofire=new GeoFire(mDatabase);
    }

    //El metodo nos permitira guardar la localizacion del usuario en la base de datos que tenemos en Firebas
    public void saveLocation(String idDriver, LatLng latLng){
        //Con esta linea guardamos la localizacion del usuario en database
        mGiofire.setLocation(idDriver,new GeoLocation(latLng.latitude,latLng.longitude));
    }

    //Metodo para eliminar la localizacion
    public void removeLocation(String idDriver){
        mGiofire.removeLocation(idDriver);
    }

    //Metodo para optener los conductores disponibles
    public GeoQuery getActiveDrivers(LatLng latLng, double radio){
        //Ubicamos nuestra localizacion y de ahi a un radio en kilometros señalamos los conductores que queremos ver
        GeoQuery geoQuery=mGiofire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude), radio);
        geoQuery.removeAllListeners();
        return geoQuery;
    }

    //Este metodo sera el que observe si un conductor se conecto a los nodos trabajando si es asi dejams de actualizar su ubicacion en tiempo real
    //en la pantalla mapdriveractivity porque el conductor se vuelve a crear en el apartado de acive_drivers y eso esta mal
    //porque el conductor ya se encontraria en el drivers_working que es otro nodo de extension que creamos para verificar
    //cuando el conductor acepte el trabajo y ya no se encuentre de forma activa para otro hasta que termine el suyo
    public  DatabaseReference isDriverWorking(String idDriver){
        //No se utiliza el mDatabase porque como es llamado en la clase mapdriver y se pasa de referencia el active_drivers
        //Pues no lo utilizamos porque asi hacemos la referencia solo en este metodo al nodo que se selecciono
        return  FirebaseDatabase.getInstance().getReference().child("drivers_working").child(idDriver);
    }


    //Creamos un metodo para retornar la ubicacion del cliente y asi observarla en la pantalla del clientbookingprovider
    public DatabaseReference getDriverLocation(String idDriver){
        return mDatabase.child(idDriver).child("l");
    }

    public DatabaseReference getDriverActivity(String idDriver){
        return FirebaseDatabase.getInstance().getReference().child("active_drivers").child(idDriver);
    }

}
