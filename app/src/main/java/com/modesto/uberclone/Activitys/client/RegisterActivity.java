package com.modesto.uberclone.Activitys.client;

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
import com.modesto.uberclone.Activitys.MapClientActivity;
import com.modesto.uberclone.R;
import com.modesto.uberclone.models.Client;
import com.modesto.uberclone.providers.AuthProviders;
import com.modesto.uberclone.providers.ClientProvider;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {




    //VIEW
    Button mButtonRegister;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputName;
    TextInputEditText mTextInputPassword;
      AlertDialog mDialog;
      //Es para la autentificacion
      AuthProviders mAuthProviders;
      //Ingresar los datos en la base de datos en nuestro nodo llamado Clients
      ClientProvider mClientProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

         MyToolbar.show(this,"Registro de usuario",true);
          mDialog= new SpotsDialog.Builder().setContext(RegisterActivity.this).setMessage("Espere un momento").build();

        mAuthProviders=new AuthProviders();
        mClientProvider=new ClientProvider();

        mTextInputEmail=findViewById(R.id.textInputEmail);
        mTextInputName=findViewById(R.id.textInputName);
        mTextInputPassword=findViewById(R.id.textInputPassword);
        mButtonRegister=findViewById(R.id.btnRegister);



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
       final  String password=mTextInputPassword.getText().toString();
        if(!email.isEmpty() && !password.isEmpty()){
            if(password.length() >= 6){
                mDialog.show();
               register(name,email,password);
            }
            else{
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }

    }

    private void register(final String name, final String email, String password) {
        //Ejecutamos el metodo registrar del mAuthProvider que nos permitira registrar un usuario en Firebase Authentication
        //Y si todoo sale bien nos deja crear un usuario con el ID que nos proporciona cuando el registro fue exitoso al igual que nombre y email
        mAuthProviders.register(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if(task.isSuccessful()){
                    String id= FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Client client=new Client(id,name,email);
                    create(client);
                }else{
                    Toast.makeText(RegisterActivity.this, "No se pudo registrar el Usuario", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    void create(Client client){
        mClientProvider.create(client).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //Si todo sale bien se ejecutara un mensaje para indicarle al usuario que es se realizo correctamente
                   // Toast.makeText(RegisterActivity.this, "El registro se realizo correctamente", Toast.LENGTH_SHORT).show();
                    //Con este intent mandamos a la actividad
                    Intent intent= new Intent(RegisterActivity.this, MapClientActivity.class);
                    //Con esta instruccion definimos que al dar atras no nos regresara a esta antalla ya
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else{
                    Toast.makeText(RegisterActivity.this, "No se pudo crear el cliente", Toast.LENGTH_SHORT).show();
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