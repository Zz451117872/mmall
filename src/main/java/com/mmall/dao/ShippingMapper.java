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

    int updateByPrimaryKey(Shipping record);
    int deleteByShippingidAndUserid(@Param("shippingId") Integer shippingId, @Param("userId")Integer userId);
    int updateByShipping(Shipping record);
    Shipping selectByUseridAndShippingid(@Param("shippingId") Integer shippingId, @Param("userId")Integer userId);
    List<Shipping> selectByUserid(Integer userId);
}