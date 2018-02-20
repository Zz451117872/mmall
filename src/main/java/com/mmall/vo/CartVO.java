package com.mmall.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by aa on 2017/6/23.
 */
public class CartVO implements Serializable{

    private List<CartItemVO> cartItemVOList;
    private int cartTotalQuantity;
    private Double cartTotalPrice;

    public List<CartItemVO> getCartItemVOList() {
        return cartItemVOList;
    }

    public void setCartItemVOList(List<CartItemVO> cartItemVOList) {
        this.cartItemVOList = cartItemVOList;
    }

    public Double getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(Double cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public int getCartTotalQuantity() {
        return cartTotalQuantity;
    }

    public void setCartTotalQuantity(int cartTotalQuantity) {
        this.cartTotalQuantity = cartTotalQuantity;
    }
}
