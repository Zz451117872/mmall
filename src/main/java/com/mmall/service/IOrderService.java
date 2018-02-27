package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVO;
import com.mmall.vo.OrderVO;

import java.util.Date;
import java.util.Map;

/**
 * Created by aa on 2017/6/24.
 */
public interface IOrderService {

    ServerResponse aliCallback(Map<String,String> parmas);
    ServerResponse createOrder(Integer userId, Integer shippingId,String cartIds);
    ServerResponse<PageInfo> myOrderList(Integer userId,Integer status, Integer pageNum, Integer pageSize);
    ServerResponse<OrderVO> getOrderDetail(Integer userId, Long orderNo);
    ServerResponse isPayed(Integer userId, Long orderNo);
    ServerResponse<PageInfo> getOrderByMultiCondition(Integer userId, String username, Integer orderStatus, Integer createTime, Integer pageNum, Integer pageSize,String orderby,Boolean convert);

    ServerResponse verifyAccepted(Integer id, Long orderNo);
    ServerResponse cancel(Boolean isManager,Integer user,Long orderNo);
    ServerResponse sendGoods(Long orderNo);
    ServerResponse closeOrder(Long orderNo);
    ServerResponse pay(Long orderNo,Integer userId,String path);
}
