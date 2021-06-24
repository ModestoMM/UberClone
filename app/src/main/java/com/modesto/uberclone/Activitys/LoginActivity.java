package com.modesto.uberclone.Activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.modesto.uberclone.Includes.MyToolbar;
import com.modesto.uberclone.R;
import com.modesto.uberclone.providers.AuthProviders;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputPassword;
    Button mButtonLogin;
    AuthProviders mAuthProviders;
    AlertDialog mDialog;
    SharedPreferences mPref;
    private CircleImageView mCircleImageBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mCircleImageBack= findViewById(R.id.CircleImageViewBack);

       // MyToolbar.show(LoginActivity.this,"Login de Usuario",true);

        mPref=getApplicationContext().getSharedPreferences("typeUser",MODE_PRIVATE);

        mTextInputEmail=findViewById(R.id.textInputEmail);
        mTextInputPassword=findViewById(R.id.textInputpassword);
        mButtonLogin=findViewById(R.id.btnLogin);
        mAuthProviders=new AuthProviders();
        mDialog= new SpotsDialog.Builder().setContext(LoginActivity.this).setMessage("Espere un momento").build();

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        //El click listener del login
        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Llamamos solo a este metodo finish ya que este terminara la actividad en la que nos encontramos
                finish();
            }
        });
    }

    public void login() {
        String email= mTextInputEmail.getText().toString();
        String password=mTextInputPassword.getText().toString();

        if(!email.isEmpty() && !password.isEmpty()){
            if(password.length() >= 6){
                mDialog.show();
                mAuthProviders.login(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String typeUser=mPref.getString("user","");
                            if(typeUser.equals("client")){
                                Intent intent =new Intent(LoginActivity.this, MapClientActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }else{
                                Intent intent =new Intent(LoginActivity.this, MapDriverActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "La contraseña o el password son incorrectos", Toast.LENGTH_LONG).show();
                        }
                        mDialog.dismiss();
                    }
                });
            }
            else{
                Toast.makeText(this, "La contraseña debe tener mas de 6 caracteres", Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(this, "La Contraseña y el Email son Obligatorios", Toast.LENGTH_LONG).show();
        }
    }
}