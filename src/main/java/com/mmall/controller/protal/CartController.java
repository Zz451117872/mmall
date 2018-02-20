package com.mmall.controller.protal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Cart;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

/**
 * Created by aa on 2017/6/23.
 */
@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

    //添加购物条目
    @RequestMapping("add_cart.do")
    @ResponseBody
    public ServerResponse addCart(HttpSession session , @Valid Cart cart , BindingResult bindingResult)
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
        return iCartService.add(user.getId(), cart.getQuantity(),cart.getProductId());
    }


    //更新购物 数量
    @RequestMapping("update_quantity.do")
    @ResponseBody
    public ServerResponse updateQuantity(HttpSession session ,Integer quantity,Integer cartId)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(cartId != null && quantity != null) {
            return iCartService.updateQuantiry(user.getId(), quantity, cartId);
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }

    //删除购物 条目
    @RequestMapping("delete_by_cartids.do")
    @ResponseBody
    public ServerResponse deleteByCartIds(HttpSession session , String cartIds)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(cartIds != null && !"".equals(cartIds)) {
            return iCartService.deleteByCartids(user.getId(), cartIds);
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }

    //我的购物车
    @RequestMapping("my_cart_list.do")
    @ResponseBody
    public ServerResponse<CartVO> myCartList(HttpSession session)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.myCartList(user.getId());
    }

    //获取预订单
    @RequestMapping("prepare_order.do")
    @ResponseBody
    public ServerResponse<CartVO> createPrepareOrder(HttpSession session, String cartIds)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(cartIds != null && !"".equals(cartIds)) {
            return iCartService.createPrepareOrder(user.getId(), cartIds);
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }
}
