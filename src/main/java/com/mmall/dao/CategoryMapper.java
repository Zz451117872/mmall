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

    Category getByCategoryName(String name);

    List<Category> getChildCategory(Integer parentId);

    int updateCategoryStatusByCategoryIds(@Param("categoryIds") List<Integer> categoryIds,@Param("status")Integer status);
}