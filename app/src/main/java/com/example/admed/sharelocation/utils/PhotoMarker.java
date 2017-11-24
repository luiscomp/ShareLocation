package com.example.admed.sharelocation.utils;

import android.net.Uri;

import com.example.admed.sharelocation.objetos.Usuario;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Pichau on 23/11/2017.
 */

public class PhotoMarker {
    private Usuario usuario;
    private Marker marker;
    private Uri uri;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
