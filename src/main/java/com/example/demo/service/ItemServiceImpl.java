package com.example.demo.service;


import com.example.demo.dao.ItemDOMapper;
import com.example.demo.dao.ItemStockDOMapper;
import com.example.demo.dao.UserItemDOMapper;
import com.example.demo.dataobject.ItemDO;
import com.example.demo.dataobject.ItemStockDO;
import com.example.demo.dataobject.UserItemDO;
import com.example.demo.error.BusinessException;
import com.example.demo.error.EnumBUsinessError;
import com.example.demo.service.model.ItemModel;
import com.example.demo.service.model.PromoModel;
import com.example.demo.service.model.UserModel;
import com.example.demo.validator.ValidationResult;
import com.example.demo.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ValidatorImpl validator;
    @Autowired
    private ItemDOMapper itemDOMapper;
    @Autowired
    private ItemStockDOMapper itemStockDOMapper;
    @Autowired
    private PromoService promoService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserItemDOMapper userItemDOMapper;

    public ItemServiceImpl() {
    }
    //Convert Objects
    private ItemDO convertItemDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        } else {
            ItemDO itemDO = new ItemDO();
            BeanUtils.copyProperties(itemModel, itemDO);
            itemDO.setPrice(itemModel.getPrice().doubleValue());
            return itemDO;
        }
    }

    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        } else {
            ItemStockDO itemStockDO = new ItemStockDO();
            itemStockDO.setItemId(itemModel.getId());
            itemStockDO.setStock(itemModel.getStock());
            return itemStockDO;
        }
    }

    @Transactional
    public ItemModel createItem(ItemModel itemModel, UserModel userModel) throws BusinessException {
        ValidationResult result = this.validator.validate(itemModel);
        if (result.isHasError()) {
            throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        } else {
            ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);
            this.itemDOMapper.insertSelective(itemDO);
            itemModel.setId(itemDO.getId());
            ItemStockDO itemStockDO = this.convertItemStockDOFromItemModel(itemModel);
            this.itemStockDOMapper.insertSelective(itemStockDO);

            UserItemDO userItemDO = new UserItemDO();
            userItemDO.setItemId(itemDO.getId());
            userItemDO.setUserId(userModel.getId());
            this.userItemDOMapper.insertSelective(userItemDO);
            return this.getItemById(itemModel.getId());
        }
    }



    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = this.itemDOMapper.listItem();
        List<ItemModel> itemModelList = (List)itemDOList.stream().map((itemDO) -> {
            ItemStockDO itemStockDO = this.itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = this.itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null) {
            return null;
        } else {
            ItemStockDO itemStockDO = this.itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO, itemStockDO);
            PromoModel promoModel = this.promoService.getPromoByItemId(itemModel.getId());
            if (promoModel != null && promoModel.getStatus() != 3) {
                itemModel.setPromoModel(promoModel);
            }

            return itemModel;
        }
    }

    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel = (ItemModel)this.redisTemplate.opsForValue().get("item_validate_" + id);
        if (itemModel == null) {
            itemModel = this.getItemById(id);
            this.redisTemplate.opsForValue().set("item_validate_" + id, itemModel);
            this.redisTemplate.expire("item_validate_" + id, 10L, TimeUnit.MINUTES);
        }

        return itemModel;
    }

    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {
        int affectedRow = this.itemStockDOMapper.decreaseStock(itemId, amount);
        return affectedRow > 0;
    }

    @Transactional
    public void increaseSales(Integer itemId, Integer amount) {
        this.itemDOMapper.increaseSales(itemId, amount);
    }

    private ItemModel convertModelFromDataObject(ItemDO itemDo, ItemStockDO itemStockDO) {
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDo, itemModel);
        itemModel.setPrice(new BigDecimal(itemDo.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }
}
