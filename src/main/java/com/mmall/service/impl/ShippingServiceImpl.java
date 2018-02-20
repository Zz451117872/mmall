package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import com.mmall.vo.ShippingVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by aa on 2017/6/23.
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse addOrUpdateShipping(Integer userId , Shipping shipping)
    {
        if(userId == null || shipping == null)
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        if(shipping.getId() == null)
        {
            int shippingCapacity = shippingMapper.selectByUserid(userId).size();
            if(shippingCapacity >= Const.shippingCapacityUpperLimit)
            {
                return ServerResponse.createByErrorMessage("达到地址数量上限");
            }
            shipping.setUserId(userId);
            int num = shippingMapper.insert(shipping);
            if(num > 0)
            {
                return ServerResponse.createBySuccess();
            }
            return ServerResponse.createByError();
        }else{
            shipping.setUserId(userId); //需要设置对象用的userid，避免横向越权
            int num = shippingMapper.updateByShipping(shipping);
            if(num >0)
            {
                return ServerResponse.createBySuccess("新增地址成功");
            }
            return ServerResponse.createByErrorMessage("新增地址错误");
        }
    }

    @Override
    public ServerResponse<String> delete(Integer userId , Integer shippingId)
    {
        try {
            if (userId == null || shippingId == null) {
                return ServerResponse.createByErrorMessage("参数错误");
            }
            int resultCount = shippingMapper.deleteByShippingidAndUserid(shippingId,userId);
            if (resultCount > 0) {
                return ServerResponse.createBySuccess("删除地址成功");
            }
            return ServerResponse.createByErrorMessage("删除地址错误");
        }catch (Exception e)
        {
            logger.error("delete:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    @Override
    public ServerResponse<List<Shipping>> list(Integer userId)
    {
        try {
            if(userId != null) {
                List<Shipping> shippings = shippingMapper.selectByUserid(userId);
                if(shippings != null && !shippings.isEmpty()) {
                    return ServerResponse.createBySuccess(shippings);
                }
                return ServerResponse.createBySuccess(null);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("list:",e);
            return  ServerResponse.createByErrorMessage("未知错误");
        }
    }

    public ServerResponse<Shipping> getShipping(Integer id, Integer shippingId)
    {
        try {
            if(id != null && shippingId != null) {
                Shipping shipping = shippingMapper.selectByPrimaryKey(shippingId);
                if(shipping != null) {
                    return ServerResponse.createBySuccess(shipping);
                }
                return ServerResponse.createBySuccess(null);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e){
            logger.error("getShipping:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }
}
