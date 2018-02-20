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
    ServerResponse<ProductVO> getProductsByNameOrId(String productName,Integer productId);
    ServerResponse<ProductVO> getProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductByKeywordOrCategory(String keyword,Integer categoryId,Integer pageNum,Integer pageSize,String orderBy);
    PageInfo getProductListToSolr(Integer pageNum,Integer pageSize);

    ServerResponse soldOutOrPutaway(Integer productId);
}
