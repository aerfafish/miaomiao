package com.alpha.seckill.model.exception;

/**
 * @author yuqiu.yhz
 * @date 2021/12/7
 * @description
 */
public class UnknownException extends Exception {

    private String errMsg;

    private String code;

    public UnknownException(String code, String message) {
        super(message);
        this.errMsg = message;
        this.code = code;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public String getCode() {
        return code;
    }
}
