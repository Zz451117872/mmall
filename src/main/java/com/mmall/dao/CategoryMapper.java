package com.mmall.dao;

import com.mmall.pojo.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CategoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);

    //通过产品分类名 获取 产品分类
    Category getByCategoryName(String name);
    //通过父类产品分类 获取 子分类
    List<Category> getChildCategory(Integer parentId);
    //更新产品分类状态 对 传入的产品分类id
    int updateCategoryStatusByCategoryIds(@Param("categoryIds") List<Integer> categoryIds,@Param("status")Integer status);
}