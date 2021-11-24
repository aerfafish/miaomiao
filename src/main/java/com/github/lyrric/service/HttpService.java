package com.github.lyrric.service;

import com.alibaba.fastjson.JSONObject;
import com.github.lyrric.conf.Config;
import com.github.lyrric.model.BusinessException;
import com.github.lyrric.model.Member;
import com.github.lyrric.model.RequestDO;
import com.github.lyrric.model.VaccineList;
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
import org.apache.http.message.BufferedHeader;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2020-07-22.
 *
 * @author wangxiaodong
 */
public class HttpService {

    private String baseUrl = "https://miaomiao.scmttec.com";

    private final Logger logger = LogManager.getLogger(HttpService.class);


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
        return get(path, params, header);
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
        String json = get(path, param, null);
        return JSONObject.parseArray(json).toJavaList(VaccineList.class);
    }


    /**
     * 获取接种人信息
     * @return
     */
    public List<Member> getMembers() throws Exception {
        String path = baseUrl + "/seckill/linkman/findByUserId.do";
        String json = get(path, null, null);
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
        String json =  get(path, params, null);
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
        get(path, params, null);
    }

    public String getVaccineDay(String seckillId, String orderId) throws Exception {
        String path = baseUrl+"/seckill/subscribeDays.do";
        Map<String, String> params = new HashMap<>();
        params.put("id", seckillId);
        params.put("sid", orderId);
        return get(path, params, null);
    }

    public String getVaccineDayTime(String seckillId, String orderId, String day) throws Exception {
        String path = baseUrl+"/seckill/dayTimes.do";
        Map<String, String> params = new HashMap<>();
        params.put("id", seckillId);
        params.put("sid", orderId);
        params.put("day", day);
        return get(path, params, null);
    }

    public String submitDayTime(String seckillId, String orderId, String day, String wid) throws Exception {
        String path = baseUrl+"/seckill/submitDateTime.do";
        Map<String, String> params = new HashMap<>();
        params.put("id", seckillId);
        params.put("sid", orderId);
        params.put("day", day);
        params.put("wid", wid);
        return get(path, params, null);
    }

    private void hasAvailableConfig() throws BusinessException {
        if(Config.cookie.isEmpty()){
            throw new BusinessException("0", "请先配置cookie");
        }
    }

    private String get(String path, Map<String, String> params, Header extHeader) throws Exception {
        RequestDO requestDO = new RequestDO();
        List<Header> headers = null;
        RequestConfig requestConfig = null;
        List<Cookie> cookies = null;
        String responseString = null;
        String url = null;
        Exception exception = null;
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
            headers = getCommonHeader();
            if(extHeader != null){
                headers.add(extHeader);
            }
            requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(2500)
                    .setSocketTimeout(2500)
                    .setConnectTimeout(2500)
                    .build();
            get.setConfig(requestConfig);
            get.setHeaders(headers.toArray(new Header[0]));
            CookieStore cookieStore = new BasicCookieStore();
            CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
            CloseableHttpResponse response = httpClient.execute(get);
            cookies = cookieStore.getCookies();
            for (Cookie cookie : cookies) {
                Config.cookie.put(cookie.getName(), cookie.getValue());
            }
            HttpEntity httpEntity = response.getEntity();
            responseString =  EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
            JSONObject jsonObject = JSONObject.parseObject(responseString);
        } catch (Exception e) {
            exception = e;
            logger.error("", e);
        } finally {
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
        JSONObject jsonObject = JSONObject.parseObject(responseString);
        if("0000".equals(jsonObject.get("code"))){
            return jsonObject.getString("data");
        }
        logger.error(responseString);
        throw new BusinessException(jsonObject.getString("code"), jsonObject.getString("msg"));
    }

//    public getSta

    private void dealHeader(CloseableHttpResponse response){
        Header[] responseHeaders = response.getHeaders("Set-Cookie");
        if (responseHeaders.length > 0) {
            for (Header responseHeader : responseHeaders) {
                String cookie = ((BufferedHeader) responseHeader).getBuffer().toString().split(";")[0].split(":")[1].trim();
                String[] split = cookie.split("=");
                Config.cookie.put(split[0], cookie);
            }
        }
    }

    private List<Header> getCommonHeader(){
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Linux; Android 5.1.1; SM-N960F Build/JLS36C; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36 MMWEBID/1042 MicroMessenger/7.0.15.1680(0x27000F34) Process/appbrand0 WeChat/arm32 NetType/WIFI Language/zh_CN ABI/arm32"));
        headers.add(new BasicHeader("Referer", "https://servicewechat.com/wxff8cad2e9bf18719/2/page-frame.html"));
        headers.add(new BasicHeader("tk", Config.tk));
        headers.add(new BasicHeader("Accept","application/json, text/plain, */*"));
        headers.add(new BasicHeader("Host","miaomiao.scmttec.com"));
        String cookie = String.join("; ", new ArrayList<>(Config.cookie.values()));
        headers.add(new BasicHeader("Cookie", cookie));
        return headers;
    }

    private String eccHs(String seckillId, String st){
        String salt = "ux$ad70*b";
        final Integer memberId = Config.memberId;
        String md5 = DigestUtils.md5Hex(seckillId + memberId + st);
        return DigestUtils.md5Hex(md5 + salt);
    }

    public static void main(String[] args) {
        String salt = "ux$ad70*b";
        Integer memberId = 12372032;
        String md5 = DigestUtils.md5Hex("1085" + memberId + "1630902134216");
        System.out.println(DigestUtils.md5Hex(md5 + salt));
    }
}
