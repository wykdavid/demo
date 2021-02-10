package com.example.demo.service;

import com.example.demo.service.model.PromoModel;

public interface PromoService {
    PromoModel getPromoByItemId(Integer itemId);

    void publishPromo(Integer itemId);

    String generateSecondKillToken(Integer promoId,Integer itemId,Integer userId);
}
