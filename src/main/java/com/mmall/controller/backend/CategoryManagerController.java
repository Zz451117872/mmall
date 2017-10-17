package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import com.mmall.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by aa on 2017/6/21.
 */
@Controller
@RequestMapping("/managercategory/")
public class CategoryManagerController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse<String> addCategory(HttpSession session,String categoryName,@RequestParam(value = "parentId",defaultValue = "0") int parentId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            return iCategoryService.addCategory(categoryName,parentId);
        }
        return ServerResponse.createByErrorMessage("增加分类出错");
    }

    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpSession session,Integer categoryId,String categoryName)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            return iCategoryService.setCategoryName(categoryId,categoryName);
        }
        return ServerResponse.createByErrorMessage("设置分类名称出错");
    }

    @RequestMapping("get_category_child.do")
    @ResponseBody
    public ServerResponse getCategoryChildByParentId(HttpSession session,Integer categoryId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            return iCategoryService.getChildParallelCategory(categoryId);
        }
        return ServerResponse.createByErrorMessage("设置分类名称出错");
    }


    @RequestMapping("get_category_deep_child.do")
    @ResponseBody
    public ServerResponse getCategoryDeepChildByParentId(HttpSession session,Integer categoryId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            return iCategoryService.selectCategoryAndChildByParentId(categoryId);
        }
        return ServerResponse.createByErrorMessage("设置分类名称出错");
    }


    @RequestMapping("get_all_bottom_category.do")
    @ResponseBody
    public ServerResponse<List<Category>> getAllBottomCategory(HttpSession session)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
           return iCategoryService.getAllBottomCategory();
        }
        return ServerResponse.createByErrorMessage("设置分类名称出错");
    }

}
