package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.ISolrService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by aa on 2017/6/22.
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ICategoryService iCategoryService;
    @Autowired
    private ISolrService iSolrService;
    @Autowired
    private IFileService iFileService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    //通过solr 查询
    @Override
    public PageInfo getProductListToSolr(Integer pageNum,Integer pageSize)
    {
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.getList();

        PageInfo pageInfo = new PageInfo(productList);
        return pageInfo;
    }

    //保存和更新产品，是根据id来判断。如果为空则为保存，反之则为更新
    @Override
    public ServerResponse<String> saveOrUpdateProduct(Product product)
    {
        try {
            if (product != null && product.getName() != null) {
                try {
                    if (product.getStatus() != null) {
                        Const.ProductStatusEnum.codeof(product.getStatus());
                    }
                }catch (Exception e)
                {
                    return ServerResponse.createByErrorMessage("参数错误");
                }
                if (product.getId() != null)  //如果id不为空，则为更新
                {
                    int resultCount = productMapper.updateByPrimaryKeySelective(product);
                    if (resultCount > 0) {
                        return ServerResponse.createBySuccessMessage("更新产品成功");
                    }
                    return ServerResponse.createByErrorMessage("更新产品失败");
                } else { //如果id为空，则为新增
                    int result = productMapper.insertSelective(product);
                    if (result > 0) {
                        return ServerResponse.createBySuccessMessage("新增产品成功");
                    }
                    return ServerResponse.createByErrorMessage("新增产品失败");
                }
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("saveOrUpdateProduct",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    public ServerResponse soldOutOrPutaway(Integer productId)
    {
        try{
            if(productId != null)
            {
                Product product = productMapper.selectByPrimaryKey(productId);
                if(product != null)
                {
                    if(product.getStatus() == Const.ProductStatusEnum.ON_SALE.getCode())
                    {
                        product.setStatus(Const.ProductStatusEnum.SOLD_OUT.getCode());
                    }else{
                        product.setStatus(Const.ProductStatusEnum.ON_SALE.getCode());
                    }
                    int result = productMapper.updateByPrimaryKeySelective(product);
                    if(result > 0 )
                    {
                        return ServerResponse.createBySuccess();
                    }
                    return ServerResponse.createByError();
                }
                return ServerResponse.createByErrorMessage("产品不存在");
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("soldOutOrPutaway:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    //根据产品名称 或 产品id查找产品
    @Override
    public ServerResponse<ProductVO> getProductsByNameOrId(String productName,Integer productId)
    {
       try{
           productName =productName == null || productName.equals("") ? null : productName;
           if(productName != null || productId != null ) {
               Product product = productMapper.getProductByNameOrId(productName, productId);
               if(product != null) {
                   return ServerResponse.createBySuccess(convertProductVO(product));
               }
               return ServerResponse.createByErrorMessage("产品不存在");
           }
           return ServerResponse.createByErrorMessage("参数错误");
       }catch (Exception e)
       {
           logger.error("getProductsByNameOrId:",e);
           return ServerResponse.createByErrorMessage("未知错误");
       }
    }

    //得到产品详细
    @Override
    public ServerResponse<ProductVO> getProductDetail(Integer productId)
    {
       try{
           if(productId != null) {
               Product product = productMapper.selectByPrimaryKey(productId);
               if (product == null) {
                   return ServerResponse.createByErrorMessage("产品不存在");
               }
               if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
                   return ServerResponse.createByErrorMessage("产品已下架");
               }
               ProductVO productVO = convertProductVO(product);
               return ServerResponse.createBySuccess(productVO);
           }
           return ServerResponse.createByErrorMessage("参数错误");
       }catch (Exception e)
       {
           logger.error("getProductDetail:",e);
           return ServerResponse.createByErrorMessage("未知错误");
       }
    }

    //通过关键字 和 分类id查看产品集合
    @Override
    public ServerResponse<PageInfo> getProductByKeywordOrCategory(String keyword,Integer categoryId,Integer pageNum,Integer pageSize,String orderBy)
    {
        try {
            if (StringUtils.isBlank(keyword) && categoryId == null) {//如果参数错误，则返回
                return ServerResponse.createByErrorMessage("参数错误");
            }

            List<Integer> categoryIds = Lists.newArrayList();
            if (categoryId != null) {
                categoryIds = iCategoryService.getChildCategory(categoryId);
            }
            if (StringUtils.isNotBlank(keyword)) {
                keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
            }
            PageHelper.startPage(pageNum, pageSize); //分页
            if (StringUtils.isNotBlank(orderBy)) {
                if (Const.MmallOrderbySet.orderbySet.contains(orderBy)) {//设置排序，前台传入格式为 排序字段_排序顺序,后台设置排序格式为 排序字段+空格—排序顺序
                    String[] orderByArray = orderBy.split(":");
                    PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);//排序
                }
            }//通过MAPPING 得到结果集
            List<Product> products = productMapper.selectByNameOrCategoryIds(StringUtils.isBlank(keyword) ? null : keyword, categoryIds.size() == 0 ? null : categoryIds);

           if(products != null && !products.isEmpty())
           {
               List<ProductVO> result = Lists.newArrayList();
               for (Product product : products) {
                   ProductVO productVO = convertProductVO(product);
                   result.add(productVO);
               }
               PageInfo pageInfo = new PageInfo(products);
               pageInfo.setList(result);
               return ServerResponse.createBySuccess(pageInfo);
           }
            return ServerResponse.createBySuccess(null);
        }catch (Exception e)
        {
            logger.error("",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    //转换
    public ProductVO convertProductVO(Product product)
    {
        if(product == null) return null;
        ProductVO productVO = new ProductVO();
        productVO.setId(product.getId());
        productVO.setStatus(product.getStatus());
        productVO.setStatusDesc(Const.ProductStatusEnum.codeof(product.getStatus()).getValue());
        productVO.setSubtitle(product.getSubtitle());
        productVO.setMainImage(product.getMainImage());
        productVO.setPrice(product.getPrice().doubleValue());
        productVO.setSubImages(product.getSubImages());
        productVO.setCategoryId(product.getCategoryId());
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        productVO.setCategoryName(category == null ? "无":category.getName());
        productVO.setDetail(product.getDetail());
        productVO.setName(product.getName());
        productVO.setStock(product.getStock());
        productVO.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productVO.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productVO;
    }
}
