package com.modesto.uberclone.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.modesto.uberclone.Activitys.client.RegisterActivity;
import com.modesto.uberclone.Activitys.driver.RegisterDriverActivity;
import com.modesto.uberclone.R;

public class SelectOptionAuthActivity extends AppCompatActivity {

    Toolbar mToolbar;
    Button mButtonGoLogin;
    Button mButtonRegister;
    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option_auth);
        mToolbar=findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Seleccionar Opcion");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPref=getApplicationContext().getSharedPreferences("typeUser",MODE_PRIVATE);

        mButtonGoLogin=findViewById(R.id.BtnGoToLogin);
        mButtonRegister=findViewById(R.id.BtnGoToRegister   );
        mButtonGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });
        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegistre();
            }
        });
    }

    private void goToLogin() {
        Intent intent=new Intent(SelectOptionAuthActivity.this,LoginActivity.class);
        startActivity(intent);
    }

    public void goToRegistre(){
        String typeUser=mPref.getString("user","");
        if(typeUser.equals("client")){
            Intent intent=new Intent(SelectOptionAuthActivity.this, RegisterActivity.class);
            startActivity(intent);
        }else{
            Intent intent=new Intent(SelectOptionAuthActivity.this, RegisterDriverActivity.class);
            startActivity(intent);
        }

    }
}