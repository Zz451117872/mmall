package com.mmall.controller.protal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import com.mmall.service.ISolrService;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by aa on 2017/7/7.
 */
@Controller
@RequestMapping("/managerSolr/")
public class SolrController {

    @Autowired
    private ISolrService iSolrService;

    @RequestMapping("fill_to_solr.do")
    @ResponseBody
    public ServerResponse fillAllProductToSolr()
    {
        return iSolrService.fillAllProductToSolr();
    }



}
