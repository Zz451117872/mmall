package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductVO;

import java.util.List;

/**
 * Created by aa on 2017/6/22.
 */
public interface IProductService {
    ServerResponse<String> saveOrUpdateProduct(Product product);
    ServerResponse<String> setSaleStatus(Integer productId,Integer status);
    ServerResponse<ProductVO> managerProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize);
    ServerResponse<PageInfo> searchProduct(String productName,Integer productId,Integer pageNum,Integer pageSize);
    ServerResponse<ProductVO> getProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductByKeywordAndCategoryId(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy);
    PageInfo getProductListToSolr(Integer pageNum,Integer pageSize);
    ServerResponse<String> autoUpload(String path);

}
