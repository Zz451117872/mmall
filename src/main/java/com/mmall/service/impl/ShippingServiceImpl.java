package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by aa on 2017/6/23.
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse<Map> add(Integer userId , Shipping shipping)
    {
        if(userId == null || shipping == null)
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        shipping.setUserId(userId);
        int num = shippingMapper.insert(shipping);//这里需要把新增的id返回，需要在xml中配置
        if(num >0)
        {
            Map result = Maps.newHashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("新增地址成功",result);
        }
        return ServerResponse.createByErrorMessage("新增地址错误");
    }

    @Override
    public ServerResponse<String> delete(Integer userId , Integer shippingId)
    {//这里要传userid，否则会有横向越权
        if(userId == null || shippingId == null)
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        int resultCount = shippingMapper.deleteByShippingidAndUserid(userId,shippingId);
        if(resultCount >0)
        {
            return ServerResponse.createBySuccess("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址错误");
    }


    @Override
    public ServerResponse update(Integer userId , Shipping shipping)
    {
        if(userId == null || shipping == null)
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        shipping.setUserId(userId); //需要设置对象用的userid，避免横向越权
        int num = shippingMapper.updateByShipping(shipping);
        if(num >0)
        {
            return ServerResponse.createBySuccess("新增地址成功");
        }
        return ServerResponse.createByErrorMessage("新增地址错误");
    }


    public ServerResponse<Shipping> select(Integer userId , Integer shippingId) {
        if (userId == null || shippingId == null) {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Shipping shipping = shippingMapper.selectByUseridAndShippingid(userId,shippingId);
        if(shipping == null)
        {
            return ServerResponse.createByErrorMessage("未查到");
        }
        return ServerResponse.createBySuccess("查询成功",shipping);
    }

    @Override
    public ServerResponse<PageInfo> list(Integer userId,int pageNum,int pageSize)
    {
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippings = shippingMapper.selectByUserid(userId);
        PageInfo pageInfo = new PageInfo(shippings);
        return ServerResponse.createBySuccess(pageInfo);
    }

}
