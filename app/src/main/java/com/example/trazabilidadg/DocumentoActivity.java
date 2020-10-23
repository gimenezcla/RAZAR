package com.example.trazabilidadg;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DocumentoActivity extends AppCompatActivity {

    public static String Cuit;
    public static String Telefono;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documento);

        final EditText txtCuit = findViewById(R.id.edCuit);
        final EditText txtTel = findViewById(R.id.edTel);

        Button btnGuardar = findViewById(R.id.btnSiguiente);
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Abre Seleccion establecimiento
                if (txtCuit.getText().toString().isEmpty()) {
                    CustomToast.showError(DocumentoActivity.this,
                            "Debe ingresar el Cuit del Establecimiento o Dni del Titular",
                            Toast.LENGTH_SHORT);
                    return;
                }

                if (txtTel.getText().toString().isEmpty()) {
                    CustomToast.showError(DocumentoActivity.this,
                            "Debe ingresar el N° de Teléfono del Dispositivo.",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (txtTel.getText().toString().length()>10) {
                    CustomToast.showError(DocumentoActivity.this,
                            "El Teléfono del Dispositivo no puede superar los 10 digitos.",
                            Toast.LENGTH_SHORT);
                    return;
                }

                Cuit =txtCuit.getText().toString();
                Telefono =txtTel.getText().toString();

                //Abre la seleccion de establecimiento
                Intent intent = new Intent( DocumentoActivity.this, SeleccionEstablecimientoActivity.class);
                intent.putExtra("CUIT", Cuit);
                intent.putExtra("TELEFONO_USU", Telefono);
                startActivityForResult(intent, 11);

            }
        });

        showSettingAlert();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK)
        {
            setResult(RESULT_OK, new Intent());
            finish();
        }
    }

    private boolean getGPSStatus()
    {
        LocationManager manager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
        String allowedLocationProviders =
                Settings.System.getString(getContentResolver(),
                        Settings.System.LOCATION_PROVIDERS_ALLOWED);

        if (allowedLocationProviders == null) {
            allowedLocationProviders = "";
        }

        return allowedLocationProviders.contains(manager.GPS_PROVIDER);
    }
    public void showSettingAlert()
    {

        LocationManager manager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
        if (!getGPSStatus() || !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setCancelable(false);
            alertDialog.setTitle("Para usar esta app debe encender el GPS");
            alertDialog.setMessage("El GPS no esta habilidado, desea encenderlo ? ");
            alertDialog.setPositiveButton("Configurar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            alertDialog.show();
        }
    }
}