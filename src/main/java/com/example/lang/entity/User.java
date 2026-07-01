package com.example.lang.entity;

public class User {
    private String login;
    private String psw;

    // Геттеры и сеттеры ОБЯЗАТЕЛЬНЫ для th:field
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPsw() { return psw; }
    public void setPsw(String psw) { this.psw = psw; }
}
