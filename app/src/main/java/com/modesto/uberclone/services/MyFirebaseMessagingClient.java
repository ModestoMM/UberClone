package com.modesto.uberclone.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.modesto.uberclone.Activitys.driver.NotificationBookingActivity;
import com.modesto.uberclone.R;
import com.modesto.uberclone.channel.NotificationHelper;
import com.modesto.uberclone.receiver.AcceptReceiver;
import com.modesto.uberclone.receiver.CancelReceiber;

import java.util.Map;

public class MyFirebaseMessagingClient extends FirebaseMessagingService {

    private  static final int NOTIFICATION_CODE =100;

    //ESTE METODO NOS SERVIRA PARA GENERAR UN TOKEN DE USUARIO CON EL FIN DE PODER MANDAR NOTIFICACION DE DISPOSITIVO A DISPOSITIVO
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    //En este metodo vamos a estar reciviendo las notificaciones push que lleguen del servidor
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification= remoteMessage.getNotification();
        Map<String,String> data= remoteMessage.getData();
        String title= data.get("title");
        String body= data.get("body");

        if(title != null){
            //CUANDO RECIVANOS UNA SOLICITUD DE SERVICIO LO QUE HAREMOS ES RECIBIR POR LOS PARAMETROS DE NOTIFICACION ESA INFORMACION
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                //validamos si el titulo contiene esto
                if(title.contains("SOLICITUD DE SERVICIO")){
                    String id_client= data.get("id_client");
                    String origin= data.get("origin");
                    String destination= data.get("destination");
                    String min= data.get("min");
                    String distance= data.get("distance");
                    showNotificationAPIOreoAction(title,body,id_client);

                    showNotificationActivity(id_client,origin,min,destination,distance);
                } else  if(title.contains("VIAJE CANCELADO")){
                    //Copiamos las lineas de codigo del boton cancel para
                    //Para hacer que la notificacion se quite automaticamente al precionar sobre ella
                    //RECORDATORIO EL ID DEBE SER EL MISMO QUE COLOCAMOS CUANDO MOSTRAMOS LAS NOTIFICACIONES DEPENDIENDO EL API
                    NotificationManager manager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(2);
                    showNotificationAPIOreo(title,body);
                }
                else{
                    showNotificationAPIOreo(title,body);
                }

            }else{
                if(title.contains("SOLICITUD DE SERVICIO")){
                    String id_client= data.get("id_client");
                    String origin= data.get("origin");
                    String destination= data.get("destination");
                    String min= data.get("min");
                    String distance= data.get("distance");
                    showNotificationAction(title,body,id_client);

                    showNotificationActivity(id_client,origin,min,destination,distance);
                }else  if(title.contains("VIAJE CANCELADO")){
                    //Copiamos las lineas de codigo del boton cancel para
                    //Para hacer que la notificacion se quite automaticamente al precionar sobre ella
                    //RECORDATORIO EL ID DEBE SER EL MISMO QUE COLOCAMOS CUANDO MOSTRAMOS LAS NOTIFICACIONES DEPENDIENDO EL API
                    NotificationManager manager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(2);
                    showNotification(title,body);
                }
                else{
                    showNotification(title,body);
                }
            }
        }
    }

    private void showNotificationActivity(String id_client, String origin, String min, String destination, String distance) {
        //EN PRIMER LUGAR HAREMOS QUE LA NOTIFICACION QUE NOS LLEGUE NOS ENCIENDA EL CELULAR ASI ESTE BLOQUEADO
        PowerManager pm = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if(!isScreenOn){
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK |
                                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                                PowerManager.ON_AFTER_RELEASE,
                                "AppName: MyLock"
            );
            //PARA QUE EL WAKE LOCK FUNCIONE SE TIENE QUE IR AL MANIFEST A AÑADIR UN NUEVO PERMISO
            wakeLock.acquire(10000);
        }
        //Mandamos los parametros al la clase NotificationBookingActivity para que puedan ser mostrados
        Intent intent= new Intent(getBaseContext(), NotificationBookingActivity.class);
        intent.putExtra("id_client",id_client);
        intent.putExtra("origin",origin);
        intent.putExtra("destination",destination);
        intent.putExtra("min",min);
        intent.putExtra("distance",distance);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationAPIOreo(String title,String body) {
        //LLAMAMOS A LA CLASE DNDE TENEMOS UNSTANCIADOS LOS METODOS DE LAS NOTIFICACIONES Y EN ESTE CASO UTILIZAREMOS LOS DE A API MENOR A O
        //O API MENOR A LA VERSION 26
        PendingIntent inten= PendingIntent.getActivity(getBaseContext(),0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper=new NotificationHelper(getBaseContext());
        Notification.Builder builder= notificationHelper.getNotification(title,body,inten,sound);
        //Para mostrar la notification
        notificationHelper.getManager().notify(1,builder.build());
    }

    //Este metodo se utilizara para la creacion del boton de aceptar en la notificacion
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationAPIOreoAction(String title,String body, String id_client) {

        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("id_client", id_client);
        //Recive un contexto que es la misma clase y un codigo identificador del PendingIntent que debe ser de tipo static y final
        //tambien nos pide el intent que es el acceotinten y al final una constante que esta en PENDINGINTENT
        PendingIntent acceptpendingIntent= PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Lo primero que recive es el icono de nuestra aplicacion, despues el titulo de la accion y al final el pendingintent
        Notification.Action acceptAction= new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptpendingIntent
        ).build();

        //CANCELAR
        Intent cancelIntent = new Intent(this, CancelReceiber.class);
        cancelIntent.putExtra("id_client", id_client);
        //Recive un contexto que es la misma clase y un codigo identificador del PendingIntent que debe ser de tipo static y final
        //tambien nos pide el intent que es el acceotinten y al final una constante que esta en PENDINGINTENT
        PendingIntent cancelpendingIntent= PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Lo primero que recive es el icono de nuestra aplicacion, despues el titulo de la accion y al final el pendingintent
        Notification.Action cancelAction= new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelpendingIntent
        ).build();

        Uri sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper=new NotificationHelper(getBaseContext());
        //SE MODIFICA EL METODO POR EL DE ACTION QUE TENEMOS EN LA MISMA CLASE DE NOTIFICATIONHELPER
        //NO SE USA EL PENDING INTENT PORQUE SOLO VAMOS A ESTAR UTILIZANDO LAS ACCIONES
        Notification.Builder builder= notificationHelper.getNotificationAction(title,body,sound,acceptAction,cancelAction);
        //Para mostrar la notification se le cambia a numero 2 ya que las de numero uno son las que muestran el cuerpo y titulo
        //de la notificacion
        notificationHelper.getManager().notify(2,builder.build());
    }

    private void showNotification(String title, String body){
        //LLAMAMOS A LA CLASE DNDE TENEMOS UNSTANCIADOS LOS METODOS DE LAS NOTIFICACIONES Y EN ESTE CASO UTILIZAREMOS LOS DE A API MENOR A O
        //O API MENOR A LA VERSION 26
        PendingIntent inten= PendingIntent.getActivity(getBaseContext(),0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper=new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder= notificationHelper.getNotificationOldAPI(title,body,inten,sound);
        //Para mostrar la notification
        notificationHelper.getManager().notify(1,builder.build());
    }

    private void showNotificationAction(String title, String body, String id_client){

        Intent acceptIntent = new Intent(this, AcceptReceiver.class);
        acceptIntent.putExtra("id_client", id_client);
        //Recive un contexto que es la misma clase y un codigo identificador del PendingIntent que debe ser de tipo static y final
        //tambien nos pide el intent que es el acceotinten y al final una constante que esta en PENDINGINTENT
        PendingIntent acceptpendingIntent= PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Lo primero que recive es el icono de nuestra aplicacion, despues el titulo de la accion y al final el pendingintent
       //ACEPTAR
        NotificationCompat.Action acceptAction= new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptpendingIntent
        ).build();

        //CANCELAR
        Intent cacelIntent = new Intent(this, CancelReceiber.class);
        cacelIntent.putExtra("id_client", id_client);
        //Recive un contexto que es la misma clase y un codigo identificador del PendingIntent que debe ser de tipo static y final
        //tambien nos pide el intent que es el acceotinten y al final una constante que esta en PENDINGINTENT
        PendingIntent cancelpendingIntent= PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cacelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action cancelAction= new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "cancelar",
                cancelpendingIntent
        ).build();


        Uri sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper=new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder= notificationHelper.getNotificationOldAPIAction(title,body,sound,acceptAction,cancelAction);
        //Para mostrar la notification
        notificationHelper.getManager().notify(2,builder.build());
    }

}
