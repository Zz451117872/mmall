package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.vo.CategoryVO;

import java.util.List;

/**
 * Created by aa on 2017/6/21.
 */
public interface ICategoryService {
    ServerResponse saveOrUpdateCategory(Category category);

    ServerResponse<PageInfo> getCategoryByParent(Integer parentId, Boolean isFillChildCategory, Integer pageNum, Integer pageSize);

    List<Integer> getChildCategory(Integer categoryId);

    ServerResponse deleteCategory(Integer categoryId);

    ServerResponse getCategoryById(Integer categoryId);
}
