package com.mmall.dao;

import com.mmall.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    //通过用户Id 和 订单号 查询订单，用户id主要用来防止越权
    Order selectByUseridAndOrderno(@Param(value = "userId") Integer userId,@Param(value = "orderNo") Long orderNo);


    //通过 用户id 或者 订单状态 或者 下单时间 查询订单集合，若都为null，则查询所有
    List<Order> getOrderByMultiCondition(@Param(value = "userId")Integer userId,
                                         @Param(value = "orderStatus")Integer orderStatus,
                                         @Param(value = "createTime")String createTime);

}