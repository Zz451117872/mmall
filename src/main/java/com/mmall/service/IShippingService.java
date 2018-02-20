package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.vo.ShippingVO;

import java.util.List;
import java.util.Map;

/**
 * Created by aa on 2017/6/23.
 */
public interface IShippingService {
    ServerResponse<String> delete(Integer userId , Integer shippingId);
    ServerResponse<List<Shipping>> list(Integer userId);
    ServerResponse<Map> addOrUpdateShipping(Integer id, Shipping shipping);

    ServerResponse<Shipping> getShipping(Integer id, Integer shippingId);
}
