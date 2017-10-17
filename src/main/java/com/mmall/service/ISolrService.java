package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;

import java.util.List;

/**
 * Created by aa on 2017/7/7.
 */
public interface ISolrService {
    ServerResponse fillAllProductToSolr();
    boolean fillProductListToSolr(List<Product> productList) throws Exception;
    boolean fillProductToSolr(Product product) throws Exception;
    boolean deleteAllProductFromSolr() throws Exception;
    boolean deleteProductFromSolr(Integer id) throws Exception;
    ServerResponse<PageInfo> getProductListBySolr(String keyword, int pageNum, int pageSize);
}
