package com.mmall.common;

/**
 * Created by aa on 2017/6/20.
 */
public enum  ResponseCode {

    SUCCESS(1,"SUCCESS"),
    EOORO(0,"EOORO"),
    NEED_LOGIN(10,"NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");
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
