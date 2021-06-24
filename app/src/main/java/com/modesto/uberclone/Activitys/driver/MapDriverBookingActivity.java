package com.modesto.uberclone.Activitys.driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.modesto.uberclone.Activitys.MapDriverActivity;
import com.modesto.uberclone.Activitys.client.DetailRequestActivity;
import com.modesto.uberclone.Activitys.client.MapClienBookingActivity;
import com.modesto.uberclone.Activitys.client.RequestDriverActivity;
import com.modesto.uberclone.R;
import com.modesto.uberclone.Utils.DecodePoints;
import com.modesto.uberclone.models.ClientBooking;
import com.modesto.uberclone.models.FCMBody;
import com.modesto.uberclone.models.FCMResponse;
import com.modesto.uberclone.models.Info;
import com.modesto.uberclone.providers.AuthProviders;
import com.modesto.uberclone.providers.ClientBookingProvider;
import com.modesto.uberclone.providers.ClientProvider;
import com.modesto.uberclone.providers.GeofireProvider;
import com.modesto.uberclone.providers.GoogleApiProvider;
import com.modesto.uberclone.providers.InfoProvider;
import com.modesto.uberclone.providers.NotificationProvider;
import com.modesto.uberclone.providers.TokensProvider;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapDriverBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProviders mAuthProvider;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private LatLng mCurrentLatLng;
    private GeofireProvider mGeofireProvider;

    private Boolean mIsCloseToClient=false;



    //Esti es una bandera para saber si deb solicitar los permisos de ubicacion
    private final static int LOCATION_REQUEST_CODE = 1;
    //Variable global para mostrar el Dialog de la clase showAlertDialogNOGPS que se utiliza para mandarte a las configuracones
    //para que enciendas el gps
    private final static int SETTINGS_REQUEST_CODE = 2;

    //Esta propiedad es para utilizar un marcador en nuestra ubicacion a medida de que nos movamos en el mapa
    private Marker mMarket;

    //Estas propiedades son las necesarias para trazar la ruta con el metodo drawroute
    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;
    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolilyneList;
    private PolylineOptions mPolilyneOptions;

    private boolean mIsFirstTime = true;

    private NotificationProvider mNotificationProvider;

    private ImageView mImageView;

    //Variable que usaremos para que cada vez que se mueva las veces necesarias el vehiculo se actualize la linea
    private int actualizarDraw = 0;

    boolean mRideStart = false;
    //Las varuables que usaremos para sacar el tiempo y la distancia
    double mDistanceInMeters=1;
    int mMinutes = 0;
    int mSeconds = 0;
    //Para saber si los segundos ya estan terminados
    boolean mSeconsIsOver=false;
    Handler mHandler = new Handler();
    Location mPreviusLocation = new Location("");

    //Crearemos un cronometro para verificar el tiempo que ha pasado del viaje y en el metodo run correremos nuestro remporizador
    //Tenemos que inicializar nuestro cronometro es por eso que cremamos la variable Handler
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //Incrementamos los segundos de uno en uno por eso usamos la propiedad ++
            mSeconds++;

            //Verificamos cuantos minutos y cuantos segundos han pasado con esta verificacion
            if(!mSeconsIsOver){
                mTextViewTime.setText(mSeconds+" Seg");
            }else{
                mTextViewTime.setText(mMinutes+" Min "+mSeconds+" Seg");
            }

            //Verificamos si los segundos son gual a 59 y si es asi se regresa a cero y se suma uno a los minutos
            if(mSeconds == 59){
                mSeconds=0;
                //Aqui volvemos verdadera la variable booleana ya que ya paso ecxactamente un minuto del contador osea 60 seg
                mSeconsIsOver = true;
                mMinutes++;
            }
            //Recibe el runnable y cada cuanto se va a ir actualizando este proceso en este caso ponemos mil porque es por milisegundos
        mHandler.postDelayed(runnable, 1000);
        }
    };

    //Es el que escucha cuando el usuario se mueve
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                //Hacemos una validacion si el contexto de la aplicacion es diferente de null
                if (getApplicationContext() != null) {
                    //Inicializamos el componente LatLng para guardar la localizacion que se vaya actualizando en el mapa
                    mCurrentLatLng= new LatLng(location.getLatitude(),location.getLongitude());

                    //Hacems una validacion para que no se cree el marcador cada vez que la ubicacion se actualice
                    if(mMarket!=null){
                        mMarket.remove();
                    }

                    //Verificamos si el conductor ya inicio el viaje
                    if(mRideStart){
                        mDistanceInMeters = mDistanceInMeters + mPreviusLocation.distanceTo(location);
                        Log.d("DISTANCIA","DISTANCIA RECORRIDA: "+mDistanceInMeters);
                    }
                    //Inicializamos dentro de locationCallBack la variable de tipo Location llamada mLocationPrevius
                    mPreviusLocation = location;

                    //Instanciamos en el metodo para que se logre ver la imagen
                    mMarket = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                            //Le damos un titulo a la ubicacion
                            .title("Tu posicion")
                            //En esta parte instanciamos la imagen requerida que se encuentra en la carpeta
                            //drawable
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver))
                    );


                    updateLocation();

                    //Con esta validacion nos daremos cuenta si entro por primera vez al loocationCallback y si es asi
                    //mandamos a llamar el metodo de getClientBooking para que trace la ruta ya que dentro de ese metodo
                    //se encuentra el drawRoute
                    if (mIsFirstTime) {
                        mIsFirstTime = false;
                        getClientBooking();
                        // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                        //Esto se encarga de mover la camara hacia nuestra ubicacion actual
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                        .zoom(16f)
                                        .build()
                        ));
                    }

                }
            }
        }
    };

    private TokensProvider mTokensProvider;

    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;
    private TextView mTextViewOriginClientBooking;
    private TextView mTextViewDestinationClientBooking;
    private String mExtraClientId = "";
    private ClientProvider mClientProvider;
    private ClientBookingProvider mClientBookingProvider;

    private TextView mTextViewTime;

    private Button mButtonStartBooking;
    private Button mButtonfinishBooking;

    private InfoProvider mInfoProvider;
    private Info mInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver_booking);

        //Lo instanciamos en el Oncreate
        //Con esta propiedad vamos a poder inciar o detener la ubicacion del usuario cada vez que lo veamos conveniente
        //Pero para lograrlo necesitams optenrr los permisos de ubicacion que nos otorgara el usuario que utilice nuestra aplicacion
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mAuthProvider = new AuthProviders();
        mTokensProvider= new TokensProvider();
        mClientProvider=new ClientProvider();
        mClientBookingProvider=new ClientBookingProvider();
        mGeofireProvider= new GeofireProvider("drivers_working");

        mInfoProvider= new InfoProvider();

        mGoogleApiProvider = new GoogleApiProvider(MapDriverBookingActivity.this);

        mNotificationProvider = new NotificationProvider();

        mImageView = findViewById(R.id.ImageViewClientBooking);

        mButtonStartBooking=findViewById(R.id.btnStartBooking);
        mButtonfinishBooking=findViewById(R.id.btnfinishBooking);

        mTextViewClientBooking= findViewById(R.id.txtViewClientBooking);
        mTextViewEmailClientBooking=findViewById(R.id.txtViewEmailClientBooking);
        mTextViewOriginClientBooking= findViewById(R.id.txtViewOriginClientBooking);
        mTextViewDestinationClientBooking=findViewById(R.id.txtViewDestinationClientBooking);
        mTextViewTime=findViewById(R.id.textViewTime);


        mExtraClientId=getIntent().getStringExtra("idClient");
        //En este metodo optenemos la informacion del cliente
        getClient();

        mButtonStartBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Verificaremos aqui en el setOnClickListener del boton si el driver se encuentra o no cerca de la posicion de
                //recogida
                if(mIsCloseToClient){
                    startBooking();
                }else{
                    Toast.makeText(MapDriverBookingActivity.this, "Debes estar mas cerca a la posicion de recogida", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mButtonfinishBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishBooking();
            }
        });

        getInfo();

    }

    //Metodo para hacer el calculo del precio
    private void CalculatePrice(){
        //Validamos los minutos para que quede por defecto en uno
        if(mMinutes == 0){
            mMinutes = 1;
        }
        //Aqui sacamos el precio de los minutos en mi casa no lo voy a utilizar
        double priceMin = mMinutes * mInfo.getMin();
        //Lo dividimos entre mil para cacular los km
        double pricekm = (mDistanceInMeters / 1000) * mInfo.getKm();
        //double total = priceMin + pricekm;
        final double total = pricekm + priceMin;

        //Elog es para ver los valores del viaje
        Log.d("Valores","Min total: "+mMinutes);
        Log.d("Valores","km total: "+(mDistanceInMeters / 1000));

        //Creamos un metodo en el clientbookingProvider un metodo que nos ayudara a actualizar el precio del viaje
        mClientBookingProvider.updatePrice(mExtraClientId,total).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Hace,s que el metodo de actualizar el precio se ejecute el primero y ya despues cambiamos el estado del viaje a
                //finalizado y mandamos a llamar al intent para cambiar de pantalla o clase
                mClientBookingProvider.updateStatus(mExtraClientId,"finish");
                Intent intent= new Intent(MapDriverBookingActivity.this,CalificationClientActivity.class);
                intent.putExtra("id_client",mExtraClientId);
                startActivity(intent);
                finish();
            }
        });
    }

    //Aqui en este metodo calculamos cuanto costara los kilometros y tambien los minutos
    private void getInfo() {
        //Es el metodo que nos retorna la informacion
        mInfoProvider.getInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //Nos creamos un modelo de tipo info para jalar toda la informacion
                    mInfo= snapshot.getValue(Info.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void finishBooking() {
        mClientBookingProvider.updateIdHistoryBooking(mExtraClientId).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            //Checamos si el metodo se termino de ejecutar correctamente y si es asi mandamos a llamar todo lo demas que se debe ejecutar
                sendNotification("Viaje Finalizado");
                //En este if verificamos si ya no se esta escuchando la ubicacion en tiempo real del driver en este caso
                //si si se esta escuchando osea si es diferente de null lo que haremos es remover la actualizacion de la ubicacion
                //del driver
                if(mFusedLocation != null){
                    mFusedLocation.removeLocationUpdates(mLocationCallback);
                }
                //Activamos el metodo que removera la localizacion guardada en la base de datos
                mGeofireProvider.removeLocation(mAuthProvider.getIdDriver());
                //En esta parte hacemos que deje de escuchar el temporizador si dejamos el temporizador el handler va a estar corriendo
                //constantemente cada segundo sin parar y es importante que lo vaidemos
                if(mHandler != null){
                    mHandler.removeCallbacks(runnable);
                }
                CalculatePrice();
            }
        });
    }

    private void startBooking() {
        mClientBookingProvider.updateStatus(mExtraClientId,"start");
        mButtonStartBooking.setVisibility(View.GONE);
        mButtonfinishBooking.setVisibility(View.VISIBLE);
        //EL CLEAR ES EL ENCARGADO DE BORRAR EL MARCADOR Y LA  RUTA TRAZADA
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_green)));
        drawRoute(mDestinationLatLng);
        sendNotification("Viaje Iniciado");
        mRideStart = true;
        //Aqui inicializamos nuestro contador llamando al metodo postDelayed
        mHandler.postDelayed(runnable, 1000);
    }

    //Este metodo lo utilizaremos para saber la distancia entre la posicion del conductor y la posicion de recorrida del cliente
    private double getDistanceBetween(LatLng clientLatLng, LatLng driverLatLng){
    double distance = 0;
    Location clientLocation = new Location("");
    Location driverLocation = new Location("");
    clientLocation.setLatitude(clientLatLng.latitude);
    clientLocation.setLongitude(clientLatLng.longitude);
    driverLocation.setLatitude(driverLatLng.latitude);
    driverLocation.setLongitude(driverLatLng.longitude);
    //El metodo de DistanceTo se utiliza para conocer la distancia entre un objeto y otro es propio de la clase Location
    distance= clientLocation.distanceTo(driverLocation);
    return  distance;

    }


    //Este metodo es el que recibe tdos los parametros del ClientBooking
    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String destination= snapshot.child("destination").getValue().toString();
                    String origin= snapshot.child("origin").getValue().toString();
                    double destination_lat= Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destination_lng= Double.parseDouble(snapshot.child("destinationLng").getValue().toString());
                    double origin_lat= Double.parseDouble(snapshot.child("oring_Lat").getValue().toString());
                    double origin_lng= Double.parseDouble(snapshot.child("oring_Lng").getValue().toString());
                    mTextViewOriginClientBooking.setText("Recoger en: "+origin);
                    mTextViewDestinationClientBooking.setText("destino: "+destination);
                    mOriginLatLng= new LatLng(origin_lat,origin_lng);
                    mDestinationLatLng= new LatLng(destination_lat,destination_lng);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
                    drawRoute(mOriginLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //AL MANDAR UN PARAMETRO EN EL MOTODO DRAWROUTE LO HACEOS MAS DINAMICO Y LO PODREMOS OCUPAR TAMBIEN PARA MARCAR LA RUTA
    //DE ORIGEN CUANDO EL CONDUCTOR SE ENCUENTRE CERCA
    private void drawRoute(LatLng latLng){
        mGoogleApiProvider.getDirection(mCurrentLatLng,latLng).enqueue(new Callback<String>() {
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


    //Aqui en este metodo mostramos los datos del cliente al conductor
    private void getClient() {
        mClientProvider.getClient(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String email = snapshot.child("email").getValue().toString();
                    String name= snapshot.child("name").getValue().toString();
                    String image = "";
                    //Aqui verificamos si el cliente actualizo sus dats y puso una imagen ya que si o lo hizo entonces no tendremos
                    //que pedir quesaque la url de ella ya que no existira esa instancia, el haschild pregunta si el nodo del id del
                    //usuario contiene un compo llamado image
                    if(snapshot.hasChild("image")){
                        image = snapshot.child("image").getValue().toString();
                        //Aqui usamos la libreria Picasso aqui recivimos el contexto de ahi el metodo load agarrara la url de la imagen
                        //y al final el metodo into le instanciara esa imagen al ImageView
                        Picasso.with(MapDriverBookingActivity.this).load(image).into(mImageView);
                    }
                    mTextViewClientBooking.setText(email);
                    mTextViewEmailClientBooking.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //Aqui ejecutamos los metodos para guardar la localizacion que se encuentran en el GeofireProvider
    //Este metodo se va a estar actuaalizando siempre que se mueva el conductor ya que esta en el
    //mLocationCallBack
    private void updateLocation(){
        //Si la sesion existe y la localizacion en diferente de null se almacenara en la base de datos
        if(mAuthProvider.ExisteSession() && mCurrentLatLng!=null){
            mGeofireProvider.saveLocation(mAuthProvider.getIdDriver(),mCurrentLatLng);
            //Verificamos si el La posicion origen y de conductor es diferete de null
            if(mOriginLatLng != null && mCurrentLatLng != null) {
                //Verificamos en este if si el conductor no esta cerca del cliente que es calcular la distancia en metros
                //con el metodo getDistanceBetween
                if (!mIsCloseToClient) {

                    double distance = getDistanceBetween(mOriginLatLng, mCurrentLatLng); //Se retornara en METROS
                    //ESTA VALIDACION ES PARA VERIFICAR CUANDO EL CONDUCTOR ESTE CERCA EN ESTE CASO A 200 METROS
                    if (distance <= 200) {
                        mIsCloseToClient = true;
                        Toast.makeText(this, "Estas cerca de la posicion de recogida", Toast.LENGTH_SHORT).show();
                    }
                }
                //Hacemos que la variable de control de la actualizacion de la ruta aumente
                actualizarDraw++;
                //Verificamos si se entro o no al boton Start
                if (!mRideStart) {
                    if(actualizarDraw > 5){
                        actualizarDraw=0;
                        mMap.clear();
                        drawRoute(mOriginLatLng);
                        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
                        mMarket = mMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLatLng.latitude, mCurrentLatLng.longitude))
                                //Le damos un titulo a la ubicacion
                                .title("Tu posicion")
                                //En esta parte instanciamos la imagen requerida que se encuentra en la carpeta
                                //drawable
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver)));
                    }

                } else {
                    if(actualizarDraw > 5){
                        actualizarDraw=0;
                        mMap.clear();
                        drawRoute(mDestinationLatLng);
                        //Checar si meter los marcadores en el drawroute
                        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_green)));
                        mMarket = mMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLatLng.latitude, mCurrentLatLng.longitude))
                                //Le damos un titulo a la ubicacion
                                .title("Tu posicion")
                                //En esta parte instanciamos la imagen requerida que se encuentra en la carpeta
                                //drawable
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver)));


                    }

                }
            }}
    }

    //Este metodo tiene que sobre escribirse
    //Aqui inicializamos el mapa de google
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //Esta parte es para controlar el zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //Instanciamos la prpiedad de mLocationRequest
        mLocationRequest = new LocationRequest();
        //Establece los intervalos de tiempo en que se va a estar actualizando la ubicacion del usuario en el mapa
        mLocationRequest.setInterval(1000);
        //Esto es en cuanto tiempo se va actualizar recordar que es en milisegundos
        mLocationRequest.setFastestInterval(1000);
        //Le damos la prioridad que va a tener el gps trabajando en esta aplicacion para hacer uso del gps con la mejor precision posible
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //Establece el desplasamiento mas pequeño
        mLocationRequest.setSmallestDisplacement(5);
        //Asi se crea un marcador en una u otra ruta con el mMaker que es la clase Maker de las bibliotecas implementadas
        //con anterioridad

        startLocation();

    }

    //Este metodo tiene que sobre escribirse se utiliza principalmente para otorgar los permisos del gps
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //El request codigo del metodo lo comparametros con nuestra bandera que tenemos definida como variable global
        //Nuestra bandera es LOCATION_REQUEST_CODE
        if (requestCode == LOCATION_REQUEST_CODE) {
            //En este if consulta si los permisos fueron accesido por el usuario
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //SI FUERON CONCEDIDOS LOS PERISOS DE LA ACTIVIDAD QUE ESTAMOS UTILIZANDO DENTRO DEL MANIFEST
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Verificamos si esta activado el gps
                    if (gpsActived()) {
                        //Checamos la location que tiene el usuario en tiempo real
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        //Esta parte es para conceder el permiso de que ubique nuestra ubicacion actual con un punto
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        //Con esto activamos y desactivamos que se vea el punto de nuestra ubicacion
                        mMap.setMyLocationEnabled(false);
                    } else {
                        //Si no mandamos esta alerta para activarlo
                        showAlertDialogNOGPS();
                    }
                } else {
                    //Llamamos el metodo para que siga pidiendo los permisos de ubicacion
                    checkLocationPermissions();
                }
            } else {
                //Llamamos el metodo para que siga pidiendo los permisos de ubicacion
                checkLocationPermissions();
            }
        }
    }

    //Este metodo tiene que sobre escribirse
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Con este if podemos hacer la valoracion de si el gps esta activado
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            //Este if es el que comprueba la auto permision si si esta dado el permiso se inicializa el escuchador de nuestr ubicacion actual
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //En este apartado actualizamos la ubicacion de la persona
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            //Con esto activamos y desactivamos que se vea el punto de nuestra ubicacion
            mMap.setMyLocationEnabled(false);
        }
        else {
            //Si el gps no esta activado se sigue mostrando la alerta para activarlo
            showAlertDialogNOGPS();
        }
    }

    //Esta es la alerta que se lleva acabo si no se tiene activado el gps y te manda a las configuraciones
    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Te manda directo a las configuraciones para que puedas ensender el gps
                        //Este metodo de startActivityForResult va a estar esperando y escuchando hasta que el usuario active
                        //el gps
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }

    //Este metodo hace la verificacion del si gps esta activado y lo retorna con una variable de ipo boolean
    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Es para verificar si tiene el gps activado
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    //Este metodo es para
    private void disconnect(){
        if(mFusedLocation != null){
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if(mAuthProvider.ExisteSession()){
                //Activamos el metodo que removera la localizacion guardada en la base de datos
                mGeofireProvider.removeLocation(mAuthProvider.getIdDriver());
            }
        }else{
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    //El metodo se utiliza para iniciar el escuchador de nuestra ubicacion
    private void startLocation() {
        //Validamos si la version de android es mayor igual a la version API 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //SI FUERON CONCEDIDOS LOS PERISOS DE LA ACTIVIDAD QUE ESTAMOS UTILIZANDO DENTRO DEL MANIFEST
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Este if es para checar si el gps esta activado
                if (gpsActived()) {
                    //Se solicita la actualizacion de ubicacion es donde se mueve a nuestra ubicacion actual
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    //Esta parte es para conceder el permiso de que ubique nuestra ubicacion actual con un punto
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    //Con esto activamos y desactivamos que se vea el punto de nuestra ubicacion
                    mMap.setMyLocationEnabled(false);
                }
                else {
                    //Si el gps no esta activado muestra una alerta hasta que se active
                    showAlertDialogNOGPS();
                }
            }
            else {
                //Si los permisos de ubicacion no estan activados muestra el Dialog que se encuentra en esta clase es por eso que se
                //inicializa el metodo
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                //Esta parte es para conceder el permiso de que ubique nuestra ubicacion actual con un punto
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                //Con esto activamos y desactivamos que se vea el punto de nuestra ubicacion
                mMap.setMyLocationEnabled(false);
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }

    //Metodo para validar que pasaria si el usuario no acepta los permiso
    private void checkLocationPermissions() {
        //EN ESTE IF SERA LO CONTRARIO QUE EL USUARIO NO CONCEDIERA LOS PERMISOS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Este if para checar si debe mostrar la solicitud de pedir los permisos
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Esta sera la alerta que nos saldra a la hora de pedir los permisos al usuario
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Esta linea habilita los permisos para utilizar la ubicacion del celular
                                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                //Esta linea habilita los permisos para utilizar la ubicacion del celular
                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    //Con este metodo es el encargado de mandar las notificaciones
    private void sendNotification(final String status) {
        //El mTokens Provider localizara al cliente o conductor que reciba la notificacion en este caso lo haremos para mostrar los estados
        //del viaje al cliente
        mTokensProvider.getToken(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            //EL DARASNAPSHOT CONTIENE TODA LA INFORMACION DEL NODO del ID del usuario
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if(datasnapshot.exists()){
                    //Aqui optenemos de la Databse todos los datos del nodo hijo en este caso el Token
                    //Siempre validamos es por eso que hacemos esta validacion para saber si el token se encuentra dentro de la database
                    //Y asi el programa no se cierre por este error
                    String token= datasnapshot.child("token").getValue().toString();
                    //Este map es el encargado de mandar las cosas al data el cual podemos usar dentro de la clase MyFirebaseMessagingClient
                    //Ahi es donde recibe los parametros que mandamos en esta clase
                    Map<String,String> map= new HashMap<>();
                    map.put("title", "ESTADO DE TU VIAJE");
                    map.put("body", "Tu estado del viaje es: "+status);
                    FCMBody fcmBody= new FCMBody(token,"high", "4500s" ,map);
                    //ESTOS METODOS NOS RETORNARA UN OBJETO DE TIPO FCMRESPONSE QUE ERA ÑA RESPUESTA QUE OPTENIAMOS DEL SERVIDOR
                    mNotificationProvider.sedNotification(fcmBody).enqueue(new Callback<FCMResponse>() {

                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            //ESTA VALIDACION ES PARA VERIFICAR SI LLEGO RESPUESTA DEL SERVIDOR
                            if(response.body()!=null){
                                //ESTO QUIERE DECIR QUE NO SE MANDO CORRECTAMENTE SI LA VALIDACION ES CIERTA
                                if(response.body().getSuccess() != 1){
                                    Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        //ESTE METODO ES EN CASO DE QUE FALLE ESTA CONSUKTA HACIA EL SERVIDOR
                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error","Error: "+t.getMessage());
                        }
                    });
                }else{
                    Toast.makeText(MapDriverBookingActivity.this, "No se pudo enviar la notificacion porque el conductor no tiene un token de sesion", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}