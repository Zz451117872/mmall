package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
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
import com.mmall.vo.ProductListVO;
import com.mmall.vo.ProductVO;
import com.sun.xml.internal.fastinfoset.sax.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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

    public ServerResponse autoUpload(String path)
    {
        int uploadCount = 0;
        int[] categoryIds = {100006,100009,100012,100016,100021};
        String[] filenames = {"fridge","air-conditioning","phone","clothing","food"};
        for(int i=0; i<categoryIds.length; i++)
        {
            int quantity = Integer.parseInt(PropertiesUtil.getProperty("quantity"));
            int categoryId = categoryIds[i];
            String categoryName = categoryMapper.selectByPrimaryKey(categoryId).getName();
            for(int k=0; k<quantity; k++)
            {
                Product product = new Product();
                product.setCategoryId(categoryId);
                String name = UUID.randomUUID()+"";
                product.setName(name);
                product.setSubtitle(categoryName+name);

                File file = getFileByCategoryName(i,filenames);
                String targerName = iFileService.uploadFile(file,path);
                product.setMainImage(targerName);

                product.setDetail(categoryName+categoryName+categoryName);
                product.setStock(100);
                product.setPrice(new BigDecimal("200"));
                product.setStatus(1);

                productMapper.insert(product);
                uploadCount++;
            }
        }
        return ServerResponse.createBySuccess(uploadCount+"");
    }

    public File getFileByCategoryName(int index,String[] filenames)
    {
        String dirname = filenames[index];
        String path = "E:\\work\\img\\"+dirname;
        String filename = new Random().nextInt(30)+".jpg";
        File file = new File(path,filename);
        while(!file.exists())
        {
            filename = new Random().nextInt(30)+".jpg";
            file = new File(path,filename);
            System.out.println("图片不存在，重新生成文件");
        }
        return file;
    }

    @Override
    public PageInfo getProductListToSolr(Integer pageNum,Integer pageSize)
    {
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.getList();

        PageInfo pageInfo = new PageInfo(productList);
        return pageInfo;
    }

    /*
    保存和更新产品，是根据id来判断。如果为空则为保存，反之则为更新
     */
    @Override
    public ServerResponse<String> saveOrUpdateProduct(Product product)
    {
        if(product !=null)
        {
            if(StringUtils.isNotBlank(product.getSubImages()))//从子图中取出一个来做主图。
            {
                String[] subImages = product.getSubImages().split(",");
                if(subImages.length >0)
                {
                    product.setMainImage(subImages[0]);
                }
            }
            if(product.getId()!=null)  //如果id不为空，则为更新
            {
               int resultCount = productMapper.updateByPrimaryKey(product);

                if(resultCount >0)
                {
                    try {
                        iSolrService.fillProductToSolr(product);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return ServerResponse.createBySuccessMessage("更新产品成功");
                }
                return ServerResponse.createByErrorMessage("更新产品失败");
            }else{ //如果id为空，则为新增
                int productId =productMapper.insert(product);
                if(productId >0)
                {
                    try {
                        product.setId(productId);
                        iSolrService.fillProductToSolr(product);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return ServerResponse.createBySuccessMessage("新增产品成功");
                }
                return ServerResponse.createByErrorMessage("新增产品失败");
            }
        }
        return ServerResponse.createByErrorMessage("输入参数错误");
    }

    /*
    设置销售状态
     */
    @Override
    public ServerResponse<String> setSaleStatus(Integer productId,Integer status)
    {
        if( productId == null || status == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int resultCount = productMapper.updateByPrimaryKeySelective(product);
        if(resultCount >0)
        {
            return ServerResponse.createBySuccess("更新产品状态成功");
        }
        return ServerResponse.createByErrorMessage("更新产品状态失败");
    }

    /*
    获取产品详细
     */
    @Override
    public ServerResponse<ProductVO> managerProductDetail(Integer productId)
    {
        if(productId == null)
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null)
        {
            return ServerResponse.createByErrorMessage("产品不存在");
        }
        ProductVO productVO = assemleProductVO(product);
        return ServerResponse.createBySuccess(productVO);
    }

    /*
    获取产品集合
     */
    @Override
    public ServerResponse<PageInfo> getProductList(Integer pageNum,Integer pageSize)
    {
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.getList();
        List<ProductListVO> productListVOs = Lists.newArrayList();
        for(Product product : productList)
        {
            ProductListVO productListVO = assemleProductListVO(product);
            productListVOs.add(productListVO);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVOs);//这里为什么要多此一举咧，为什么不直接new
        return ServerResponse.createBySuccess(pageInfo);
    }

    /*
    根据产品名称或者产品id查找产品
     */
    @Override
    public ServerResponse<PageInfo> searchProduct(String productName,Integer productId,Integer pageNum,Integer pageSize)
    {
        PageHelper.startPage(pageNum,pageSize);
        if(StringUtils.isNotBlank(productName))
        {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName,productId);
        List<ProductListVO> productListVOs = Lists.newArrayList();
        for(Product product : productList)
        {
            ProductListVO productListVO = assemleProductListVO(product);
            productListVOs.add(productListVO);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVOs);//这里为什么要多此一举咧，为什么不直接new
        return ServerResponse.createBySuccess(pageInfo);
    }


    /*
    得到产品详细
     */
    @Override
    public ServerResponse<ProductVO> getProductDetail(Integer productId)
    {
        if(productId == null)
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null)
        {
            return ServerResponse.createByErrorMessage("产品不存在");
        }
        if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode())
        {
            return ServerResponse.createByErrorMessage("产品已下架");
        }
        ProductVO productVO = assemleProductVO(product);
        return ServerResponse.createBySuccess(productVO);
    }

    /*
    通过关键字或者分类id查看产品集合
     */
    @Override
    public ServerResponse<PageInfo> getProductByKeywordAndCategoryId(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy)
    {
        if(StringUtils.isBlank(keyword) && categoryId == null)
        {//如果参数错误，则返回
            return ServerResponse.createByErrorMessage("");
        }
        List<Integer> categoryIds = new ArrayList<Integer>();
        if(categoryId !=null)
        {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null && StringUtils.isBlank(keyword))
            {//没有该分类，且关键字为空，则返回一个空的结果集
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVO> productListVOs = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVOs);
                return ServerResponse.createBySuccess(pageInfo);
            }//如果该分类不为空，得到 该分类ID及其子分类ID
            categoryIds =iCategoryService.selectCategoryAndChildByParentId(categoryId).getData();
        }
        if(StringUtils.isNotBlank(keyword))
        {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        PageHelper.startPage(pageNum,pageSize); //分页
        if(StringUtils.isNotBlank(orderBy))
        {
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy))
            {//设置排序，前台传入格式为 排序字段_排序顺序,后台设置排序格式为 排序字段+空格—排序顺序
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);//排序
            }
        }//通过MAPPING 得到结果集
        List<Product> products = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,categoryIds.size()==0?null:categoryIds);

        List<ProductListVO> productListVOs = Lists.newArrayList();
        for (Product product : products)
        {
            ProductListVO productListVO = assemleProductListVO(product);
            productListVOs.add(productListVO);
        }
        System.out.println("size:"+productListVOs.size());
        PageInfo pageInfo = new PageInfo(products);
        pageInfo.setList(productListVOs);
        return ServerResponse.createBySuccess(pageInfo);
    }












    public ProductListVO assemleProductListVO(Product product)
    {
        ProductListVO productListVO = new ProductListVO();
        productListVO.setId(product.getId());
        productListVO.setName(product.getName());
        productListVO.setMainImage(product.getMainImage());
        productListVO.setStatus(product.getStatus());
        productListVO.setCategoryId(product.getCategoryId());
        productListVO.setPrice(product.getPrice());
        productListVO.setStock(product.getStock());
        productListVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVO.setSubtitle(product.getSubtitle());
        return productListVO;
    }

    public ProductVO assemleProductVO(Product product)
    {
        ProductVO productVO = new ProductVO();
        productVO.setId(product.getId());
        productVO.setStatus(product.getStatus());
        productVO.setSubtitle(product.getSubtitle());
        productVO.setMainImage(product.getMainImage());
        productVO.setPrice(product.getPrice());
        productVO.setSubImages(product.getSubImages());
        productVO.setCategoryId(product.getCategoryId());
        productVO.setDetail(product.getDetail());
        productVO.setName(product.getName());
        productVO.setStock(product.getStock());
        //imageHost
        productVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        //parentCategoryId
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null)
        {
            productVO.setParentCategoryId(0);
        }else{
            productVO.setParentCategoryId(category.getParentId());
        }
        //createTime
        productVO.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        //updateTime
        productVO.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productVO;
    }
}
