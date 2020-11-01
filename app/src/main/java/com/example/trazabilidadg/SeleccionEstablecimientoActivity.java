package com.example.trazabilidadg;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SeleccionEstablecimientoActivity extends AppCompatActivity {

    public static Integer idEstablecimiento;
    public ProgressBar progressBar;
    LinkedHashMap<String,Integer> Establecimientos;
    private Persistencia persistencia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion__establecimiento);
        final String Cuit = getIntent().getStringExtra("CUIT");
        final String Telefono_usu = getIntent().getStringExtra("TELEFONO_USU");

        progressBar = findViewById(R.id.progressBarSeleccion);
        progressBar.setVisibility(View.VISIBLE);

        persistencia = (Persistencia)getApplication();


        //Carga los establecimientos encontrados con el cuit.
        final Spinner spinnerEstablecimiento = findViewById(R.id.spinnerEstablecimiento);

        new AsyncTask<Void, LinkedHashMap<String, Integer>, LinkedHashMap<String, Integer>>() {
            @Override
            protected LinkedHashMap<String, Integer> doInBackground(final Void ... params ) {
                // something you know that will take a few seconds
                LinkedHashMap<String, Integer> establecimientos = MainActivity.persistencia.getEstablecimientosPorCuit(Cuit);

                return establecimientos;
            }
            @Override
            protected void onPostExecute(LinkedHashMap<String, Integer> establecimientos) {
                super.onPostExecute(establecimientos);

                if(establecimientos == null) {
                    CustomToast.showError(SeleccionEstablecimientoActivity.this,
                            "Ocurrio un problema al conectar con el servidor, por favor verifique su conexión a internet y que la fecha de su dispositivo sea correcta.",
                            Toast.LENGTH_LONG);
                    finish();
                    return;
                }

                Establecimientos = establecimientos;
                if( establecimientos.size() >0 ){

                    spinnerEstablecimiento.setAdapter(new ArrayAdapter<String>(
                            SeleccionEstablecimientoActivity.this,android.R.layout.simple_spinner_dropdown_item,
                            (List<String>) new ArrayList<>(establecimientos.keySet())));
                    progressBar.setVisibility(View.GONE);
                }else
                {
                    Intent intent = new Intent(SeleccionEstablecimientoActivity.this, EstablecimientoActivity.class);
                    intent.putExtra("CUIT",Cuit);
                    intent.putExtra("TELEFONO_USU",Telefono_usu);
                    startActivityForResult(intent,12);
                }
            }

        }.execute();



        Button buttonSeleccionar = findViewById(R.id.buttonSeleccionar);


        buttonSeleccionar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Object selected = spinnerEstablecimiento.getSelectedItem();
                if(selected != null)
                {
                    //Abre Seleccion establecimiento
                    final String nombreEstablecimiento = spinnerEstablecimiento.getSelectedItem()!= null ?
                            spinnerEstablecimiento.getSelectedItem().toString() : "";
                    idEstablecimiento = Establecimientos.get(nombreEstablecimiento);
                    progressBar.setVisibility(View.VISIBLE);

                    //Cierra y vuelve a Main.
                    new AsyncTask<Void, Void, Integer>() {
                        @Override
                        protected Integer doInBackground(final Void ... params ) {
                            // something you know that will take a few seconds
                            Integer r_IdUsuEstab = MainActivity.persistencia.GuardarUsuario(idEstablecimiento,
                                    nombreEstablecimiento,
                                    MainActivity.USUARIO,
                                    Telefono_usu);

                            return r_IdUsuEstab;
                        }

                        @Override
                        protected void onPostExecute(Integer r_IdUsuEstab) {
                            super.onPostExecute(r_IdUsuEstab);

                            if(r_IdUsuEstab == null){
                                progressBar.setVisibility(View.INVISIBLE);
                                CustomToast.showError(SeleccionEstablecimientoActivity.this
                                        , "Ocurrio un error, compruebe su conexión a internet y que la fecha de su dispositivo sea correcta.", Toast.LENGTH_LONG);

                            }
                            else{
                                progressBar.setVisibility(View.INVISIBLE);
                                setResult(SeleccionEstablecimientoActivity.RESULT_OK,new Intent());
                                finish();
                            }
                        }

                    }.execute();

                }
            }
            });


        Button buttonCrear = findViewById(R.id.buttonCrear);
        buttonCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SeleccionEstablecimientoActivity.this, EstablecimientoActivity.class);
                intent.putExtra("CUIT",Cuit);
                intent.putExtra("TELEFONO_USU",Telefono_usu);
                startActivityForResult(intent,12);

            }
        });


        persistencia.BuscarLocation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK)
        {
            setResult(RESULT_OK, new Intent());
            finish();
        }else
        {
            finish();
        }
    }
}