package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * Created by aa on 2017/6/19.
 */                 //若属性值为null，则该属性不会转化为Json
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable{
    private int status; //响应的状态
    private String msg; //响应的消息
    private T data;     //响应的数据

    private ServerResponse(int status)
    {
        this.status = status;
    }
    private ServerResponse(int status,T data)
    {
        this.status = status;
        this.data = data;
    }
    private ServerResponse(int status,String msg,T data)
    {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }
    private ServerResponse(int status,String msg)
    {
        this.status = status;
        this.msg = msg;
    }
    @JsonIgnore         // 意思是该属性不会转化为json
    public boolean isSuccess()
    {
        return this.status == ResponseCode.SUCCESS.getCode();
    }
    public int getStatus()
    {
        return status;
    }

    public String getMsg()
    {
        return msg;
    }
    public T getData()
    {
        return data;
    }

    public  static  <T> ServerResponse<T> createBySuccess()
    {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }
    public  static  <T> ServerResponse<T> createBySuccessMessage(String msg)
    {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }
    public  static  <T> ServerResponse<T> createBySuccess(T data)
    {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }
    public  static  <T> ServerResponse<T> createBySuccess(String msg,T data)
    {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }
    public static <T> ServerResponse<T> createByError()
    {
        return new ServerResponse<T>(ResponseCode.EOORO.getCode(),ResponseCode.EOORO.getDesc());
    }
    public static <T> ServerResponse<T> createByErrorMessage(String msg)
    {
        return new ServerResponse<T>(ResponseCode.EOORO.getCode(),msg);
    }
    public static <T> ServerResponse<T> createByErrorCodeMessage(int code,String msg)
    {
        return new ServerResponse<T>(code,msg);
    }

}
