package com.example.trazabilidadg;
/*
Clase para pasar parametros de persistencia a activity
  lo implementan todos los Activity
 */


import java.util.HashMap;

public interface PersistenciaResponse {
    void persistenciaFinish(HashMap<String, String> output);
}
