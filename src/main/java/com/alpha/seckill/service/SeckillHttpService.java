package com.alpha.seckill.service;

import com.alibaba.fastjson.JSONObject;
import com.alpha.seckill.conf.Config;
import com.alpha.seckill.model.Member;
import com.alpha.seckill.model.RequestDO;
import com.alpha.seckill.model.VaccineList;
import com.alpha.seckill.model.exception.BusinessException;
import com.alpha.seckill.model.exception.LoginTimeoutException;
import com.alpha.seckill.model.exception.UnknownException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yuqiu.yhz
 * @date 2021/11/25
 * @description
 */
public class SeckillHttpService {

    private final Logger logger = LogManager.getLogger(SeckillHttpService.class);

    private static String baseUrl = "https://miaomiao.scmttec.com";

    /**
     * 调用接口时返回的set-cookie
     */
    protected static Map<String, String> cookieMap = new ConcurrentHashMap<>();

    private String cookie = "";

    private String token = "";

    public SeckillHttpService(String token, String cookie) {
        if (token == null) {
            logger.error("token is null");
        } else {
            token = token.trim();
        }
        if (cookie != null) {
            cookie = cookie.trim();
            String[] s = cookie.replaceAll(" ", "").split(";");
            for (String s1 : s) {
                cookieMap.put(s1.split("=")[0], s1);
            }
        }
        this.token = token;
        this.cookie = cookie;
    }

    /***
     * 获取秒杀资格
     * @param seckillId 疫苗ID
     * @param vaccineIndex 固定1
     * @param linkmanId 接种人ID
     * @param idCard 接种人身份证号码
     * @return 返回订单ID
     * @throws IOException
     * @throws BusinessException
     */
    public String secKill(String seckillId, String vaccineIndex, String linkmanId, String idCard, String st) throws Exception {
        String path = baseUrl+"/seckill/seckill/subscribe.do";
        Map<String, String> params = new HashMap<>();
        params.put("seckillId", seckillId);
        params.put("vaccineIndex", vaccineIndex);
        params.put("linkmanId", linkmanId);
        params.put("idCardNo", idCard);
        //加密参数
        Header header = new BasicHeader("ecc-hs", eccHs(seckillId, st));
        List<Header> commonHeader = getCommonHeader();
        commonHeader.add(header);
        return get(path, params, commonHeader);
    }

    /**
     * 获取疫苗列表
     * @return
     * @throws BusinessException
     */
    public List<VaccineList> getVaccineList(String regionCode) throws Exception {
        hasAvailableConfig();
        String path = baseUrl+"/seckill/seckill/list.do";
        Map<String, String> param = new HashMap<>();
        //九价疫苗的code
        param.put("offset", "0");
        param.put("limit", "100");
        //这个应该是成都的行政区划前四位
        param.put("regionCode", regionCode);
        String json = get(path, param, getCommonHeader());
        return JSONObject.parseArray(json).toJavaList(VaccineList.class);
    }


    /**
     * 获取接种人信息
     * @return
     */
    public List<Member> getMembers() throws Exception {
        String path = baseUrl + "/seckill/linkman/findByUserId.do";
        String json = get(path, null, getCommonHeader());
        return  JSONObject.parseArray(json, Member.class);
    }
    /***
     * 获取加密参数st
     * @param vaccineId 疫苗ID
     */
    public String getSt(String vaccineId) throws Exception {
        String path = baseUrl+"/seckill/seckill/checkstock2.do";
        Map<String, String> params = new HashMap<>();
        params.put("id", vaccineId);
        String json =  get(path, params, getCommonHeader());
        JSONObject jsonObject = JSONObject.parseObject(json);
        return jsonObject.getString("st");
    }

    /***
     * log接口，不知道有何作用，但返回值会设置一个名为tgw_l7_route的cookie
     * @param vaccineId 疫苗ID
     */
    public void log(String vaccineId) throws Exception {
        String path = baseUrl+"/seckill/seckill/log.do";
        Map<String, String> params = new HashMap<>();
        params.put("id", vaccineId);
        get(path, params, getCommonHeader());
    }

    public String getVaccineDay(String seckillId, String orderId) throws Exception {
        String path = baseUrl+"/seckill/seckill/subscribeDays.do";
        Map<String, String> params = new HashMap<>();
        params.put("id", seckillId);
        params.put("sid", orderId);
        return get(path, params, getCommonHeader());
    }

    public String getVaccineDayTime(String seckillId, String orderId, String day) throws Exception {
        String path = baseUrl+"/seckill/seckill/dayTimes.do";
        Map<String, String> params = new HashMap<>();
        params.put("id", seckillId);
        params.put("sid", orderId);
        params.put("day", day);
        return get(path, params, getCommonHeader());
    }

