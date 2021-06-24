package com.modesto.uberclone.Activitys.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.modesto.uberclone.Activitys.MapDriverActivity;
import com.modesto.uberclone.R;
import com.modesto.uberclone.providers.AuthProviders;
import com.modesto.uberclone.providers.ClientBookingProvider;
import com.modesto.uberclone.providers.GeofireProvider;

//En esta clase haremos de otra forma el recibimiento de notificaciones que sera de una manera mas atractiva para la vista del usuario

public class NotificationBookingActivity extends AppCompatActivity {

    private TextView mTextViewDestination;
    private TextView mTextViewOrigin;
    private TextView mTextViewMin;
    private TextView mTextViewDistance;
    private TextView mTextViewCounter;
    private Button mButtonAccept;
    private Button mButtonCancel;

    //Creamos una instancia al GeofireProvider
    private GeofireProvider mGeofireProvider;
    //Instancia del mAuth Provider
    private AuthProviders mAuthProvider;


    private ClientBookingProvider mclientBookingProvider;

    //EN ESTE CASO OPTENDREMOS EL ID DEL CLIENTE POR LOS EXTRAS
    private String mExtraIdClient;
    private String mExtraOrigin;
    private String mExtraDestination;
    private String mExtraMin;
    private String mExtraDistance;

    //PARA UTILIZAR UN SONIDO EN NUESTRAS NOTIFICACIONES NECESITAMOS CREAR UNA VARIABLE DE MEDIA PLAYER
    private MediaPlayer mMediaPlayer;

    private int mcounter = 27;

    private  ValueEventListener mListner;

    //PARA HACER LA FUNCION DEL CONTADOR USAMOS ESTA PROPIEDAD DE TIPO HANLDER Y RONNABLE
    private Handler mHandler;
    private Runnable runnable = new Runnable() {
        //en este metodo pondremos toda la logica
        @Override
        public void run() {
            mcounter = mcounter - 1;
            mTextViewCounter.setText(String.valueOf(mcounter));
            if (mcounter > 0) {
                InitTime();
            } else {
                //con este metodo cancelamos el viaje
                cancelBooking();

            }
        }
    };

    private void InitTime() {
        mHandler = new Handler();
        //Este metodo resive el runnable y cada cuanto se va a ejecutar que seria en este caso 1 segundo que es igual a mil milisegundos
        mHandler.postDelayed(runnable, 1000);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_booking);
        mTextViewOrigin = findViewById(R.id.txtViewOrigin);
        mTextViewDestination = findViewById(R.id.txtViewDestination);
        mTextViewMin = findViewById(R.id.textViewMin);
        mTextViewDistance = findViewById(R.id.textViewDistance);
        //EL TEXTVIEWCOUNTER NOS SERVIRA PARA INSTANCIAR LA VARIBLE DE CONTADOR Y QUE ASI DESPUES DE UN CIERTO TIEMPO SI NO RESPONDES
        //LA NOTIFICACION QUE SE CANCELA AUTOMATICAMENTE
        mTextViewCounter = findViewById(R.id.textViewCounter);
        mButtonAccept = findViewById(R.id.btnAceptarBooking);
        mButtonCancel = findViewById(R.id.btnCancelBooking);

        //LA VARIABLE PARA EL SONIDO TIENE QUE SER INSTANCIADA EN EL ONCREATE
        mMediaPlayer = MediaPlayer.create(this, R.raw.firestone);
        //Este metodo de setLooping es para que el sonido se repita varias veces en mi caso no lo usare ya que la cancion dura un buen
        // mMediaPlayer.setLooping(true);

        //DE ESTA MANERA CONSEGUIMOS EL ID DEL CLIENTE y los demas extras
        mExtraIdClient = getIntent().getStringExtra("id_client");
        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");
        mExtraMin = getIntent().getStringExtra("min");
        mExtraDistance = getIntent().getStringExtra("distance");

        mclientBookingProvider = new ClientBookingProvider();

        mTextViewOrigin.setText(mExtraOrigin);
        mTextViewDestination.setText(mExtraDestination);
        mTextViewMin.setText(mExtraMin);
        mTextViewDistance.setText(mExtraDistance);



        //CON ESTE GETWINDOWS HAREMOS QUE LA NOTIFICACION SE PUEDE OBSERVAR INCLUSO CON LA PANTALLA APAGADA DEL CELULAR
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        checkIfClientCancelBookink();

        InitTime();

        mButtonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptBooking();
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBooking();
            }
        });

    }

    //ESTE METODO CANCELAREMOS LA NOTIFICACION SI ES QUE EL CLIENTE DESCIDE CANCELARLA ANTES QUE EL DRIVER LA ACEPTE
    private void checkIfClientCancelBookink(){
       mListner = mclientBookingProvider.getClientBooking(mExtraIdClient).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Toast.makeText(NotificationBookingActivity.this, "El cliente cancelo el viaje", Toast.LENGTH_LONG).show();
                    cancelBooking();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
       });
    }

    //Estos metodos de cancelar y accepar ya los tenemos en los receiver
    private void cancelBooking() {
        //VERIFICAMOS SI EL HANDLER ES DIFERENTE DE NULL esto lo hacemos para que el contador no siga corriendo
        if (mHandler != null) mHandler.removeCallbacks(runnable);
        //mAuthProvider = new AuthProviders();
        //mGeofireProvider = new GeofireProvider("active_drivers");
        //mGeofireProvider.removeLocation(mAuthProvider.getIdDriver());
        //Sacamos el id ya que lo necesitamos como parametro para para el metodo updatestatus y asi cambiar el status del
        //ClientBooking
        mclientBookingProvider.updateStatus(mExtraIdClient, "cancel");


        //Para hacer que la notificacion se quite automaticamente al presonar el boton Cancelar
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);
        Intent intent = new Intent(NotificationBookingActivity.this, MapDriverActivity.class);
        startActivity(intent);
        finish();
    }

    private void acceptBooking() {
        //VERIFICAMOS SI EL HANDLER ES DIFERENTE DE NULL esto lo hacemos para que el contador no siga corriendo
        if (mHandler != null) mHandler.removeCallbacks(runnable);
        mAuthProvider = new AuthProviders();
        mGeofireProvider = new GeofireProvider("active_drivers");
        mGeofireProvider.removeLocation(mAuthProvider.getIdDriver());

        mclientBookingProvider.updateStatus(mExtraIdClient, "accept");

        //Para hacer que la notificacion se quite automaticamente al presonar el boton aceptar
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        //Esta parte se utiliz apara poder mandar al conductor a la pantalla de viaje si es que acepta el teabajo
        //El clearTask se utiliza para cuando el conductor llegue a la nueva pantalla ya no se pueda regresar
        Intent intent1 = new Intent(NotificationBookingActivity.this, MapDriverBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idClient", mExtraIdClient);
        startActivity(intent1);
        finish();

    }

    //SOBRE ESCRIBIMOS OTRO METODO DE CICLO DE VIDA DE ANDROID QUE ES EL ONPAUSA CON TODOS LOS METODOS QUE SOBRE ESCRIBIMOS NOS ASEGURAMOS
    //NO TENGA NINGUN INCONVENIENTE CUANDO SE REPRODUZCA Y NUESTRA APLICACION NO SE FINALICE AUTOMATICAMENTE
    @Override
    protected void onPause() {
        super.onPause();
        if(mMediaPlayer != null){
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
            }
        }
    }

    //SOBRE ESCRIBIMOS OTRO METODO DE CICLO DE VIDA DE ANDROID QUE ES CUANDO MINIMIZAMOS LA APLICACION
    @Override
    protected void onStop() {
        super.onStop();
        if(mMediaPlayer != null){
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.release();
            }
        }
    }

    //SOBRE ESCRIBIMOS OTRO METODO DE CICLO DE VIDA DE ANDROID Y ES EL QUE SE EJECUTA CUANDO TODA LA ACTIVIDAD HA SIDO CREADA
    @Override
    protected void onResume() {
        super.onResume();
        if(mMediaPlayer != null){
            //Vamo a validar si ya no esta sonando
            if(!mMediaPlayer.isPlaying()){
                mMediaPlayer.start();
            }
        }
    }

    //TAMBIEN DEBEMOS DE PARAR EL CONTADOR CUANDO LA ACTIVIDAD SE DESTRUYA O FINALICE
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHandler != null) mHandler.removeCallbacks(runnable);

        if(mMediaPlayer!= null){
            //Validamos si el metodo esta sonando
            if(mMediaPlayer.isPlaying()){
                //Le agregamos una pausa para que deje de sonar
                mMediaPlayer.pause();
            }
        }
        if(mListner != null){
            mclientBookingProvider.getClientBooking(mExtraIdClient).removeEventListener(mListner);
        }
    }
}