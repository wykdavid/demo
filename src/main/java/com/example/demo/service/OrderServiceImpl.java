package com.example.demo.service;

import com.example.demo.MQ.MQProducer;
import com.example.demo.dao.OrderDOMapper;
import com.example.demo.dao.SequenceDOMapper;
import com.example.demo.dao.StockLogDOMapper;
import com.example.demo.dataobject.OrderDO;
import com.example.demo.dataobject.SequenceDO;
import com.example.demo.dataobject.StockLogDO;
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
public class OrderServiceImpl implements OrderService{



    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;
    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId,Integer amount,String stockLogId) throws BusinessException {
        //check whether item exist, whether user is legal, whether amount is correct
        //ItemModel itemModel = itemService.getItemById(itemId);

        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
//        if(itemModel == null){
//            throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR,"Item does not exist");
//        }
//
//        UserModel userModel = userService.getUserByIdInCache(userId);
//        if(userModel==null){
//            throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR,"User does not exist");
//        }

        if(amount <=0 || amount>99){
            throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR,"Amount too small or too biger");
        }

        //check promo
//        if(promoId!=null){
//            if(promoId.intValue()!=itemModel.getId()){
//                throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR,"Promo info incorrect");
//            }
//
//            else if(itemModel.getPromoModel().getStatus().intValue()!=2){
//                throw new BusinessException(EnumBUsinessError.PARAMETER_VALIDATION_ERROR,"Promo has not started");
//
//            }
//        }


        //reduce item amount
        boolean reduce = itemService.decreaseStock(itemId,amount);
        if(!reduce){
            throw new BusinessException(EnumBUsinessError.STOCK_NOT_ENOUGH);
        }

        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);
        if(promoId!=null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }
        else {
            orderModel.setItemPrice(itemModel.getPrice());
        }

        orderModel.setPromoId(promoId);
        orderModel.setOrderAmount(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        orderModel.setId(generateOrderNo());
        OrderDO orderDO = convertFromOrderModel(orderModel);

        orderDOMapper.insertSelective(orderDO);

        itemService.increaseSales(itemId,amount);

        //设置库存流水状态为成功
        StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
        if(stockLogDO==null){
            throw new BusinessException(EnumBUsinessError.UNKNOWN_ERROR);

        }
        stockLogDO.setStatus(2);
        stockLogDOMapper.updateByPrimaryKey(stockLogDO);

//        try {
//            Thread.sleep(60000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//            @Override
//            public void afterCommit() {
//                boolean sendResult = itemService.asyncDecreaseStock(itemId,amount);
////                if(!sendResult){
////                    itemService.increaseStock(itemId,amount);
////                    throw new BusinessException(EnumBUsinessError.MQ_SEND_FAIL);
////                }
//            }
//        });


        return orderModel;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo(){
        //first 8bits yyyy/mm//dd
        StringBuilder stringBuilder = new StringBuilder();
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        //nowDate.replace("-","");
        stringBuilder.append(nowDate);


        //auto increase seq
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for(int i=0;i<6-sequenceStr.length();i++){
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);

        //divide database, table flag
        stringBuilder.append("00");

        return stringBuilder.toString();
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel==null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderAmount().doubleValue());
        return orderDO;
    }
}
