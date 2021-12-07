package com.alpha.seckill.model.securl;

/**
 * @author yuqiu.yhz
 * @date 2021/11/25
 * @description
 */
public class SecUrl {

    private static String baseUrl = "https://miaomiao.scmttec.com";

    public static String MEMBER_URL = baseUrl + "/seckill/linkman/findByUserId.do";

    public static String ST_URL = baseUrl + "/seckill/seckill/checkstock2.do";

    public static String SUBSCRIBE_URL = baseUrl + "/seckill/seckill/subscribe.do";

    public static String VACCINE_LIST_URL = baseUrl + "/seckill/seckill/list.do";

    public static String LOG_URL = baseUrl + "/seckill/seckill/log.do";

    public static String ORDER_VACCINE_DAY_URL = baseUrl + "/seckill/seckill/subscribeDays.do";

    public static String VACCINE_DAY_TIME_URL = baseUrl + "/seckill/seckill/dayTimes.do";

    public static String SUBMIT_DAY_TIME_URL = baseUrl + "/seckill/seckill/submitDateTime.do";

    public static String ONLINE_TIME_URL = "http://api.m.taobao.com/rest/api3.do";
}
