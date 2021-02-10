package com.example.demo.dao;

import com.example.demo.dataobject.ItemDO;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemDOMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ItemDO record);

    int insertSelective(ItemDO record);

    ItemDO selectByPrimaryKey(Integer id);
    List<ItemDO> listItem();

    int updateByPrimaryKeySelective(ItemDO record);

    int updateByPrimaryKey(ItemDO record);

    void increaseSales(@Param("id") Integer id, @Param("amount") Integer amount);
}