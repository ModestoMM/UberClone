package com.modesto.uberclone.Activitys.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.modesto.uberclone.R;
import com.modesto.uberclone.models.HistoryBooking;
import com.modesto.uberclone.providers.DriverProviders;
import com.modesto.uberclone.providers.HistoryBookingProvider;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryBookingDetailClientActivity extends AppCompatActivity {

    private TextView mTextViewName;
    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewYourCalification;
    private RatingBar mRatingBarCalification;
    private CircleImageView mCircleImage;
    private CircleImageView mCircleImageBack;

    private String mExtraId;

    private HistoryBookingProvider mHistoryBookingProvider;

    private DriverProviders mDriverProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detail_client);
        mTextViewName= findViewById(R.id.TextViewNameBookingDeail);
        mTextViewOrigin= findViewById(R.id.textViewOriginHistoryBookingDetail);
        mTextViewDestination= findViewById(R.id.textViewDestinationHistoryBookingDetail);
        mTextViewYourCalification= findViewById(R.id.textViewCalificationHistoryBookingDetail);
        mRatingBarCalification= findViewById(R.id.ratingBarHistoryBookingDetail);
        mCircleImage= findViewById(R.id.CircleImageHistoryBookingDetail);

        mDriverProvider= new DriverProviders();

        mHistoryBookingProvider = new HistoryBookingProvider();

        mCircleImageBack= findViewById(R.id.CircleImageViewBack);
        //Recolectando el id
        mExtraId = getIntent().getStringExtra("idHistoryBooking");
        getHistoryBooking();

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Llamamos solo a este metodo finish ya que este terminara la actividad en la que nos encontramos
               finish();
            }
        });
    }

    private void getHistoryBooking() {
        mHistoryBookingProvider.getHistoryBooking(mExtraId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //Asi recuperamos toda la informacion de la base de datos del id del hsitorybooking
                  HistoryBooking historyBooking= snapshot.getValue(HistoryBooking.class);

                  if(snapshot.hasChild("origin")){
                      mTextViewOrigin.setText(historyBooking.getOrigin());
                  }

                  if(snapshot.hasChild("destination")){
                      mTextViewDestination.setText(historyBooking.getDestination());
                  }

                  if(snapshot.hasChild("calificationClient"))
                  mTextViewYourCalification.setText("Tu calificacion: "+historyBooking.getCalificationDriver());

                  //Vamos a verificar si tiene esa calificacion
                  if(snapshot.hasChild("calificationDriver")){
                      mRatingBarCalification.setRating((float) historyBooking.getCalificationClient());
                  }
                    //con esto saca toda la informacion de la clase a la que estamos apuntando en este caso al id del conductor para sacar su
                    //nombre y la imagen de este
                    mDriverProvider.getDriver(historyBooking.getId_Driver()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String name =  snapshot.child("name").getValue().toString();
                                //Con el toUpperCase convierte todas las letras a mayuscula
                                mTextViewName.setText(name.toUpperCase());
                                if(snapshot.hasChild("image")){
                                    String image =  snapshot.child("image").getValue().toString();
                                    Picasso.with(HistoryBookingDetailClientActivity.this).load(image).into(mCircleImage);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}