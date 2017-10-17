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
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by aa on 2017/6/22.
 */
@Controller
@RequestMapping("/managerproduct/")
public class ProductManagerController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    @RequestMapping("manager_save_product.do")
    @ResponseBody
    public ServerResponse<String> managerSaveProduct(HttpSession session, Product product)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            //保存产品信息逻辑
            return iProductService.saveOrUpdateProduct(product);
        }else{
            return ServerResponse.createByErrorMessage("不是管理员");
        }
    }

    @RequestMapping("manager_set_sale_status.do")
    @ResponseBody
    public ServerResponse<String> managerSetSaleStatus(HttpSession session,Integer productId,Integer status)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            //更新产品状态逻辑
            return iProductService.setSaleStatus(productId,status);
        }else{
            return ServerResponse.createByErrorMessage("不是管理员");
        }
    }

    @RequestMapping("manager_detail.do")
    @ResponseBody
    public ServerResponse<ProductVO> managerDetail(HttpSession session, Integer productId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            //得到产品详细逻辑
            return iProductService.managerProductDetail(productId);
        }else{
            return ServerResponse.createByErrorMessage("不是管理员");
        }
    }

    @RequestMapping("manager_list.do")
    @ResponseBody
    public ServerResponse<PageInfo> managerList(HttpSession session, @RequestParam(value="pageNum",defaultValue = "1") int pageNum, @RequestParam(value="pageSize",defaultValue = "10") int pageSize)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            //得到产品分页数据逻辑
            return iProductService.getProductList(pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("不是管理员");
        }
    }

    @RequestMapping("manager_search_product.do")
    @ResponseBody
    public ServerResponse<PageInfo> managerSearchProduct(HttpSession session
            ,String productName
            ,Integer productId
            , @RequestParam(value="pageNum",defaultValue = "1") int pageNum
            , @RequestParam(value="pageSize",defaultValue = "10") int pageSize)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {
            //搜索产品数据逻辑
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("不是管理员");
        }
    }

    @RequestMapping("auto_upload.do")
    @ResponseBody
    public ServerResponse<String> autoUpload(HttpSession session,HttpServletRequest request)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {   // auto upload // TODO: 2017/8/31
            String path = request.getSession().getServletContext().getRealPath("upload");
            return iProductService.autoUpload(path);
        }else{
            return ServerResponse.createByErrorMessage("不是管理员");
        }
    }

    @RequestMapping("upload.do")
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
            String targerName = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targerName;
            Map<String,String> map = Maps.newHashMap();
            map.put("uri",targerName);
            map.put("url",url);
            return ServerResponse.createBySuccess(map);
        }else{
            return ServerResponse.createByErrorMessage("不是管理员");
        }
    }

    @RequestMapping("richtextUpload.do")
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
