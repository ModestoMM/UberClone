package com.modesto.uberclone.models;

//Modelo para los tokens de los usuarios
public class Token {

    String token;

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
