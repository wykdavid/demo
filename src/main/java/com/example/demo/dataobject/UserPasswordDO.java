package com.example.demo.dataobject;

public class UserPasswordDO {
    private Integer id;

    private String encrpyPassword;

    private Integer userId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEncrpyPassword() {
        return encrpyPassword;
    }

    public void setEncrpyPassword(String encrpyPassword) {
        this.encrpyPassword = encrpyPassword == null ? null : encrpyPassword.trim();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}