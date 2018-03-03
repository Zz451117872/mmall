package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    //通过用户id与产品id 查询 购物条目
    Cart selectCartByUseridAndProductid(@Param(value = "userId") Integer userId, @Param(value = "productId")  Integer productId);

    ////通过用户id 与 购物条目ids 查询购物条目集合,ids可以为空
    List<Cart> getByUserAndCartIds(@Param(value = "userId")Integer userId, @Param(value = "cartIdList")List<Integer> cartIdList);

    //通过用户id 查询购物条目计数
    int getCartCount(Integer userId);
}