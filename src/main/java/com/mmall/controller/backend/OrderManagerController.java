package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.service.impl.UserServiceImpl;
import com.mmall.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * Created by aa on 2017/6/24.
 */
@Controller
@RequestMapping("/manager_order/")
public class OrderManagerController {

    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private IUserService iUserService;

    //所有订单
    @RequestMapping("get_order_by_multi_condition.do")
    @ResponseBody
    public ServerResponse<PageInfo> getOrderByMultiCondition(HttpSession session ,
                                                          @RequestParam(value = "userId",required = false)   Integer userId,
                                                          @RequestParam(value = "username",required = false)   String username,
                                                          @RequestParam(value = "orderStatus",required = false)   Integer orderStatus,
                                                          @RequestParam(value = "createTime",required = false)    Integer createTime,
                                                          @RequestParam(value = "pageNum",required = false,defaultValue = "1")   Integer pageNum,
                                                          @RequestParam(value = "pageSize",required = false,defaultValue = "10")   Integer pageSize
        )
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {//业务逻辑
            return iOrderService.getOrderByMultiCondition(userId,username,orderStatus,createTime,pageNum,pageSize,null,true);
        }else{
            return ServerResponse.createByErrorMessage("无权限");
        }
    }


    //订单详细
    @RequestMapping("order_detail.do")
    @ResponseBody
    public ServerResponse<OrderVO> getOrderDetail(HttpSession session, Long orderNo)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {//业务逻辑
            return iOrderService.getOrderDetail(null,orderNo);
        }else{
            return ServerResponse.createByErrorMessage("无权限");
        }
    }

    //发货
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<String> sendGoods(HttpSession session, Long orderNo)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {//业务逻辑
            return iOrderService.sendGoods(orderNo);
        }else{
            return ServerResponse.createByErrorMessage("无权限");
        }
    }

    //关闭订单
    @RequestMapping("close_order.do")
    @ResponseBody
    public ServerResponse<String> closeOrder(HttpSession session, Long orderNo)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {//业务逻辑
            return iOrderService.sendGoods(orderNo);
        }else{
            return ServerResponse.createByErrorMessage("无权限");
        }
    }
}
