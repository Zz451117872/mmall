package com.mmall.exception;

import com.mmall.common.Const;

/**
 * Created by aa on 2017/12/20.
 */
public class AppException extends RuntimeException{

    int code;
    String msg;

    public AppException(Const.ErrorEnum errorEnum)
    {
        this.code = errorEnum.getCode();
        this.msg = errorEnum.getValue();
    }

    public AppException()
    {
        this(Const.ErrorEnum.UNKNOWN_ERROR);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
