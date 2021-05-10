package com.example.demo.service;

import com.example.demo.error.BusinessException;
import com.example.demo.service.model.OrderModel;

public interface OrderService {
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException;
}
