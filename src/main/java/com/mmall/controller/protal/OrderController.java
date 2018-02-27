package com.mmall.controller.protal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.OrderMapper;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.vo.OrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by aa on 2017/6/24.
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    private Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    //通过 购物条目集合 与 收货地址 生成订单
    @RequestMapping(value = "create_order.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Long> createOrder(HttpSession session, Integer shippingId , String cartIds)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.createOrder(user.getId(),shippingId,cartIds);
    }

    //通过 订单号 取消订单
    @RequestMapping(value = "cancel_order.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<OrderVO> cancel(HttpSession session, Long orderNo )
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }                           //false表示非管理员取消订单
        return iOrderService.cancel(false,user.getId(),orderNo);
    }

    //通过 订单号 验证收货
    @RequestMapping(value = "verify_accepted.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse verifyAccepted(HttpSession session, Long orderNo )
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.verifyAccepted(user.getId(),orderNo);
    }

    //通过 订单号 获取 订单详细
    @RequestMapping(value = "order_detail.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<OrderVO> detail(HttpSession session ,Long orderNo)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }

    //通过 订单状态 查询订单
    @RequestMapping(value = "my_order_list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> myOrderList(HttpSession session ,
                                         @RequestParam(value = "status",required = false,defaultValue = "0")Integer status,
                                         @RequestParam(value = "pageNum",required = false,defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize",required = false,defaultValue = "10") Integer pageSize)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.myOrderList(user.getId(),status,pageNum,pageSize);
    }

    //通过 订单号 支付
    @RequestMapping(value = "pay.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo)
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByError();
        }
        String path = session.getServletContext().getRealPath("upload");
        return iOrderService.pay(orderNo,user.getId(),path);
    }

    //通过 订单号 检查 该订单是否已支付
    @RequestMapping(value = "isPayed.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse isPayed( HttpSession session , Long orderNo )
    {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null)
        {
            return ServerResponse.createByError();
        }
        return iOrderService.isPayed(user.getId(),orderNo);
    }

    //支付宝回调
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        HashMap<String,String> params = Maps.newHashMap();
        Map paramMap = request.getParameterMap();

        for(Iterator i = paramMap.keySet().iterator(); i.hasNext();)
        {
            String name = (String) i.next();
            String[] values = (String[]) paramMap.get(name);
            String valueStr = "";
            for(int k=0; k<values.length; k++)
            {
                valueStr = (k == values.length-1) ? valueStr+values[k] : valueStr+values[k]+",";
            }
            params.put(name,valueStr);
        }
        logger.info("支付宝回调，sign:{},trade_status:{},params:{}", params.get("sign"),params.get("trade_status"),params.toString());

        //验证回调的正确性，是否是支付宝发的，且要验证是否重复
        params.remove("sign_type");
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if(!alipayRSACheckedV2)
            {
                return ServerResponse.createByErrorMessage("再来打扁你");
            }

        } catch (AlipayApiException e) {
            logger.error("支付宝回调异常");
            e.printStackTrace();
        }

        //验证各种数据

        ServerResponse serverResponse = iOrderService.aliCallback(params);
        if(serverResponse.isSuccess())
        {
            return Const.AlipayCallable.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallable.RESPONSE_FAILED;
    }
}
