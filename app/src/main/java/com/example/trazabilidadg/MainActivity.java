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
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;

import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
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

import java.util.Arrays;
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
        /*Establecimiento establecimiento = persistencia.getEstablecimientoLocal();
        if(establecimiento != null){
            if(establecimiento.RegistraSalidas)
                _menu.findItem(R.id.registrarSalidas).setVisible(true);
            else
                _menu.findItem(R.id.registrarSalidas).setVisible(false);
        }*/
        _menu.findItem(R.id.registrarSalidas).setVisible(true);
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
        String titulo = "Registro de ENTRADAS";
        ((TextView) findViewById(R.id.titulo)).setTextColor(Color.rgb(7, 32, 173));
        Establecimiento establecimiento = persistencia.getEstablecimientoLocal();

        if (establecimiento != null)
            if (establecimiento.TipoMovimientoActual != null && establecimiento.TipoMovimientoActual.equals("SALIDA")) {
                titulo = "Registro de SALIDAS";

                if(establecimiento.Salida_telefono.equals("NO"))
                    findViewById(R.id.txtTelefono).setVisibility(View.GONE);

                ((TextView) findViewById(R.id.titulo)).setTextColor(Color.rgb(15, 127, 31));
            } else {
                findViewById(R.id.txtTelefono).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.titulo)).setTextColor(Color.rgb(7, 32, 173));
            }

        ((TextView) findViewById(R.id.titulo)).setText(titulo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //registerReceiver(new NetworkReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        USUARIO = getIntent().getStringExtra("USUARIO");
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

        persistencia = ((Persistencia) getApplication());

        //Inicializa como escaneo.
        txtDNI.setEnabled(false);
        txtApellido.setEnabled(false);
        txtNombres.setEnabled(false);

        btnLeerCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VaciarCampos();
                escanear();
                setUltimaSincronizacion();

                txtDNI.setEnabled(false);
                txtApellido.setEnabled(false);
                txtNombres.setEnabled(false);
                showSettingAlert();
                //persistencia.getLocationShot();
            }
        });

        btnManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // vaciamos los  campos de texto
                VaciarCampos();
                setUltimaSincronizacion();

                txtDNI.setEnabled(true);
                txtApellido.setEnabled(true);
                txtNombres.setEnabled(true);
                txtDNI.setFocusableInTouchMode(true);
                txtDNI.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(txtDNI, InputMethodManager.SHOW_IMPLICIT);
                showSettingAlert();
                //persistencia.getLocationShot();
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUltimaSincronizacion();
                guardarEnBaseDeDatos();
                //getLocationShot();
            }
        });

        btnVer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUltimaSincronizacion();
                Intent intent = new Intent(MainActivity.this, ActivityHistorial.class);
                startActivity(intent);
            }
        });

        txtDNI.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    if (!txtDNI.getText().toString().isEmpty()) {
                        llenarTelefonoHistorico(txtDNI.getText().toString().trim());
                        if (txtApellido.getText().toString().isEmpty()) {
                            llenarApeNomHistorico(txtDNI.getText().toString().trim());
                        }
                    }
            }
        });


        //inicia tarea background de envio masivo movimientos.
        if (!EncendioTarea) {
            AlarmManager manager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(this, AlarmEnviadorReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
            int interval = 5 * 60 * 1000; //5 minutos
            manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, interval,pendingIntent);
            EncendioTarea = true;
        }


        idUsuEstab = persistencia.getIdUsuEstabDBLocal();
        if (idUsuEstab == 0) {
            //Inicia proceso de registración
            findViewById(R.id.progressBarMain).setVisibility(View.VISIBLE);
            Intent documento = new Intent(MainActivity.this, DocumentoActivity.class);
            startActivityForResult(documento, 0x0000c0dd);
        }


        checkPermission(ACCESS_FINE_LOCATION, 1);
        checkPermission(ACCESS_COARSE_LOCATION, 1);

        //SolicitarEmail();
        CheckVersion();
        CheckDatosPendientes();
        ActualizarUI();
        persistencia.Eliminar30Dias();
        setUltimaSincronizacion();
    }

    private void SolicitarEmail() {
        if (persistencia.getEstablecimientoLocal() == null) {
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
    }

    public void CheckDatosPendientes() {
        Integer dias = persistencia.getCantidadDiasPendientes();
        if (dias >= 1) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setCancelable(false);
            alertDialog.setTitle("Trazar - Trazabilidad Digital");
            alertDialog.setMessage("Por favor conecte a Internet su dispositivo para" +
                    " subir la información al Sistema Central de Trazabilidad Digital de Salud." +
                    " Verifique tambien que la fecha y hora de su dispositivo sean correctas.");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            if (dias >= 4) {
                alertDialog.setMessage("Para continuar usando la App, por favor conecte a Internet su dispositivo para" +
                        " subir la información al Sistema Central de Trazabilidad Digital de Salud." +
                        " Verifique tambien que la fecha y hora de su dispositivo sean correctas.");
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
            Map<String, Object> version;

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

                if (version != null) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setCancelable(false);
                    alertDialog.setTitle("Trazar - Trazabilidad Digital");
                    alertDialog.setMessage(version.get("MSG").toString());
                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (version.get("ACTUALIZA").toString().contains("SI"))
                                MainActivity.this.finish();
                            else {
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


    private boolean getGPSStatus() {
        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String allowedLocationProviders =
                Settings.System.getString(getContentResolver(),
                        Settings.System.LOCATION_PROVIDERS_ALLOWED);

        if (allowedLocationProviders == null) {
            allowedLocationProviders = "";
        }

        return allowedLocationProviders.contains(manager.GPS_PROVIDER);
    }

    public void showSettingAlert() {

        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!getGPSStatus() || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
        dni = dni.replaceAll("[^0-9]+", "");
        if (dni.length() > 5)
            txtTelefono.setText(persistencia.getUltimoTelefono(dni));
    }

    private void llenarApeNomHistorico(String dni) {
        dni = dni.replaceAll("[^0-9]+", "");
        if (dni.length() > 5) {
            txtApellido.setText(persistencia.getUltimoApellido(dni));
            txtNombres.setText(persistencia.getUltimoNombres(dni));
        }
    }

    @Override
    public void onResume() {
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

    private void setUltimaSincronizacion() {
        String fecha = persistencia.getUltimaSincronizacion();

        if (!fecha.isEmpty()) {
            TextView ultSinc = findViewById(R.id.ultSincronizacion);
            ultSinc.setText("Última Sincronización con Sistema Central de Trazabilidad Digital de Salud: " + fecha);
        }
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

        final String _dni = txtDNI.getText().toString().replaceAll("[^0-9]+","").trim();
        final String _ape = txtApellido.getText().toString();
        final String _nom = txtNombres.getText().toString();
        final String _gen = Genero;
        final String _tel = txtTelefono.getText().toString().replaceAll("[^0-9]+","").trim();

        if (_dni.isEmpty()) {
            CustomToast.showError(this,
                    "Debe ingresar el Dni.",
                    Toast.LENGTH_SHORT);
            return;
        }
        if (_dni.length() < 6
            || _dni.length() > 8 ) {
            CustomToast.showError(this,
                    "El Dni ingresado es inválido.",
                    Toast.LENGTH_SHORT);
            return;
        }

        if (_ape.isEmpty() ) {
            CustomToast.showError(this,
                    "Debe ingresar el Apellido",
                    Toast.LENGTH_SHORT);
            return;
        }
        if (_ape.length() < 2 || _ape.length() > 50) {
            CustomToast.showError(this,
                    "El Apellido ingresado es inválido",
                    Toast.LENGTH_SHORT);
            return;
        }

        if (_nom.isEmpty()) {
            CustomToast.showError(this,
                    "Debe ingresar el Nombre",
                    Toast.LENGTH_SHORT);
            return;
        }
        if (_nom.length() < 2 || _nom.length() > 50) {
            CustomToast.showError(this,
                    "El Nombre ingresado es inválido",
                    Toast.LENGTH_SHORT);
            return;
        }

        if (_tel.isEmpty() && txtTelefono.getVisibility() == View.VISIBLE ) {
            CustomToast.showError(this,
                    "Debe ingresar el Teléfono",
                    Toast.LENGTH_SHORT);
            return;
        }

        if ((_tel.length()>10 || _tel.length() <= 5) & txtTelefono.getVisibility() == View.VISIBLE) {
            CustomToast.showError(this,
                    "El Teléfono ingresado es inválido",
                    Toast.LENGTH_SHORT);
            return;
        }

        btnGuardar.setEnabled(false);
        findViewById(R.id.progressBarMain).setVisibility(View.VISIBLE);


        final Establecimiento establecimientoLocal = persistencia.getEstablecimientoLocal();
        idUsuEstab = establecimientoLocal.IdUsuEstab;
        if(idUsuEstab > 0)
        {

            //getLocationShot();


            Location location = persistencia.getLocation();
            double lat = 0;
            double lon = 0;
            if(location != null){
                lat = location.getLatitude();
                lon = location.getLongitude();
            }


            /*if(location != null && location.getLatitude() != 0)
            {*/
                persistencia.GuardarVisita( _dni,
                        _ape,
                        _nom,
                        _gen,
                        _tel,
                        String.valueOf(lat),//location.latitude),
                        String.valueOf(lon),//location.longitude),
                        txtDescripcion.getText().toString(),
                        establecimientoLocal.IdUsuEstab,
                        establecimientoLocal.TipoMovimientoActual);

                if(establecimientoLocal.TipoMovimientoActual.equals("SALIDA"))
                    CustomToast.showSuccess(MainActivity.this,
                            "SALIDA registrada con éxito", Toast.LENGTH_SHORT);
                else
                    CustomToast.showSuccess(MainActivity.this,
                            "ENTRADA registrada con éxito", Toast.LENGTH_SHORT);

                if(_gen.isEmpty())
                {
                    txtDNI.setFocusableInTouchMode(true);
                    txtDNI.requestFocus();
                }
                VaciarCampos();
            /*}else
            {
                CustomToast.showError(MainActivity.this,
                        "Ocurrio un error con el GPS de su dispositivo, por favor vuelva a intentar.", Toast.LENGTH_LONG);
            }*/





            /*SingleShotLocationProvider.requestSingleUpdate(MainActivity.this,
                    new SingleShotLocationProvider.LocationCallback() {
                        @Override
                        public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {

                            if(location != null && location.latitude != 0)
                            {
                                persistencia.GuardarVisita( _dni,
                                        _ape,
                                        _nom,
                                        _gen,
                                        _tel,
                                        String.valueOf(location.latitude),//location.latitude),
                                        String.valueOf(location.longitude),//location.longitude),
                                        txtDescripcion.getText().toString(),
                                        establecimientoLocal.IdUsuEstab,
                                        establecimientoLocal.TipoMovimientoActual);

                                if(establecimientoLocal.TipoMovimientoActual.equals("SALIDA"))
                                    CustomToast.showSuccess(MainActivity.this,
                                            "SALIDA registrada con éxito", Toast.LENGTH_SHORT);
                                else
                                    CustomToast.showSuccess(MainActivity.this,
                                            "ENTRADA registrada con éxito", Toast.LENGTH_SHORT);

                                if(_gen.isEmpty())
                                {
                                    txtDNI.setFocusableInTouchMode(true);
                                    txtDNI.requestFocus();
                                }
                                VaciarCampos();
                            }else
                            {
                                CustomToast.showError(MainActivity.this,
                                        "Ocurrio un error con el GPS de su dispositivo, por favor vuelva a intentar.", Toast.LENGTH_LONG);
                            }
                        }
                    }

             );*/

            //findViewById(R.id.progressBarMain).setVisibility(View.GONE);
        }else
            CustomToast.showError(MainActivity.this, "Ocurrio un error al guardar los datos, por favor cierre y vuelva a intentar.", Toast.LENGTH_LONG);

        CheckDatosPendientes();
        btnGuardar.setEnabled(true);
        findViewById(R.id.progressBarMain).setVisibility(View.GONE);
    }

    public void escanear() {


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

    /*public void getLocationShot() {
        SingleShotLocationProvider.requestSingleUpdate(this,
                new SingleShotLocationProvider.LocationCallback() {
                    @Override public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                        latitude = location.latitude;
                        longitude = location.longitude;
                    }
                });

    }*/

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


        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        //remove location callback:
        //locationManager.removeUpdates(this);

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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(ACCESS_COARSE_LOCATION) || permission.equals(ACCESS_FINE_LOCATION) ) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED)
                        finish();
                    else
                        persistencia.BuscarLocation();
                }

            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        //Si cerro el registro inicial
        if(requestCode == 0x0000c0dd) {
            if( resultCode == RESULT_OK){
                idUsuEstab = persistencia.getIdUsuEstabDBLocal();
                supportInvalidateOptionsMenu();
                findViewById(R.id.progressBarMain).setVisibility(View.GONE);
            }else
            {
                idUsuEstab = persistencia.getIdUsuEstabDBLocal();
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

                checkPermission(ACCESS_FINE_LOCATION, 1);
                checkPermission(ACCESS_COARSE_LOCATION, 1);

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

    public String dividirBarra(String barra, SubBarra busqueda){
        String[] tokens = barra.split("@");
        if (tokens.length<10) {
            if (busqueda.ordinal() > tokens.length - 1)
                CustomToast.showError(MainActivity.this, "No se pudo leer el código, intente nuevamente.", Toast.LENGTH_LONG);

            return tokens[busqueda.ordinal()];
        } else {
            SubBarra2 busqueda2 = SubBarra2.valueOf(busqueda.name());
            if (busqueda2.ordinal() > tokens.length - 1)
                CustomToast.showError(MainActivity.this, "No se pudo leer el código, intente nuevamente.", Toast.LENGTH_LONG);

            return tokens[busqueda2.ordinal()];
        }

    }
}
