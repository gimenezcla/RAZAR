package com.example.trazabilidadg;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class EstablecimientoEdit extends AppCompatActivity {

    EditText edCuitDniResponsable;
    EditText edNombreEstablecimiento;
    EditText edDomicilio;
    Spinner  spLocalidad;
    EditText edTelefono;
    EditText edTelefonoEstab;
    EditText edNombreResponsable;
    RadioButton Solo_Entradas;
    RadioButton EntradasYSalidas;
    EditText edPermanencia;
    ProgressBar progressBarComercio;
    Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_establecimiento_edit);

        edCuitDniResponsable = findViewById(R.id.edCuitDniResponsable);
        edNombreEstablecimiento = findViewById(R.id.edNombreEstablecimiento);
        edDomicilio = findViewById(R.id.edDomicilio);
        spLocalidad = findViewById(R.id.spLocalidad);
        edTelefono = findViewById(R.id.edTelefono);
        edTelefonoEstab = findViewById(R.id.edTelefonoEstab);
        edNombreResponsable = findViewById(R.id.edNombreResponsable);
        Solo_Entradas = findViewById(R.id.Solo_Entradas);
        EntradasYSalidas = findViewById(R.id.EntradasYSalidas);
        edPermanencia = findViewById(R.id.edPermanencia);
        progressBarComercio = findViewById(R.id.progressBarComercio);
        btnGuardar = findViewById(R.id.btnGuardar);

        edCuitDniResponsable.setVisibility(View.INVISIBLE);
        edNombreEstablecimiento.setVisibility(View.INVISIBLE);
        edDomicilio.setVisibility(View.INVISIBLE);
        spLocalidad.setVisibility(View.INVISIBLE);
        edTelefono.setVisibility(View.INVISIBLE);
        edTelefonoEstab.setVisibility(View.INVISIBLE);
        edNombreResponsable.setVisibility(View.INVISIBLE);
        Solo_Entradas.setVisibility(View.INVISIBLE);
        EntradasYSalidas.setVisibility(View.INVISIBLE);
        edPermanencia.setVisibility(View.INVISIBLE);
        btnGuardar.setVisibility(View.INVISIBLE);

        progressBarComercio.setVisibility(View.VISIBLE);

        Establecimiento establecimiento = MainActivity.persistencia.getEstablecimientoLocal();

        EntradasYSalidas.setChecked(false);
        Solo_Entradas.setChecked(false);

        if(establecimiento == null) finish();

        ArrayList<String> listaLocalidades = MainActivity.persistencia.getLocalidades();
        listaLocalidades.add(0,"Seleccione una Localidad...");
        spLocalidad.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, listaLocalidades));

        edCuitDniResponsable.setVisibility(View.VISIBLE);
        edCuitDniResponsable.setText(establecimiento.CuitDniResponsable);
        edCuitDniResponsable.setEnabled(false);

        edNombreEstablecimiento.setEnabled(false);
        edNombreEstablecimiento.setVisibility(View.VISIBLE);
        edNombreEstablecimiento.setText(establecimiento.NombreEstablecimiento);

        EntradasYSalidas.setVisibility(View.VISIBLE);
        Solo_Entradas.setVisibility(View.VISIBLE);


        edTelefono.setEnabled(false);
        edTelefono.setVisibility(View.VISIBLE);
        edTelefono.setText(establecimiento.Telefono);

        EntradasYSalidas.setVisibility(View.VISIBLE);
        Solo_Entradas.setVisibility(View.VISIBLE);

        if(establecimiento.RegistraSalidas) {
            EntradasYSalidas.setChecked(true);
        }else{
            Solo_Entradas.setChecked(true);
        }

        EntradasYSalidas.setEnabled(false);
        Solo_Entradas.setEnabled(false);

        if(!establecimiento.TelefonoEstab.isEmpty() )
        {
            edNombreEstablecimiento.setEnabled(true);
            edTelefono.setEnabled(true);
            edCuitDniResponsable.setEnabled(true);
            EntradasYSalidas.setEnabled(true);
            Solo_Entradas.setEnabled(true);

            //edCuitDniResponsable.setEnabled(true);

            edDomicilio.setVisibility(View.VISIBLE);
            edDomicilio.setText(establecimiento.Domicilio);

            edNombreResponsable.setVisibility(View.VISIBLE);
            edNombreResponsable.setText(establecimiento.NombreResponsable);

            edPermanencia.setText(establecimiento.Permanencia.toString());

            edTelefonoEstab.setVisibility(View.VISIBLE);
            edTelefonoEstab.setText(establecimiento.TelefonoEstab);



            if(establecimiento.RegistraSalidas) {
                EntradasYSalidas.setChecked(true);
                edPermanencia.setVisibility(View.INVISIBLE);
            }else{
                Solo_Entradas.setChecked(true);
                edPermanencia.setVisibility(View.VISIBLE);
            }

            EntradasYSalidas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        edPermanencia.setVisibility(View.INVISIBLE);
                    else
                        edPermanencia.setVisibility(View.VISIBLE);
                }
            });

            spLocalidad.setVisibility(View.VISIBLE);
            int IndexLocalidad = listaLocalidades.indexOf(establecimiento.Localidad);
            spLocalidad.setSelection(IndexLocalidad);

            btnGuardar.setVisibility(View.VISIBLE);
            btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edCuitDniResponsable.getText().toString().isEmpty()) {
                    CustomToast.showError(EstablecimientoEdit.this,
                            "Debe ingresar el Cuit del Establecimiento o Dni del Titular",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (edNombreEstablecimiento.getText().toString().isEmpty()) {
                    CustomToast.showError(EstablecimientoEdit.this,
                            "Debe ingresar el Nombre del Establecimiento",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (edNombreResponsable.getText().toString().isEmpty()) {
                    CustomToast.showError(EstablecimientoEdit.this,
                            "Debe ingresar el Nombre del Responsable del Establecimiento",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if ((edPermanencia.getText().toString().isEmpty() || edPermanencia.getText().toString().equals("0")) &&
                        !EntradasYSalidas.isChecked() ) {
                    CustomToast.showError(EstablecimientoEdit.this,
                            "Debe ingresar los minutos promedios de atención en local",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (edDomicilio.getText().toString().isEmpty()) {
                    CustomToast.showError(EstablecimientoEdit.this,
                            "Debe ingresar el Domicilio del Establecimiento",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (edTelefono.getText().toString().isEmpty()) {
                    CustomToast.showError(EstablecimientoEdit.this,
                            "Debe ingresar el Teléfono del Responsable",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (edTelefono.getText().toString().length()>10) {
                    CustomToast.showError(EstablecimientoEdit.this,
                            "El Teléfono del Responsable no puede superar los 10 digitos",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (edTelefonoEstab.getText().toString().isEmpty()) {
                    CustomToast.showError(EstablecimientoEdit.this,
                            "Debe ingresar el Teléfono del Dispositivo",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (edTelefonoEstab.getText().toString().length()>10) {
                    CustomToast.showError(EstablecimientoEdit.this,
                            "El Teléfono del Dispositivo no puede superar los 10 digitos",
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (spLocalidad.getSelectedItemPosition()==0) {
                    CustomToast.showError(EstablecimientoEdit.this,
                            "Debe seleccionar una Localidad",
                            Toast.LENGTH_SHORT);
                    return;
                }


                progressBarComercio.setVisibility(View.VISIBLE);

                final String CuitDniResponsable = edCuitDniResponsable.getText().toString();
                final String NombreEstablecimiento = edNombreEstablecimiento.getText().toString();
                final String NombreResponsable = edNombreResponsable.getText().toString();
                final String Domicilio = edDomicilio.getText().toString();
                final String Telefono = edTelefono.getText().toString();
                final String TelefonoEstab = edTelefonoEstab.getText().toString();

                final String Localidad = spLocalidad.getSelectedItem().toString();
                boolean RegistraSalidas = false;
                if(EntradasYSalidas.isChecked())
                    RegistraSalidas= true;
                //Cierra y vuelve a Main.
                final String Permanencia = edPermanencia.getText().toString();

                final Handler h = new Handler();
                //Cierra y vuelve a Main.
                final boolean finalRegistraSalidas = RegistraSalidas;
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(final Void ... params ) {
                        // something you know that will take a few seconds
                        MainActivity.persistencia.GuardarUsuarioEstabEdit(
                                CuitDniResponsable,
                                NombreEstablecimiento,
                                NombreResponsable,
                                Domicilio,
                                Telefono,
                                TelefonoEstab,
                                Localidad,
                                finalRegistraSalidas,
                                Permanencia);

                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBarComercio.setVisibility(View.GONE);
                                finish();
                            }
                        });

                        return null;
                    }

                }.execute();

                }
            });

        }

        progressBarComercio.setVisibility(View.GONE);


    }
}