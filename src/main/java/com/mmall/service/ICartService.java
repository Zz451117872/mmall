package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVO;

/**
 * Created by aa on 2017/6/23.
 */
public interface ICartService {
    ServerResponse<CartVO> createPrepareOrder(Integer userId, String cartIds);
    ServerResponse add(Integer userId, Integer count,Integer productId);
    ServerResponse<CartVO> myCartList(Integer userId);
    ServerResponse updateQuantiry(Integer userId,Integer count,Integer cartId);
    ServerResponse deleteByCartids(Integer userId,String cartIds);

}
