package com.example.trazabilidadg;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.common.AccountPicker;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Arrays;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SplashActivity extends AppCompatActivity {

    // Duración en milisegundos que se mostrará el splash
    private final int DURACION_SPLASH = 3000; // 3 segundos
    private Persistencia persistencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tenemos una plantilla llamada splash.xml donde mostraremos la información que queramos (logotipo, etc.)
        setContentView(R.layout.activity_splash);
        //Intent service = new Intent(this, MyBrodcastRecieverService.class);
        //startService(service);

        //SQLiteDatabase db = openOrCreateDatabase("GermanDB", Context.MODE_PRIVATE, null);

        // db.execSQL("DROP TABLE QRs"); // Descomentar cuando se quiera vaciar la Base de Datos
        persistencia = ((Persistencia)getApplication());//new Persistencia(db);

        new Handler().postDelayed(new Runnable(){
            public void run(){
                // Cuando pasen los 3 segundos, pasamos a la actividad principal de la aplicación
                if (persistencia.getEstablecimientoLocal() == null)
                {
                    Intent intent =
                            AccountPicker.newChooseAccountIntent(
                                    new AccountPicker.AccountChooserOptions.Builder()
                                            .setAllowableAccountsTypes(Arrays.asList("com.google"))
                                            .build());
                    startActivityForResult(intent, 0x0000c0dc);
                }else
                {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            };
        }, DURACION_SPLASH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0x0000c0dc ) {

            if( resultCode == RESULT_OK){
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else
                finish();
            return;
        }
    }
}