package com.mmall.dao;

import com.mmall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByShipping(Shipping record);

    int updateByPrimaryKey(Shipping record);

    //通过收货地址id　和 用户id 删除 收货地址
    int deleteByShippingidAndUserid(@Param("shippingId") Integer shippingId, @Param("userId")Integer userId);

    //通过用户id查询所有收货地址
    List<Shipping> selectByUserid(Integer userId);
}