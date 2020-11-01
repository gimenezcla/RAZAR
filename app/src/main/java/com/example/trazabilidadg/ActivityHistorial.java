package com.example.trazabilidadg;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ActivityHistorial extends AppCompatActivity {

    public static SQLiteDatabase DB = null;

    ListView lvValores;
    Button btnVaciar;

    public ArrayAdapter<String> adapter;
    private Persistencia persistencia;

    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            String basePath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            File root = new File(basePath);
            if (!root.exists()) {
                root.mkdirs();
            }
            File file = new File(root, sFileName);
            FileWriter writer = new FileWriter(file);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(context, "CSV Creado", Toast.LENGTH_SHORT).show();

            Intent viewDoc = new Intent(Intent.ACTION_VIEW);
            viewDoc.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            viewDoc.setDataAndType(
                    FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file),
                    // Uri.fromFile(file),
                    "text/csv"
            );

            PackageManager pm = getPackageManager();
            List<ResolveInfo> apps =
                    pm.queryIntentActivities(viewDoc, PackageManager.MATCH_DEFAULT_ONLY);
            if (apps.size() > 0)
                startActivity(viewDoc);

        } catch (IOException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.d("PEPE", errors.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        persistencia = (Persistencia)getApplication();
        DB = persistencia.db;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        DateFormat formatterServer = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date;
        formatterServer.setTimeZone(TimeZone.getTimeZone("UTC"));

        lvValores = (ListView)findViewById(R.id.lvValores);

        List<String> valoresList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, valoresList);

        lvValores.setAdapter(adapter);
        Cursor fila = DB.rawQuery("SELECT FECHA, DNI, APELLIDO, NOMBRES, TIPO_MOVIMIENTO, ENVIADO  FROM QRs ORDER BY id_registro desc", null); //  where enviado = 0  ORDER BY timestamp DESC
        String valores, CSV = "";

        while(fila.moveToNext())
        {
            try {
                //date = (Date) formatterServer.parse(fila.getString(0));
                date = (Date) formatterServer.parse(fila.getString(0));
                valores = "";
                valores += "Fecha: " + persistencia.corregirFecha(date,"dd/MM/yyyy - HH:mm") +" Enviado: " +
                        (fila.getString(5).equals("0") ? "N" : "S") + "\n";
                //valores += "Dni: " + fila.getString(1) + "\n";
                valores += "Nombre: " + fila.getString(2) +" "+ fila.getString(3) + "\n";
                valores += "Tipo: " + fila.getString(4) ;//+ "\n";
                /*valores += "nombre: (" + fila.getString(3) +")" ;//+ "\n";
                valores += "apellido: (" + fila.getString(2) +")";//+ "\n";
                valores += "dni: (" + fila.getString(1) +")";//+ "\n";*/
                //valores += "------------------------------------------------E: " + fila.getString(5) ;
                adapter.add(valores);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        //generateNoteOnSD(this, "historial.csv", CSV);

        /*btnVaciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DB.execSQL("DELETE FROM QRs");
                //adapter.clear();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(final Void ... params ) {
                        MainActivity.persistencia.EnviarVisitasMasivasPendientes();
                        return null;
                    }

                }.execute();

            }
        });*/
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void GrabarEnviados(int uRegistro){
        DB.execSQL("UPDATE QRs SET enviado=1 WHERE enviado=0  and id_registro <= "+uRegistro);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}