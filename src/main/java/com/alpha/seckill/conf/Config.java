package com.alpha.seckill.conf;

import com.alpha.seckill.model.RequestDO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2020-07-23.
 *
 * @author wangxiaodong
 */
public class Config {

    private static final Logger log = LogManager.getLogger(Config.class);

    static {
        Map<String, String> config = new HashMap();
        try {
            Yaml yaml = new Yaml();
            //通过class.getResource来获取yaml的路径
            InputStream stream = Config.class.getClassLoader().getResourceAsStream("application.yml");
            //读取yaml中的数据并且以map集合的形式存储
            config = yaml.load(stream);
            if (config != null) {
                tk = config.get("token");
                cookie = config.get("cookie");
                memberName = config.get("name");
                regionCode = String.valueOf(config.get("regionCode"));
                log.info(config.get("name"));
            }
        } catch (Exception e) {
            log.error("", e);
            System.exit(1);
        }
    }

    public static String tk;

    public static String reqHeader = "GET https://miaomiao.scmttec.com/seckill/seckill/list.do?offset=0&limit=10&regionCode=5101 HTTP/1.1\n" +
            "Host: miaomiao.scmttec.com\n" +
            "Connection: keep-alive\n" +
            "Accept: application/json, text/plain, */*\n" +
            "Cookie: _xxhm_=%7B%22heade" +
            "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36 MicroMessenger/7.0.9.501 NetType/WIFI MiniProgramEnv/Windows WindowsWechat\n" +
            "X-Requested-With: XMLHttpRequest\n" +
            "content-type: application/json\n" +
            "tk: wxapptoken:10:e0963da6b3e544f48fef5f6f27c32b5f\n" +
            "Referer: https://servicewechat.com/wxff8cad2e9bf18719/7/page-frame.html\n" +
            "Accept-Encoding: gzip, deflate, br\n" +
            "\n";

    /**
     * 接种成员ID
     */
    public static Integer memberId;

    /**
     * 接种人的token
     */
    public static String cookie;

    /**
     * 接种成员身份证号码
     */
    public static String idCard;
    /**
     * 接种成员姓名
     */
    public static String memberName;
    /**
     * 选择的地区代码
     */
    public static String regionCode;

    /**
     * 抢购是否成功
     * false表示疫苗已抢光
     */
    public static Boolean success = false;

    /**
     * 加密参数st
     */
    public static String st;

    /**
     * 获取的order_Id，用于获取疫苗日期
     */
    public static String orderId = null;

    /**
     * 复盘整个抢疫苗请求过程
     */
    public static List<RequestDO> requestDOList = new ArrayList<>(16);

    /**
     * timediff网络时间和系统时间的时间差
     */
    public static Long timeDiff = 0L;

}
