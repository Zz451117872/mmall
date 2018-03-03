package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import com.mmall.service.ISolrService;
import com.mmall.vo.ProductVO;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by aa on 2017/7/7.
 */
@Service("iSolrService")
public class SolrServiceImpl implements ISolrService {

    @Autowired
    private HttpSolrClient httpSolrClient;
    @Autowired
    private IProductService iProductService;

    public ServerResponse<PageInfo> getProductListBySolr(String keyword,int pageNum,int pageSize)
    {
        List<ProductVO> productList = Lists.newArrayList();
        SolrQuery query = new SolrQuery();
        query.set("q",keyword);
        query.set("df","name");
        query.setStart((pageNum-1)*pageSize);
        query.setRows(pageSize);
        query.setSort("id", SolrQuery.ORDER.desc);

        try {
            PageHelper.startPage(pageNum,pageSize);
            QueryResponse response = httpSolrClient.query(query);
            SolrDocumentList solrDocumentList =  response.getResults();
            for(SolrDocument solrDocument : solrDocumentList)
            {
                ProductVO product = new ProductVO();
                product.setId( Integer.parseInt(solrDocument.get("id").toString()));
                product.setName((String)solrDocument.get("name"));
                product.setSubtitle((String)solrDocument.get("subtitle"));
                product.setMainImage((String)solrDocument.get("main_image"));
                product.setPrice(Double.parseDouble((String)solrDocument.get("price")));
                product.setStock(Integer.parseInt(solrDocument.get("stock").toString()));
                product.setStatus(Integer.parseInt(solrDocument.get("status").toString()));
                productList.add(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createByError();
        }

        PageInfo pageInfo = new PageInfo();
        pageInfo.setList(productList);
        return ServerResponse.createBySuccess(pageInfo);
    }


    public boolean deleteAllProductFromSolr() throws Exception {
            try{
                httpSolrClient.deleteByQuery("*:*");
            }catch (Exception ex){
                throw new Exception("从solr中删除产品出错");
            }
        return true;
    }

    public boolean deleteProductFromSolr(Integer id) throws Exception {
        if(id !=null)
        {
            try{
                httpSolrClient.deleteById(id.toString());
            }catch (Exception ex){
                throw new Exception("从solr中删除产品出错");
            }
        }
        return true;
    }

    public ServerResponse fillAllProductToSolr()
    {
        int start = 1;
        PageInfo pageInfo = null ; //iProductService.getProductListToSolr(start,100);
        List<Product> productList = null;//pageInfo.getList();
        try {
            fillProductListToSolr(productList);
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.createByError();
        }

        if(!pageInfo.isIsLastPage())
        {
            start++;
            pageInfo = null; // iProductService.getProductListToSolr(start,100);
            try {
                fillProductListToSolr(pageInfo.getList());
            } catch (Exception e) {
                return ServerResponse.createByError();
            }
        }
        return ServerResponse.createBySuccess();
    }

    public   boolean fillProductListToSolr(List<Product> productList) throws Exception {
        if(productList != null)
        {
            System.out.println("正在写入："+productList.size());
            for(int i=0; i<productList.size(); i++)
            {
                try{
                    fillProductToSolr(productList.get(i));
                }catch (Exception ex){
                    throw new Exception("写入solr出错");
                }
            }
        }
        return true;
    }

    public boolean fillProductToSolr(Product product) throws Exception {
        if(product != null)
        {
            try{
                SolrInputDocument document = new SolrInputDocument();
                document.addField("id",product.getId());
                document.addField("name",product.getName());
                document.addField("main_image",product.getMainImage());
                document.addField("subtitle",product.getSubtitle());
                document.addField("price",product.getPrice().toString());
                document.addField("stock",product.getStock());
                document.addField("status",product.getStatus());
                httpSolrClient.add(document);
                httpSolrClient.commit();
            }catch (Exception ex){
                ex.printStackTrace();
                throw new Exception("写入solr出错");
            }
        }
        return true;
    }
}
