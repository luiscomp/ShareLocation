package com.example.admed.sharelocation.objetos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Luis Eduardo on 20/11/2017.
 */

public class Usuario implements Parcelable {
    private String id;
    private String nome;
    private String email;
    private String senha;
    private Double latitude;
    private Double longitude;

    public Usuario() {
    }

    protected Usuario(Parcel in) {
        id = in.readString();
        nome = in.readString();
        email = in.readString();
        senha = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<Usuario> CREATOR = new Creator<Usuario>() {
        @Override
        public Usuario createFromParcel(Parcel in) {
            return new Usuario(in);
        }

        @Override
        public Usuario[] newArray(int size) {
            return new Usuario[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(nome);
        parcel.writeString(email);
        parcel.writeString(senha);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }
}
