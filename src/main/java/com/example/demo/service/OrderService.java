package com.example.demo.service;

import com.example.demo.error.BusinessException;
import com.example.demo.service.model.OrderModel;

public interface OrderService {
    //1前端传来秒杀活动id，下单接口校验是否对应商品活动是否开始（推荐）
    //2在下单接口内判断是否秒杀
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount,String stockLogId) throws BusinessException;


}
