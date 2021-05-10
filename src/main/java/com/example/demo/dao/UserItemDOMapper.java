package com.example.demo.dao;

import com.example.demo.dataobject.UserItemDO;

import java.util.List;

public interface UserItemDOMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserItemDO record);

    int insertSelective(UserItemDO record);

    UserItemDO selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserItemDO record);

    int updateByPrimaryKey(UserItemDO record);
    List<UserItemDO> listMyItem(Integer userId);
}