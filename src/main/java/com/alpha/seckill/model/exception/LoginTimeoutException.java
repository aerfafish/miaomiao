package com.alpha.seckill.model.exception;

/**
 * @author yuqiu.yhz
 * @date 2021/12/7
 * @description
 */
public class LoginTimeoutException extends Exception{

    public LoginTimeoutException(String message) {
        super(message);
    }
}
