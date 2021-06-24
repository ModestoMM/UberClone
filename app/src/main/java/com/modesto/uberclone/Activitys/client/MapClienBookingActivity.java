package com.modesto.uberclone.Activitys.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.L;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.modesto.uberclone.Activitys.MapDriverActivity;
import com.modesto.uberclone.Activitys.driver.MapDriverBookingActivity;
import com.modesto.uberclone.R;
import com.modesto.uberclone.Utils.DecodePoints;
import com.modesto.uberclone.models.ClientBooking;
import com.modesto.uberclone.models.Driver;
import com.modesto.uberclone.providers.AuthProviders;
import com.modesto.uberclone.providers.ClientBookingProvider;
import com.modesto.uberclone.providers.DriverProviders;
import com.modesto.uberclone.providers.GeofireProvider;
import com.modesto.uberclone.providers.GoogleApiProvider;
import com.modesto.uberclone.providers.TokensProvider;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapClienBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProviders mAuthProvider;
    private GeofireProvider mGeoFireProvider;

    private ClientBookingProvider mClientBookingProvider;

    private Marker mMarketDriver;
    private boolean mIsFirstTime = true;

    private PlacesClient mPlaces;

    private String mDestination;
    private LatLng mDestinationLat;

    //Almacenara el nombre del origen o de lugar de recogida del cliente
    private String mOrigen;
    //Este almacenara la latitud y longitud del lugar seleccionado del usuario
    private LatLng mOriginLantLng;


    private TokensProvider mTokensProvider;

    private String midDriver;


    private TextView mTextViewDriverBooking;
    private TextView mTextViewEmailDriverBooking;
    private TextView mTextViewOriginDriverBooking;
    private TextView mTextViewDestinationDriverBooking;
    private TextView mTextViewStausBooking;

    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolilyneList;
    private PolylineOptions mPolilyneOptions;

    //Esta variable global sera la posicion del conductor
    private LatLng mDriverLatLng;

    private DriverProviders mDriverProvider;

    private  ValueEventListener mValueListener;
    private ValueEventListener mListenerStatus;

    private ImageView mImageView;

    private boolean startViaje=false;

    private int actualizarroute=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_clien_booking);

        mAuthProvider = new AuthProviders();

        //Hacemos la referencia de la base de datos en el nodo Drivers_working
        mGeoFireProvider = new GeofireProvider("drivers_working");
        mTokensProvider= new TokensProvider();

        mClientBookingProvider=new ClientBookingProvider();

        mDriverProvider= new DriverProviders();

        mImageView = findViewById(R.id.ImageViewClientBooking);

        //Esta es la instancia para vr el mapa son necesarias hacerlas
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_api));
        }

        mGoogleApiProvider = new GoogleApiProvider(MapClienBookingActivity.this);

        mTextViewDriverBooking= findViewById(R.id.txtViewDriverBooking);
        mTextViewEmailDriverBooking=findViewById(R.id.txtViewEmailDriverBooking);
        mTextViewOriginDriverBooking= findViewById(R.id.txtViewOriginDriverBooking);
        mTextViewDestinationDriverBooking=findViewById(R.id.txtViewDestinationDriverBooking);
        mTextViewStausBooking= findViewById(R.id.txtViewStatusBooking);

        getStatus();
        getClientBooking();
    }

    //Este metodo sera nuestro escuchador para el nodo del apartado status asi verificaremos en tiempo real como ira cambiando
    //y se lo harems saber al cliente con el textView
    private void getStatus(){
       mListenerStatus = mClientBookingProvider.getStatus(mAuthProvider.getIdDriver()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String Status = snapshot.getValue().toString();
                    //Validamos primero en que tipo de estado se encuentra y referente a ese hacemos que lo muestre en el
                    //textView
                    if(Status.equals("accept")){
                        mTextViewStausBooking.setText("Estado: Aceptado");
                    }
                    if(Status.equals("start")){
                        mTextViewStausBooking.setText("Estado: Viaje Iniciado");
                        startBooking();
                    }else if(Status.equals("finish")){
                        mTextViewStausBooking.setText("Estado: Viaje Finalizado");
                        finisBokking();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //ESTE METODO SE LLEVARA ACABO CUANDO EL ESTADO DE CLIENT SEA FINISH
    private void finisBokking() {
        Intent intent= new Intent(MapClienBookingActivity.this,CalificationDriverActivity.class);
        startActivity(intent);
        finish();
    }

    //ESTE METODO SE LLEVARA ACABO CUANDO EL STADO DEL CLIENT SEA START
    private void startBooking() {
        startViaje =true;
        //Vamos a quitar la ruta y el marcador que se trazo
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinationLat).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_green)));
        drawRoute(mDestinationLat);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //LAS VALIDACIONES SON PARA REMOVER EL ESCUCHADOR CUANDO ESTE NO SE NECESITE Y NO SE QUEDE ESCUCHANDO SIEMPRE AUNQUE SE CAMBIE
        //DE PANTALLA
        if(mValueListener!=null){
            mGeoFireProvider.getDriverLocation(midDriver).removeEventListener(mValueListener);
        }
        if(mListenerStatus!=null){
            mClientBookingProvider.getStatus(mAuthProvider.getIdDriver()).removeEventListener(mListenerStatus);
        }
    }

    //Este metodo es el que recibe tdos los parametros del ClientBooking
    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getIdDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String destination= snapshot.child("destination").getValue().toString();
                    String origin= snapshot.child("origin").getValue().toString();
                     String idDriver= snapshot.child("id_Driver").getValue().toString();
                     midDriver=idDriver;
                    double destination_lat= Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destination_lng= Double.parseDouble(snapshot.child("destinationLng").getValue().toString());
                    double origin_lat= Double.parseDouble(snapshot.child("oring_Lat").getValue().toString());
                    double origin_lng= Double.parseDouble(snapshot.child("oring_Lng").getValue().toString());
                    mTextViewOriginDriverBooking.setText("Recoger en: "+origin);
                    mTextViewDestinationDriverBooking.setText("destino: "+destination);
                    mOriginLantLng = new LatLng(origin_lat,origin_lng);
                    mDestinationLat = new LatLng(destination_lat,destination_lng);
                    mMap.addMarker(new MarkerOptions().position(mOriginLantLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

                    getDriverLocation(idDriver);
                    getDriver(idDriver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDriver(String idDriver) {
        mDriverProvider.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String name= snapshot.child("name").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    String image ="";
                    //Aqui verificamos si el cliente actualizo sus dats y puso una imagen ya que si o lo hizo entonces no tendremos
                    //que pedir quesaque la url de ella ya que no existira esa instancia, el haschild pregunta si el nodo del id del
                    //usuario contiene un compo llamado image
                    if(snapshot.hasChild("image")){
                        image = snapshot.child("image").getValue().toString();
                        //Aqui usamos la libreria Picasso aqui recivimos el contexto de ahi el metodo load agarrara la url de la imagen
                        //y al final el metodo into le instanciara esa imagen al ImageView
                        Picasso.with(MapClienBookingActivity.this).load(image).into(mImageView);
                    }
                    mTextViewEmailDriverBooking.setText(email);
                    mTextViewDriverBooking.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDriverLocation(String idDriver) {
        mValueListener = mGeoFireProvider.getDriverLocation(idDriver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    double lat= Double.parseDouble(snapshot.child("0").getValue().toString());
                    double lng= Double.parseDouble(snapshot.child("1").getValue().toString());
                    mDriverLatLng= new LatLng(lat,lng);
                    //se utiliza esta validacion para que no se cree una marca nueva cada vez que se vaya moviendo el usuario
                    //en este caso el conductor
                    if(mMarketDriver!=null){
                        mMarketDriver.remove();
                    }
                    //Instanciamos en el metodo para que se logre ver la imagen
                    mMarketDriver = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                            //Le damos un titulo a la ubicacion
                            .title("Tu conductor")
                            //En esta parte instanciamos la imagen requerida que se encuentra en la carpeta
                            //drawable
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver)));
                    //Esta variable es solo para que entre una vez para trazar la ruta ya que el metodo get driver location
                    //como sera nuestro escucha de la ubicacion real del cliente se estara actualizando en tiempo real
                    if(mIsFirstTime){
                        mIsFirstTime=false;
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        //En el target pondremos la direccion donde queremos que la camara se mueva mas en este caso sera el punto de origen
                                        .target(mDriverLatLng)
                                        .zoom(16f)
                                        .build()
                        ));
                        drawRoute(mOriginLantLng);
                    }
                    actualizarroute++;
                    if(!startViaje){
                        if(actualizarroute >5){
                            actualizarroute=0;
                            mMap.clear();
                            drawRoute(mOriginLantLng);
                            mMap.addMarker(new MarkerOptions().position(mOriginLantLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
                            //Instanciamos en el metodo para que se logre ver la imagen
                            mMarketDriver = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                                    //Le damos un titulo a la ubicacion
                                    .title("Tu conductor")
                                    //En esta parte instanciamos la imagen requerida que se encuentra en la carpeta
                                    //drawable
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver)));
                        }
                    }else{
                        if(actualizarroute >5){
                            actualizarroute=0;
                            mMap.clear();
                            drawRoute(mDestinationLat);
                            //Instanciamos en el metodo para que se logre ver la imagen
                            mMarketDriver = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                                    //Le damos un titulo a la ubicacion
                                    .title("Tu conductor")
                                    //En esta parte instanciamos la imagen requerida que se encuentra en la carpeta
                                    //drawable
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver)));
                            mMap.addMarker(new MarkerOptions().position(mDestinationLat).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_green)));

                        }
                         }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void drawRoute(LatLng latLng){
        mGoogleApiProvider.getDirection(mDriverLatLng,latLng).enqueue(new Callback<String>() {
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



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }

}