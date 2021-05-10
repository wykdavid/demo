package com.example.demo.service;

import com.example.demo.error.BusinessException;
import com.example.demo.service.model.ItemModel;
import com.example.demo.service.model.UserModel;

import java.util.List;

public interface UserService {
    UserModel getUserById(Integer id);

    UserModel getUserByIdInCache(Integer id);
    UserModel getUserByIdInCacheForCreateItem(Integer id);

    void register(UserModel userModel) throws BusinessException;

    UserModel validateLogin(String telephone, String encrptPassword) throws BusinessException;

    List<ItemModel> listMyItem(Integer userId);
    List<ItemModel> listMyOrder(Integer userId);
}
