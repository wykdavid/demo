package com.example.demo.service;

import com.example.demo.dao.PromoDOMapper;
import com.example.demo.dataobject.PromoDO;
import com.example.demo.error.BusinessException;
import com.example.demo.error.EnumBUsinessError;
import com.example.demo.service.model.ItemModel;
import com.example.demo.service.model.PromoModel;
import com.example.demo.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//Promotion Service, remain unfinished
@Service
public class PromoServiceImpl implements PromoService{

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        PromoModel promoModel = convertFromDataObject(promoDO);

        if(promoModel==null) return null;
        //DateTime now = new DateTime();
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }
        else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }
        else{
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    @Override
    public void publishPromo(Integer itemId) {
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(itemId);
        if(promoDO.getItemId()==null||promoDO.getItemId().intValue()==0) return;
        ItemModel itemModel = itemService.getItemById(promoDO.getItemId());

        redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(),itemModel.getStock());

    }

    @Override
    public String generateSecondKillToken(Integer promoId,Integer itemId,Integer userId) {
        PromoDO promoDO = promoDOMapper.selectByItemId(promoId);

        PromoModel promoModel = convertFromDataObject(promoDO);

        if(promoModel==null) return null;
        //DateTime now = new DateTime();
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }
        else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }
        else{
            promoModel.setStatus(2);
        }

        if(promoModel.getStatus()!=2) return null;

        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(itemModel == null){
            //throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR,"Item does not exist");
            return null;
        }

        UserModel userModel = userService.getUserByIdInCache(userId);
        if(userModel==null){
            return null;
            //throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR,"User does not exist");
        }

        String token = UUID.randomUUID().toString().replace("-","");

        redisTemplate.opsForValue().set("promo_token_"+promoId,token);
        redisTemplate.expire("promo_token_"+promoId,5, TimeUnit.MINUTES);
        return token;


    }

    public PromoModel convertFromDataObject(PromoDO promoDO){
        if(promoDO == null) return null;

        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));

        return promoModel;
    }
}
