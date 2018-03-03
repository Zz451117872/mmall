package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.exception.AppException;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.vo.CartItemVO;
import com.mmall.vo.CartVO;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by aa on 2017/6/23.
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;


    //创建预订单
    @Override
    public ServerResponse<CartVO> createPrepareOrder(Integer userId, String cartIds) {
        try{
            if(userId != null && cartIds != null) {
                List<Integer> cartIdList = Lists.newArrayList();
                String[] cartIdArr = cartIds.split(",");
                try {
                    for (int i = 0; i < cartIdArr.length; i++) {
                        cartIdList.add(Integer.parseInt(cartIdArr[i]));
                    }
                } catch (Exception e) {
                    logger.error("createPrepareOrder:",e);
                    return ServerResponse.createByErrorMessage("参数错误");
                }
                List<Cart> cartList = cartMapper.getByUserAndCartIds(userId, cartIdList);
                if (cartList != null && cartList.size() > 0) {
                    return ServerResponse.createBySuccess(convertCartVO(cartList));
                }
                return ServerResponse.createBySuccess(null);
            }
            return  ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("createPrepareOrder:",e);
            return  ServerResponse.createByErrorMessage("未知错误");
        }
    }

    //添加购物条目
    @Override
    public ServerResponse add(Integer userId,Integer count,Integer productId)
    {
       try{
           if(userId != null && count != null && productId != null) {
               Cart cart = cartMapper.selectCartByUseridAndProductid(userId, productId);
               if (cart == null) {//这个产品不在购物车里，需要新增
                   int cartCapacityUpperLimit = cartMapper.getCartCount(userId);
                   if(cartCapacityUpperLimit >= Const.cartCapacityUpperLimit)
                   {
                       return ServerResponse.createByErrorMessage("购物车已满，请清理 ");
                   }
                   Cart cartItem = new Cart();
                   cartItem.setQuantity(count);
                   cartItem.setProductId(productId);
                   cartItem.setUserId(userId);
                   cartMapper.insertSelective(cartItem);
                   return ServerResponse.createBySuccess();
               } else {//这个产品在购物车里，不需要新增，只需改变数量
                   count = cart.getQuantity() + count;
                   cart.setQuantity(count);
                   cartMapper.updateByPrimaryKeySelective(cart);
                   return ServerResponse.createBySuccess();
               }
           }
           return ServerResponse.createByErrorMessage("参数异常");
       }catch (AppException e) {
           logger.error("add:",e);
          return ServerResponse.createByErrorMessage("未知异常");
       }
    }

    //修改购物车中产品的购买数量
    @Override
    public ServerResponse updateQuantiry(Integer userId,Integer count,Integer cartId)
    {
        try{
            if(userId != null && count != null && cartId != null) {
                Cart cart = cartMapper.selectByPrimaryKey(cartId);
                if (cart != null) {
                    Integer result = cart.getQuantity() - count > 0 ? cart.getQuantity() - 1 : cart.getQuantity() + 1;
                    cart.setQuantity(result);
                } else {
                    return ServerResponse.createByErrorMessage("没有目标项");
                }
                cartMapper.updateByPrimaryKeySelective(cart);
                return ServerResponse.createBySuccess();
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("updateQuantiry:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    @Override
    @Transactional      //带事务的方法，不能捕获异常，否则事务失效
    public ServerResponse deleteByCartids(Integer userId,String cartIds)
    {
           if(userId != null && cartIds != null) {
               List<String> cartIdList = Splitter.on(",").splitToList(cartIds);
               if (CollectionUtils.isEmpty(cartIdList)) {
                   return ServerResponse.createByErrorMessage("参数错误");
               }
               for (int i = 0; i < cartIdList.size(); i++) {
                   Integer cartId = Integer.parseInt(cartIdList.get(i));
                   cartMapper.deleteByPrimaryKey(cartId);
               }
               return myCartList(userId);
           }
           return ServerResponse.createByErrorMessage("参数错误");

    }

    @Override
    public ServerResponse<CartVO> myCartList(Integer userId)
    {
        try {
            if(userId != null) {
                List<Cart> cartList = cartMapper.getByUserAndCartIds(userId,null);
                if(cartList != null && !cartList.isEmpty()) {
                    return ServerResponse.createBySuccess(convertCartVO(cartList));
                }
                return ServerResponse.createBySuccess(null);
            }
            return ServerResponse.createByErrorMessage("参数错误");
        }catch (Exception e)
        {
            logger.error("myCartList:",e);
            return ServerResponse.createByErrorMessage("未知错误");
        }
    }

    private CartVO convertCartVO(List<Cart> cartList)
    {
        CartVO cartVO = new CartVO();
        List<CartItemVO> cartItemVOs = Lists.newArrayList();
        BigDecimal cartTotalPrice = new BigDecimal("0");
        int  cartTotalQuantity = 0;

        if(CollectionUtils.isNotEmpty(cartList))
        {
            for(Cart cart : cartList)
            {
                CartItemVO cartItemVO = convertCartItemVO(cart);
                cartTotalQuantity += cartItemVO.getQuantity();
                cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartItemVO.getProductTotalPrice());
                cartItemVOs.add(cartItemVO);
            }
        }
        cartVO.setCartTotalPrice(cartTotalPrice.doubleValue());
        cartVO.setCartItemVOList(cartItemVOs);
        cartVO.setCartTotalQuantity(cartTotalQuantity);
        return cartVO;
    }

    private CartItemVO convertCartItemVO(Cart cart) {
        if (cart == null)  return null;
            CartItemVO cartItemVO = new CartItemVO();
            cartItemVO.setId(cart.getId());
            cartItemVO.setUserId(cart.getUserId());
            cartItemVO.setProductId(cart.getProductId());
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if (product != null) {
                cartItemVO.setProductMainImage(product.getMainImage());
                cartItemVO.setProductName(product.getName());
                cartItemVO.setProductSubtitle(product.getSubtitle());
                cartItemVO.setProductStatus(Const.ProductStatusEnum.codeof(product.getStatus()).getValue());
                cartItemVO.setProductPrice(product.getPrice().doubleValue());
                cartItemVO.setProductStock(product.getStock());
                cartItemVO.setCreateTime(DateTimeUtil.dateToStr(cart.getCreateTime()));
                //判断库存，如果产品库存足够，则表示限制成功，购买数量不变，若库存不够，则表示限制失败，数据数量为产品库存。
                int buyLimitCount = 0;
                if (product.getStock() >= cart.getQuantity()) {
                    buyLimitCount = cart.getQuantity();
                    cartItemVO.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                } else {
                    buyLimitCount = product.getStock();
                    cartItemVO.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                    //购物车中更新用效库存
                    Cart temp = new Cart();
                    temp.setId(cart.getId());
                    temp.setQuantity(buyLimitCount);
                    cartMapper.updateByPrimaryKeySelective(temp);
                }
                cartItemVO.setQuantity(buyLimitCount);
                //计算购物车单个条目总价
                cartItemVO.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartItemVO.getQuantity().doubleValue()).doubleValue());
                return cartItemVO;
            }
            return null;
        }
}
