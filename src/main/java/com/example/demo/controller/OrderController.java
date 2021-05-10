package com.example.demo.controller;



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
public class OrderController extends BaseController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private RedisTemplate redisTemplate;

    public OrderController() {
    }

    @RequestMapping(
            value = {"/createorder"},
            method = {RequestMethod.POST},
            consumes = {"application/x-www-form-urlencoded"}
    )
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId, @RequestParam(name = "amount") Integer amount, @RequestParam(name = "promoId",required = false) Integer promoId) throws BusinessException {
        String token = ((String[])this.httpServletRequest.getParameterMap().get("token"))[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EnumBUsinessError.USER_NOT_LOGIN, "User has not log in");
        } else {
            UserModel userModel = (UserModel)this.redisTemplate.opsForValue().get(token);
            if (userModel == null) {
                throw new BusinessException(EnumBUsinessError.USER_NOT_LOGIN, "session expire");
            } else {
                this.orderService.createOrder(userModel.getId(), itemId, promoId, amount);
                return CommonReturnType.create((Object)null);
            }
        }
    }
}
