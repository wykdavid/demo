package com.example.demo.dao;

import com.example.demo.dataobject.UserOrderDO;

import java.util.List;

public interface UserOrderDOMapper {
    int deleteByPrimaryKey(Integer userOrder);

    int insert(UserOrderDO record);

    int insertSelective(UserOrderDO record);

    UserOrderDO selectByPrimaryKey(Integer userOrder);

    int updateByPrimaryKeySelective(UserOrderDO record);

    int updateByPrimaryKey(UserOrderDO record);

    List<UserOrderDO> listMyOrder(Integer id);
}