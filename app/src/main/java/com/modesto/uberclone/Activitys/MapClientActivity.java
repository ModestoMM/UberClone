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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;
import com.modesto.uberclone.Activitys.client.DetailRequestActivity;
import com.modesto.uberclone.Activitys.client.HistoryBookingClientActivity;
import com.modesto.uberclone.Activitys.client.UpdateProfileActivity;
import com.modesto.uberclone.Includes.MyToolbar;
import com.modesto.uberclone.R;
import com.modesto.uberclone.providers.AuthProviders;
import com.modesto.uberclone.providers.GeofireProvider;
import com.modesto.uberclone.providers.TokensProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapClientActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProviders mAuthProvider;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private GeofireProvider mGeoFireProvider;
    private LatLng mCurrentLatLng;
    private List<Marker> mDriversMarket = new ArrayList<>();
    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;
    private Marker mMarket;
    private boolean mIsFirstTime = true;
    private AutocompleteSupportFragment mAutoComplete;
    private AutocompleteSupportFragment mAutocompleteDestinoo;
    private PlacesClient mPlaces;

    private String mDestination;
    private LatLng mDestinationLat;

    private GoogleMap.OnCameraIdleListener mCameraListener;
    //Almacenara el nombre del origen o de lugar de recogida del cliente
    private String mOrigen;
    //Este almacenara la latitud y longitud del lugar seleccionado del usuario
    private LatLng mOriginLantLng;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {


                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    /*
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
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_users))
                    );*/

                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f)
                                    .build()
                    ));
                    if (mIsFirstTime) {
                        mIsFirstTime = false;
                        limiteSearch();
                    }
                    getActiveDriver();
                }
            }
        }
    };

    Button mButtonRequestDriver;

   private TokensProvider mTokensProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client);
        MyToolbar.show(this, "Cliente", false);

        mAuthProvider = new AuthProviders();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mGeoFireProvider = new GeofireProvider("active_drivers");
        mTokensProvider= new TokensProvider();
        mButtonRequestDriver=findViewById(R.id.btnRequestDriver);

        //Esta es la instancia para vr el mapa son necesarias hacerlas
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_api));
        }

        mPlaces = Places.createClient(this);
        instanceAutocompleteOrigin();
        instanceAutocompleteDestination();
        onCameraMove();
        //Este onclick listener lo utilizaremos para que al precionar el boton pueda diseñar la solicitud del viaje del destino y origen
        mButtonRequestDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDriver();
            }
        });
        generateToken();
    }

    private void requestDriver() {
        //Verificamos si el LantLng del origen y destino no estan vacio ya que eso significa que ya tenemos la referencia del punto de
        //partida y el de llegada
        if(mOriginLantLng != null && mDestinationLat != null){
            Intent intent=new Intent(MapClientActivity.this, DetailRequestActivity.class);
            intent.putExtra("origin_lat",mOriginLantLng.latitude);
            intent.putExtra("origin_lng",mOriginLantLng.longitude);
            intent.putExtra("destination_lat",mDestinationLat.latitude);
            intent.putExtra("destination_lng",mDestinationLat.longitude);
            intent.putExtra("origin",mOrigen);
            intent.putExtra("destination",mDestination);
            startActivity(intent);
        }else{
            Toast.makeText(this, "Debe seleccionar el lugar de recogida y el destino", Toast.LENGTH_LONG).show();
        }
    }

    private void limiteSearch(){
        //Especificamos en el segundo parametro la distancia al darle 5000 son 5 km
        LatLng nothSide= SphericalUtil.computeOffset(mCurrentLatLng,5000,0);
        LatLng southSide= SphericalUtil.computeOffset(mCurrentLatLng,5000,180);
        mAutoComplete.setCountry("MEX");
        mAutoComplete.setLocationBias(RectangularBounds.newInstance(southSide,nothSide));
        mAutocompleteDestinoo.setCountry("MEX");
        mAutocompleteDestinoo.setLocationBias(RectangularBounds.newInstance(southSide,nothSide));
    }

    private void instanceAutocompleteOrigin(){
        mAutoComplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placesAutocompleteOrigin);
        mAutoComplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        //Poner una representacion de lo que hara
        mAutoComplete.setHint("Lugar de recogida");
        mAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigen = place.getName();
                mOriginLantLng = place.getLatLng();
                Log.d("PLACE", "Name: " + mOrigen);
                Log.d("PLACE", "Lat: " + mOriginLantLng.latitude);
                Log.d("PLACE", "Lng: " + mOriginLantLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

    }

    private void instanceAutocompleteDestination(){
        mAutocompleteDestinoo = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placesAutocompleteDestination);
        mAutocompleteDestinoo.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        //Poner una representacion de lo que hara
        mAutocompleteDestinoo.setHint("Destino");
        mAutocompleteDestinoo.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mDestination = place.getName();
                mDestinationLat = place.getLatLng();
                Log.d("PLACE", "Name: " + mDestination);
                Log.d("PLACE", "Lat: " + mDestinationLat.latitude);
                Log.d("PLACE", "Lng: " + mDestinationLat.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

    }

    private void onCameraMove(){
        //El metodo es cuando el usuario cambie la posicion de la camara en el mapa
        mCameraListener=new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try{
                    Geocoder geocoder= new Geocoder(MapClientActivity.this);
                    mOriginLantLng=mMap.getCameraPosition().target;
                    List<Address> addressList= geocoder.getFromLocation(mOriginLantLng.latitude,mOriginLantLng.longitude,1);
                    String city= addressList.get(0).getLocality();
                    String country=addressList.get(0).getCountryName();
                    String addres=  addressList.get(0).getAddressLine(0);
                    mOrigen= addres+" "+city;
                    mAutoComplete.setText(addres+" "+city);

                }catch(Exception e){
                    Log.d("Error:","Mensaje error: "+e.getMessage());
                }
            }};
    }

    private void getActiveDriver() {
        //Recibira la ubicacion actual del cliente
        mGeoFireProvider.getActiveDrivers(mCurrentLatLng,10).addGeoQueryEventListener(new GeoQueryEventListener() {

            //El metodo donde nosotros añadiremos los marcadores de los conductores que se vayan conectando
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for (Marker marker : mDriversMarket) {
                    if (marker.getTag() != null) {
                        //El key viene de la Base de Datos de Firebase cuando se conecte un conductor
                        if (marker.getTag().equals(key)) {
                            return;
                        }
                    }
                }
                //Esta almacenara la posicion del conductor que se conecto
                LatLng driverLatLng = new LatLng(location.latitude, location.longitude);
                //Creamos un Marcador con la posicion del conductor que tenemos previamente en driverLatLng
                Marker marker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Conductor Disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_driver)));
                //El tag sera el ID del conductor que lo optendremos de la propiedad key
                marker.setTag(key);
                //Con esto añadimos el marcador a la lista de marcadores
                mDriversMarket.add(marker);
            }

            //Eliminaremos los marcadores de los conductores que se desconectan de la aplicacion
            @Override
            public void onKeyExited(String key) {
                for (Marker marker : mDriversMarket) {
                    if (marker.getTag() != null) {
                        //El key viene de la Base de Datos de Firebase cuando se conecte un conductor
                        if (marker.getTag().equals(key)) {
                            marker.remove();
                            mDriversMarket.remove(marker);
                            return;
                        }
                    }
                }
            }

            //Vamo a ir actualizando en tiempo real la posicion del conductor a medida que este se mueve
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker marker : mDriversMarket) {
                    if (marker.getTag() != null) {
                        //El key viene de la Base de Datos de Firebase cuando se conecte un conductor
                        if (marker.getTag().equals(key)) {
                            //Hacemos que se establesca en una nueva posiscion con el siguiente metotdo y recibe un LatLong
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }
                }

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraIdleListener(mCameraListener);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    } else {
                        showAlertDialogNOGPS();
                    }
                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }
        else if(requestCode == SETTINGS_REQUEST_CODE && !gpsActived()) {
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MapClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    //Sobre escribimos este metodo para inicializar el menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //SOBRE ESCRIBIMOS ESTE METODO PARA INSTANCIAR LO QUE HARA EL MENU
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_Logout) {
            logout();
        }
        if (item.getItemId() == R.id.action_update) {
            Intent intent = new Intent(MapClientActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.action_history) {
            Intent intent = new Intent(MapClientActivity.this, HistoryBookingClientActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    void logout() {
        //Este llamado es el que hace el cierre de sesion
        mAuthProvider.logout();
        Intent intent = new Intent(MapClientActivity.this, MainActivity.class);
        startActivity(intent);
        //Finalizamos la actividad
        finish();
    }

    //Este es el metodo que ayudara a generar un token por cliente
    void generateToken(){
        //El meodo e getiddriver es para conseguir el id del usuario no solo de los que son conductores
        mTokensProvider.Create(mAuthProvider.getIdDriver());
    }
}