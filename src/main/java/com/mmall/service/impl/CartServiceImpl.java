package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVO;
import com.mmall.vo.CartVO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by aa on 2017/6/23.
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    /*
    新增购物车
     */
    @Override
    public ServerResponse<CartVO> add(Integer userId,Integer count,Integer productId)
    {
        if(count == null || productId==null)
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Cart cart = cartMapper.selectCartByUseridAndProductid(userId,productId);
        if(cart == null)
        {//这个产品不在购物车里，需要新增
            Cart cartItem = new Cart();
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setQuantity(count);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        }else{//这个产品在购物车里，不需要新增，只需改变数量
            count = cart.getQuantity()+count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        CartVO cartVO = this.getCartVO(userId);
        return ServerResponse.createBySuccess(cartVO);
    }

    /*
    修改购物车中产品的购买数量，返回数量
     */
    @Override
    public ServerResponse<Integer> updateQuantiry(Integer userId,Integer count,Integer cartId)
    {
        if(count == null || cartId == null)
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Cart cart = cartMapper.selectByPrimaryKey(cartId);
        Integer result = cart.getQuantity() - count > 0 ?  cart.getQuantity()-1 :  cart.getQuantity()+1;
        if(cart != null)
        {
            cart.setQuantity(count);
        }else{
            return ServerResponse.createByErrorMessage("没有该项");
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        return ServerResponse.createBySuccess(result);
    }



    /*
   修改购物车中产品的购买数量，返回CartVO
    */
    @Override
    public ServerResponse<CartVO> update(Integer userId,Integer count,Integer productId)
    {
        if(count == null || productId==null)
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        Cart cart = cartMapper.selectCartByUseridAndProductid(userId,productId);
        if(cart != null)
        {
            cart.setQuantity( count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        CartVO cartVO = this.getCartVO(userId);
        return ServerResponse.createBySuccess(cartVO);
    }

    @Override
    public ServerResponse<CartVO> delete(Integer userId,String productIds)
    {
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productIdList))
        {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteCartByUseridAndProductids(userId,productIdList);
        CartVO cartVO = this.getCartVO(userId);
        return ServerResponse.createBySuccess(cartVO);
    }


    @Override
    public ServerResponse<CartVO> deleteByCartids(Integer userId,String cartIds)
    {
        if(userId == null || cartIds == null)
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        List<String> cartIdList = Splitter.on(",").splitToList(cartIds);
        if(CollectionUtils.isEmpty(cartIdList))
        {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        for(int i=0; i<cartIdList.size(); i++)
        {
            Integer cartId = null;
            try{
                cartId = Integer.parseInt(cartIdList.get(i));
            }catch (Exception e){
                return ServerResponse.createByErrorMessage("未知错误");
            }
            if(cartId != null)
            {
                cartMapper.deleteByPrimaryKey(cartId);
            }
        }
        CartVO cartVO = this.getCartVO(userId);
        return ServerResponse.createBySuccess(cartVO);
    }
    @Override
    public ServerResponse<CartVO> list(Integer userId)
    {
        CartVO cartVO = this.getCartVO(userId);
        return ServerResponse.createBySuccess(cartVO);
    }

    @Override
    public ServerResponse<CartVO> selectOrUnselect(Integer userId,Integer checked,Integer productId)
    {
        cartMapper.checkedOrUncheckedProduct(userId,checked,productId);
        return list(userId);
    }

    @Override
    public ServerResponse<Integer> selectCartProductCount(Integer userId)
    {
        if(userId == null)
        {
            return  ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }









    private CartVO getCartVO(Integer userId)
    {
        CartVO cartVO = new CartVO();
        List<Cart> cartList = cartMapper.selectCartByUserid(userId);
        //CartProductVO 就是cart 和 product的合成类
        List<CartProductVO> cartProductVOs = Lists.newArrayList();
        BigDecimal cartTotalPrice = new BigDecimal("0");

        if(CollectionUtils.isNotEmpty(cartList))
        {
            for(Cart cart : cartList)
            {
                CartProductVO cartProductVO = new CartProductVO();
                cartProductVO.setId(cart.getId());
                cartProductVO.setUserId(cart.getUserId());
                cartProductVO.setProductId(cart.getProductId());
                cartProductVO.setProductChecked(cart.getChecked());

                Product product =productMapper.selectByPrimaryKey(cart.getProductId());
                if(product != null)
                {
                    cartProductVO.setProductMainImage(product.getMainImage());
                    cartProductVO.setProductName(product.getName());
                    cartProductVO.setProductSubtitle(product.getSubtitle());
                    cartProductVO.setProductStatus(product.getStatus());
                    cartProductVO.setProductPrice(product.getPrice());
                    cartProductVO.setProductStock(product.getStock());
                    //判断库存
                    int buyLimitCount = 0;
                    if(product.getStock() >= cart.getQuantity())
                    {
                        buyLimitCount = cart.getQuantity();
                        cartProductVO.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else{
                        buyLimitCount = product.getStock();
                        cartProductVO.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新用效库存
                        Cart temp = new Cart();
                        temp.setId(cart.getId());
                        temp.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(temp);
                    }
                    cartProductVO.setQuantity(buyLimitCount);
                    //计算购物车单个条目总价
                    cartProductVO.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVO.getQuantity().doubleValue()));
                    cartProductVO.setProductChecked(cart.getChecked());

                    if(cart.getChecked() == Const.Cart.CHECKED)
                    {//如果是已勾选的，则算入整个购物 车总价中
                        cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVO.getProductTotalPrice().doubleValue());
                    }
                }
                cartProductVOs.add(cartProductVO);
            }
        }
        cartVO.setCartTotalPrice(cartTotalPrice);
        cartVO.setCartProductVOList(cartProductVOs);
        cartVO.setAllChecked(this.getAllCheckStatus(userId));
        cartVO.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVO;
    }


    /*
    检查所有购物车条目 是否是选中状态
     */
    private boolean getAllCheckStatus(Integer userId)
    {
        if(userId == null)
        {
            return false;
        }
        return cartMapper.selectCartProductCheckStatusByUserid(userId) == 0;
    }
}
