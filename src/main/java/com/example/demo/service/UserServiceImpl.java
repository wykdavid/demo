package com.example.demo.service;

import com.example.demo.dao.*;
import com.example.demo.dataobject.*;
import com.example.demo.error.BusinessException;
import com.example.demo.error.EnumBUsinessError;
import com.example.demo.service.model.ItemModel;
import com.example.demo.service.model.UserModel;
import com.example.demo.validator.ValidationResult;
import com.example.demo.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserPasswordDOMapper userPasswordDOMapper;
    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private ValidatorImpl validator;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ItemDOMapper itemDOMapper;
    @Autowired
    private UserItemDOMapper userItemDOMapper;
    @Autowired
    private ItemStockDOMapper itemStockDOMapper;
    @Autowired
    private UserOrderDOMapper userOrderDOMapper;

    @Autowired
    private OrderDOMapper orderDOMapper;
    public UserServiceImpl() {
    }

    public UserModel getUserById(Integer id) {
        UserDO userDO = this.userDOMapper.selectByPrimaryKey(id);
        if (userDO == null) {
            return null;
        } else {
            UserPasswordDO userPasswordDO = this.userPasswordDOMapper.selectByUserId(userDO.getId());
            return this.convertFromDataObject(userDO, userPasswordDO);
        }
    }
    public UserModel getUserByIdInCacheForCreateItem(Integer id){
        UserModel userModel = (UserModel)this.redisTemplate.opsForValue().get("user_createitem_" + id);
        if (userModel == null) {
            userModel = this.getUserById(id);
            this.redisTemplate.opsForValue().set("user_createitem_" + id, userModel);
            //this.redisTemplate.expire("user_validate_" + id, 10L, TimeUnit.MINUTES);
        }

        return userModel;
    }
    public UserModel getUserByIdInCache(Integer id) {
        UserModel userModel = (UserModel)this.redisTemplate.opsForValue().get("user_validate_" + id);
        if (userModel == null) {
            userModel = this.getUserById(id);
            this.redisTemplate.opsForValue().set("user_validate_" + id, userModel);
            this.redisTemplate.expire("user_validate_" + id, 10L, TimeUnit.MINUTES);
        }

        return userModel;
    }

    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR);
        } else {
            ValidationResult validationResult = this.validator.validate(userModel);
            if (validationResult.isHasError()) {
                System.out.println(validationResult.getErrMsg());
                throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR, validationResult.getErrMsg());
            } else {
                UserDO userDO = this.convertFromModel(userModel);

                try {
                    this.userDOMapper.insertSelective(userDO);
                } catch (DuplicateKeyException var5) {
                    throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR, "Email had been registered");
                }

                userModel.setId(userDO.getId());
                UserPasswordDO userPasswordDO = this.convertPasswordFromModel(userModel);
                this.userPasswordDOMapper.insertSelective(userPasswordDO);
            }
        }
    }

    public UserModel validateLogin(String telephone, String encrptPassword) throws BusinessException {
        if (telephone.indexOf("@") == -1 || telephone.split("@").length>2 || !telephone.split("@")[1].equals("syr.edu")) {
            throw new BusinessException(EnumBUsinessError.WRONG_ACCOUNT);
        }
        UserDO userDO = this.userDOMapper.selectByTelephone(telephone);
        if (userDO == null) {
            throw new BusinessException(EnumBUsinessError.USER_LOGIN_FAIL);
        } else {
            UserPasswordDO userPasswordDO = this.userPasswordDOMapper.selectByUserId(userDO.getId());
            UserModel userModel = this.convertFromDataObject(userDO, userPasswordDO);
            if (!StringUtils.equals(encrptPassword, userModel.getEncryptPassword())) {
                throw new BusinessException(EnumBUsinessError.USER_LOGIN_FAIL);
            } else {
                return userModel;
            }
        }
    }

    //list user's orders
    public List<ItemModel> listMyOrder(Integer userId) {

        List<UserOrderDO> UserItemDOList = this.userOrderDOMapper.listMyOrder(userId);
        List<ItemModel> itemModelList = (List)UserItemDOList.stream().map((userOrderDO) -> {
            OrderDO orderDO = this.orderDOMapper.selectByPrimaryKey(userOrderDO.getOrderId());
            ItemDO itemDO = this.itemDOMapper.selectByPrimaryKey(orderDO.getItemId());
            ItemStockDO itemStockDO = this.itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    //list user's uploaded item
    @Override
    public List<ItemModel> listMyItem(Integer userId) {
        List<UserItemDO> UserItemDOList = this.userItemDOMapper.listMyItem(userId);
        List<ItemModel> itemModelList = (List)UserItemDOList.stream().map((userItemDO) -> {
            ItemDO itemDO = this.itemDOMapper.selectByPrimaryKey(userItemDO.getItemId());
            ItemStockDO itemStockDO = this.itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    //object convert
    private ItemModel convertModelFromDataObject(ItemDO itemDo, ItemStockDO itemStockDO) {
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDo, itemModel);
        itemModel.setPrice(new BigDecimal(itemDo.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }


    public UserPasswordDO convertPasswordFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        } else {
            UserPasswordDO userPasswordDO = new UserPasswordDO();
            userPasswordDO.setEncrpyPassword(userModel.getEncryptPassword());
            userPasswordDO.setUserId(userModel.getId());
            return userPasswordDO;
        }
    }

    private UserDO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        } else {
            UserDO userDO = new UserDO();
            BeanUtils.copyProperties(userModel, userDO);
            return userDO;
        }
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO) {
        if (userDO == null) {
            return null;
        } else {
            UserModel userModel = new UserModel();
            BeanUtils.copyProperties(userDO, userModel);
            if (userPasswordDO != null) {
                userModel.setEncryptPassword(userPasswordDO.getEncrpyPassword());
            }

            return userModel;
        }
    }
}