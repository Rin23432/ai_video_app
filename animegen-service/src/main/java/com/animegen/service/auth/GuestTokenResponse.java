package com.animegen.service.auth;

public class GuestTokenResponse {
    private String token;
    private AuthUserInfo user;

    public GuestTokenResponse() {
    }

    public GuestTokenResponse(String token, AuthUserInfo user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public AuthUserInfo getUser() { return user; }
    public void setUser(AuthUserInfo user) { this.user = user; }
}
