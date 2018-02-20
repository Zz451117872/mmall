package com.mmall.controller.protal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.service.ISolrService;
import com.mmall.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by aa on 2017/6/23.
 */
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;
    @Autowired
    private ISolrService iSolrService;

    //产品详细
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductVO> detail(Integer productId)
    {
        if(productId != null) {
            return iProductService.getProductDetail(productId);
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }

    //所有产品
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> getProductsByNameOrCategory(
                                         @RequestParam(value = "keyword",required = false) String keyword,
                                         @RequestParam(value = "categoryId",required = false)Integer categoryId,
                                         @RequestParam(value = "pageNum", required = false,defaultValue = "1")int pageNum,
                                         @RequestParam(value = "pageSize",required = false,defaultValue = "10")int pageSize,
                                         @RequestParam(value = "orderBy",required = false,defaultValue = "")String orderBy)
    {
        if(keyword == null && categoryId == null) {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return iProductService.getProductByKeywordOrCategory(keyword, categoryId, pageNum, pageSize, orderBy);
    }


    //查看产品通过solr
    @RequestMapping("get_product_list_by_solr.do")
    @ResponseBody
    public ServerResponse<PageInfo> getProductListBySolr(@RequestParam(value = "keyword",required = false)String keyword,
                                                         @RequestParam(value = "pageNum",defaultValue = "1")int pageNum,
                                                         @RequestParam(value = "pageSize",defaultValue = "10")int pageSize)
    {
        return iSolrService.getProductListBySolr(keyword,pageNum,pageSize);
    }
}
