package com.modesto.uberclone.Activitys;

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
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.modesto.uberclone.Activitys.adapters.HistoryBookingDriverAdapter;
import com.modesto.uberclone.Activitys.client.HistoryBookingClientActivity;
import com.modesto.uberclone.Activitys.client.UpdateProfileActivity;
import com.modesto.uberclone.Activitys.driver.HistoryBookingDriverActivity;
import com.modesto.uberclone.Activitys.driver.UpdateProfileDriverActivity;
import com.modesto.uberclone.Includes.MyToolbar;
import com.modesto.uberclone.R;
import com.modesto.uberclone.providers.AuthProviders;
import com.modesto.uberclone.providers.DriverProviders;
import com.modesto.uberclone.providers.GeofireProvider;
import com.modesto.uberclone.providers.TokensProvider;

import retrofit2.http.POST;


public class MapDriverActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProviders mAuthProvider;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private LatLng mCurrentLatLng;
    private GeofireProvider mGeofireProvider;


    //Esti es una bandera para saber si deb solicitar los permisos de ubicacion
    private final static int LOCATION_REQUEST_CODE = 1;
    //Variable global para mostrar el Dialog de la clase showAlertDialogNOGPS que se utiliza para mandarte a las configuracones
    //para que enciendas el gps
    private final static int SETTINGS_REQUEST_CODE = 2;

    //Esta propiedad es para utilizar un marcador en nuestra ubicacion a medida de que nos movamos en el mapa
    private Marker mMarket;

    //La utilizaremos para que el e
    private ValueEventListener mListener;

    private ValueEventListener mListenerActivy;


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
                        //Instanciamos en el metodo para que se logre ver la imagen
                        mMarket = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                                //Le damos un titulo a la ubicacion
                                .title("Tu posicion")
                                //En esta parte instanciamos la imagen requerida que se encuentra en la carpeta
                                //drawable
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver))
                        );

                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    //Esto se encarga de mover la camara hacia nuestra ubicacion actual
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));
                    updateLocation();
                }
            }
        }
    };

    Button mButtonConnect;
    private boolean mIsConnect=false;
    private TokensProvider mTokensProvider;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver);
        MyToolbar.show(this, "Conductor", false);

        //Lo instanciamos en el Oncreate
        //Con esta propiedad vamos a poder inciar o detener la ubicacion del usuario cada vez que lo veamos conveniente
        //Pero para lograrlo necesitams optenrr los permisos de ubicacion que nos otorgara el usuario que utilice nuestra aplicacion
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mAuthProvider = new AuthProviders();
        mTokensProvider= new TokensProvider();
        mGeofireProvider= new GeofireProvider("active_drivers");

        mButtonConnect=findViewById(R.id.btnconnect);
        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(mIsConnect){
                if(mListenerActivy != null){
                    mGeofireProvider.getDriverActivity(mAuthProvider.getIdDriver()).removeEventListener(mListenerActivy);
                }
                disconnect();
            }else{
                //Permite la conexion de nuestro dispositivo para otorgar la ubicacion
                startLocation();
            }
            }
        });
        generateToken();
        isDriverWorking();
        isDriverActivity();
    }

    private void isDriverActivity() {
        mListenerActivy=mGeofireProvider.getDriverActivity(mAuthProvider.getIdDriver()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    startLocation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }



    private void isDriverWorking() {
        //Recordatorio el metodo addValueEventListener es el que nos permite escuchar en tiempo real
        mListener = mGeofireProvider.isDriverWorking(mAuthProvider.getIdDriver()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Verificamos si el datasnapshot existe que si se creo el nodo drivers_working
                if(snapshot.exists()){
                    //Se manda a llamar este motodo porque si recuerdas es el que sirve para dejar de escuchar la ubicacion en
                    //tiempo real en nuestra aplicacion y ademas elimina la referencia al nodo en active_drivers
                    if(mFusedLocation != null){
                        mButtonConnect.setText("Conectarse");
                        mIsConnect=false;
                        mFusedLocation.removeLocationUpdates(mLocationCallback);
                            //Activamos el metodo que removera la localizacion guardada en la base de datos
                            mGeofireProvider.removeLocation(mAuthProvider.getIdDriver());
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //Aqui ejecutamos los metodos para guardar la localizacion que se encuentran en el GeofireProvider
    private void updateLocation(){
        //Si la sesion existe y la localizacion en diferente de null se almacenara en la base de datos
        if(mAuthProvider.ExisteSession() && mCurrentLatLng!=null){
            mGeofireProvider.saveLocation(mAuthProvider.getIdDriver(),mCurrentLatLng);
        }
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

    //Este metodo es para desconectar la localizacion y evitar que se actualiz¿ce en el localCallBack
    public void disconnect(){
        if(mFusedLocation != null){
            mButtonConnect.setText("Conectarse");
            mIsConnect=false;
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
                    //Con esto cambiamos el texto del botom dando a entender que la coneccion esta activada y se puede desactivar
                    mButtonConnect.setText("Desconectarse");
                    mIsConnect=true;
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
                                ActivityCompat.requestPermissions(MapDriverActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                //Esta linea habilita los permisos para utilizar la ubicacion del celular
                ActivityCompat.requestPermissions(MapDriverActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drivermenu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_Logout) {
            if (mListener != null) {
                mGeofireProvider.isDriverWorking(mAuthProvider.getIdDriver()).removeEventListener(mListener);
            }
            if(mListenerActivy != null){
                mGeofireProvider.getDriverActivity(mAuthProvider.getIdDriver()).removeEventListener(mListenerActivy);
            }
            logout();
        }
        if (item.getItemId() == R.id.action_update) {

                disconnect();

            Intent intent = new Intent(MapDriverActivity.this, UpdateProfileDriverActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.action_history) {

            Intent intent = new Intent(MapDriverActivity.this, HistoryBookingDriverActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    void logout() {
        disconnect();
        mAuthProvider.logout();
        Intent intent = new Intent(MapDriverActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

   void generateToken() {
        //El meodo e getiddriver es para conseguir el id del usuario no solo de los que son conductores
        mTokensProvider.Create(mAuthProvider.getIdDriver());
    }
}
