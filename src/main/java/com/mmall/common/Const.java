package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by aa on 2017/6/20.
 */
public class Const {
    //当前用户
    public static final String CURRENT_USER = "currentUser";
    public static final Integer cartCapacityUpperLimit = 10;
    public static final Integer shippingCapacityUpperLimit = 6;
    public static final Integer orderPayTimeoutLimit = 10*60*1000; //分钟
    public static final String INITIAL_PASSWORD = "1234";

    //购物条目 在购物车中的状态
    public interface Cart{
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS="LIMIT_NUM_SUCCESS";
    }
    public interface MmallOrderbySet{
        Set<String > orderbySet = Sets.newHashSet("price:asc","price:desc","create_time&desc","create_time:asc");
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
        SUCCESS(4,"已收货"),
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
        SOLD_OUT(0,"下架"),
        ON_SALE(1,"售卖中");
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

        public static ProductStatusEnum codeof(int code)
        {
            for(ProductStatusEnum productStatusEnum : values())
            {
                if(productStatusEnum.getCode() == code)
                {
                    return productStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }

    public enum UserStatusEnum{
        NORMAL_USER(0,"正常"),
        NONACTIVATED_USER(1,"未激活"),
        FREEZE_USER(3,"已冻结"),
        INATIVE_USER(4,"不活跃");
        private String value;
        private int code;
        UserStatusEnum(int code,String value)
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

        public static UserStatusEnum codeof(int code)
        {
            for(UserStatusEnum userStatusEnum : values())
            {
                if(userStatusEnum.getCode() == code)
                {
                    return userStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }

    public enum CategoryStatusEnum{
        USEING(1,"使用中"),
        DEPRECATED(0,"已弃用");
        private String value;
        private int code;
        CategoryStatusEnum(int code,String value)
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

        public static CategoryStatusEnum codeof(int code)
        {
            for(CategoryStatusEnum categoryStatusEnum : values())
            {
                if(categoryStatusEnum.getCode() == code)
                {
                    return categoryStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }


    public enum ErrorEnum{
        DATA_VERIFY_ERROR(100,"数据校验错误"),
        DATABASE_ERROR(101,"数据库操作错误"),
        PARAM_ERROR(102,"参数错误"),
        UNKNOWN_ERROR(103,"未知错误"),
        NOT_LOGIN_ERROR(104,"未登录"),
        PERMISSION_DENIED(105,"拒绝访问"),
        TARGET_EXISTS_ERROR(106,"拒绝访问"),
        TARGET_NOT_EXISTS_ERROR(107,"拒绝访问"),
        ;
        private String value;
        private int code;
        ErrorEnum(int code,String value)
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

        public static ErrorEnum codeof(int code)
        {
            for(ErrorEnum errorEnum : values())
            {
                if(errorEnum.getCode() == code)
                {
                    return errorEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }
}
