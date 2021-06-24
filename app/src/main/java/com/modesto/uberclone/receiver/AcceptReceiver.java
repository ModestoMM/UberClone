package com.modesto.uberclone.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.modesto.uberclone.Activitys.driver.MapDriverBookingActivity;
import com.modesto.uberclone.channel.NotificationHelper;
import com.modesto.uberclone.providers.AuthProviders;
import com.modesto.uberclone.providers.ClientBookingProvider;
import com.modesto.uberclone.providers.GeofireProvider;

//ESTA CLASE SE UTILIZARA PARA LAS NOTIFICACIONES EN ESTE CASO EL BOTON DE ACEPTAR EN ELLA Y SE INSTANCIARA EN LA CLASE
//MYFIREBASEMESSAGINGCLIENT Tambien tenemos que registrar el recevier en el manifest
public class AcceptReceiver extends BroadcastReceiver {

    //Creamos una instancia al GeofireProvider
    private GeofireProvider mGeofireProvider;
    //Instancia del mAuth Provider
    private AuthProviders mAuthProvider;

    private ClientBookingProvider mclientBookingProvider;
    //Este metodo es el que se va a ejecutar cuando se presione sobre la accion de aceptar sobre la notificacion
    @Override
    public void onReceive(Context context, Intent intent) {
        mAuthProvider= new AuthProviders();
        mGeofireProvider= new GeofireProvider("active_drivers");
        mGeofireProvider.removeLocation(mAuthProvider.getIdDriver());

        //Sacamos el id ya que lo necesitamos como parametro para para el metodo updatestatus y asi cambiar el status del
        //ClientBooking
        String  id_client=intent.getExtras().getString("id_client");
        mclientBookingProvider=new ClientBookingProvider();
        mclientBookingProvider.updateStatus(id_client,"accept");

        //Para hacer que la notificacion se quite automaticamente al presonar el boton aceptar
        NotificationManager manager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        //Esta parte se utiliz apara poder mandar al conductor a la pantalla de viaje si es que acepta el teabajo
        //El clearTask se utiliza para cuando el conductor llegue a la nueva pantalla ya no se pueda regresar
        Intent intent1= new Intent(context, MapDriverBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idClient",id_client);
        context.startActivity(intent1);

    }
}
