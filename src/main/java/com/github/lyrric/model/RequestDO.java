package com.github.lyrric.model;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.cookie.Cookie;

import java.util.List;

/**
 * @author yuqiu.yhz
 * @date 2021/11/23
 * @description
 */
public class RequestDO {

    String path;

    RequestConfig requestConfig;

    List<Header> headers;

    List<Cookie> cookies;

    String response;

    Exception exception;

    public RequestConfig getRequestConfig() {
        return requestConfig;
    }

    public void setRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Exception getE() {
        return exception;
    }

    public void setE(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
