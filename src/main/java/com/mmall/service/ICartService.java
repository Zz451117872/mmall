package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVO;

/**
 * Created by aa on 2017/6/23.
 */
public interface ICartService {

    ServerResponse<CartVO> add(Integer userId, Integer count, Integer productId);
    ServerResponse<CartVO> update(Integer userId,Integer count,Integer productId);
    ServerResponse<CartVO> delete(Integer userId,String productIds);
    ServerResponse<CartVO> list(Integer userId);
    ServerResponse<CartVO> selectOrUnselect(Integer userId,Integer checked,Integer productId);
    ServerResponse<Integer> selectCartProductCount(Integer userId);
    ServerResponse<Integer> updateQuantiry(Integer userId,Integer count,Integer cartId);
    ServerResponse<CartVO> deleteByCartids(Integer userId,String cartIds);

}
