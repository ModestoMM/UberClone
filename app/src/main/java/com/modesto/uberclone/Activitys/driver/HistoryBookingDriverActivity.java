package com.modesto.uberclone.Activitys.driver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.modesto.uberclone.Activitys.adapters.HistoryBookingClientAdapter;
import com.modesto.uberclone.Activitys.adapters.HistoryBookingDriverAdapter;
import com.modesto.uberclone.Activitys.client.HistoryBookingClientActivity;
import com.modesto.uberclone.Includes.MyToolbar;
import com.modesto.uberclone.R;
import com.modesto.uberclone.models.HistoryBooking;
import com.modesto.uberclone.providers.AuthProviders;

public class HistoryBookingDriverActivity extends AppCompatActivity {

    private RecyclerView mRecycleView;
    //Esta es la clase donde viene lo que llenara el RecycleVyew con el historial de viajes del drver
    //tenems que llamarlo en un metodo de ciclo de vida de android llamado onstart();
    private HistoryBookingDriverAdapter mAdapter;

    private AuthProviders mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_driver);
        MyToolbar.show(this, "Historial de Viajes", true);

        mRecycleView = findViewById(R.id.ReciycleViewHistoryBooking);
        //Este linerlayout nos va a permitir mostrar la informacion de la base de datos de manera vertical sin el no nos mostraria nada
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Para crear las opciones necesitamos lo siguiente
        //Creamos una variable de tipo query para que esta pueda regresar los parametros que queremos y hacemos que lo ordene
        //por el id_client utilizamos el metodo equals para decirle que queremos que el id del cliente sea igual al id almacenado en la
        //sesion del usuario para ello instanciamos el AuthProvider
        mAuthProvider = new AuthProviders();
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("HistoryBooking")
                .orderByChild("id_Driver")
                .equalTo(mAuthProvider.getIdDriver());

        FirebaseRecyclerOptions<HistoryBooking> options= new FirebaseRecyclerOptions.Builder<HistoryBooking>()
                //La consulta que haremos a la database mandando la consulta y despues de que tipo sera
                //la clase que nos retorne en este caso HistoryBooking
                .setQuery(query,HistoryBooking.class)
                .build();

        mAdapter = new HistoryBookingDriverAdapter(options, HistoryBookingDriverActivity.this);

        mRecycleView.setAdapter(mAdapter);
        //Con el metodo startKustener empieza a escuchar los cambios que se hagan en firebase
        mAdapter.startListening();
    }

    //sobre escribimos este metodo para que cuando la aplicacion la minimicemos que deje de escuchar los cambios con stoplisterner

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}