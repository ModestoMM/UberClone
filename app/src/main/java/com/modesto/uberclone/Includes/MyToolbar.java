package com.modesto.uberclone.Includes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.modesto.uberclone.R;

public class MyToolbar {

    public static void show(AppCompatActivity activity, String Titulo, Boolean appButtom){
        Toolbar toolbar= activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
       activity.getSupportActionBar().setTitle(Titulo);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(appButtom);

    }
}
