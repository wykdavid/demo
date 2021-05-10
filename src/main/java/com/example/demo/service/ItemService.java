package com.example.demo.service;

import com.example.demo.error.BusinessException;
import com.example.demo.service.model.ItemModel;
import com.example.demo.service.model.UserModel;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

public interface ItemService {
    ItemModel createItem(ItemModel itemModel, UserModel userModel) throws BusinessException;

    List<ItemModel> listItem();

    ItemModel getItemById(Integer id);

    ItemModel getItemByIdInCache(Integer id);

    boolean decreaseStock(Integer itemId, Integer amount);

    void increaseSales(Integer itemId, Integer amount);
}
