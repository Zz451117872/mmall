package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVO;

import java.util.Map;

/**
 * Created by aa on 2017/6/24.
 */
public interface IOrderService {
    ServerResponse pay(Long orderNo,Integer userId,String path);
    public ServerResponse aliCallback(Map<String,String> parmas);
    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);
    ServerResponse createOrder(Integer userId, Integer shippingId);
    ServerResponse cancel(Integer user,Long orderNo);
    ServerResponse getCartOrderProduct(Integer userId);
    ServerResponse<OrderVO> detail(Integer userId,Long orderNo);
    ServerResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize);
    ServerResponse<PageInfo> managerList(int pageNum,int pageSize);
    ServerResponse<OrderVO> managerDetail(Long orderNo);
    ServerResponse<PageInfo> managerSearch(Long orderNo,int pageNum,int pageSize);
    ServerResponse<String> managerSendGoods(Long orderNo);
}
