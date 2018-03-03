package com.mmall.dao;

import com.mmall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);
    //通过产品名称 或者 产品id 查询产品
    Product getProductByNameOrId(@Param("productName") String productName, @Param("productId")Integer productId);

    //通过产品名称关键字 或者 产品分类 查询产品集合，参数都为null时，查询所有产品
    List<Product> selectByNameOrCategoryIds(@Param("productName")String productName,@Param("categoryIds")List<Integer> categoryIds);
}