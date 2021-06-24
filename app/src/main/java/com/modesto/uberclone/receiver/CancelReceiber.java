package com.modesto.uberclone.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.modesto.uberclone.Activitys.MapDriverActivity;
import com.modesto.uberclone.providers.AuthProviders;
import com.modesto.uberclone.providers.ClientBookingProvider;
import com.modesto.uberclone.providers.GeofireProvider;



//Esta clase se utilizara para el boton cancelar
public class CancelReceiber extends BroadcastReceiver {


    //Creamos una instancia al GeofireProvider
    private GeofireProvider mGeofireProvider;
    //Instancia del mAuth Provider
    private AuthProviders mAuthProvider;

    private ClientBookingProvider mclientBookingProvider;
    @Override
    public void onReceive(Context context, Intent intent) {
       mAuthProvider = new AuthProviders();
        mGeofireProvider= new GeofireProvider("active_drivers");
        mGeofireProvider.removeLocation(mAuthProvider.getIdDriver());

        //Sacamos el id ya que lo necesitamos como parametro para para el metodo updatestatus y asi cambiar el status del
        //ClientBooking
        String  id_client=intent.getExtras().getString("id_client");
        mclientBookingProvider=new ClientBookingProvider();
        mclientBookingProvider.updateStatus(id_client,"cancel");

        //Para hacer que la notificacion se quite automaticamente al presonar el boton cancelar
        NotificationManager manager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

    }
}
