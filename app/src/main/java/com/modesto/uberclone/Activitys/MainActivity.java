package com.modesto.uberclone.Activitys;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.modesto.uberclone.R;

public class MainActivity extends AppCompatActivity {

    Button mButtonIAmCliente;
    Button mButtonIAmDriver;
    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPref=getApplicationContext().getSharedPreferences("typeUser",MODE_PRIVATE);
        final SharedPreferences.Editor editor= mPref.edit();


        mButtonIAmCliente=findViewById(R.id.btnIAmCliente);
        mButtonIAmDriver=findViewById(R.id.btnIAmDriver);

        mButtonIAmCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("user","client");
                editor.apply();
                goToSelectAuth();
            }
        });


     mButtonIAmDriver.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            editor.putString("user","driver");
            editor.apply();
            goToSelectAuth();
        }
    });
}
  //Estamos sobre escribiendo un metodo de entrada del android estudio lo hacemos para hacer que identifique si ya entraste con tu cuenta de firebase
  //y asi posicionarte en la pantalla adecuada sin necesidad de tener que volver a iniciar sesion....!!!
    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            String user=mPref.getString("user","");
            if(user.equals("client")){
                Intent intent =new Intent(MainActivity.this, MapClientActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else{
                Intent intent =new Intent(MainActivity.this, MapDriverActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    private void goToSelectAuth() {
        Intent intent=new Intent(MainActivity.this,SelectOptionAuthActivity.class);
        startActivity(intent);
    }
}