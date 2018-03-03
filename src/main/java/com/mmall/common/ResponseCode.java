package com.mmall.common;

/**
 * Created by aa on 2017/6/20.
 */
public enum  ResponseCode {

    SUCCESS(1,"SUCCESS"),   //成功
    EOORO(0,"EOORO"),       //错误
    NEED_LOGIN(10,"NEED_LOGIN"),    //需要登录
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT"); //非法参数
    private final int code;
    private final String desc;
    ResponseCode(int code,String desc)
    {
        this.code = code;
        this.desc = desc;
    }
    public int getCode()
    {
        return code;
    }
    public String getDesc()
    {
        return desc;
    }
}
