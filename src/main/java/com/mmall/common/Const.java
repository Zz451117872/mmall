package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by aa on 2017/6/20.
 */
public class Const {
    //当前用户
    public static final String CURRENT_USER = "currentUser";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    //购物条目 在购物车中的状态
    public interface Cart{
        int CHECKED =1; //选中
        int UNCHECKED=0;//未选中
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS="LIMIT_NUM_SUCCESS";
    }
    public interface ProductListOrderBy{
        Set<String > PRICE_ASC_DESC = Sets.newHashSet("price_asc","price_desc");
    }

    //角色
    public interface Role{
        int ROLE_CUSTOMER = 0; //用户
        int ROLE_ADMIN = 1;     //管理者
    }

    //支付类型
    public enum PaymentTypeEnum{
        ONLINE_PAY(1,"在线支付");
        private String value;
        private int code;
        PaymentTypeEnum(int code,String value)
        {
            this.code = code;
            this.value = value;
        }
        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }

        public static PaymentTypeEnum codeof(int code)
        {
            for(PaymentTypeEnum paymentTypeEnum : values())
            {
                if(paymentTypeEnum.getCode() == code)
                {
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }
    //支付平台
    public enum PayPlatformEnum{
        ALIPAY(1,"支付宝");
        private String value;
        private int code;
        PayPlatformEnum(int code,String value)
        {
            this.code = code;
            this.value = value;
        }
        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }
    }

    //订单状态
    public enum OrderStatusEnum{
        ON_PAY(0,"未付款"),
        CANCELED(1,"已取消"),
        PAID(2,"已付款"),
        SHIPPED(3,"已发货"),
        SUCCESS(4,"成功"),
        CLOSED(5,"已关闭");

        private String value;
        private int code;
        OrderStatusEnum(int code,String value)
        {
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }
        public static OrderStatusEnum codeof(int code)
        {
            for(OrderStatusEnum orderStatusEnum : values())
            {
                if(orderStatusEnum.getCode() == code)
                {
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }

    //支付宝回调
    public interface AlipayCallable{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";
        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }
    //产品状态
    public enum ProductStatusEnum{
        ON_SALE(1,"在线");
        private String value;
        private int code;
        ProductStatusEnum(int code,String value)
        {
            this.code = code;
            this.value = value;
        }
        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }
    }
}
