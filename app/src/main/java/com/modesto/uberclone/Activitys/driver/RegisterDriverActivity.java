package com.modesto.uberclone.Activitys.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.modesto.uberclone.Includes.MyToolbar;
import com.modesto.uberclone.Activitys.MapDriverActivity;
import com.modesto.uberclone.R;
import com.modesto.uberclone.models.Driver;
import com.modesto.uberclone.providers.AuthProviders;
import com.modesto.uberclone.providers.DriverProviders;

import dmax.dialog.SpotsDialog;

public class RegisterDriverActivity extends AppCompatActivity {



    //VIEW
    Button mButtonRegister;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputName;
    TextInputEditText mTextInputPassword;
    TextInputEditText mTextInputVehicleBrand;
    TextInputEditText mTextInputVehiclePlate;

    AlertDialog mDialog;
    //Es para la autentificacion
    AuthProviders mAuthProviders;
    //Ingresar los datos en la base de datos en nuestro nodo llamado Clients
    DriverProviders mDriverProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);

        MyToolbar.show(this,"Registro del conductor",true);
        mDialog= new SpotsDialog.Builder().setContext(RegisterDriverActivity.this).setMessage("Espere un momento").build();

        mAuthProviders=new AuthProviders();
        mDriverProvider=new DriverProviders();

        mTextInputEmail=findViewById(R.id.textInputEmail);
        mTextInputName=findViewById(R.id.textInputName);
        mTextInputPassword=findViewById(R.id.textInputPassword);
        mButtonRegister=findViewById(R.id.btnRegister);
        mTextInputVehicleBrand=findViewById(R.id.textInputVehicleBrand);
        mTextInputVehiclePlate=findViewById(R.id.textInputVehiculPlate);



        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickRegister();
            }
        });
    }

    //Tenemos los datos que el usuario ingreso en el formulario
    private void ClickRegister() {
        final String name=mTextInputName.getText().toString();
        final String email=mTextInputEmail.getText().toString();
        final String vehiclebarnd=mTextInputVehicleBrand.getText().toString();
        final String vehicleplate=mTextInputVehiclePlate.getText().toString();
        final  String password=mTextInputPassword.getText().toString();
        if(!email.isEmpty() && !password.isEmpty() && !vehiclebarnd.isEmpty() && !vehicleplate.isEmpty()){
            if(password.length() >= 6){
                mDialog.show();
                register(name,email,password,vehiclebarnd,vehicleplate);
            }
            else{
                Toast.makeText(RegisterDriverActivity.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(RegisterDriverActivity.this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }

    }

    private void register(final String name, final String email, String password,final String vehiclebrand, final String vehicleplate) {
        //Ejecutamos el metodo registrar del mAuthProvider que nos permitira registrar un usuario en Firebase Authentication
        //Y si todo sale bien nos deja crear un usuario con el ID que nos proporciona cuando el registro fue exitoso al igual que nombre y email
        mAuthProviders.register(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if(task.isSuccessful()){
                    String id= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Driver driver=new Driver(id,name,email,vehiclebrand,vehicleplate);
                    create(driver);
                }else{
                    Toast.makeText(RegisterDriverActivity.this, "No se pudo registrar el Usuario", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    void create(Driver driver){
        mDriverProvider.create(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //Si todo sale bien se ejecutara un mensaje para indicarle al usuario que es se realizo correctamente
                    //Toast.makeText(RegisterDriverActivity.this, "El registro se realizo correctamente", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(RegisterDriverActivity.this, MapDriverActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else{
                    Toast.makeText(RegisterDriverActivity.this, "No se pudo crear el cliente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
/*
    private void saveUser(String id,String name, String email) {
        String selecterUser= mPref.getString("user","");
        User user=new User();
        user.setEmail(email);
        user.setName(name);
        if(selecterUser.equals("driver")){
            mDatabase.child("Users").child("Drivers").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registro Exitoso", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(RegisterActivity.this, "Fallo el Registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else if(selecterUser.equals("client")){
            mDatabase.child("Users").child("Clients").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registro Exitoso", Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(RegisterActivity.this, "Fallo el Registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }*/
}