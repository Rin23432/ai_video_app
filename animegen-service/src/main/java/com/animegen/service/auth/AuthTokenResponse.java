package com.animegen.service.auth;

public class AuthTokenResponse {
    private String token;
    private AuthUserInfo user;

    public AuthTokenResponse() {
    }

    public AuthTokenResponse(String token, AuthUserInfo user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public AuthUserInfo getUser() { return user; }
    public void setUser(AuthUserInfo user) { this.user = user; }
}

