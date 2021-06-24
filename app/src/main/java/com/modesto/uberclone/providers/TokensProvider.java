package com.modesto.uberclone.providers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.modesto.uberclone.Activitys.MapClientActivity;
import com.modesto.uberclone.models.Token;

public class TokensProvider {
    DatabaseReference mDatabase;

    public TokensProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Tokens");
    }

    public void Create(final String idUser) {
        if (idUser == null) return;
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                //Esto nos devolveria el nuevo token de usuario
                Token token = new Token(instanceIdResult.getToken());
                //El Child en este caso sera el ID del usuario logeado le decimos que establezca el vaor del token
                mDatabase.child(idUser).setValue(token);
            }
        });
    }

    //ESTE METODO SE UTILIZARA PARA DESCUBRIR EL ID DE USUARIO DE TIPO DRIVE MAS CERCA
    public DatabaseReference getToken(String idUser) {
        //Hacemos que el database apunte ahora al idUser deñ token mas cercano del tipo driver
        return mDatabase.child(idUser);
    }

}