    public String submitDayTime(String seckillId, String orderId, String day, String wid) throws Exception {
        String path = baseUrl+"/seckill/seckill/submitDateTime.do";
        Map<String, String> params = new HashMap<>();
        params.put("id", seckillId);
        params.put("sid", orderId);
        params.put("day", day);
        params.put("wid", wid);
        return get(path, params, getCommonHeader());
    }

    public String getCurTime() throws Exception {
        String path = baseUrl + "/seckill/seckill/now2.do";
        Map<String, String> params = new HashMap<>();
        return get(path, params, getCommonHeader());
    }


    public void setCookie(String cookie){
        String[] s = cookie.replaceAll(" ", "").split(";");
        for (String s1 : s) {
            cookieMap.put(s1.split("=")[0], s1);
        }
    }

    private void hasAvailableConfig() throws LoginTimeoutException {
        if(cookieMap.isEmpty()){
            throw new LoginTimeoutException("请先配置cookie");
        }
    }


    public String get(String path, Map<String, String> params, List<Header> extHeader) throws Exception {
        RequestDO requestDO = new RequestDO();
        List<Header> headers = new ArrayList<>();
        RequestConfig requestConfig = null;
        List<Cookie> cookies = null;
        String responseString = null;
        String url = null;
        Exception exception = null;
        CookieStore cookieStore = new BasicCookieStore();
        try {
            if(params != null && params.size() !=0){
                StringBuilder paramStr = new StringBuilder("?");
                params.forEach((key,value)->{
                    paramStr.append(key).append("=").append(value).append("&");
                });
                String t = paramStr.toString();
                if(t.endsWith("&")){
                    t = t.substring(0, t.length()-1);
                }
                path+=t;
            }
            url = path;
            HttpGet get = new HttpGet(path);
            if(extHeader != null && extHeader.size() > 0){
                for (int i = 0; i < extHeader.size(); i++) {
                    headers.add(extHeader.get(i));
                }

            }
            requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(2500)
                    .setSocketTimeout(2500)
                    .setConnectTimeout(2500)
                    .build();
            get.setConfig(requestConfig);
            get.setHeaders(headers.toArray(new Header[0]));

            CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
            CloseableHttpResponse response = httpClient.execute(get);
            HttpEntity httpEntity = response.getEntity();
            responseString =  EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
        } catch (Exception e) {
            exception = e;
            logger.error("", e);
        } finally {
            cookies = cookieStore.getCookies();
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie.getValue());
            }
            if (path.startsWith(baseUrl)) {
                requestDO.setRequestConfig(requestConfig);
                requestDO.setCookies(cookies);
                requestDO.setResponse(responseString);
                requestDO.setHeaders(headers);
                requestDO.setException(exception);
                requestDO.setPath(url);
                Config.requestDOList.add(requestDO);
                if (exception != null) {
                    throw exception;
                }
            }
        }
        JSONObject jsonObject = JSONObject.parseObject(responseString);
        if("0000".equals(jsonObject.get("code"))){
            return jsonObject.getString("data");
        }
        if ("1001".equals(jsonObject.get("code"))) {
            throw new LoginTimeoutException(jsonObject.getString("msg"));
        }
        if ("9999".equals(jsonObject.get("code"))) {
            throw new BusinessException(jsonObject.getString("msg"));
        }
        throw new UnknownException(jsonObject.getString("code"), jsonObject.getString("msg"));
    }

    private List<Header> getCommonHeader(){
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; SM-N960F Build/JLS36C; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36 MMWEBID/1042 MicroMessenger/7.0.15.1680(0x27000F34) Process/appbrand0 WeChat/arm32 NetType/WIFI Language/zh_CN ABI/arm32"));
        headers.add(new BasicHeader("Referer", "https://servicewechat.com/wxff8cad2e9bf18719/2/page-frame.html"));
        headers.add(new BasicHeader("tk", Config.tk));
        headers.add(new BasicHeader("Accept","application/json, text/plain, */*"));
        headers.add(new BasicHeader("Host","miaomiao.scmttec.com"));
        String cookie = String.join("; ", new ArrayList<>(cookieMap.values()));
        headers.add(new BasicHeader("Cookie", cookie));
        return headers;
    }





    private String eccHs(String seckillId, String st){
        String salt = "ux$ad70*b";
        final Integer memberId = Config.memberId;
        String md5 = DigestUtils.md5Hex(seckillId + memberId + st);
        return DigestUtils.md5Hex(md5 + salt);
    }

}
