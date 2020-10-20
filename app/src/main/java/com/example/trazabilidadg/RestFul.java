package com.example.trazabilidadg;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.JsonReader;
import android.widget.Toast;

import com.google.android.gms.common.util.IOUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class RestFul {
    Context _main;
    public RestFul(Context main) {
        _main = main;
    }

    public void subirRegistros(String csv, int uRegistro) {
        final int _uRegistro = uRegistro;
        post("https://www.programainformatico.sanluis.gob.ar/ords/sica/trazabilidad/seguimiento/",
                "{\"CSV\":\""+csv+"\" }",
                null
                );
    }

    public String post(String url, String param, Runnable callback){
        final String _url = url;
        final String _param = param;
        final Runnable _callback = callback;
        AsyncTask.execute(new Runnable() {

            @Override
            public void run() {
                // Create URL
                URL githubEndpoint = null;
                try {
                    githubEndpoint = new URL(_url);
                    HttpsURLConnection myConnection =
                            (HttpsURLConnection) githubEndpoint.openConnection();
                    myConnection.setRequestProperty(new String(Base64.decode("VXNlci1BZ2VudA==",Base64.DEFAULT),"UTF-8"),
                            new String(Base64.decode("VHJhemFiaWxpZGFkIEdQU0wt",Base64.DEFAULT),"UTF-8") +
                            new SimpleDateFormat(new String(Base64.decode("ZGRtbXl5eXk=",Base64.DEFAULT),"UTF-8")).format(new Date()));
//                    myConnection.setRequestProperty("Accept", "application/json");
                    myConnection.setRequestProperty("Content-Type", "application/json; utf-8");
                    myConnection.setRequestMethod("POST");
                    myConnection.setDoOutput(true);
                    OutputStream os = myConnection.getOutputStream();

                        byte[] input = _param.getBytes("utf-8");
                        os.write(input, 0, input.length);

                    if (myConnection.getResponseCode() == 200) { //
                        // Success
/*
                        InputStream responseBody = myConnection.getInputStream();
                        InputStreamReader responseBodyReader =
                                new InputStreamReader(responseBody, "UTF-8");
                        //JsonReader jsonReader = new JsonReader(responseBodyReader);
                        String retorno = responseBodyReader.toString();
*/

                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(myConnection.getInputStream(), "utf-8"));

                            StringBuilder response = new StringBuilder();
                            String responseLine = null;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());}
//                            System.out.println(response.toString());
                        //Toast.makeText(_main,response.toString(),Toast.LENGTH_SHORT).show();
                        //IOUtils.toString(responseBody, "UTF-8");

                        //_callback.run();
                        //((ActivityHistorial)_main).GrabarEnviados(Integer.parseInt( response.toString()));

                    } else {
                        // Error handling code goes here
                        BufferedReader br_err = new BufferedReader(
                                new InputStreamReader(myConnection.getErrorStream(), "utf-8"));

                        StringBuilder response = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine = br_err.readLine()) != null) {
                            response.append(responseLine.trim());}

                        String hola = response.toString();
                        hola = null;
//                            System.out.println(response.toString());
                        //Toast.makeText(_main,response.toString(),Toast.LENGTH_SHORT).show();

                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                catch (java.io.IOException e) {
                    e.printStackTrace();
                }

            }
        });
        return "";
    }
}
