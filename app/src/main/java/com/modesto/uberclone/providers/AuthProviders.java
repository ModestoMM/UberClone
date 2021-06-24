package com.modesto.uberclone.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class AuthProviders {
    FirebaseAuth mAuth;
    public AuthProviders(){
        mAuth=FirebaseAuth.getInstance();
    }

    public Task<AuthResult> register(String email, String password){
        return mAuth.createUserWithEmailAndPassword(email,password);
    }

    public Task<AuthResult> login(String email, String password){
        return mAuth.signInWithEmailAndPassword(email,password);
    }

    //Con este metodo cerramos la sesion que tengamos activada lo podemos habilitar instanciando la clase en la que se encuentra
    //y llevandolo acabo cuando se presione un boton
    public void logout(){
        mAuth.signOut();
    }

    public String getIdDriver(){
        return mAuth.getCurrentUser().getUid();
    }

    public boolean ExisteSession(){
        boolean exist=false;
        if(mAuth.getCurrentUser() != null){
            exist=true;
        }
        return  exist;
    }
}
