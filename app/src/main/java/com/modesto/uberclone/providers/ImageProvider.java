package com.modesto.uberclone.providers;

import android.content.Context;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.modesto.uberclone.Utils.CompressorBitmapImage;
import com.modesto.uberclone.Utils.FileUtil;

import java.io.File;

//EN ESTA CLASE TENDREMOS LA LOGICA PARA EL ENVIO DE LA IMAGEN A LA FIREBASE
public class ImageProvider {

    private StorageReference storageReference;

    public ImageProvider(String ref) {
        storageReference = FirebaseStorage.getInstance().getReference().child(ref);
    }

    public UploadTask saveImage (Context context, String idUsers, File imagen){
        byte[] imageByte = CompressorBitmapImage.getImage(context, imagen.getPath(), 500, 500);
        final StorageReference storage = storageReference.child(idUsers + ".jpg");
        storageReference =storage;
        UploadTask uploadTask = storage.putBytes(imageByte);

        return uploadTask;

    }

    public StorageReference getStorage(){
        return storageReference;
    }
}
