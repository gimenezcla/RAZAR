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
        spLocalidad.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, listaLocalidades));


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
                    Toast.makeText(EstablecimientoActivity.this,
                            "Debe ingresar el Cuit del Establecimiento o Dni del Titular",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edNombreEstablecimiento.getText().toString().isEmpty()) {
                    Toast.makeText(EstablecimientoActivity.this,
                            "Debe ingresar el Nombre del Establecimiento",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edNombreResponsable.getText().toString().isEmpty()) {
                    Toast.makeText(EstablecimientoActivity.this,
                            "Debe ingresar el Nombre del Responsable del Establecimiento",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edPermanencia.getText().toString().isEmpty()) {
                    Toast.makeText(EstablecimientoActivity.this,
                            "Debe ingresar los minutos promedios de atención en local",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edDomicilio.getText().toString().isEmpty()) {
                    Toast.makeText(EstablecimientoActivity.this,
                            "Debe ingresar el Domicilio del Establecimiento",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edTelefono.getText().toString().isEmpty()) {
                    Toast.makeText(EstablecimientoActivity.this,
                            "Debe ingresar el Teléfono del Establecimiento",
                            Toast.LENGTH_SHORT).show();
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
                //Cierra y vuelve a Main.
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(final Void ... params ) {
                        // something you know that will take a few seconds
                        int IdUsuEstab = MainActivity.persistencia.GuardarUsuarioEstablecimiento(
                                CuitDniResponsable,
                                NombreEstablecimiento,
                                NombreResponsable,
                                Domicilio,
                                Telefono,
                                Localidad,
                                RegistraSalidas,
                                MainActivity.USUARIO,
                                Permanencia,
                                DocumentoActivity.Telefono,
                                MainActivity.latitude,
                                MainActivity.longitude);


                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                                setResult(SeleccionEstablecimientoActivity.RESULT_OK,new Intent());
                                finish();
                            }
                        });

                        return null;
                    }

                }.execute();

                setResult(EstablecimientoActivity.RESULT_OK,new Intent());
                finish();
            }
        });


    }
}