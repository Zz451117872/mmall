package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);
    Cart selectCartByUseridAndProductid(@Param(value = "userId") Integer userId, @Param(value = "productId")  Integer productId);
    List<Cart> selectCartByUserid(Integer userId);

    int selectCartProductCheckStatusByUserid(Integer userId);

    int deleteCartByUseridAndProductids(@Param(value = "userId") Integer userId,@Param(value = "productIds")List<String> productIds);

    int checkedOrUncheckedProduct(@Param(value = "userId") Integer userId,@Param(value = "checked")Integer checked,@RequestParam(value = "productId")  Integer productId  );
    int selectCartProductCount(Integer userId);
    List<Cart> selectCheckedCartByUserid(Integer userId);

}