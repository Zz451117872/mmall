package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
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
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private IProductService iProductService;
    /*
    得到所有底层分类
     */
    public ServerResponse<List<Category>> getAllBottomCategory()
    {
        List<Category> result = Lists.newArrayList();
        List<Category> parents = categoryMapper.getParentCategory();
        if(parents != null)
        {
           for(int i=0; i<parents.size(); i++)
           {
               getBottomCategoryToResult(parents.get(i),result);
           }
        }
        return ServerResponse.createBySuccess(result);
    }
    /*
    遍历顶层分类，并将底层分类填充至结果集
     */
    private void getBottomCategoryToResult(Category parent ,List<Category> result)
    {
        List<Category> childs = categoryMapper.getCategoryByParentId(parent.getId());
        if(childs == null || childs.size()==0)
        {
            result.add(parent);
            return;
        }else{
            for(int i=0; i<childs.size(); i++)
            {
                getBottomCategoryToResult(childs.get(i),result);
            }
        }
    }
    /*
    获得主页需要显示的数据
     */
    @Override
    public ServerResponse<List<CategoryVO>> getHomePageData()
    {
        List<Category> parents = categoryMapper.getParentCategory();
        List<CategoryVO> categoryVOs = Lists.newArrayList();
        if(parents != null)
        {
            for(int i=0; i<parents.size(); i++)
            {
                CategoryVO categoryVO = assemableCategoryVO(parents.get(i));
                fillChildsToParent(categoryVO);
                categoryVOs.add(categoryVO);
            }
        }else
        {
            return ServerResponse.createByError();
        }
        return ServerResponse.createBySuccess(categoryVOs);
    }

    /*
    为某父分类填充子分类
     */
    private void fillChildsToParent(CategoryVO parent)
    {
        List<Category> childs = categoryMapper.getCategoryByParentId(parent.getId());
       if(childs == null || childs.size()==0)
       { //证明是最底层分类
           List<Product> products = productMapper.getProductByCategoryId(parent.getId());
           if(products.size()>6)
           {
               products = products.subList(0,7);
           }
           parent.setSun(true);
           parent.setProducts(products);
           return;
       }else
       {//证明还有子分类
           List<CategoryVO> childVOs = assemableCategoryVOlist(childs);
           parent.setChilds(childVOs);
           parent.setSun(false);
           for(int i=0; i<childVOs.size(); i++)
           {
               fillChildsToParent(childVOs.get(i));
           }
       }

    }

    /*
    将Cagetory集合转化为CategoryVO集合
     */
    private List<CategoryVO> assemableCategoryVOlist(List<Category> categories)
    {
        List<CategoryVO> categoryVOs = Lists.newArrayList();
        for(int i=0; i<categories.size(); i++)
        {
            CategoryVO categoryVO = assemableCategoryVO(categories.get(i));
            categoryVOs.add(categoryVO);
        }
        return categoryVOs;
    }

    /*
    将Category转化为CategoryVO
     */
    private CategoryVO assemableCategoryVO(Category category)
    {
        CategoryVO categoryVO = new CategoryVO();
        categoryVO.setId(category.getId());
        categoryVO.setName(category.getName());
        return categoryVO;
    }


    /*
    新增分类
     */
    @Override
    public ServerResponse<String> addCategory(String categoryName, Integer parentId) {
        if(parentId == null || StringUtils.isBlank(categoryName))
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
        int resultCount = categoryMapper.insert(category);
        if(resultCount >0)
        {
            return ServerResponse.createBySuccess("增加分类成功");
        }
        return ServerResponse.createByErrorMessage("增加分类错误");
    }

    /*
    修改分类名称
     */
    @Override
    public ServerResponse<String> setCategoryName(Integer categoryId, String categoryName) {
        if(categoryId == null || StringUtils.isBlank(categoryName))
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(resultCount >0)
        {
            return ServerResponse.createBySuccessMessage("更新分类名称成功 ");
        }
        return ServerResponse.createByErrorMessage("更新分类名称失败");
    }

    /*
    查询平级子节点
     */
    @Override
    public ServerResponse<List<Category>> getChildParallelCategory(Integer categoryId) {
        List<Category> categories = categoryMapper.selectCategoryChildByParentId(categoryId);
        if(CollectionUtils.isEmpty(categories))
        {
            logger.info("未找到子分类");
        }
        return ServerResponse.createBySuccess(categories);
    }

    /*
    递归查询本节点及其子节点的id(只查id有什么用)
     */
    @Override
    public ServerResponse<List<Integer>> selectCategoryAndChildByParentId(Integer categoryId)
    {
        Set<Category> temp = Sets.newHashSet();
        findChildCategory(temp,categoryId);
        List<Integer> categoryIds = Lists.newArrayList();
        for(Category category : temp)
        {
            categoryIds.add(category.getId());
        }
        return ServerResponse.createBySuccess(categoryIds);
    }

    /*
    找到某分类的所有子分类的id
     */
    private Set<Category> findChildCategory(Set<Category> temp,Integer categoryId)
    {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null)
        {
            temp.add(category);
        }
        List<Category> categories = categoryMapper.selectCategoryChildByParentId(categoryId);
        for(Category c : categories)
        {
            findChildCategory(temp,c.getId());
        }
        return temp;
    }
}
