package com.modesto.uberclone.channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.modesto.uberclone.R;

//ESTA CLASE SERA NUESTRA BASE PARA PODER ENVIAR NOTIFICACIONES ENTRE DISPOSITIVAS
public class NotificationHelper extends ContextWrapper {

    private static final String CHANNEL_ID="com.modesto.uberclone";
    private static final String CHANNEL_NAME="UberClone";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        //Este verufucacion se tiene que hacer ya que tenemos que saber si se encuentra en un dispositivo movil que su sistema operativo es
        //mayor o igual al Oreo y asi ejecute el metodo
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }

    }

    //Esto es necesario ya que el canal de notificaciones es obigatoria desde las versiones de android Oreo o superiores
    @RequiresApi(api= Build.VERSION_CODES.O)
    private void createChannel(){

    NotificationChannel notificationChannel = new NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
    );
    //En esta parte estamos configurando las notificaciones sobre si tendra vibracion y asi
    notificationChannel.enableLights(true);
    notificationChannel.enableVibration(true);
    notificationChannel.setLightColor(Color.GRAY);
    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
    //Con esta instruccion tendriamos configurado el metodo
    getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager(){
        if(manager == null){
            manager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    //ESTE METODO NOS SERVIRA PARA CREARNOS UNA NOTIFICACION EN VERSIONES DE API QUE SEAN 26 O SUPERIOR
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotification(String title, String body, PendingIntent intent, Uri soundUri){
        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                //Para que el usuario cuando precione sobre esta notificacion se cierre automaticamente
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_car)
                //En esta parte hacemos a que el contenido que tendremos en el titulo y body pueda ser observado completamente
                //y no solo una parte como antes lo hacia
                .setStyle(new Notification.BigTextStyle()
                .bigText(body).setBigContentTitle(title));

    }

    //Este metodo se utilizara para agregar el boton de aceptar con el cual interctuara el conductor para aceptar
    //el trabajo por asi decirlo
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificationAction(String title, String body, Uri soundUri, Notification.Action AcceptAction, Notification.Action cancelAction){
        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                //Para que el usuario cuando precione sobre esta notificacion se cierre automaticamente
                .setAutoCancel(true)
                .setSound(soundUri)
                .setSmallIcon(R.drawable.ic_car)
                .addAction(AcceptAction)
                .addAction(cancelAction)
                //En esta parte hacemos a que el contenido que tendremos en el titulo y body pueda ser observado completamente
                //y no solo una parte como antes lo hacia
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));

    }

    //ESTE METODO NOS SERVIRA PARA CREAR UNA NOTIFICACION CON UNA API INFERIOR A 26, EL INTENT ES LA ACCION QUE VA EJECUTAR ESTA NOTIFICACION
    public NotificationCompat.Builder getNotificationOldAPI(String title, String body, PendingIntent intent, Uri soundUri){
        return new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                //Para que el usuario cuando precione sobre esta notificacion se cierre automaticamente
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_car)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));

    }

    //Este metodo se utilizara para agregar el boton de aceptar con el cual interctuara el conductor para aceptar y cancelar
    //el trabajo por asi decirlo Pero en este caso para Api menor a O = 26
    public NotificationCompat.Builder getNotificationOldAPIAction(String title, String body, Uri soundUri, NotificationCompat.Action AcceptAction, NotificationCompat.Action cancelAction){
        return new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                //Para que el usuario cuando precione sobre esta notificacion se cierre automaticamente
                .setAutoCancel(true)
                .setSound(soundUri)
                .addAction(AcceptAction)
                .addAction(cancelAction)
                .setSmallIcon(R.drawable.ic_car)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));

    }


}
