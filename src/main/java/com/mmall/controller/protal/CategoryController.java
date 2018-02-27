package com.mmall.controller.protal;

import com.mmall.common.ServerResponse;
import com.mmall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by aa on 2017/7/5.
 */
@Controller
@RequestMapping("/category/")
public class CategoryController {

    @Autowired
    private ICategoryService iCategoryService;

    //获取所有 顶层分类，并要包含子分类
    @RequestMapping(value = "client_get_top_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getTopCategory()
    {                                           //第一个null 表示父分类id为空，要查顶层分类，true 表示要填充子分类
        return iCategoryService.getCategoryByParent(null,true,null,null);
    }
}
