package com.example.trazabilidadg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.View;


public class AlarmEnviadorReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void ... params ) {
                /*SQLiteDatabase db = context.openOrCreateDatabase("GermanDB", Context.MODE_PRIVATE, null);
                Persistencia persistencia = new Persistencia(db);*/

                Persistencia persistencia = ((Persistencia) context.getApplicationContext());

                persistencia.EnviarVisitasMasivasPendientes();
                persistencia.ActualizarDatosLocalEstablecimiento();

                return null;
            }

        }.execute();

    }
}
