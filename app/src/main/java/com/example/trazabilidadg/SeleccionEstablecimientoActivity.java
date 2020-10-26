package com.example.trazabilidadg;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion__establecimiento);
        final String Cuit = getIntent().getStringExtra("CUIT");
        final String Telefono_usu = getIntent().getStringExtra("TELEFONO_USU");

        progressBar = findViewById(R.id.progressBarSeleccion);
        progressBar.setVisibility(View.VISIBLE);

        //Carga los establecimientos encontrados con el cuit.
        final Spinner spinnerEstablecimiento = findViewById(R.id.spinnerEstablecimiento);
        final Handler h = new Handler();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void ... params ) {
                // something you know that will take a few seconds
                Establecimientos = MainActivity.persistencia.getEstablecimientosPorCuit(Cuit);


                h.post(new Runnable() {
                    @Override
                    public void run() {
                        if( Establecimientos.size() >0 ){

                            spinnerEstablecimiento.setAdapter(new ArrayAdapter<String>(
                                    SeleccionEstablecimientoActivity.this,android.R.layout.simple_spinner_dropdown_item,
                                    (List<String>) new ArrayList<>(Establecimientos.keySet())));
                            progressBar.setVisibility(View.GONE);
                        }else
                        {
                            Intent intent = new Intent(SeleccionEstablecimientoActivity.this, EstablecimientoActivity.class);
                            intent.putExtra("CUIT",Cuit);
                            intent.putExtra("TELEFONO_USU",Telefono_usu);
                            startActivityForResult(intent,12);
                        }

                    }
                });


                return null;
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
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(final Void ... params ) {
                            // something you know that will take a few seconds
                            final Integer IdUsuEstab = MainActivity.persistencia.GuardarUsuario(idEstablecimiento,
                                    nombreEstablecimiento,
                                    MainActivity.USUARIO,
                                    Telefono_usu);

                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(IdUsuEstab == null){
                                        progressBar.setVisibility(View.INVISIBLE);
                                        CustomToast.showError(SeleccionEstablecimientoActivity.this
                                                , "Ocurrio un error, compruebe su conexión a internet.", Toast.LENGTH_LONG);
                                    }
                                    else{
                                        progressBar.setVisibility(View.INVISIBLE);
                                        setResult(SeleccionEstablecimientoActivity.RESULT_OK,new Intent());
                                        finish();
                                    }

                                }
                            });

                            return null;
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