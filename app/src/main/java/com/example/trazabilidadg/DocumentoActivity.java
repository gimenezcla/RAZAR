package com.example.trazabilidadg;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
                    Toast.makeText(DocumentoActivity.this,
                            "Debe ingresar el Cuit del Establecimiento o Dni del Titular",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (txtTel.getText().toString().isEmpty()) {
                    Toast.makeText(DocumentoActivity.this,
                            "Debe ingresar el N° de Teléfono del Dispositivo.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Cuit =txtCuit.getText().toString();
                Telefono =txtTel.getText().toString();

                //Abre la seleccion de establecimiento
                Intent intent = new Intent( DocumentoActivity.this, SeleccionEstablecimientoActivity.class);
                startActivityForResult(intent, 11);

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
        }
    }
}