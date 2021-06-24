package com.modesto.uberclone.Activitys.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.modesto.uberclone.Activitys.MapClientActivity;
import com.modesto.uberclone.Activitys.MapDriverActivity;
import com.modesto.uberclone.Activitys.driver.CalificationClientActivity;
import com.modesto.uberclone.R;
import com.modesto.uberclone.models.ClientBooking;
import com.modesto.uberclone.models.HistoryBooking;
import com.modesto.uberclone.providers.AuthProviders;
import com.modesto.uberclone.providers.ClientBookingProvider;
import com.modesto.uberclone.providers.HistoryBookingProvider;

import java.util.Date;

public class CalificationDriverActivity extends AppCompatActivity {
    TextView mTextViewOrigin;
    TextView mTextViewDestination;
    RatingBar mRatingBar;
    TextView mTextViewPrice;
    Button mButtonCalification;

    //Del AuthProvider optenemos el id que necesitamos en este caso el del driver
    private AuthProviders mAuthProvider;

    private ClientBookingProvider mClientBookingProvider;

    private HistoryBooking mHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;

    //En esta variable global conoceremos la calificacion que asigno el conductor al cliente
    private float mCalification=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_driver);

        mTextViewOrigin= findViewById(R.id.txtViewOriginCalification);
        mTextViewDestination= findViewById(R.id.txtViewDestinationCalification);
        mRatingBar = findViewById(R.id.ratingbarClafication);
        mButtonCalification = findViewById(R.id.btnCalification);
        mTextViewPrice= findViewById(R.id.textViewPrice);

        mAuthProvider= new AuthProviders();

        mClientBookingProvider= new ClientBookingProvider();

        mHistoryBookingProvider= new HistoryBookingProvider();

        //Este metodo de setOnRtingBarChange Listener nos devuelve la eleccion del usuario en tipo float
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float calificacion, boolean fromUser) {
                mCalification= calificacion;
            }
        });

        mButtonCalification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calificate();
            }
        });
        getClientBooking();
        getPrice();
    }

    private void getPrice() {
        mClientBookingProvider.getPrice(mAuthProvider.getIdDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //Checar esto no pude mandar de parametro el precio me aparece el valor null checarlo mas adelante
                    //tambien lo intente sacar del clientbooking pero tambien me da null la referencia asi que no olvidar checar
                    //que el precio no lo pude visualizar en la pantalla de la calificacion
                    // String price = snapshot.getValue().toString();
                    //mTextViewPrice.setText("$"+price);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //Este metodo lo usaremos para conectarnos al ClientBooking y sacar a informacion requerida sobre la ubicacion de origen y destino
    //para ingresarla en la pantalla del celular y asi la pueda ver en este caso el conductor quien es el que calificara al cliente
    private void getClientBooking(){
        mClientBookingProvider.getClientBooking(mAuthProvider.getIdDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //Para sacar toda la informacion que se encuentra en un nodo de la base de datos de manera mas secilla es la siguiente
                    ClientBooking clientBooking= snapshot.getValue(ClientBooking.class);
                    mTextViewOrigin.setText(clientBooking.getOrigin());
                    mTextViewDestination.setText(clientBooking.getDestination());
                    //LA PRIMERA PARTE EDEL IDHISTORYBOOKING LO GENERARA EL CONDUCTOR A LA HORA DE FINALIZAR EL VIAJE
                    mHistoryBooking= new HistoryBooking(
                            clientBooking.getIdHistoryBooking(),
                            clientBooking.getId_Client(),
                            clientBooking.getId_Driver(),
                            clientBooking.getDestination(),
                            clientBooking.getOrigin(),
                            clientBooking.getTime(),
                            clientBooking.getKm(),
                            clientBooking.getStatus(),
                            clientBooking.getOring_Lat(),
                            clientBooking.getOring_Lng(),
                            clientBooking.getDestinationLat(),
                            clientBooking.getDestinationLng()
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //Cuando el conductor o el cliente se califican se creara un historial de viajes en nuestra base de datos para ello creamos un modelos
    //que e llamara HistoriBooking
    private void calificate() {
        //SI ESTO ES VERDAD VAMOS A CREAR O ACTUALIZAR ESE HISTORIAL EN LA BASE DE DATOS
        if(mCalification > 0){
            mHistoryBooking.setCalificationDriver(mCalification);
            //Aqui en e metodo setTimestamp asignamos la fecha y hora gracias a la clase Date
            mHistoryBooking.setTimestamp(new Date().getTime());
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //Va a verificar si existe ya dentro del nodo HistoryBooking y si es asi lo que va hacer nadamas es actualizar el campo
                    //de la calificacion del driver en este caso y si no va a crear todo el historybooking desde el principio
                    if(snapshot.exists()){
                        mHistoryBookingProvider.updateCalificationDriver(mHistoryBooking.getIdHistoryBooking(),mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationDriverActivity.this, "La calificacion se guardo correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent= new Intent(CalificationDriverActivity.this, MapClientActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }else{
                        //EL METODO ADDONSUCCESLISTENER SE UTILIZA PARA SABER SI SE REALIZO LA TAREA DE FORMA CORRECTA
                        mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationDriverActivity.this, "La calificacion se guardo correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent= new Intent(CalificationDriverActivity.this, MapClientActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }else{
            Toast.makeText(this, "Debe ingresar la calificacion", Toast.LENGTH_SHORT).show();
        }
    }
}