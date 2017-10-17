package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by aa on 2017/6/24.
 */
@Controller
@RequestMapping("/managerorder/")
public class OrderManagerController {

    @Autowired
    private IOrderService iOrderService;
    @Autowired
    private IUserService iUserService;

    //所有订单
    @RequestMapping("manager_list.do")
    @ResponseBody
    public ServerResponse<PageInfo> managerList(HttpSession session ,
                                         @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10") int pageSize)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {//业务逻辑
            return iOrderService.managerList(pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限");
        }
    }


    //订单详细
    @RequestMapping("manager_detail.do")
    @ResponseBody
    public ServerResponse<OrderVO> managerDetail(HttpSession session, Long orderNo)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {//业务逻辑
            return iOrderService.managerDetail(orderNo);
        }else{
            return ServerResponse.createByErrorMessage("无权限");
        }
    }

    //查询订单
    @RequestMapping("manager_search.do")
    @ResponseBody
    public ServerResponse<PageInfo> managerSearch(HttpSession session ,Long orderNo,
                                         @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10") int pageSize)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {//业务逻辑
            return iOrderService.managerSearch(orderNo,pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限");
        }
    }

    //发货
    @RequestMapping("manager_send_goods.do")
    @ResponseBody
    public ServerResponse<String> managerSendGoods(HttpSession session, Long orderNo)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iUserService.checkAdminRole(user).isSuccess())
        {//业务逻辑
            return iOrderService.managerSendGoods(orderNo);
        }else{
            return ServerResponse.createByErrorMessage("无权限");
        }
    }
}
