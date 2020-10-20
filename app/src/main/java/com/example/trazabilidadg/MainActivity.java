package com.example.trazabilidadg;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;

import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements LocationListener {

    EditText txtDNI;
    EditText txtApellido;
    EditText txtNombres;
    EditText txtTelefono;
    EditText txtDescripcion;
    TextView lblUsuario;
    Button btnLeerCodigo;
    Button btnGuardar;
    Button btnManual;
    Button btnVer;
    static SQLiteDatabase db;
    public static Persistencia persistencia;


    public static double latitude;
    public static double longitude;
    static LocationManager locationManager;
    String bestProvider;
    Criteria criteria;
    public static String USUARIO = "";
    private int idUsuEstab;
    private static String Genero = "";
    private static boolean EncendioTarea = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        setMenuEntradaSalida(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    private void setMenuEntradaSalida(Menu _menu) {
        Establecimiento establecimiento = persistencia.getEstablecimientoLocal();
        if(establecimiento != null){
            if(establecimiento.RegistraSalidas)
                _menu.findItem(R.id.registrarSalidas).setVisible(true);
            else
                _menu.findItem(R.id.registrarSalidas).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.establecimiento:
                Intent intent = new Intent(MainActivity.this, EstablecimientoEdit.class);
                startActivityForResult(intent, 9988);
                return true;
            case R.id.registrarEntradas:
                SetTipoRegistroActual("ENTRADA");
                return true;
            case R.id.registrarSalidas:
                SetTipoRegistroActual("SALIDA");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void SetTipoRegistroActual(String tipo) {
        persistencia.setTipoMovimientoActual(tipo);
        ActualizarUI();
    }

    private void ActualizarUI() {
        String titulo = "Registro de Entradas";
        Establecimiento establecimiento = persistencia.getEstablecimientoLocal();

        if(establecimiento != null )
            if(establecimiento.TipoMovimientoActual != null && establecimiento.TipoMovimientoActual.equals("SALIDA")){
                titulo = "Registro de Salidas";
                findViewById(R.id.txtTelefono).setVisibility(View.GONE);
            }else
                findViewById(R.id.txtTelefono).setVisibility(View.VISIBLE);

        ((TextView)findViewById(R.id.titulo)).setText(titulo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //registerReceiver(new NetworkReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

            txtDNI = findViewById(R.id.txtDNI);
            txtApellido = findViewById(R.id.txtApellidos);
            txtNombres = findViewById(R.id.txtNombres);
            txtTelefono = findViewById(R.id.txtTelefono);
            txtDescripcion = findViewById(R.id.txtDescripcion);
            btnLeerCodigo = findViewById(R.id.btnLeerCodigo);
            btnManual = findViewById(R.id.btnManual);
            btnGuardar = findViewById(R.id.btnGuardar);
            btnVer = findViewById(R.id.btnVer);
            lblUsuario = findViewById(R.id.lblUsuario);

            showSettingAlert();
            // creando/abriendo el archivo que va a contener la base de datos
            db = openOrCreateDatabase("GermanDB", Context.MODE_PRIVATE, null);

            // db.execSQL("DROP TABLE QRs"); // Descomentar cuando se quiera vaciar la Base de Datos
            persistencia = new Persistencia(db);
            // si no ha sido previamente creada, creo la tabla "QR" con columnas "timestamp", "qr", "telefono" y "descripcion".
            //   db.execSQL("DROP TABLE IF EXISTS QRs");

           //Log.e("XOR",persistencia.encode("hola","chau"));
            // le paso a la Activida con el list view la referencia al objeto con la conexión a la base de datos
            ActivityHistorial.DB = db;

            //Inicializa como escaneo.
            txtDNI.setEnabled(false);
            txtApellido.setEnabled(false);
            txtNombres.setEnabled(false);

            btnLeerCodigo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VaciarCampos();
                    escanear();

                    txtDNI.setEnabled(false);
                    txtApellido.setEnabled(false);
                    txtNombres.setEnabled(false);
                    showSettingAlert();
                }
            });

            btnManual.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // vaciamos los  campos de texto
                    VaciarCampos();

                    txtDNI.setEnabled(true);
                    txtApellido.setEnabled(true);
                    txtNombres.setEnabled(true);
                    txtDNI.setFocusableInTouchMode(true);
                    txtDNI.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txtDNI, InputMethodManager.SHOW_IMPLICIT);
                    showSettingAlert();
                }
            });

            btnGuardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guardarEnBaseDeDatos();
                    getLocationShot();
                }
            });

            btnVer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*if (checkPermission(WRITE_EXTERNAL_STORAGE, 101)) {
                        Intent intent = new Intent(MainActivity.this, ActivityHistorial.class);
                        startActivity(intent);
                    }*/
                    Intent intent = new Intent(MainActivity.this, ActivityHistorial.class);
                    startActivity(intent);
                }
            });

            txtDNI.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus)
                        if(!txtDNI.getText().toString().isEmpty()){
                            llenarTelefonoHistorico(txtDNI.getText().toString().trim());
                            if(txtApellido.getText().toString().isEmpty()){
                                llenarApeNomHistorico(txtDNI.getText().toString().trim());
                            }
                        }
                }
            });

            getLocationShot();
            getLocation();

            if (USUARIO.isEmpty())
            {
                //Abre ventana de login GMAIL.
                /*Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                        new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);*/
                Intent intent =
                        AccountPicker.newChooseAccountIntent(
                                new AccountPicker.AccountChooserOptions.Builder()
                                        .setAllowableAccountsTypes(Arrays.asList("com.google"))
                                        .build());
                startActivityForResult(intent, 0x0000c0dc);
            }

            //inicia tarea background de envio masivo movimientos.
            if(!EncendioTarea){
                AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                Intent alarmIntent = new Intent(this, AlarmEnviadorReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
                int interval = 3 * 60 * 1000; //3 minutos
                manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
                EncendioTarea = true;
            }

            persistencia.Eliminar30Dias();

            CheckDatosPendientes();

            CheckVersion();
            ActualizarUI();

    }

    public void CheckDatosPendientes(){
        Integer dias = persistencia.getCantidadDiasPendientes();
        if(dias >= 1)
        {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setCancelable(false);
            alertDialog.setTitle("Trazar - Trazabilidad Digital");
            alertDialog.setMessage("Por favor conecte a Internet su dispositivo para" +
                    " subir la trazabilidad al servidor principal de Salud.");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            if(dias >=4){
                alertDialog.setMessage("Para continuar usando la App, por favor conecte a Internet su dispositivo para" +
                        " subir la trazabilidad al servidor principal de Salud.");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                    }
                });
            }

            alertDialog.show();
        }
    }

    private void CheckVersion() {
        new AsyncTask<String, Void, String>() {
            AlertDialog.Builder alertDialog;
            Map<String,Object> version;
            @Override
            protected String doInBackground(String... strings) {
                version = persistencia.verifyVersion();
                return null;
            }

            protected void onPreExecute() {
                super.onPreExecute();
                alertDialog = new AlertDialog.Builder(MainActivity.this);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                if(version != null){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setCancelable(false);
                    alertDialog.setTitle("Trazar - Trazabilidad Digital");
                    alertDialog.setMessage(version.get("MSG").toString());
                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(version.get("ACTUALIZA").toString().contains("SI"))
                                MainActivity.this.finish();
                            else
                            {
                                dialog.cancel();
                                //finish();
                            }

                            //Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //startActivity(intent);
                        }
                    });
                    /*alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            finish();
                        }
                    });*/
                    alertDialog.show();
                }
            }






        }.execute();
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
    private void llenarTelefonoHistorico(String dni) {
        dni = dni.replaceAll("[^0-9]+","");
        if (dni.length()>5)
            txtTelefono.setText(persistencia.getUltimoTelefono(dni));
    }
    private void llenarApeNomHistorico(String dni) {
        dni = dni.replaceAll("[^0-9]+","");
        if (dni.length()>5) {
            txtApellido.setText(persistencia.getUltimoApellido(dni));
            txtNombres.setText(persistencia.getUltimoNombres(dni));
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        // put your code here...
        //getLocation2();
        //getLocation();
    }

    private void VaciarCampos() {
        // vaciamos los  campos de texto
        Genero = "";
        txtDNI.setText("");
        txtApellido.setText("");
        txtNombres.setText("");
        txtTelefono.setText("");
        txtDescripcion.setText("");
    }

    public boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{permission},
                    requestCode
            );
        }
        return ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void guardarEnBaseDeDatos() {
        if (txtDNI.getText().toString().replaceAll("[^0-9]+","").isEmpty()) {
            CustomToast.showError(this,
                    "Debe ingresar el Dni.",
                    Toast.LENGTH_SHORT);
            return;
        }
        if (txtApellido.getText().toString().isEmpty()) {
            CustomToast.showError(this,
                    "Debe ingresar el Apellido",
                    Toast.LENGTH_SHORT);
            return;
        }
        if (txtNombres.getText().toString().isEmpty()) {
            CustomToast.showError(this,
                    "Debe ingresar el Nombre",
                    Toast.LENGTH_SHORT);
            return;
        }

        if (txtTelefono.getText().toString().length()>10) {
            CustomToast.showError(this,
                    "El Teléfono no puede superar los 10 digitos",
                    Toast.LENGTH_SHORT);
            return;
        }

        if (txtTelefono.getText().toString().replaceAll("[^0-9]+","").isEmpty()
                && txtTelefono.getVisibility() == View.VISIBLE ) {
            CustomToast.showError(this,
                    "Debe ingresar el Teléfono",
                    Toast.LENGTH_SHORT);
            return;
        }

        final Establecimiento establecimientoLocal = persistencia.getEstablecimientoLocal();
        idUsuEstab = establecimientoLocal.IdUsuEstab;
        if(idUsuEstab> 0)
        {
            findViewById(R.id.progressBarMain).setVisibility(View.VISIBLE);
            SingleShotLocationProvider.requestSingleUpdate(MainActivity.this,
                    new SingleShotLocationProvider.LocationCallback() {
                        @Override
                        public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {

                            if(location!= null && location.latitude!=0)
                            {
                                persistencia.GuardarVisita( txtDNI.getText().toString().replaceAll("[^0-9]+","").trim(),
                                        txtApellido.getText().toString(),
                                        txtNombres.getText().toString(),
                                        Genero,
                                        txtTelefono.getText().toString().replaceAll("[^0-9]+","").trim(),
                                        String.valueOf(latitude),//location.latitude),
                                        String.valueOf(longitude),//location.longitude),
                                        txtDescripcion.getText().toString(),
                                        idUsuEstab,
                                        establecimientoLocal.TipoMovimientoActual);

                                CustomToast.showSuccess(MainActivity.this, "Visita registrada con éxito", Toast.LENGTH_SHORT);

                                VaciarCampos();
                                if(Genero.isEmpty())
                                {
                                    txtDNI.setFocusableInTouchMode(true);
                                    txtDNI.requestFocus();
                                }
                            }else
                            {
                                CustomToast.showError(MainActivity.this,
                                        "Ocurrio un error con el GPS de su dispositivo, por favor vuelva a intentar.", Toast.LENGTH_LONG);
                            }
                        }
                    }
             );

            findViewById(R.id.progressBarMain).setVisibility(View.GONE);
        }else
            CustomToast.showError(MainActivity.this, "Ocurrio un error al guardar los datos, por favor cierre y vuelva a intentar.", Toast.LENGTH_LONG);

        CheckDatosPendientes();
    }

    public void escanear() {
        /*IntentIntegrator intent = new IntentIntegrator( this);
         intent.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        //intent.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        intent.setOrientationLocked(true);
        intent.setPrompt("ESCANEAR CODIGO");
        intent.setCameraId(0);
        intent.setBeepEnabled(false);
        intent.setBarcodeImageEnabled(false);

        intent.initiateScan();*/


        IntentIntegrator intent = new IntentIntegrator( this);
        intent.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intent.setOrientationLocked(false);
        intent.setPrompt("ESCANEAR CODIGO");
        intent.setCameraId(0);
        intent.setBeepEnabled(false);
        intent.setBarcodeImageEnabled(false);
        intent.setCaptureActivity(CaptureActivityPortrait.class);

        intent.initiateScan();

        CustomToast.showInfo(this,"Para activar el Flash presione la tecla Volumen Arriba.", Toast.LENGTH_LONG);
    }

    public void getLocationShot() {
        SingleShotLocationProvider.requestSingleUpdate(this,
                new SingleShotLocationProvider.LocationCallback() {
                    @Override public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                        latitude = location.latitude;
                        longitude = location.longitude;
                    }
                });

    }

    protected void getLocation() {
        locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        // bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString(); // casteos redundantes
        bestProvider =locationManager.getBestProvider(criteria, true);
        //You can still do this if you like, you might get lucky:
        if (checkPermission(ACCESS_FINE_LOCATION, 1) &&  checkPermission(ACCESS_COARSE_LOCATION, 1)) {
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location == null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                location = locationManager.getLastKnownLocation(bestProvider);
            };


            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

                // Toast.makeText(MainActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
/*
            } else {
                Log.d("PEPE", "requestLocationUpdates");
                //This is what you need:
                //locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
*/

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        //remove location callback:
        locationManager.removeUpdates(this);

        //open the map:
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        // Toast.makeText(MainActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        //Si cerro el registro inicial
        if(requestCode == 0x0000c0dd) {
            if( resultCode == RESULT_OK){
                idUsuEstab = Persistencia.getIdUsuEstabDBLocal();
                getLocationShot();
                supportInvalidateOptionsMenu();
                findViewById(R.id.progressBarMain).setVisibility(View.GONE);
            }else
            {
                idUsuEstab = Persistencia.getIdUsuEstabDBLocal();
                if(idUsuEstab == 0) {
                    //Inicia proceso de registración
                    Intent documento = new Intent(MainActivity.this, DocumentoActivity.class);
                    startActivityForResult(documento, 0x0000c0dd);
                }
            }
            return;
        }


        //Cuando responde el selector de cuentas
        if (requestCode == 0x0000c0dc ) {

            if( resultCode == RESULT_OK){
                USUARIO = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                lblUsuario.setText("Usuario: " + USUARIO);
                //Toast.makeText(MainActivity.this, lblUsuario.getText(), Toast.LENGTH_SHORT).show();

                //Abre Activity Documento
                //si el Establecimiento no existe... abre..
                idUsuEstab = Persistencia.getIdUsuEstabDBLocal();
                if(idUsuEstab == 0) {
                    //Inicia proceso de registración
                    findViewById(R.id.progressBarMain).setVisibility(View.VISIBLE);
                    Intent documento = new Intent(MainActivity.this, DocumentoActivity.class);
                    startActivityForResult(documento, 0x0000c0dd);
                }

            }else
                finish();
            return;
        }

        // Cuando responde el escaneador de barra
        if(requestCode == 0x0000c0de && result != null) {
            if (result.getContents() == null) {
                CustomToast.showInfo(MainActivity.this, "Cancelaste el escaneo", Toast.LENGTH_SHORT);
            } else {
                if (result.getFormatName().contains("PDF_417")) {
                    String barraLeida = result.getContents();
                    txtDNI.setText(dividirBarra(barraLeida, SubBarra.DNI));
                    txtApellido.setText(dividirBarra(barraLeida, SubBarra.APELLIDO));
                    txtNombres.setText(dividirBarra(barraLeida, SubBarra.NOMBRES));
                    Genero = dividirBarra(barraLeida, SubBarra.GENERO);
                    txtTelefono.setFocusableInTouchMode(true);
                    txtTelefono.requestFocus();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    //imm.showSoftInput(txtTelefono, InputMethodManager.SHOW_FORCED);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


                    llenarTelefonoHistorico(txtDNI.getText().toString().trim());
                }
                else
                    CustomToast.showError(MainActivity.this, "Código de Barra no válido:("+result.getFormatName()+")", Toast.LENGTH_LONG);
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    public static enum SubBarra{TRAMITE, APELLIDO, NOMBRES, GENERO, DNI, EJEMPLAR, FECHA_NACIMIENTO, FECHA_EMISION};
    enum SubBarra2{VACIO, DNI,EJEMPLAR, VERSION, APELLIDO, NOMBRES, NACIONALIDAD, FECHA_NACIMIENTO, GENERO, FECHA_EMISION, TRAMITE};

    public static String dividirBarra(String barra, SubBarra busqueda){
        String[] tokens = barra.split("@");
        if (tokens.length<10)
            return tokens[busqueda.ordinal()];
        else {
            SubBarra2 busqueda2 = SubBarra2.valueOf(busqueda.name());
            return tokens[busqueda2.ordinal()];
        }

    }
}
