package com.modesto.uberclone.Activitys.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.modesto.uberclone.Includes.MyToolbar;
import com.modesto.uberclone.R;
import com.modesto.uberclone.Utils.DecodePoints;
import com.modesto.uberclone.models.Info;
import com.modesto.uberclone.providers.DriverProviders;
import com.modesto.uberclone.providers.GoogleApiProvider;
import com.modesto.uberclone.providers.InfoProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//Esta clase la utilizaremos para poder llevar acabo la solicitud de que se necesita un vehiculo teniendo como parametros
//El destino y el punto de origen
public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    //En las 4 variables proximas vamos a cargar los datos que se envian de la pantalla MapClientActivity que en si seria
    //la latitud y longitud del punto de origen y el punto destino
    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinatiLat;
    private double mExtraDestinatiLng;
    private String mExtraOrigen;
    private String mExtraDestination;


    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolilyneList;
    private PolylineOptions mPolilyneOptions;

    private TextView txtViewOrigin;
    private TextView txtViewDestination;
    private TextView txtViewTime;
    private TextView txtViewPrice;

    private InfoProvider mInfoProvider;

    private Button mButtonRequest;

    private CircleImageView mCircleImageBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);

        //MyToolbar.show(this,"Tus Datos",true);

        //Esta es la instancia para vr el mapa son necesarias hacerlas
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        //Aqui obtenemos los parametros
        mExtraOriginLat=getIntent().getDoubleExtra("origin_lat",0);
        mExtraOriginLng=getIntent().getDoubleExtra("origin_lng",0);
        mExtraDestinatiLat=getIntent().getDoubleExtra("destination_lat",0);
        mExtraDestinatiLng=getIntent().getDoubleExtra("destination_lng",0);
        mExtraOrigen=getIntent().getStringExtra("origin");
        mExtraDestination=getIntent().getStringExtra("destination");

        //iNICIALIZAMOS LAS VARIABLES Y MANDAMOS COMO PARAMETROS LA LATITUD Y LONGITUD A LA VARIABLE INICIALIZADA EN LATLNG
        mOriginLatLng=new LatLng(mExtraOriginLat,mExtraOriginLng);
        mDestinationLatLng=new LatLng(mExtraDestinatiLat,mExtraDestinatiLng);

        mInfoProvider = new InfoProvider();

        //Instanciamos la clase GoogleApiProvider y le pasamos el contexto que seria esta clase
        mGoogleApiProvider = new GoogleApiProvider(DetailRequestActivity.this);

        txtViewOrigin=findViewById(R.id.textViewOrigin);
        txtViewDestination=findViewById(R.id.textViewDestination);
        txtViewPrice=findViewById(R.id.textViewPrice);
        txtViewTime=findViewById(R.id.textViewTime);

        txtViewOrigin.setText(mExtraOrigen);
        txtViewDestination.setText(mExtraDestination);

        mButtonRequest=findViewById(R.id.btnRequestNow);
        mButtonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRequestDriver();
            }
        });

        mCircleImageBack= findViewById(R.id.CircleImageViewBack);

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Llamamos solo a este metodo finish ya que este terminara la actividad en la que nos encontramos
                finish();
            }
        });

    }

    private void goToRequestDriver() {
        Intent intent=new Intent(DetailRequestActivity.this, RequestDriverActivity.class);
        intent.putExtra("origin_Lat",mOriginLatLng.latitude);
        intent.putExtra("origin_Lng",mOriginLatLng.longitude);
        intent.putExtra("origin",mExtraOrigen);
        intent.putExtra("destination",mExtraDestination);
        intent.putExtra("destination_lat",mDestinationLatLng.latitude);
        intent.putExtra("destination_lng",mDestinationLatLng.longitude);
        //Con este metodo cierra la actividad
        finish();
        startActivity(intent);
    }

    private void drawRoute(){
        mGoogleApiProvider.getDirection(mOriginLatLng,mDestinationLatLng).enqueue(new Callback<String>() {
            //En OnResponse vamos a estar resiviendo la respuesta de nuestro servidor
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
            try{

                JSONObject jsonObject= new JSONObject(response.body());
                //El extring que resive e het JsonArray es el String de a que propiedad se va apuntar y en este caso optendremos la ruta
                //Asi que sera el que dice routes ya que este es el que contiene informacion necesaria para trasar las rutas
                JSONArray jsonArray=jsonObject.getJSONArray("routes");
                //Ya que queremos optener todos los datos de la ruta que marcaremos con la aplicacion
                JSONObject route = jsonArray.getJSONObject(0);
                //Esta instancia la utilizaremos para optener los poligonos necesarios para trazar la ruta
                JSONObject polylines= route.getJSONObject("overview_polyline");
                //Con esta instancia vamos a optener el parametro de tipo String de los puntos
                //Elparametro que se le pasa es porque asi se llama la propiedad de JSON
                String points=polylines.getString("points");
                //Aqui decodificamos la lista
                mPolilyneList= DecodePoints.decodePoly(points);
                mPolilyneOptions=new PolylineOptions();
                //Establecemos un color para la linea que se reflejara en nuestra pantalla
                mPolilyneOptions.color(Color.DKGRAY);
                //Establecemos el ancho que tendra la linea reflejada
                mPolilyneOptions.width(13f);
                mPolilyneOptions.startCap(new SquareCap());
                mPolilyneOptions.jointType(JointType.ROUND);
                mPolilyneOptions.addAll(mPolilyneList);
                mMap.addPolyline(mPolilyneOptions);

                //Asi optenemos el array Legs
                JSONArray legs=route.getJSONArray("legs");
                //Aqui definimos que queremos que empiece desde la posicion 0 agarrar el array
                JSONObject leg= legs.getJSONObject(0);
                //Aqui agarramos el apartado de la distancia
                JSONObject distance=leg.getJSONObject("distance");
                //Este es para el de duracion
                JSONObject duration_in_traffic= leg.getJSONObject("duration");
                //Opteniendo e texto de la distancia y el tiempo
                String distancetext= distance.getString("text");
                String duratiocetext= duration_in_traffic.getString("text");
                //Asignamos el tiempo y duracion al textView
                txtViewTime.setText(duratiocetext + " " + distancetext);

                // Lo que hacemos es dividri por un split osea por un espacio los km para asi solo traer el digito
                //Va a recibir por que caracter queremos dividirlo y sera por un espacio ya que solo queremos el valor
                //y despues del espacio esta el texto en km
                String [] distanceAndKm = distancetext.split(" ");
                //aqui optenemos el valor como double
                double distanceValue = Double.parseDouble(distanceAndKm[0]);

                // Lo que hacemos es dividri por un split osea por un espacio los minutos para traer el digito en este caso
                //Va a recibir por que caracter queremos dividirlo y sera por un espacio ya que solo queremos el valor
                //y despues del espacio esta el texto en minutos en esta caso
                String [] durationAndMins = duratiocetext.split(" ");
                //aqui optenemos el valor como double
                double durationValue = Double.parseDouble(durationAndMins[0]);

                calculatePrice(distanceValue,durationValue);

            }catch (Exception e){
                Log.d("Error","Error encontrado"+e.getMessage());
            }
            }


            //Este se ejecutara en caso de que falle nuestra peticin al servidor
            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    //Aqui en este metodo calculamos cuanto costara los kilometros y tambien los minutos
    private void calculatePrice(final double distanceValue, final double durationValue) {
        //Es el metodo que nos retorna la informacion
        mInfoProvider.getInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //Nos creamos un modelo de tipo info para jalar toda la informacion
                    Info info= snapshot.getValue(Info.class);
                    double totalDistance = distanceValue * info.getKm();
                    //En mi caso yo no utilizare los minutos para cobrarasi que solo hare el calculo con los km y tambien
                    //no pondre el minimo y maximo del precio
                    //double totalDuration = durationValue * info.getMin();
                    double total= totalDistance;
                    //Sacamos el valor aproximado simplemente descontandole o aumentandole 50 centavos para decir lo minimo
                    //y maximo que se le puede cobrar por el viaje
                    double mintotal = total - 0.5;
                    double maxtotal = total + 0.5;
                    txtViewPrice.setText("$"+total);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //Añadimos un marcador
        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_green)));
        //Con este metodo hacemos que se mueva la camara a otra posicion
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                //En el tarjet pondremos la direccion donde queremos que la camara se mueva mas en este caso sera el punto de origen
                .target(mOriginLatLng)
                .zoom(14f)
                .build()
        ));
        drawRoute();
    }
}