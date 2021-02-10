package com.example.demo.service;

import com.example.demo.error.BusinessException;
import com.example.demo.service.model.ItemModel;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

public interface ItemService{

    ItemModel createItem(ItemModel itemModel) throws BusinessException;
    List<ItemModel> listItem();
    ItemModel getItemById(Integer id);

    ItemModel getItemByIdInCache(Integer id);

    boolean decreaseStock(Integer itemId,Integer amount);
    boolean increaseStock(Integer itemId,Integer amount);

    //商品销量增加
    void increaseSales(Integer itemId,Integer amount);

    //异步更新库存
    boolean asyncDecreaseStock(Integer itemid, Integer amount);

    String initStockLog(Integer itemId,Integer amount);
}
