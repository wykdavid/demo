package com.example.demo.service;


import com.example.demo.dao.OrderDOMapper;
import com.example.demo.dao.SequenceDOMapper;
import com.example.demo.dao.StockLogDOMapper;
import com.example.demo.dao.UserOrderDOMapper;
import com.example.demo.dataobject.OrderDO;
import com.example.demo.dataobject.SequenceDO;
import com.example.demo.dataobject.StockLogDO;
import com.example.demo.dataobject.UserOrderDO;
import com.example.demo.error.BusinessException;
import com.example.demo.error.EnumBUsinessError;
import com.example.demo.service.model.ItemModel;
import com.example.demo.service.model.OrderModel;
import com.example.demo.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDOMapper orderDOMapper;
    @Autowired
    private SequenceDOMapper sequenceDOMapper;
    @Autowired
    private UserOrderDOMapper userOrderDOMapper;

    public OrderServiceImpl() {
    }

    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException {
        ItemModel itemModel = this.itemService.getItemByIdInCache(itemId);
        if (itemModel == null) {
            throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR, "Item does not exist");
        } else {
            UserModel userModel = this.userService.getUserByIdInCache(userId);
            if (userModel == null) {
                throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR, "User does not exist");
            } else if (amount > 0 && amount <= 99) {
                if (promoId != null) {
                    if (promoId != itemModel.getId()) {
                        throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR, "Promo info incorrect");
                    }

                    if (itemModel.getPromoModel().getStatus() != 2) {
                        throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR, "Promo has not started");
                    }
                }

                boolean reduce = this.itemService.decreaseStock(itemId, amount);
                if (!reduce) {
                    throw new BusinessException(EnumBUsinessError.STOCK_NOT_ENOUGH);
                } else {
                    OrderModel orderModel = new OrderModel();
                    orderModel.setUserId(userId);
                    orderModel.setAmount(amount);
                    orderModel.setItemId(itemId);
                    if (promoId != null) {
                        orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
                    } else {
                        orderModel.setItemPrice(itemModel.getPrice());
                    }

                    orderModel.setPromoId(promoId);
                    orderModel.setOrderAmount(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
                    orderModel.setId(this.generateOrderNo());
                    OrderDO orderDO = this.convertFromOrderModel(orderModel);
                    this.orderDOMapper.insertSelective(orderDO);
                    this.itemService.increaseSales(itemId, amount);

                    UserOrderDO userOrderDO = new UserOrderDO();
                    userOrderDO.setOrderId(orderDO.getId());
                    userOrderDO.setUserId(userModel.getId());
                    this.userOrderDOMapper.insertSelective(userOrderDO);
                    return orderModel;
                }
            } else {
                throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR, "Amount too small or too biger");
            }
        }
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    String generateOrderNo() {
        StringBuilder stringBuilder = new StringBuilder();
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(nowDate);
        //int sequence = false;
        SequenceDO sequenceDO = this.sequenceDOMapper.getSequenceByName("order_info");
        int sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        this.sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);

        for(int i = 0; i < 6 - sequenceStr.length(); ++i) {
            stringBuilder.append(0);
        }

        stringBuilder.append(sequenceStr);
        stringBuilder.append("00");
        return stringBuilder.toString();
    }

    //Convert Objects
    private OrderDO convertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        } else {
            OrderDO orderDO = new OrderDO();
            BeanUtils.copyProperties(orderModel, orderDO);
            orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
            orderDO.setOrderPrice(orderModel.getOrderAmount().doubleValue());
            return orderDO;
        }
    }
}