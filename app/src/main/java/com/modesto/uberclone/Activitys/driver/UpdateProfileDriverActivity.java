package com.modesto.uberclone.Activitys.driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.modesto.uberclone.Activitys.client.MapClienBookingActivity;
import com.modesto.uberclone.Activitys.client.UpdateProfileActivity;
import com.modesto.uberclone.Includes.MyToolbar;
import com.modesto.uberclone.R;
import com.modesto.uberclone.Utils.CompressorBitmapImage;
import com.modesto.uberclone.Utils.FileUtil;
import com.modesto.uberclone.models.Client;
import com.modesto.uberclone.models.Driver;
import com.modesto.uberclone.providers.AuthProviders;
import com.modesto.uberclone.providers.ClientProvider;
import com.modesto.uberclone.providers.DriverProviders;
import com.modesto.uberclone.providers.ImageProvider;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.security.PrivateKey;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileDriverActivity extends AppCompatActivity {
    private ImageView mImageViewProfile;
    private Button mButtonUpdate;
    private TextView mTextViewName;
    private TextView mTextViewBrandVehicle;
    private TextView mTextViewPlateVehicle;

    private DriverProviders mDriverProvider;
    private AuthProviders mAuthProvider;

    private File mImageFile;
    private String mImage;

    private final int GALLERY_REQUEST = 1;
    private ProgressDialog mProgressDialog;
    private String mName;
    private String mBrand;
    private String mPlate;

    //El provider que utilizaremos para subir la imagen y tener mejor organizadas nuestras csas
    private ImageProvider mImageProvider;

    private CircleImageView mCircleImageBack;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_driver);
       // MyToolbar.show(this, "Actualizar perfil", true);

        mImageViewProfile = findViewById(R.id.ImageViewProfile);
        mButtonUpdate = findViewById(R.id.btnUpdateProfile);
        mTextViewName = findViewById(R.id.textInputName);
        mTextViewBrandVehicle = findViewById(R.id.textInputVehicleBrand);
        mTextViewPlateVehicle = findViewById(R.id.textInputVehiculPlate);

        mDriverProvider = new DriverProviders();
        mAuthProvider = new AuthProviders();
        mCircleImageBack= findViewById(R.id.CircleImageViewBack);

        mImageProvider = new ImageProvider("driver_images");

        mProgressDialog = new ProgressDialog(this);

        getDriverInfo();

        mImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
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

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== GALLERY_REQUEST && resultCode == RESULT_OK) {
            try {
                mImageFile = FileUtil.from(this, data.getData());
                mImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch(Exception e) {
                Log.d("ERROR", "Mensaje: " +e.getMessage());
            }
        }
    }

    private void getDriverInfo() {
        mDriverProvider.getDriver(mAuthProvider.getIdDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String brand = dataSnapshot.child("vehicleBrand").getValue().toString();
                    String plate = dataSnapshot.child("vehiclePlate").getValue().toString();
                    String image ="";
                    //Aqui verificamos si el cliente actualizo sus dats y puso una imagen ya que si o lo hizo entonces no tendremos
                    //que pedir quesaque la url de ella ya que no existira esa instancia, el haschild pregunta si el nodo del id del
                    //usuario contiene un compo llamado image
                    if(dataSnapshot.hasChild("image")){
                        image = dataSnapshot.child("image").getValue().toString();
                        //Aqui usamos la libreria Picasso aqui recivimos el contexto de ahi el metodo load agarrara la url de la imagen
                        //y al final el metodo into le instanciara esa imagen al ImageView
                        Picasso.with(UpdateProfileDriverActivity.this).load(image).into(mImageViewProfile);
                    }
                    mTextViewName.setText(name);
                    mTextViewBrandVehicle.setText(brand);
                    mTextViewPlateVehicle.setText(plate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateProfile() {
        mName = mTextViewName.getText().toString();
        mBrand = mTextViewBrandVehicle.getText().toString();
        mPlate = mTextViewPlateVehicle.getText().toString();
        if (!mName.equals("") && mImageFile != null) {
            mProgressDialog.setMessage("Espere un momento...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            saveImage();
        }
        else {
            Toast.makeText(this, "Ingresa la imagen y el nombre", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage() {
        mImageProvider.saveImage(UpdateProfileDriverActivity.this,mAuthProvider.getIdDriver(),mImageFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String image = uri.toString();
                            Driver driver = new Driver();
                            driver.setImage(image);
                            driver.setName(mName);
                            driver.setId(mAuthProvider.getIdDriver());
                            driver.setVehicleBrand(mBrand);
                            driver.setVehiclePlate(mPlate);
                            mDriverProvider.update(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(UpdateProfileDriverActivity.this, "Su informacion se actualizo correctamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                else {
                    Toast.makeText(UpdateProfileDriverActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}