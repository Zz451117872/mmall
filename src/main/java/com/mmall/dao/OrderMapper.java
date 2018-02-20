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

    Order selectByUseridAndOrderno(@Param(value = "userId") Integer userId,@Param(value = "orderNo") Long orderNo);

    Order selectByOrderno(Long orderNo);

    List<Order> selectAll();

    List<Order> selectByUseridOrStatus(@Param(value = "userId")Integer userId, @Param(value = "status")Integer status);

    List<Order> getOrderByMultiCondition(@Param(value = "userId")Integer userId,
                                         @Param(value = "orderStatus")Integer orderStatus,
                                         @Param(value = "createTime")String createTime);

}