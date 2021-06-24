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
import com.modesto.uberclone.R;
import com.modesto.uberclone.models.HistoryBooking;
import com.modesto.uberclone.providers.DriverProviders;
import com.squareup.picasso.Picasso;

//EN ESTA CLASE TE PIDE EL MODELO CON EL QUE VAS A TRABAJAR COMO EN ESTE CASO MOSTRAREMOS EL HISTORIAL DE VIAJE Y CALIFICACION
//DE CLIENTES Y DRIVER TODO ESO SE ENCUENTRA O MEJOR DICHO LA BASE DE NUESTRA MODELO CON EL CUAL DESPUES REFERENCIAMOS A LA BASE DE
//DATOS ES EL HISTORYBOOKING Y TAMBIEN NECESITARA ALGO QUE ES UN VIEWHOLDER QUE CREAREMOS EN LA MISMA CLASE
public class HistoryBookingClientAdapter extends FirebaseRecyclerAdapter<HistoryBooking, HistoryBookingClientAdapter.ViewHolder> {

    private DriverProviders mDriverProvider;
    private Context mContext;

    //Esta seria practicamente la configuracion que nosotros debemos enviar para mostrar la informacion almacena en firebasedatabase
    public HistoryBookingClientAdapter(FirebaseRecyclerOptions<HistoryBooking> option,Context context){
        super(option);
        mDriverProvider = new DriverProviders();
        mContext= context;
    }

    //EN ESTE METODO ESTABLECEREMOS LOS VALORES DE LAS VISTAS QUE DESARROLLAMOS EN NUESTRA TARJETA
    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull HistoryBooking historyBooking) {

        //Asi conseguimos el id del client booking Histori el cada uno
        final String id =getRef(position).getKey();

        //El objeto llamado holder nos permitira acceseder a cada uno de los campos establecidos de nuestro view holder
        //para optener el origen vamos a usar el parametro de modelo que en este caso es de historybooking le cambiamos de name
        holder.mTextViewOrigin.setText(historyBooking.getOrigin());
        holder.mTextViewDestination.setText(historyBooking.getDestination());
        holder.mTextViewCalification.setText(String.valueOf(historyBooking.getCalificationClient()));
        //con esto saca toda la informacion de la clase a la que estamos apuntando en este caso al id del conductor para sacar su
        //nombre y la imagen de este
        mDriverProvider.getDriver(historyBooking.getId_Driver()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                Intent intent = new Intent(mContext, HistoryBookingDetailClientActivity.class);
                //Pasaos de parametro el id del historial del viaje para usarlo en la clase HistoryBookingDetailClientActivity
                intent.putExtra("idHistoryBooking",id);
                mContext.startActivity(intent);
            }
        });

    }

    //AQUI ESTANCIAREMOS EL LAYOUT QUE VAMOS A UTILIZAR  QUE EN ESTE CASO ES EL card_history_booking.xml
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
            mView=view;
            //En este caso para instanciarlo no podemos llamar a findviewbyid ya que no estamos en dentro de una actividad asi
            //que utilizaremos el objeto View y asi podremos accceder al metodo findviewbyid
            mTextViewName = view.findViewById(R.id.textViewName);
            mTextViewOrigin = view.findViewById(R.id.textViewOrigen);
            mTextViewDestination = view.findViewById(R.id.textViewDestination);
            mTextViewCalification = view.findViewById(R.id.textViewCalificacion);

            imageViewHistoryBooking = view.findViewById(R.id.ImageViewHistoryBooking);

        }
    }
}
