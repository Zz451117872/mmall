package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpSession;

/**
 * Created by aa on 2017/6/21.
 */
@Controller
@RequestMapping("/manager_category/")
public class CategoryManagerController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    //创建 或者 更新 产品分类
    @RequestMapping(value = "save_or_update_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse saveOrUpdateCategory(HttpSession session, @Validated Category category,BindingResult bindingResult)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }

        if(bindingResult.hasErrors())
        {
            return ServerResponse.createByErrorMessage(bindingResult.getFieldError().getDefaultMessage());
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
           if(category != null)
           {
               return iCategoryService.saveOrUpdateCategory(category);
           }
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createByErrorMessage("没有权限");
    }

    @RequestMapping(value = "delete_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse deleteCategory(HttpSession session,Integer categoryId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            if(categoryId != null) {
                return iCategoryService.deleteCategory(categoryId);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createByErrorMessage("没有权限");
    }

    @RequestMapping(value = "get_category_by_id.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getCategory(HttpSession session,Integer categoryId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            if(categoryId != null) {
                return iCategoryService.getCategoryById(categoryId);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createByErrorMessage("没有权限");
    }

    @RequestMapping(value = "get_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getCategory(HttpSession session,
                                      Integer categoryId,
                                      Boolean isFillChild,
                                      Integer pageNum,
                                      Integer pageSize)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
                return iCategoryService.getCategoryByParent(categoryId, isFillChild,pageNum,pageSize);

        }
        return ServerResponse.createByErrorMessage("没有权限");
    }
}
