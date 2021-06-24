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
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.modesto.uberclone.Activitys.MapClientActivity;
import com.modesto.uberclone.R;
import com.modesto.uberclone.Utils.DecodePoints;
import com.modesto.uberclone.models.ClientBooking;
import com.modesto.uberclone.models.FCMBody;
import com.modesto.uberclone.models.FCMResponse;
import com.modesto.uberclone.providers.AuthProviders;
import com.modesto.uberclone.providers.ClientBookingProvider;
import com.modesto.uberclone.providers.GeofireProvider;
import com.modesto.uberclone.providers.GoogleApiProvider;
import com.modesto.uberclone.providers.NotificationProvider;
import com.modesto.uberclone.providers.TokensProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestDriverActivity extends AppCompatActivity {

    private LottieAnimationView mlottieAnimationView;
    private TextView txtViewLookingFor;
    private Button btnCancelRequest;
    private GeofireProvider mGeofireProvider;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private String mExtraOrigin;
    private String mExtraDestination;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private double mRadius=0.1;
    private boolean mDriverFound=false;
    private  String mIdDriverFound="";
    private LatLng mDriverFoundLatLng;
    private NotificationProvider mNotificationProvider;
    private TokensProvider mTokenProvider;
    private ClientBookingProvider mClientBookingProvider;
    private AuthProviders mAuthProvider;

    private  ValueEventListener mListener;



    private GoogleApiProvider mGoogleApiProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);
        mlottieAnimationView=findViewById(R.id.animation);
        txtViewLookingFor=findViewById(R.id.textViewLookingFor);
        btnCancelRequest=findViewById(R.id.btnCancelRequest);

        mlottieAnimationView.playAnimation();

        mGeofireProvider=new GeofireProvider("active_drivers");

        mNotificationProvider = new NotificationProvider();
        mTokenProvider= new TokensProvider();

        //Aqui recibimos el origen, destino y la latitud y longitud del destino para mandarlas como parametros en el metodo
        //CreateclientBooking
        mExtraOrigin=getIntent().getStringExtra("origin");
        mExtraDestination=getIntent().getStringExtra("destination");
        mExtraDestinationLat=getIntent().getDoubleExtra("destination_lat",0);
        mExtraDestinationLng=getIntent().getDoubleExtra("destination_lng",0);
        mDestinationLatLng= new LatLng(mExtraDestinationLat,mExtraDestinationLng);

        //Recibimos el parametro de latitud y longitud del punto de origen
        mExtraOriginLat=getIntent().getDoubleExtra("origin_Lat",0);
        mExtraOriginLng=getIntent().getDoubleExtra("origin_Lng",0);
        mOriginLatLng=new LatLng(mExtraOriginLat,mExtraOriginLng);
        //Instanciamos el provider de clientBooking
        mClientBookingProvider= new ClientBookingProvider();
        //Instanciamos el provider porque como sabemos aqui se encuentra el id de la sesion en este caso del cliente
        mAuthProvider = new AuthProviders();

        //Instancaiamos el GoogleApiProvider porque ahi es donde encontramos los datos de la distancia y el tiempo
        mGoogleApiProvider = new GoogleApiProvider(RequestDriverActivity.this);

        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDriverFound){
                    cancelRequest();
                }else{
                   Intent intent= new Intent(RequestDriverActivity.this, MapClientActivity.class);
                    finish();
                   startActivity(intent);
                }

            }
        });

        getClosestDriver();


    }

    private void cancelRequest() {

        mClientBookingProvider.delete(mAuthProvider.getIdDriver()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Agregamos este metodo que mandara la notificacion al conductor de que el viaje a sido cancelado
                sendNotificationCancel();
            }
        });

    }

    private void getClosestDriver(){
        //lLAMAMOS AL OBJETO Y UTILIZAMOS EL MEODO GETACTIVEDRIVER EL CUAL NOS MANDARA EL PARAMETRO DE LOS CONDUCTORES ACTIVOS EN UN CIERTO RANGO
        mGeofireProvider.getActiveDrivers(mOriginLatLng,mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
            //SE EJECUTA CUANDO ENCUENTRA UNA LOCALIZACION EN ESTE CASO CUANDO ENCUENTRA UN CONDUCTOR DISPONIBLE
           //DEVOLVIENDONIOS LA LLAVE DEL USUARIO QUE EN ESTE CASO SERIA SU ID QUE SE ENCUENTRA EN LA DATABASEQUE TENEMOS EN LA FIREBASE
            //Y NOS DEVOLVERA EL OBJETO LOCATION QUE SERA LA LATITUD Y LONGITUD DONDE SE ENCUENTRA EL CONDUCTOR
                if(!mDriverFound){
                    mDriverFound=true;
                    mIdDriverFound =key;
                    mDriverFoundLatLng=new LatLng(location.latitude,location.longitude);
                    txtViewLookingFor.setText("CONDUCTOR ENCONTRADO A\nESPERANDO RESPUESTA");
                    //Este metodo se utiliza para mandar los parmetros mas especificos del cliente y conductor
                    //es por ello que lo ejecutamos al encontrar al conductor mas cercan porque dentro de el se encuentra
                    //el metodo el cual se encarga de utilizar los token para encontrar el conductor mas cercan y asi mandar
                    //la notificacion a este no es muy dificil de entender solo hay que observar bien el codigo....
                    createClientBooking();

                    Log.d("DRIVER","ID: "+mIdDriverFound);
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //SE EJECUTA CUANDO SE TERMINA DE REALIZAR ESTA TAREA ES DECIR SI EJECUTAMOS ESTE METODO EMPEZARA A BUSCAR LOS CONDUCTORES MAS CERCANOS
                //EN UN RADIO DE 0.1 KM Y CUANDO FINALICE LA BUSQUEDA EN ESE RADIO ENTRARA EN EL METODO ONGEOQUERYREADY
                if(!mDriverFound){
                    mRadius=mRadius+0.1f;
                }
                //NO ENCONTRO NINGUN CONDUCTOR SI NO SE PONE ESTA CONDICION SEGUIRIA BUSCANDO CONTANTE MENTE ES UN TOPE
                if(mRadius > 10){
                    txtViewLookingFor.setText("NO SE ENCONTRO UN CONDUCTOR");
                    Toast.makeText(RequestDriverActivity.this, "NO SE ENCONTRO UN CONDUCTOR", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    getClosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    //Este metodo sera el encargado de dar los parametros necesarios al cliente
    private void createClientBooking(){

        //En este caso hacemos que calcula la distancia entre el punto de origen y el destino pero del conductor mas cercano
        mGoogleApiProvider.getDirection(mOriginLatLng,mDriverFoundLatLng).enqueue(new Callback<String>() {
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

                    //MANDAR LA NOTIFICACION DE PEDIDO O VIAJE MEJOR DICHO
                    sendNotification(duratiocetext,distancetext);

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

    //Recibe de parametro el tiempo y la distancia con el fin de hacer una notificacion mas personalizada
    private void sendNotificationCancel() {
        mTokenProvider.getToken(mIdDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    map.put("title", "VIAJE CANCELADO");
                    map.put("body", "El cliente cancelo la solicitud");

                    //El valor de ttl lo establece por defecto en 4500s asi que lo dejamos con ese valor
                    FCMBody fcmBody= new FCMBody(token,"high", "4500s" ,map);
                    //ESTOS METODOS NOS RETORNARA UN OBJETO DE TIPO FCMRESPONSE QUE ERA ÑA RESPUESTA QUE OPTENIAMOS DEL SERVIDOR
                    mNotificationProvider.sedNotification(fcmBody).enqueue(new Callback<FCMResponse>() {

                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            //ESTA VALIDACION ES PARA VERIFICAR SI LLEGO RESPUESTA DEL SERVIDOR
                            if(response.body()!=null){
                                //ESTO QUIERE DECIR QUE SE MANDO CORRECTAMENTE SI LA VALIDACION ES CIERTA
                                if(response.body().getSuccess() == 1){
                                    Toast.makeText(RequestDriverActivity.this, "La solicitud se cancelo correctamente", Toast.LENGTH_SHORT).show();
                                   Intent intent = new Intent(RequestDriverActivity.this,MapClientActivity.class);
                                    finish();
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        //ESTE METODO ES EN CASO DE QUE FALLE ESTA CONSUKTA HACIA EL SERVIDOR
                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error","Error: "+t.getMessage());
                        }
                    });
                }else{
                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion porque el conductor no tiene un token de sesion", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //Recibe de parametro el tiempo y la distancia con el fin de hacer una notificacion mas personalizada
    private void sendNotification(final String time, final String km) {
        mTokenProvider.getToken(mIdDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    map.put("title", "SOLICITUD DE SERVICIO A "+time+" DE TU POSICION");
                    map.put("body", "Un cliente esta solicitando un servicio a una distancia de "+km +"\n" +
                            "Recoger en: "+mExtraOrigin+"\nDestino: "+mExtraDestination);
                    map.put("id_client", mAuthProvider.getIdDriver());
                    map.put("origin", mExtraOrigin);
                    map.put("destination", mExtraDestination);
                    map.put("min", time);
                    map.put("distance", km);

                    //El valor de ttl lo establece por defecto en 4500s asi que lo dejamos con ese valor
                    FCMBody fcmBody= new FCMBody(token,"high", "4500s" ,map);
                    //ESTOS METODOS NOS RETORNARA UN OBJETO DE TIPO FCMRESPONSE QUE ERA ÑA RESPUESTA QUE OPTENIAMOS DEL SERVIDOR
                    mNotificationProvider.sedNotification(fcmBody).enqueue(new Callback<FCMResponse>() {

                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            //ESTA VALIDACION ES PARA VERIFICAR SI LLEGO RESPUESTA DEL SERVIDOR
                            if(response.body()!=null){
                                //ESTO QUIERE DECIR QUE SE MANDO CORRECTAMENTE SI LA VALIDACION ES CIERTA
                                if(response.body().getSuccess() == 1){
                                    ClientBooking clientBooking= new ClientBooking(
                                            mAuthProvider.getIdDriver(),
                                            mIdDriverFound,
                                            mExtraDestination,
                                            mExtraOrigin,
                                            time,
                                            km,
                                            "Create",
                                            mExtraOriginLat,
                                            mExtraOriginLng,
                                            mExtraDestinationLat,
                                            mExtraDestinationLng
                                    );
                                    //El addOnSuccessListener es solo para escuchar una sola vez en la base de datos y no en tiempo real
                                    mClientBookingProvider.create(clientBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            checkStatusClientBooking();
                                        }
                                    });
                                     }else{
                                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        //ESTE METODO ES EN CASO DE QUE FALLE ESTA CONSUKTA HACIA EL SERVIDOR
                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error","Error: "+t.getMessage());
                        }
                    });
                }else{
                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion porque el conductor no tiene un token de sesion", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkStatusClientBooking() {

        //Este metodo no es como el de AddListenerOnsingleValue que utilizamos para obtener el token nos funciona
        //para obtenr la informacion en tiempo rea, primero hay que verificar si el campo status existe en la base de datos
        //para que no vaya a fallar la app, estos eventos son unos escuchadores que se mantienen corriendo constantemente
        //hasta que no finalicemos esto de una manera va a seguir escuchando el estado de la informacion que proviene de la base de datos
        //tener cuidado con eso....
        mListener = mClientBookingProvider.getStatus(mAuthProvider.getIdDriver()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //aqui no necesitamos posicionarlo en el campo de status con el child porque con el metodo get status
                    //se posiciono a la hora de llamarlo y mandarle de parametro el id del conductor
                    String status = snapshot.getValue().toString();
                    if(status.equals("accept")){
                        //En la pantalla que nos moveremos sera la pantalla del viaje
                        //Al finalizar y cambiar de actividad lo que vamos hacer con el event listener es decir
                        //que deje de escuchar estos cambios provenientes de la base de datos y eso lo haremos sobreescribiendo
                        //un metodo de  llamado OnDestroid que es propio del ciclo de vida de android y que se ejecuta cuando finalizamos
                        //ua actividad
                        Intent intent= new Intent(RequestDriverActivity.this,MapClienBookingActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setAction(Intent.ACTION_RUN);
                        intent.putExtra("id_driver",mIdDriverFound);
                        startActivity(intent);
                        finish();
                    }else if(status.equals("cancel")){

                        Toast.makeText(RequestDriverActivity.this, "El conductor no acepto el viaje", Toast.LENGTH_SHORT).show();
                        Intent intent= new Intent(RequestDriverActivity.this, MapClientActivity.class);
                        finish();
                        startActivity(intent);

                        /*
                        //Lo que haremos ahora es mandar a llamar a el metodo getClosestDriver() para que siga buscando si hay mas clientes
                        //disponibles igual avisandole al cliente que un conductor rechazo el viaje pero que se estaran buscando mas conductores
                        //disponibles
                        mRadius=mRadius+0.1f;
                        mDriverFound = false;
                        Toast.makeText(RequestDriverActivity.this, "El conductor no acepto el viaje", Toast.LENGTH_LONG).show();
                        txtViewLookingFor.setText("SEGUIMOS EN BUSQUEDA DE OTRO CONDUCTOR.....");
                        getClosestDriver();
                        */
                    }
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
        //Esto lo hacemos con el fin de que no se quede escuchando los cambios despues de que hayamos ejecutadfo estas
        //acciones porque si no la proxima vez que volvamos a esta actividad y que vuelva a ejecutar este metodo va a crear
        //un nuevo listener y asi se van a estar duplicando y esto seria un problema para nuestra aplicacionhacer esto como
        //buena practica
        if(mListener!=null){
            mClientBookingProvider.getStatus(mAuthProvider.getIdDriver()).removeEventListener(mListener);
        }

    }
}