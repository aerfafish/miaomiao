package com.alpha.seckill.model.exception;

/**
 * Created on 2020-07-22.
 *
 * @author wangxiaodong
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String  message) {
        super(message);
    }
}
