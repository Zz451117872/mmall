package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.vo.CategoryVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


/**
 * Created by aa on 2017/6/21.
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    @Autowired
    private CategoryMapper categoryMapper;

    //新增 或者 修改 分类
    @Override
    public ServerResponse saveOrUpdateCategory(Category category) {
        try {
            if (category != null && category.getName() != null) {
                Category db_category = categoryMapper.getByCategoryName(category.getName());
                if (category.getId() != null) {
                    if (db_category == null || db_category.getId().intValue() == category.getId().intValue()) {
                        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
                        if (resultCount > 0) {
                            return ServerResponse.createBySuccess("更新分类成功");
                        }
                        return ServerResponse.createByErrorMessage("更新分类失败");
                    }
                    return ServerResponse.createByErrorMessage("更新分类名称不能重复");
                } else {
                    if (db_category == null) {
                        category.setStatus(true);
                        category.setSortOrder(1);
                        int resultCount = categoryMapper.insert(category);
                        if (resultCount > 0) {
                            return ServerResponse.createBySuccess("增加分类成功");
                        }
                        return ServerResponse.createByErrorMessage("增加分类失败");
                    }
                    return ServerResponse.createByErrorMessage("增加分类名称不能重复");
                }
            }
            return ServerResponse.createByErrorMessage("参数错误");
        } catch (Exception e) {
            logger.error("saveOrUpdateCategory:", e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    /*
    查询分类
    parentId：父分类id，如果为null，则查顶层分类
    isFillChildCategory：是否填充子分类
     */
    @Override
    public ServerResponse getCategoryByParent(Integer parentId, Boolean isFillChildCategory,Integer pageNum,Integer pageSize) {
        try {
            if(pageNum != null && pageSize != null)
            {
                PageHelper.startPage(pageNum,pageSize);
            }
            List<Category> categoryList = null;
            if (parentId == null) {
                categoryList = categoryMapper.getChildCategory(0);
            } else {
                categoryList = categoryMapper.getChildCategory(parentId);
            }

            if (categoryList != null && !categoryList.isEmpty()) {
                List<CategoryVO> result = Lists.newArrayList();
                for (Category category : categoryList) {
                    result.add(convertCategoryVO(category, isFillChildCategory));
                }
                if(pageNum != null && pageSize != null)
                {
                    PageInfo pageInfo = new PageInfo(categoryList);
                    pageInfo.setList(result);
                    return ServerResponse.createBySuccess(pageInfo);
                }else{
                    return ServerResponse.createBySuccess(result);
                }
            }
            return ServerResponse.createBySuccess(null);
        } catch (Exception e) {
            logger.error("getCategoryByParent:", e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    //获取 所有子分类的id
    public List<Integer> getChildCategory(Integer categoryId) {
        try {
            List<Integer> result = Lists.newArrayList();
            if (categoryId != null) {
                doGetChildCategory(categoryId, result);
            }
            return result;
        } catch (Exception e) {
            logger.error("getChildCategory:", e);
            return null;
        }
    }

    @Override
    public ServerResponse deleteCategory(Integer categoryId) {
        try {
            if (categoryId != null) {
                Category category = categoryMapper.selectByPrimaryKey(categoryId);
                if (category != null) {
                    if (category.getStatus()) {
                        List<Integer> categoryIds = getChildCategory(categoryId);
                        if (categoryIds != null && !categoryIds.isEmpty()) {
                            int result = categoryMapper.updateCategoryStatusByCategoryIds(categoryIds,0);
                            if (result > 0) {
                                return ServerResponse.createBySuccess();
                            }
                            return ServerResponse.createByErrorMessage("数据操作错误");
                        }
                        return ServerResponse.createByErrorMessage("app错误");
                    }
                    return ServerResponse.createByErrorMessage("分类已弃用");
                }
                return ServerResponse.createByErrorMessage("分类不存在");
            }
            return ServerResponse.createByErrorMessage("参数错误");
        } catch (Exception e) {
            logger.error("deleteCategory", e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    public ServerResponse getCategoryById(Integer categoryId) {
        try {
            if (categoryId != null) {
                Category category = categoryMapper.selectByPrimaryKey(categoryId);
                if (category != null) {
                    return ServerResponse.createBySuccess(convertCategoryVO(category, false));
                }
                return ServerResponse.createBySuccess(null);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        } catch (Exception e) {
            logger.error("getCategoryById", e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    private void doGetChildCategory(Integer categoryId, List<Integer> result) {
        try {
            if (categoryId != null && result != null) {
                result.add(categoryId);
                List<Category> categoryList = categoryMapper.getChildCategory(categoryId);
                if (categoryList != null && !categoryList.isEmpty()) {
                    for (Category category : categoryList) {
                        doGetChildCategory(category.getId(), result);
                    }
                    return;
                }
                return;
            }
            return;
        } catch (Exception e) {
            logger.error("doGetChildCategory:", e);
            return;
        }
    }

    //转换
    private CategoryVO convertCategoryVO(Category category, Boolean isFillChildCategory) {
        try {
            CategoryVO result = new CategoryVO();
            if (isFillChildCategory) {
                ServerResponse serverResponse = getCategoryByParent(category.getId(), isFillChildCategory,null,null);
                if (serverResponse.isSuccess()) {
                    result.setChilds((List<CategoryVO>) serverResponse.getData());
                } else {
                    result.setChilds(null);
                }
            }
            result.setSortOrder(category.getSortOrder());
            result.setCreateTime(DateTimeUtil.dateToStr(category.getCreateTime()));
            result.setName(category.getName());
            result.setId(category.getId());
            Category parent = categoryMapper.selectByPrimaryKey(category.getParentId());
            String parentName = parent == null ? "无" : parent.getName();
            result.setParentCategoryName(parentName);
            result.setParentCategory(category.getParentId());
            result.setStatusDesc(category.getStatus() ? "使用中" : "已停用");
            result.setStatus(category.getStatus());
            result.setUpdateTime(DateTimeUtil.dateToStr(category.getUpdateTime()));
            return result;
        } catch (Exception e) {
            logger.error("convertCategoryVO:", e);
            return null;
        }
    }
}
