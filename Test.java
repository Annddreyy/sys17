package com.example.cookingapp.model;

public class AuthorizationInfo {
    public int client_id;
    public String email;
    public String password;

    public AuthorizationInfo(int client_id, String email, String password) {
        this.client_id = client_id;
        this.email = email;
        this.password = password;
    }
}
