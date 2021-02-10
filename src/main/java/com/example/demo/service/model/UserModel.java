package com.example.demo.service.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


public class UserModel implements Serializable {
    private Integer id;
    @NotBlank(message = "user name is empty")
    private String name;

    @NotNull(message = "gender is empty")
    private Byte gender;

    @NotNull(message = "age is empty")
    @Min(value = 0,message = "age must greater than 0")
    @Max(value = 150,message = "年龄必须小于150")
    private Integer age;
    @NotBlank(message = "telephone is empty")
    private String telephone;
    private String registerMode;
    private String thirdPartyId;
    @NotBlank(message = "password is empty")
    private String encryptPassword;

    public String getEncryptPassword() {
        return encryptPassword;
    }

    public void setEncryptPassword(String encryptPassword) {
        this.encryptPassword = encryptPassword;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getRegisterMode() {
        return registerMode;
    }

    public void setRegisterMode(String registerMode) {
        this.registerMode = registerMode;
    }

    public String getThirdPartyId() {
        return thirdPartyId;
    }

    public void setThirdPartyId(String thirdPartyId) {
        this.thirdPartyId = thirdPartyId;
    }


}
