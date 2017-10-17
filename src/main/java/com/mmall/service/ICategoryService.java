package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.vo.CategoryVO;

import java.util.List;

/**
 * Created by aa on 2017/6/21.
 */
public interface ICategoryService {
    ServerResponse<String> addCategory(String categoryName,Integer parentId);
    ServerResponse<String> setCategoryName(Integer categoryId,String categoryName);
    ServerResponse<List<Category>> getChildParallelCategory(Integer categoryId);
    ServerResponse<List<Integer>> selectCategoryAndChildByParentId(Integer categoryId);
    ServerResponse<List<CategoryVO>> getHomePageData();
    ServerResponse<List<Category>> getAllBottomCategory();
}
