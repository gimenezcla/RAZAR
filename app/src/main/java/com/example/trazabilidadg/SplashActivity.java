package com.example.trazabilidadg;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Arrays;
import java.util.Map;

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

        persistencia = ((Persistencia)getApplication());

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                persistencia.EnviarVisitasMasivasPendientes();
                persistencia.ActualizarDatosLocalEstablecimiento();
                return null;
            }
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                Establecimiento establecimiento = persistencia.getEstablecimientoLocal();
                if (establecimiento == null )
                {
                    ShowSelectorCuentas();
                }else
                {
                    if(!establecimiento.Enviado)
                        ShowSelectorCuentas();
                    else{
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }.execute();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0x0000c0dc ) {
            if( resultCode == RESULT_OK){
                String USUARIO = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                persistencia.setUsuarioIfNull(USUARIO);

                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra("USUARIO",USUARIO);
                startActivity(intent);
                finish();
            }else
                finish();
            return;
        }
    }

    public void ShowSelectorCuentas(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Trazar - Selección de Cuenta");
        alertDialog.setMessage("Seleccione el proveedor de Cuenta con el que desea acceder.");
        alertDialog.setPositiveButton("Gmail/Outlook/Yahoo", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent =
                        AccountPicker.newChooseAccountIntent(
                                new AccountPicker.AccountChooserOptions.Builder()
                                        //.setAllowableAccountsTypes(Arrays.asList("com.google"))
                                        .build());
                startActivityForResult(intent, 0x0000c0dc);
                dialog.cancel();
            }
        });

        alertDialog.setNegativeButton("Otro proveedor", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setTitle("Ingrese su cuenta de Correo");
                final EditText input = new EditText(SplashActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                builder.setView(input);
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        String USUARIO = input.getText().toString();

                        if(!isValidEmail(USUARIO))
                            CustomToast.showError(SplashActivity.this,
                                    "Debe ingresar una cuenta válida.", Toast.LENGTH_LONG);

                        persistencia.setUsuarioIfNull(USUARIO);

                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.putExtra("USUARIO",USUARIO);
                        startActivity(intent);

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

                        dialog1.cancel();
                        dialog.cancel();

                        finish();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        dialog1.cancel();

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    }
                });

                builder.show();
                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

        alertDialog.show();

    }
    public final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}