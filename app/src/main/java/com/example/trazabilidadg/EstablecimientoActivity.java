package com.example.trazabilidadg;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class EstablecimientoActivity extends AppCompatActivity {

    public static String CuitDniResponsable;
    public static String NombreEstablecimiento;
    public static String NombreResponsable;
    public static String Domicilio;
    public static String Telefono;
    public static String Localidad;
    public static String Permanencia;
    public static Boolean RegistraSalidas = false;
    public ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comercio);

        //Carga las localidades.
        final Spinner spLocalidad = findViewById(R.id.spLocalidad);
        ArrayList<String> listaLocalidades= MainActivity.persistencia.getLocalidades();
        listaLocalidades.add(0,"Seleccione una Localidad...");
        spLocalidad.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, listaLocalidades));


        final EditText edCuitDniResponsable = findViewById(R.id.edCuitDniResponsable);
        edCuitDniResponsable.setText(DocumentoActivity.Cuit);
        final EditText edNombreEstablecimiento = findViewById(R.id.edNombreEstablecimiento);
        final EditText edNombreResponsable = findViewById(R.id.edNombreResponsable);
        final EditText edDomicilio = findViewById(R.id.edDomicilio);
        final EditText edTelefono = findViewById(R.id.edTelefono);
        final EditText edPermanencia = findViewById(R.id.edPermanencia);
        final RadioButton EntradasYSalidas = findViewById(R.id.EntradasYSalidas);

        progressBar = findViewById(R.id.progressBarComercio);

        Button btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Abre Seleccion establecimiento
                if (edCuitDniResponsable.getText().toString().isEmpty()) {
                    CustomToast.showError(EstablecimientoActivity.this,
                            "Debe ingresar el Cuit del Establecimiento o Dni del Titular",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (edNombreEstablecimiento.getText().toString().isEmpty()) {
                    CustomToast.showError(EstablecimientoActivity.this,
                            "Debe ingresar el Nombre del Establecimiento",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (edDomicilio.getText().toString().isEmpty()) {
                    CustomToast.showError(EstablecimientoActivity.this,
                            "Debe ingresar el Domicilio del Establecimiento",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (spLocalidad.getSelectedItemPosition()==0) {
                    CustomToast.showError(EstablecimientoActivity.this,
                            "Debe seleccionar una Localidad",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (edNombreResponsable.getText().toString().isEmpty()) {
                    CustomToast.showError(EstablecimientoActivity.this,
                            "Debe ingresar el Nombre del Responsable del Establecimiento",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (edTelefono.getText().toString().isEmpty()) {
                    CustomToast.showError(EstablecimientoActivity.this,
                            "Debe ingresar el Teléfono del Responsable",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (edTelefono.getText().toString().length()>10) {
                    CustomToast.showError(EstablecimientoActivity.this,
                            "El Teléfono del Responsable no puede superar los 10 digitos",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (edPermanencia.getText().toString().isEmpty()) {
                    CustomToast.showError(EstablecimientoActivity.this,
                            "Debe ingresar los minutos promedios de atención en local",
                            Toast.LENGTH_SHORT);
                    return;
                }

                CuitDniResponsable = edCuitDniResponsable.getText().toString();
                NombreEstablecimiento = edNombreEstablecimiento.getText().toString();
                NombreResponsable = edNombreResponsable.getText().toString();
                Domicilio = edDomicilio.getText().toString();
                Telefono = edTelefono.getText().toString();
                Localidad = spLocalidad.getSelectedItem().toString();
                if(EntradasYSalidas.isChecked())
                    RegistraSalidas= true;
                //Cierra y vuelve a Main.
                Permanencia = edPermanencia.getText().toString();

                progressBar.setVisibility(View.VISIBLE);
                final Handler h = new Handler();

                SingleShotLocationProvider.requestSingleUpdate(EstablecimientoActivity.this,
                        new SingleShotLocationProvider.LocationCallback() {
                            @Override public void onNewLocationAvailable(final SingleShotLocationProvider.GPSCoordinates location) {

                                //Cierra y vuelve a Main.
                                new AsyncTask<String, Void, String>() {
                                    @Override
                                    protected String doInBackground(final String ... params ) {
                                        // something you know that will take a few seconds
                                        int IdUsuEstab = MainActivity.persistencia.GuardarUsuarioEstablecimiento(
                                                CuitDniResponsable,
                                                NombreEstablecimiento,
                                                NombreResponsable,
                                                Domicilio,
                                                Telefono,
                                                Localidad,
                                                false,//RegistraSalidas,
                                                MainActivity.USUARIO,
                                                Permanencia,
                                                DocumentoActivity.Telefono,
                                                location.latitude,
                                                location.longitude);


                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(String result) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        setResult(SeleccionEstablecimientoActivity.RESULT_OK,new Intent());
                                        finish();
                                    }

                                }.execute();
                            }
                });

            }
        });


    }
}