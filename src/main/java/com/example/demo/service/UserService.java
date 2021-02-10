package com.example.demo.service;

import com.example.demo.error.BusinessException;
import com.example.demo.service.model.UserModel;

public interface UserService {
    public UserModel getUserById(Integer id);
    UserModel getUserByIdInCache(Integer id);
    void register(UserModel userModel) throws BusinessException;
    //password is encoded
    UserModel validateLogin(String telephone,String encrptPassword) throws BusinessException;

}
