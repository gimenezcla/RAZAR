package com.example.trazabilidadg;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Base64;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

public class Persistencia extends Application {
  public SQLiteDatabase db;
  public final String versionActual = "1.1";

  private String UrlServidor;

  public Integer getIdUsuEstabDBLocal() {
    Cursor cursor = db.rawQuery("Select COALESCE(MAX(ID_USU_ESTAB),0) from ESTAB_USUARIO",null);
    cursor.moveToNext();
    return cursor.getInt(0);
  }

  public LinkedHashMap<String,Integer> getEstablecimientosPorCuit(String cuit) {
    //Seteo los parametros
    HashMap<String, String> param = new HashMap();
    param.put("CUIT", cuit);

    //realiza el post
    Map<String,Object> retorno = null;
    try {
      retorno = Post(UrlServidor+
                      new String(Base64.decode("L0dQU0xfZ2V0RXN0YWJQb3JDdWl0Lw=="
              ,Base64.DEFAULT),"UTF-8")
              ,param);//"/GPSL_getEstabPorCuit/"

      LinkedHashMap<String, Integer> Establecimientos = new LinkedHashMap<>();

      if (retorno != null) {
        String csvEstablecimientos = retorno.get("P_LISTA").toString();

        String str[] = csvEstablecimientos.split("#");
        for (int i = 0; i < str.length; i++) {
          String arr[] = str[i].split(";");
          Establecimientos.put(arr[1], Integer.parseInt(arr[0]));
        }
      }
      return Establecimientos;
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }

  public ArrayList<String> getLocalidades() {
    ArrayList<String> aLocalidades  = new  ArrayList<String>();
    Cursor cursor = db.rawQuery("SELECT localidad||' - '||DEPARTAMENTO from localidades", null);
    if (cursor.moveToFirst()) {
      do {
        aLocalidades.add(cursor.getString(0));
      } while (cursor.moveToNext());
    }
    return aLocalidades;
  }

  public Integer GuardarUsuario(Integer idEstablecimiento, String nombreEstablecimiento, String usuario, String telefonoUsu) {
    //Seteo los parametros
    HashMap<String, String> param = new HashMap();
    param.put("ID_ESTABLECIMIENTO", idEstablecimiento.toString());
    param.put("USUARIO", usuario);
    param.put("TELEFONO_USU", telefonoUsu);

    Map<String,Object> retorno =
            null;
    try {
      retorno = Post(UrlServidor+
                      new String(Base64.decode("L0dQU0xfc2V0VXN1YXJpby8="
                              ,Base64.DEFAULT),"UTF-8")
              ,param);//"/GPSL_setUsuario/"
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    Integer IdUsuEstab = null;

    if(retorno != null) {
      IdUsuEstab = Integer.parseInt(retorno.get("P_ID_USU_ESTAB").toString());
      String registraSalidas = retorno.get("P_REGISTRA_SALIDA").toString() == "SI" ? "1" : "0";


      SQLiteStatement consulta = db.compileStatement(
              " INSERT INTO ESTAB_USUARIO( ID_USU_ESTAB , NOMBRE ," +
                      "REGISTRA_SALIDA, TELEFONO_USU ) values (?, ?, ?, ?)"
      );

      consulta.bindString(1, IdUsuEstab.toString());
      consulta.bindString(2, nombreEstablecimiento);
      consulta.bindString(3, registraSalidas);
      consulta.bindString(4, telefonoUsu);

      consulta.executeInsert();

      //Recupera todos los datos del establecimiento.
      ActualizarDatosLocalEstablecimiento();
    }
    return IdUsuEstab;
  }

  public int GuardarUsuarioEstablecimiento(String cuitDniResponsable, String nombreEstablecimiento, String nombreResponsable,
                                           String domicilio, String telefono, String localidad,
                                           Boolean registraSalidas, String usuario, String permanencia, String telefonoUsu,
                                           double latitude, double longitude)
  {
    //Seteo los parametros
    HashMap<String, String> param = new HashMap();
    param.put("CUIT", cuitDniResponsable);
    param.put("NOMBRE", nombreEstablecimiento);
    param.put("RESPONSABLE", nombreResponsable);
    param.put("TELEFONO", telefono);
    param.put("DOMICILIO", domicilio);
    param.put("LOCALIDAD", localidad);
    param.put("SALIDAS", registraSalidas ? "SI" : "NO");
    param.put("USUARIO", usuario);
    param.put("TELEFONO_USU", telefonoUsu);
    param.put("PERMANENCIA", permanencia);
    param.put("LATITUD", String.valueOf(latitude));
    param.put("LONGITUD", String.valueOf(longitude));

    Map<String,Object> retorno = null;
    try {
      retorno = Post(UrlServidor+
                      new String(Base64.decode("L0dQU0xfc2V0VXN1RXN0YWIv"
                              ,Base64.DEFAULT),"UTF-8")
              ,param); //"/GPSL_setUsuEstab/"
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    Integer IdUsuEstab = null;
    if(retorno != null)
    {
    IdUsuEstab = Integer.parseInt(retorno.get("P_ID_USU_ESTAB").toString());

    SQLiteStatement consulta = db.compileStatement(
            " INSERT INTO ESTAB_USUARIO( ID_USU_ESTAB , NOMBRE , CUIT_DNI , LOCALIDAD , LATITUD , LONGITUD , RESPONSABLE , TELEFONO , " +
                    "DOMICILIO , REGISTRA_SALIDA , TIEMPO_PERMANENCIA, TELEFONO_USU, TIPO_MOVIMIENTO_ACTUAL ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    );
    consulta.bindString(1, IdUsuEstab.toString());
    consulta.bindString(2, nombreEstablecimiento);
    consulta.bindString(3, cuitDniResponsable);
    consulta.bindString(4, localidad);
    consulta.bindString(5, String.valueOf(latitude));
    consulta.bindString(6, String.valueOf(longitude));
    consulta.bindString(7, nombreResponsable);
    consulta.bindString(8, telefono);
    consulta.bindString(9, domicilio);
    consulta.bindString(10, registraSalidas?"1":"0");
    consulta.bindString(11, permanencia);
    consulta.bindString(12, telefonoUsu);
    consulta.bindString(13, "ENTRADA");

    consulta.executeInsert();

    }
    return IdUsuEstab;
  }



  @Override
  public void onCreate() {
    super.onCreate();

    db = openOrCreateDatabase("GermanDB", Context.MODE_PRIVATE, null);;
    inicializarTablas();
    EjecutarActualizacionDeTablas();

    try {
      //https://www.programainformatico.sanluis.gob.ar/ords/salud/GPSL_Trazabilidad
      UrlServidor = new String(Base64.decode("aHR0cHM6Ly93d3cucHJvZ3JhbWFpbmZvcm1hdGljby5zYW5sdWlzLmdvYi5hci9vcmRzL3NhbHVkL0dQU0xfVHJhemFiaWxpZGFk"
              ,Base64.DEFAULT),"UTF-8") + "Test";
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  private void EjecutarActualizacionDeTablas() {
    try {
      db.execSQL("ALTER TABLE ESTAB_USUARIO ADD COLUMN TIPO_MOVIMIENTO_ACTUAL VARCHAR DEFAULT 'ENTRADA'");
    } catch (SQLiteException ex) {
      Log.w("UPDATE", "Altering : ESTAB_USUARIO " + ex.getMessage());
    }

    try {
      SQLiteStatement consulta = db.compileStatement(
              " UPDATE LOCALIDADES set LOCALIDAD = 'SAN JERONIMO' where LOCALIDAD = 'SAN GERONIMO'"
      );
      consulta.executeUpdateDelete();
    }catch (Exception e){
      Log.w("UPDATE", "Localidades " + e.getMessage());
    }

    try {
      db.execSQL("ALTER TABLE ESTAB_USUARIO ADD COLUMN ULTIMA_SINCRONIZACION TIMESTAMP");
    } catch (SQLiteException ex) {
      Log.w("UPDATE", "Altering : ESTAB_USUARIO " + ex.getMessage());
    }
  }

  public String getUltimaSincronizacion() {
    Cursor fila = db.rawQuery("SELECT max(ULTIMA_SINCRONIZACION) FROM ESTAB_USUARIO ", null);

    DateFormat formatterServer = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    formatterServer.setTimeZone(TimeZone.getTimeZone("UTC−03:00"));
    DateFormat formatterClient = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());;

    while(fila.moveToNext()) {
      Date date = null;
      try {
        if(fila.getString(0) == null || fila.getString(0).isEmpty())
          return "";

        date = (Date) formatterServer.parse(fila.getString(0));
        return formatterClient.format(date);

      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    return "";
  }

  public void GuardarVisita(String Dni, String Apellido, String Nombres, String Genero, String Telefono, String Latitud,
                            String Longitud, String Descripcion, Integer idUsuEstab, String Tipo)
  {
    SQLiteStatement consulta = db.compileStatement(
            "INSERT INTO QRs( DNI, APELLIDO, NOMBRES, GENERO, " +
                    "TIPO_MOVIMIENTO,TELEFONO,DESCRIPCION,LATITUD,LONGITUD,ID_USU_ESTAB,enviado)" +
                    " VALUES(?, ?, ?, ?, ?, ?, ?,?,?,?,?)"
    );

    consulta.bindString(1, Dni);
    consulta.bindString(2, Apellido);
    consulta.bindString(3, Nombres);
    consulta.bindString(4, Genero);
    consulta.bindString(5, Tipo);
    consulta.bindString(6, Telefono);
    consulta.bindString(7, Descripcion);
    consulta.bindString(8, Latitud);
    consulta.bindString(9, Longitud);
    consulta.bindString(10, getIdUsuEstabDBLocal().toString());
    consulta.bindString(11, "0");

    // ahora ejecutamos la consulta
    consulta.executeInsert();
  }

  public Map<String, Object> Post(String _url, HashMap<String,String> params){

    URL githubEndpoint = null;
    try {
      githubEndpoint = new URL(_url);
      HttpsURLConnection myConnection =
              (HttpsURLConnection) githubEndpoint.openConnection();
      myConnection.setRequestProperty(new String(Base64.decode("VXNlci1BZ2VudA==",Base64.DEFAULT),"UTF-8"),
              new String(Base64.decode("VHJhemFiaWxpZGFkIEdQU0wt",Base64.DEFAULT),"UTF-8") +
                      new SimpleDateFormat(new String(Base64.decode("ZGRNTXl5eXk=",Base64.DEFAULT),"UTF-8")).format(new Date()));
      myConnection.setRequestProperty("Content-Type", "application/json; utf-8");
      myConnection.setRequestMethod("POST");
      myConnection.setDoOutput(true);
      OutputStream os = myConnection.getOutputStream();

      byte[] input = new JSONObject(params).toString().getBytes("utf-8");
      os.write(input, 0, input.length);

      if (myConnection.getResponseCode() == 200) {

        BufferedReader br = new BufferedReader(
                new InputStreamReader(myConnection.getInputStream(), "utf-8"));

        StringBuilder response = new StringBuilder();
        String responseLine = null;
        while ((responseLine = br.readLine()) != null) {
          response.append(responseLine.trim());}
        Log.e("RESp POST","");
        if(!response.toString().isEmpty())
        {
          Log.e("RESp POST",response.toString());
          return jsonToMap(new JSONObject(response.toString()));
        }
        else
          return null;

      } else {
        // Error handling code goes here
        BufferedReader br_err = new BufferedReader(
                new InputStreamReader(myConnection.getErrorStream(), "utf-8"));

        StringBuilder response = new StringBuilder();
        String responseLine = null;
        while ((responseLine = br_err.readLine()) != null) {
          response.append(responseLine.trim());}
        String Error = response.toString();
        Log.e("Error POST",Error);
        return null;
      }
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }


  public void EnviarVisitasMasivasPendientes() {

    Cursor fila = db.rawQuery("SELECT *  FROM QRs " +
                    " where ENVIADO = 0 " +
                    /*" and ifnull(NOMBRES,'') != '' " +
                    " and ifnull(APELLIDO,'') != '' " +
                    " and ifnull(DNI,'') != '' " +
                    " and ifnull(ID_USU_ESTAB,'') != '' " +*/
                    " ORDER BY ID_REGISTRO", null);

    String ultimoId = "";
    String CSV = "";
    DateFormat formatterServer = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    formatterServer.setTimeZone(TimeZone.getTimeZone("UTC−03:00"));
    DateFormat formatterClient = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());;

    while(fila.moveToNext()) {
      ultimoId = fila.getString(0);

      Date date = null;
      try {
        date = (Date) formatterServer.parse(fila.getString(1));
      } catch (ParseException e) {
        e.printStackTrace();
      }
      CSV += limpiarString(formatterClient.format(date)) + ";"; //  fila.getString(1)
      CSV += limpiarString(fila.getString(2)) + ";";
      CSV += limpiarString(fila.getString(3)) + ";";
      CSV += limpiarString(fila.getString(4)) + ";";
      CSV += limpiarString(fila.getString(5)) + ";";
      CSV += limpiarString(fila.getString(6)) + ";";
      CSV += limpiarString(fila.getString(7)) + ";";
      CSV += limpiarString(fila.getString(8)) + ";";
      CSV += limpiarString(fila.getString(9)) + ";";
      CSV += limpiarString(fila.getString(10)) + ";";
      CSV += limpiarString(fila.getString(11)) + "#";
    }

    if(fila.getCount()>0) {
      //Elimina el ultimo numeral #.
      CSV = CSV.substring(0, CSV.length() - 1);

      //Seteo los parametros
      HashMap<String, String> param = new HashMap();
      param.put("CSV", CSV);

      try {
        if(Post(UrlServidor +
                        new String(Base64.decode("L0dQU0xfc2V0VmlzaXRhcy8="
                                ,Base64.DEFAULT),"UTF-8")
                , param)!= null) //"/GPSL_setVisitas/"
        {
          SQLiteStatement consulta = db.compileStatement(
                  " UPDATE QRs set ENVIADO = 1 " +
                          " where ENVIADO = 0 " +
                          /*" and ifnull(NOMBRES,'') != '' " +
                          " and ifnull(APELLIDO,'') != '' " +
                          " and ifnull(DNI,'') != '' " +
                          " and ifnull(ID_USU_ESTAB,'') != '' " +*/
                          " and ID_REGISTRO <= ? "
          );
          consulta.bindString(1, ultimoId);
          consulta.executeUpdateDelete();

          //Actualiza ultima fecha de sincronizacion.
          consulta = db.compileStatement(
                  "UPDATE ESTAB_USUARIO set ULTIMA_SINCRONIZACION = CURRENT_TIMESTAMP"
          );
          consulta.executeUpdateDelete();

        }
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }

  }

  private String limpiarString(String cadena) {
    return cadena
            .replace(";","")
            .replace("&","")
            .replace("#","");
  }

  private void inicializarTablas() {
    db.execSQL(
            "CREATE TABLE IF NOT EXISTS QRs(" +
                    "id_registro INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "FECHA TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "DNI VARCHAR," +
                    "APELLIDO VARCHAR," +
                    "NOMBRES VARCHAR," +
                    "GENERO VARCHAR," +
                    "TIPO_MOVIMIENTO VARCHAR," +
                    "TELEFONO VARCHAR," +
                    "DESCRIPCION VARCHAR," +
                    "LATITUD VARCHAR," +
                    "LONGITUD VARCHAR," +
                    "ID_USU_ESTAB VARCHAR," +
                    "enviado BOOLEAN" +
                    ")"
    );


//    db.execSQL("DROP TABLE ESTAB_USUARIO");
    db.execSQL("CREATE TABLE IF NOT EXISTS ESTAB_USUARIO(" +
            "        ID_USU_ESTAB INTEGER," +
            "        NOMBRE VARCHAR, " +
            "        CUIT_DNI VARCHAR, " +
            "        LOCALIDAD VARCHAR, " +
            "        LATITUD VARCHAR, " +
            "        LONGITUD VARCHAR, " +
            "        RESPONSABLE VARCHAR, " +
            "        ID_TIPO_ESTABLECIMIENTO VARCHAR, " +
            "        FECHA_REGISTRACION DEFAULT CURRENT_TIMESTAMP," +
            "        TELEFONO VARCHAR," +
            "        TELEFONO_USU VARCHAR,"+
            "        DOMICILIO VARCHAR," +
            "        REGISTRA_SALIDA VARCHAR," +
            "        TIEMPO_PERMANENCIA VARCHAR," +
            "        enviado BOOLEAN" +
           // "        ,TIPO_MOVIMIENTO_ACTUAL VARCHAR DEFAULT 'ENTRADA' "+
            ")");
    db.execSQL("CREATE TABLE IF NOT EXISTS VERSION(" +
            "        N_VERSION VARCHAR," +
            "        MSG VARCHAR," +
            "        ACTUALIZA VARCHAR" +
            ")");
    db.execSQL("CREATE TABLE IF NOT EXISTS LOCALIDADES(" +
            //     "        ID_LOCALIDAD INTEGER PRIMARY KEY AUTOINCREMENT," +
            "        LOCALIDAD VARCHAR," +
            "        DEPARTAMENTO VARCHAR" +
            ")");
    if (db.rawQuery("SELECT 1 FROM LOCALIDADES",new String[]{}).getCount()==0) {
      db.execSQL("INSERT into LOCALIDADES VALUES (\"AGUA DE LOS MOLLES\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"ALTO DEL MOLLE EL PUESTO LAGUNA LARGA\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"ALTO PELADO\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"ALTO PENCOSO\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"ALTO PENCOSO Y CHOSME\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"ANCHORENA\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"ARIZONA\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"BALCARCE\",\"CHACABUCO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"BALDE\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"BALDE DE ESCUDERO\",\"JUNIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"BALDE DE LA ISLA\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"BALZORA\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"BATAVIA\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"BEAZLEY\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"BUEN ORDEN\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"BUENA ESPERANZA\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CABEZA DE VACA\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CALDENADAS\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CANDELARIA\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CAÑADA HONDA\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CAÑADA LA NEGRA\",\"JUNIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CAROLINA\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CARPINTERIA\",\"JUNIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CERRO LA GARRAPATA\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CERROS LARGOS\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CHOSME\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CONCARAN\",\"CHACABUCO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CORTADERA\",\"JUNIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CORTADERAS\",\"CHACABUCO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CRUZ DE PIEDRA\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"CUCHI CORRAL\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"DESAGUADERO\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"DONOVAN\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"DUPUY\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"EL BAGUAL\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"EL BAGUAL\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"EL BAJADA\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"EL BALDE\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"EL BARRIAL\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"EL MANZANO\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"EL MILAGRO\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"EL MORRO\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"EL PALIGUANTE\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"EL TRAPICHE\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"El VOLCAN\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"ELEODORO LOBOS\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"ESTANCIA GRANDE\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"ESTANCIA SAN JOSE\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"FORTIN EL PATRIA\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"FORTUNA\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"FRAGA\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"GRANVILLE\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"GUANACO PAMPA\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"INTI-HUASI\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"JARILLA\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"JUAN JORBA\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"JUAN LLERENA\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"JUAN W. GEZ\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"JUANA KOSLAY\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"JUSTO DARACT\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA AGUADA\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA ANGELINA\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA BAJADA, LOS QUEMADOS, EL CADILLO, EL BALDECITO Y EL CALDEN\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA BOTIJA\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA CALERA\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA COCHA\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA CUMBRE\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA FLORIDA\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA LEGUA\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA PETRA\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA PUNILLA\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA PUNTA\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA RAMADA\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA TOMA\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA TOTORA\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA TRANCA\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LA VERTIENTE\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAFINUR\",\"JUNIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAGUNA LARGA\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAS AGUADAS\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAS AGUADITAS\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAS BARRANCAS\",\"\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAS BARRANQUITAS\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAS CHACRAS\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAS ISLETAS\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAS LAGUNAS\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAS LAGUNAS\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAS LOMITAS\",\"JUNIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAS TRANCAS\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAS VERTIENTES\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LAVAISSE\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LEANDRO N.ALEM\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LOMAS BLANCAS\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LOS CAJONES\",\"JUNIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LOS CERRILLOS\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LOS LOBOS\",\"JUNIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LOS MEMBRILLOS\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LOS MOLLES\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LOS MOLLES\",\"JUNIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LOS PUQUIOS\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LOS RAMBLONES\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LOS TAPIALES\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"LUJAN\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"MARTIN DE LOYOLA\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"MESILLA DEL CURA\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"MONSEÑOR CAFFERATA\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"MOSMOTA\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"NARANJO ESQUINA\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"NASCHEL\",\"CHACABUCO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"NAVIA\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"NOGOLI\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"NUEVA ESCORIA\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"NUEVA GALIA\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"PAMPA DE LAS SALINAS\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"PAMPA DEL TAMBOREO\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"PAPAGAYOS\",\"CHACABUCO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"PARAJE CASA DE LOS TIGRES\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"PARAJE DE LA CHAÑARIENTA\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"PARAJE LA MAROMA BAJADA NUEVA\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"PARAJE LOS MOLLES\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"PASO DE LOS CORRALES\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"PASO DEL REY\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"PASO GRANDE\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"POTRERILLOS\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"POTRERO DE LOS FUNES\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"POZO CAVADO\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"POZO DEL CARRIL\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"POZO DEL TALA\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"PUESTO BALZORA\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"PUNTA DEL AGUA\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"PUNTA DEL AGUA, EL RINCON Y RODEO DE CADENAS\",\"JUNIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"QUEBRADA DE LAS HIGUERITAS\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"QUINES\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"RENCA\",\"CHACABUCO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"REPRESA DEL CARMEN\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"RIO GRANDE\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"RIOCITO\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SALADILLO\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SALINAS DEL BEBEDERO\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SAN ANTONIO\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SAN FELIPE\",\"CHACABUCO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SAN FRANCISCO DEL MONTE DE ORO\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SAN JERONIMO\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SAN JOSE DEL MORRO\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SAN LUIS\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SAN MARTIN\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SAN PABLO\",\"CHACABUCO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SAN PEDRO\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SAN ROQUE\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SAN VICENTE\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SANTA MARTINA\",\"CHACABUCO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SANTA RITA DE LAS CHACRAS\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SANTA ROSA DEL CANTANTAL\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SANTA ROSA DEL CONLARA\",\"JUNIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SOCOSCORA\",\"AYACUCHO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"SUYUQUE\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"TALITA\",\"JUNIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"TILISARAO\",\"CHACABUCO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"TORO NEGRO\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"UNION\",\"DUPUY\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"VALLE DE PANCANTA\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"VARELA\",\"PUEYRREDON\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"VILLA DE LA QUEBRADA\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"VILLA DE MERLO\",\"JUNIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"VILLA DE PRAGA\",\"SAN MARTIN\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"VILLA DEL CARMEN\",\"CHACABUCO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"VILLA GENERAL ROCA\",\"BELGRANO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"VILLA LARCA\",\"CHACABUCO\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"VILLA MERCEDES\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"VILLA REYNOLDS\",\"PEDERNERA\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"VIRORCO\",\"PRINGLES\");");
      db.execSQL("INSERT into LOCALIDADES VALUES (\"ZANJITAS\",\"PUEYRREDON\");");
    }

  }

  public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
    Map<String, Object> retMap = new HashMap<String, Object>();

    if(json != JSONObject.NULL) {
      retMap = toMap(json);
    }
    return retMap;
  }

  public static Map<String, Object> toMap(JSONObject object) throws JSONException {
    Map<String, Object> map = new HashMap<String, Object>();

    Iterator<String> keysItr = object.keys();
    while(keysItr.hasNext()) {
      String key = keysItr.next();
      Object value = object.get(key);

      if(value instanceof JSONArray) {
        value = toList((JSONArray) value);
      }

      else if(value instanceof JSONObject) {
        value = toMap((JSONObject) value);
      }
      map.put(key, value);
    }
    return map;
  }

  public static List<Object> toList(JSONArray array) throws JSONException {
    List<Object> list = new ArrayList<Object>();
    for(int i = 0; i < array.length(); i++) {
      Object value = array.get(i);
      if(value instanceof JSONArray) {
        value = toList((JSONArray) value);
      }

      else if(value instanceof JSONObject) {
        value = toMap((JSONObject) value);
      }
      list.add(value);
    }
    return list;
  }

  public String getUltimoTelefono(String dni) {
    Cursor cursor = db.rawQuery("Select TELEFONO FROM QRs WHERE DNI = '"+dni+"' AND LENGTH(TELEFONO)>5 ORDER BY ID_REGISTRO DESC ",null);
    if (cursor.moveToNext())
      return cursor.getString(0);
      else return "";

  }

  public String getUltimoApellido(String dni) {
    Cursor cursor = db.rawQuery("Select APELLIDO FROM QRs WHERE DNI = '"+dni+"' AND LENGTH(APELLIDO)>3 ORDER BY ID_REGISTRO DESC ",null);
    if (cursor.moveToNext())
      return cursor.getString(0);
    else return "";
  }

  public String getUltimoNombres(String dni) {
    Cursor cursor = db.rawQuery("Select NOMBRES FROM QRs WHERE DNI = '"+dni+"' AND LENGTH(NOMBRES)>3 ORDER BY ID_REGISTRO DESC ",null);
    if (cursor.moveToNext())
      return cursor.getString(0);
    else return "";
  }

  public void Eliminar30Dias(){
    db.execSQL( "DELETE FROM QRs WHERE FECHA <= date('now','-30 day')");
 //   db.execSQL("update qrs set fecha = datetime(fecha,'-3 day')");
  }

  public Establecimiento getEstablecimientoLocal() {
    Cursor cursor = db.rawQuery("Select * FROM ESTAB_USUARIO",null);
    if (cursor.moveToNext())
    {
      DateFormat formatterServer = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
      formatterServer.setTimeZone(TimeZone.getTimeZone("UTC−03:00"));
      //DateFormat formatterClient = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());;

      Establecimiento establecimiento = new Establecimiento();
      establecimiento.IdUsuEstab = cursor.getInt(0);
      establecimiento.NombreEstablecimiento = cursor.getString(1);
      establecimiento.CuitDniResponsable = cursor.getString(2);
      establecimiento.Localidad = cursor.getString(3);
      establecimiento.NombreResponsable = cursor.getString(6);
      try {
        establecimiento.Fecha_Creacion = formatterServer.parse(cursor.getString(8));
      } catch (ParseException e) {
        e.printStackTrace();
      }
      establecimiento.TelefonoEstab = cursor.getString(9);
      establecimiento.Telefono = cursor.getString(10);
      establecimiento.Domicilio = cursor.getString(11);
      establecimiento.RegistraSalidas = cursor.getInt(12)==1?true:false;
      establecimiento.Permanencia = cursor.getInt(13);
      establecimiento.TipoMovimientoActual = cursor.getString(15);

      return establecimiento;

    }
    return null;
  }

  public Map<String,Object> verifyVersion() {

    Integer idUsuEstab = getIdUsuEstabDBLocal();

    if(idUsuEstab != null)
    {
      //Seteo los parametros
      HashMap<String, String> param = new HashMap();
      param.put("ID_USU_ESTAB", idUsuEstab.toString());
      param.put("N_VERSION", versionActual);
      param.put("OS", "ANDROID");

      Map<String,Object> retorno =
              null;
      try {
        retorno = Post(UrlServidor+
                        new String(Base64.decode("L0dQU0xfZ2V0VmVyc2lvbi8="
                                ,Base64.DEFAULT),"UTF-8")
                ,param);//"/GPSL_getVersion/"
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }

      if(retorno != null && retorno.get("MSG") != null)
      {
        if(retorno.get("MSG").toString().equals("S/N"))
          db.execSQL( "DELETE FROM VERSION");
        else
        {
          db.execSQL( "DELETE FROM VERSION");
          SQLiteStatement consulta = db.compileStatement(
                  "INSERT INTO VERSION( N_VERSION, MSG, ACTUALIZA)" +
                          " VALUES(?, ?, ?)"
          );

          consulta.bindString(1, versionActual);
          consulta.bindString(2, retorno.get("MSG").toString());
          consulta.bindString(3, retorno.get("ACTUALIZA").toString());

          consulta.executeInsert();
        }
      }

      Cursor cursor = db.rawQuery("Select * FROM VERSION",null);
      if (cursor.moveToNext())
      {
        String versionDb = cursor.getString(0);

        if(versionDb.equals(versionActual))
        {
          Map<String, Object> salida = new HashMap();
          salida.put("MSG",cursor.getString(1));
          salida.put("ACTUALIZA",cursor.getString(2));
          Log.e("MSG",salida.get("MSG").toString());
          return salida;
        }else {
          db.execSQL( "DELETE FROM VERSION");
        }
      }

//      return retorno;
    }





    return null;
  }

  public void GuardarUsuarioEstabEdit(String cuitDniResponsable, String nombreEstablecimiento, String nombreResponsable,
                                     String domicilio, String telefono,String telefonoEstab, String localidad, boolean registraSalidas,
                                     String permanencia)
  {
    //Seteo los parametros
    HashMap<String, String> param = new HashMap();
    param.put("CUIT_DNI", cuitDniResponsable);
    param.put("ID_USU_ESTAB", getIdUsuEstabDBLocal().toString());
    param.put("NOMBRE", nombreEstablecimiento);
    param.put("RESPONSABLE", nombreResponsable);
    param.put("TELEFONO", telefono);
    param.put("TELEFONO_ESTAB", telefonoEstab);
    param.put("DOMICILIO", domicilio);
    param.put("LOCALIDAD", localidad);
    param.put("SALIDAS", registraSalidas ? "SI" : "NO");
    param.put("PERMANENCIA", permanencia);


    Map<String,Object> retorno = null;
    try {
      retorno = Post(UrlServidor+
                      new String(Base64.decode("L0dQU0xfdXBkYXRlVXN1RXN0YWIv"
                              ,Base64.DEFAULT),"UTF-8")
              ,param); //"/GPSL_updateUsuEstab/"
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    String retornoString = retorno.get("RETORNO").toString();

    if(retornoString.equals("OK"))
    {
      SQLiteStatement consulta = db.compileStatement(
              " UPDATE ESTAB_USUARIO " +
                      "SET NOMBRE = ?," +
                      "    LOCALIDAD = ?, " +
                      "    RESPONSABLE = ? ," +
                      "    TELEFONO = ? , " +
                      "    TELEFONO_USU = ? , " +
                      "    DOMICILIO = ? ," +
                      "    REGISTRA_SALIDA = ?, " +
                      "    CUIT_DNI = ?, " +
                      "    TIEMPO_PERMANENCIA = ?"
      );
      consulta.bindString(1, nombreEstablecimiento);
      consulta.bindString(2, localidad);
      consulta.bindString(3, nombreResponsable);
      consulta.bindString(4, telefonoEstab);
      consulta.bindString(5, telefono);
      consulta.bindString(6, domicilio);
      consulta.bindString(7, registraSalidas?"1":"0");
      consulta.bindString(8, cuitDniResponsable);
      consulta.bindString(9, permanencia);

      consulta.executeUpdateDelete();
    }

  }

  public void ActualizarDatosLocalEstablecimiento(){
    try {
      Establecimiento estab= getEstablecimientoLocal();

      if(estab != null)
      {
        HashMap<String, String> param = new HashMap();
        param.put("P_ID_USU_ESTAB", estab.IdUsuEstab.toString());
        param.put("P_NOMBRE", estab.NombreEstablecimiento);
        param.put("P_TELEFONO_USU", estab.Telefono);

        Map<String,Object> retorno =  Post(UrlServidor+
                        new String(Base64.decode("L0dQU0xfZ2V0RXN0YWJsZWNpbWllbnRvLw=="
                                ,Base64.DEFAULT),"UTF-8")
                ,param); //"/GPSL_getEstablecimiento/"

        if (retorno != null && retorno.size() >0)
        {
          SQLiteStatement consulta = db.compileStatement(
                  " UPDATE ESTAB_USUARIO " +
                          "SET NOMBRE = ?," +
                          "    LOCALIDAD = ?, " +
                          "    RESPONSABLE = ? ," +
                          "    TELEFONO = ? , " +
                          "    DOMICILIO = ? ," +
                          "    REGISTRA_SALIDA = ?, " +
                          "    CUIT_DNI = ?, " +
                          "    TIEMPO_PERMANENCIA = ?"
          );
          consulta.bindString(1, retorno.get("NOMBRE").toString());
          consulta.bindString(2, retorno.get("LOCALIDAD")!= null? retorno.get("LOCALIDAD").toString():"");
          consulta.bindString(3, retorno.get("RESPONSABLE")!= null? retorno.get("RESPONSABLE").toString():"");
          consulta.bindString(4, retorno.get("TELEFONO")!= null? retorno.get("TELEFONO").toString():"");
          consulta.bindString(5, retorno.get("DOMICILIO")!= null? retorno.get("DOMICILIO").toString():"");
          consulta.bindString(6, "1");//retorno.get("REGISTRA_SALIDA").toString().equals("SI")?"1":"0");
          consulta.bindString(7, retorno.get("CUIT_DNI")!= null? retorno.get("CUIT_DNI").toString():"");
          consulta.bindString(8, retorno.get("TIEMPO_PERMANENCIA")!= null? retorno.get("TIEMPO_PERMANENCIA").toString():"");

          consulta.executeUpdateDelete();
        }


      }


    }catch (Exception e){
      Log.e("Error",e.getMessage());
    }
  }


  public void setTipoMovimientoActual(String tipo) {

    SQLiteStatement consulta = db.compileStatement(
            " UPDATE ESTAB_USUARIO " +
                    "SET TIPO_MOVIMIENTO_ACTUAL = ?"
    );
    consulta.bindString(1, tipo);

    consulta.executeUpdateDelete();
  }

  public Integer getCantidadDiasPendientes(){
    try {
      Cursor cursor = db.rawQuery("Select MIN(FECHA) from QRs where enviado = 0 ",null);

      if(cursor.moveToNext()){
        DateFormat formatterServer = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        formatterServer.setTimeZone(TimeZone.getTimeZone("UTC−03:00"));
        Date pFecha = formatterServer.parse(cursor.getString(0));
        long diffInMillies = Math.abs(new Date().getTime() - pFecha.getTime());
        Long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        return diff != null ? diff.intValue() : 0;
      }
    } catch (Exception e) {
      //e.printStackTrace();
      return 0;
    }
    return 0;
  }

  //////////////////////////////////////////

  public String encode(String s, String key) {
    return base64Encode(xorWithKey(s.getBytes(), key.getBytes()));
  }

  public String decode(String s, String key) {
    return new String(xorWithKey(base64Decode(s), key.getBytes()));
  }

  private byte[] xorWithKey(byte[] a, byte[] key) {
    byte[] out = new byte[a.length];
    for (int i = 0; i < a.length; i++) {
      out[i] = (byte) (a[i] ^ key[i%key.length]);
    }
    return out;
  }

  private byte[] base64Decode(String s) {
    try {

      return Base64.decode(s, Base64.DEFAULT);
    } catch (Exception e) {e.printStackTrace();}
    return null;
  }

  private String base64Encode(byte[] bytes) {

    return Base64.encodeToString(bytes,Base64.DEFAULT).replaceAll("\\s", "");

  }


  public void Encriptar(String dataIn){
    MessageDigest sha = null;
    try {
      byte[] key = "MyEncriptionKey1".getBytes("UTF-8");
      sha = MessageDigest.getInstance("SHA-1");
      key = sha.digest(key);
      key = Arrays.copyOf(key, 16);
      SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      byte[] data = cipher.doFinal(dataIn.getBytes());

      Log.i("texto", Base64.encodeToString(data, Base64.CRLF));
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }

  public static boolean checkPermission(Context context,String permission, int requestCode) {
    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
      ActivityCompat.requestPermissions(
              (Activity) context,
              new String[]{permission},
              requestCode
      );
    }
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
  }



}


