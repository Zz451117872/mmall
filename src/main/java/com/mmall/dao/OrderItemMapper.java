package com.mmall.dao;

import com.mmall.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    //根据订单号 与 用户Id 查询 订单条目，用户id主要用来区分普通用户或者管理员
    List<OrderItem> getByOrderOrUser(@Param("orderNo") Long orderNo, @Param("userId") Integer userId);

    //批量插入订单条目
    void batchInsert(@Param("orderItemList")List<OrderItem> orderItemList);


}