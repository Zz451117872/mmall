package com.mmall.controller.protal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Created by aa on 2017/6/23.
 */
@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;

    //添加或者修改地址
    @RequestMapping("add_or_update_shipping.do")
    @ResponseBody
    public ServerResponse<Map> addOrUpdateShipping(HttpSession session , @Valid Shipping shipping , BindingResult bindingResult)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(bindingResult.hasErrors())
        {
            return ServerResponse.createByErrorMessage(bindingResult.getFieldError().getDefaultMessage());
        }
        return iShippingService.addOrUpdateShipping(user.getId(),shipping);
    }

    //删除地址
    @RequestMapping("delete_shipping.do")
    @ResponseBody
    public ServerResponse<String> delete(HttpSession session , Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (shippingId != null){
            return iShippingService.delete(user.getId(), shippingId);
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }

    //获取所有地址
    @RequestMapping("list.do")
    @ResponseBody
    private ServerResponse<List<Shipping>> list(HttpSession session)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.list(user.getId());
    }

    //获取单个地址
    @RequestMapping("shipping.do")
    @ResponseBody
    private ServerResponse<Shipping> getShipping(HttpSession session,Integer shippingId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(shippingId != null) {
            return iShippingService.getShipping(user.getId(), shippingId);
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }
}
