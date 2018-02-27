package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Map;

/**
 * Created by aa on 2017/6/22.
 */
@Controller
@RequestMapping("/manager_product/")
public class ProductManagerController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    //保存 或者 更新产品
    @RequestMapping(value = "save_or_update_product.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> saveOrUpdateProduct(HttpSession session, @Valid Product product , BindingResult bindingResult)
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
            return iProductService.saveOrUpdateProduct(product);
        }
        return ServerResponse.createByErrorMessage("不是管理员");
    }

    // 下架 或者 上架 产品
    @RequestMapping(value = "sold_out_or_putaway.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse soldOutOrPutaway(HttpSession session,Integer productId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            if(productId != null) {
                return iProductService.soldOutOrPutaway(productId);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }else{
            return ServerResponse.createByErrorMessage("不是管理员");
        }
    }

    //拉取产品 信息
    @RequestMapping(value = "get_product_detail.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductVO> getProductDetail(HttpSession session, Integer productId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            if(productId != null) {
                return iProductService.getProductDetail(productId);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createByErrorMessage("不是管理员");
    }

    //查找单个产品，通过产品名称或者产品id
    @RequestMapping(value = "get_product_by_name_or_id.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductVO> getProductsByNameOrId(HttpSession session,
                                                           @RequestParam(value = "productName",required = false) String productName ,
                                                           @RequestParam(value = "productId",required = false) Integer productId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            if(productId != null || productName != null) {
                return iProductService.getProductsByNameOrId(productName, productId);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createByErrorMessage("不是管理员");
    }

    //通过产品关键字 或者 产品分类 获取产品集合
    @RequestMapping(value = "get_products_by_keyword_or_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> getProductByKeywordOrCategory(HttpSession session,
                                @RequestParam(value = "keyword",required = false) String keyword,
                                @RequestParam(value = "categoryId",required = false) Integer categoryId,
                                @RequestParam(value = "pageNum",required = false,defaultValue = "1") Integer pageNum,
                                @RequestParam(value = "pageSize",required = false,defaultValue = "10") Integer pageSize,
                                @RequestParam(value = "orderBy",required = false,defaultValue = "") String orderBy)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess()) {
            if(keyword != null || categoryId != null) {
                return iProductService.getProductByKeywordOrCategory(keyword, categoryId, pageNum, pageSize, orderBy);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createByErrorMessage("权限不足");
    }

    //上传文件
    @RequestMapping(value = "upload.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Map<String,String>> upload(HttpSession session,MultipartFile file, HttpServletRequest request)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }

        if(file == null)
        {
            return ServerResponse.createByErrorMessage("传入参数错误");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String  targerName = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targerName;
            Map<String,String> map = Maps.newHashMap();
            map.put("uri",targerName);
            map.put("url",url);
            return ServerResponse.createBySuccess(map);
        }
        return ServerResponse.createByErrorMessage("不是管理员");
    }

    //上传富文本
    @RequestMapping(value = "richtext_upload.do", method = RequestMethod.POST)
    @ResponseBody
    public Map richtextUpload(HttpSession session, @RequestParam(value = "file",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response)
    {
        Map resultMap = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            resultMap.put("success",false);
            resultMap.put("msg","no login");
            return resultMap;
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targerName = iFileService.upload(file,path);
            if(StringUtils.isBlank(targerName))
            {
                resultMap.put("success",false);
                resultMap.put("msg","upload faile");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targerName;
            resultMap.put("success",true);
            resultMap.put("msg","upload success");
            resultMap.put("file_path",url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name"); //这是做什么的
            return resultMap;
        }else{
            resultMap.put("success",false);
            resultMap.put("msg","no admin");
            return resultMap;
        }
    }


}
