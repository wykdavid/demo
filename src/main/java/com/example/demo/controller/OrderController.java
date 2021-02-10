package com.example.demo.controller;


import com.example.demo.MQ.MQProducer;
import com.example.demo.error.BusinessException;
import com.example.demo.error.EnumBUsinessError;
import com.example.demo.responce.CommonReturnType;
import com.example.demo.service.ItemService;
import com.example.demo.service.OrderService;
import com.example.demo.service.PromoService;
import com.example.demo.service.model.OrderModel;
import com.example.demo.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class OrderController extends BaseController{
    @Autowired
    private OrderService orderService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQProducer mqProducer;

    @Autowired
    private ItemService itemService;
    @Autowired
    private PromoService promoService;

    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId")Integer itemId,
                                        @RequestParam(name = "amount")Integer amount,
                                        @RequestParam(name = "promoId",required = false)Integer promoId,
                                        @RequestParam(name = "promoToken",required = false)Integer promoToken) throws BusinessException {

        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EnumBUsinessError.USER_NOT_LOGIN,"User has not log in");
        }
        //Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel==null){
            throw new BusinessException(EnumBUsinessError.USER_NOT_LOGIN,"session expire");

        }
        //先加入库存流水init状态

        boolean ex = redisTemplate.hasKey("invalid_"+itemId);
        if(ex){
            throw new BusinessException(EnumBUsinessError.STOCK_NOT_ENOUGH);
        }
        String stockLogId = itemService.initStockLog(itemId,amount);


        if(!mqProducer.transactionRedecuStock(userModel.getId(),promoId,itemId,amount,stockLogId)){
            throw new BusinessException(EnumBUsinessError.UNKNOWN_ERROR,"下单失败");
        }
        //OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId,promoId,amount);
        return CommonReturnType.create(null);
    }

    @RequestMapping(value = "/generateToken",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType generateToken(@RequestParam(name = "itemId")Integer itemId,
                                        @RequestParam(name = "promoId",required = false)Integer promoId) throws BusinessException {

        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EnumBUsinessError.USER_NOT_LOGIN,"User has not log in");
        }
        //Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel==null){
            throw new BusinessException(EnumBUsinessError.USER_NOT_LOGIN,"session expire");

        }
        String promotoken = promoService.generateSecondKillToken(promoId,itemId,userModel.getId());
        if(promotoken==null){
            throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR,"Generate token failed");
        }
        return  CommonReturnType.create(promotoken);
    }

}
