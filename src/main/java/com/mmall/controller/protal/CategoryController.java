package com.mmall.controller.protal;

import com.mmall.common.ServerResponse;
import com.mmall.service.ICategoryService;
import com.mmall.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by aa on 2017/7/5.
 */
@Controller
@RequestMapping("/category/")
public class CategoryController {

    @Autowired
    private ICategoryService iCategoryService;

    //得到 所有分类
    @RequestMapping("get_home_page_data.do")
    @ResponseBody
    public ServerResponse<List<CategoryVO>> getHomePageData()
    {
       return iCategoryService.getHomePageData();
    }
}
