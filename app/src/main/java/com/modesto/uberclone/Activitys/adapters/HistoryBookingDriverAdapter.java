package com.modesto.uberclone.Activitys.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.modesto.uberclone.Activitys.client.HistoryBookingDetailClientActivity;
import com.modesto.uberclone.Activitys.driver.HistoryBookingDetailDriverActivity;
import com.modesto.uberclone.R;
import com.modesto.uberclone.models.HistoryBooking;
import com.modesto.uberclone.providers.ClientProvider;
import com.modesto.uberclone.providers.DriverProviders;
import com.squareup.picasso.Picasso;

public class HistoryBookingDriverAdapter extends FirebaseRecyclerAdapter<HistoryBooking, HistoryBookingDriverAdapter.ViewHolder>

    {

        private ClientProvider mClientProvider;
        private Context mContext;

        //Esta seria practicamente la configuracion que nosotros debemos enviar para mostrar la informacion almacena en firebasedatabase
    public HistoryBookingDriverAdapter(FirebaseRecyclerOptions<HistoryBooking> option, Context context){
        super(option);
        mClientProvider = new ClientProvider();
        mContext= context;
    }

        //EN ESTE METODO ESTABLECEREMOS LOS VALORES DE LAS VISTAS QUE DESARROLLAMOS EN NUESTRA TARJETA
        @Override
        protected void onBindViewHolder(@NonNull final HistoryBookingDriverAdapter.ViewHolder holder, int position, @NonNull HistoryBooking historyBooking) {

        final String id= getRef(position).getKey();

        //El objeto llamado holder nos permitira acceseder a cada uno de los campos establecidos de nuestro view holder
        //para optener el origen vamos a usar el parametro de modelo que en este caso es de historybooking le cambiamos de name
        holder.mTextViewOrigin.setText(historyBooking.getOrigin());
        holder.mTextViewDestination.setText(historyBooking.getDestination());
        holder.mTextViewCalification.setText(String.valueOf(historyBooking.getCalificationDriver()));
        mClientProvider.getClient(historyBooking.getId_Client()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String name =  snapshot.child("name").getValue().toString();
                    holder.mTextViewName.setText(name);
                    if(snapshot.hasChild("image")){
                       String image =  snapshot.child("image").getValue().toString();
                        Picasso.with(mContext).load(image).into(holder.imageViewHistoryBooking);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
            //Este View vendria siendo toda nuestra tarjeta que se ven en el historial deviajes del cliente o driver
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, HistoryBookingDetailDriverActivity.class);
                    //Pasaos de parametro el id del historial del viaje para usarlo en la clase HistoryBookingDetailClientActivity
                    intent.putExtra("idHistoryBooking",id);
                    mContext.startActivity(intent);
                }
            });



        }

        //AQUI ESTANCIAREMOS EL LAYOUT QUE VAMOS A UTILIZAR  QUE EN ESTE CASO ES EL card_history_booking.xml
        @NonNull
        @Override
        public HistoryBookingDriverAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //asi es como pasamos la vista y el view a la clase ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_history_booking,parent,false);
        return new ViewHolder(view);
    }

        //AQUI VAMOS A INSTANCIAR CADA UNA DE LAS VISTAS QUE TENMOS EN NUESTRA TARJETA
        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView mTextViewName;
            private TextView mTextViewOrigin;
            private TextView mTextViewDestination;
            private TextView mTextViewCalification;
            private ImageView imageViewHistoryBooking;
            private View mView;

            public ViewHolder(View view){
                super (view);
                //En este caso para instanciarlo no podemos llamar a findviewbyid ya que no estamos en dentro de una actividad asi
                //que utilizaremos el objeto View y asi podremos accceder al metodo findviewbyid
                mTextViewName = view.findViewById(R.id.textViewName);
                mTextViewOrigin = view.findViewById(R.id.textViewOrigen);
                mTextViewDestination = view.findViewById(R.id.textViewDestination);
                mTextViewCalification = view.findViewById(R.id.textViewCalificacion);
                mView=view;

                imageViewHistoryBooking = view.findViewById(R.id.ImageViewHistoryBooking);

            }
        }
    }
