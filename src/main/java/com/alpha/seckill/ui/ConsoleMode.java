package com.alpha.seckill.ui;

import com.alpha.seckill.conf.Config;
import com.alpha.seckill.service.SecKillService;
import com.alpha.seckill.service.SeckillHttpService;
import com.alpha.seckill.model.exception.LoginTimeoutException;
import com.alpha.seckill.model.Member;
import com.alpha.seckill.model.VaccineList;
import com.alpha.seckill.model.exception.UnknownException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2020-08-14.
 * 控制台模式
 * @author wangxiaodong
 */
public class ConsoleMode {

    private final Logger log = LogManager.getLogger(ConsoleMode.class);

    private ExecutorService service = Executors.newFixedThreadPool(100);

    private SeckillHttpService httpService;

    private SecKillService secKillService;

    private Integer memberNum = null;

    private Integer vaccineNum = null;

    public void start() throws Exception {
        while (true) {
            Scanner sc = new Scanner(System.in);
//            log.info("请输入tk：");
//            Config.tk = sc.nextLine().trim();
//            log.info("请输入Cookie：");
//            Config.cookie = sc.nextLine().trim();
            httpService = new SeckillHttpService(Config.tk, Config.cookie);
            secKillService = new SecKillService(httpService);
            long webTime = Long.parseLong(httpService.getCurTime());
            Config.timeDiff = webTime - System.currentTimeMillis();
            List<Member> members = new ArrayList<>();
            try {
                while (true) {
                    try {
                        log.info("获取接种人员......");
                        members = httpService.getMembers();
                    } catch (LoginTimeoutException e) {
                        log.error(e.getMessage(), e);
                        System.exit(1);
                    } catch (SocketTimeoutException e) {
                        log.info("接种人员获取超时 重试");
                        Thread.sleep(2000);
                        continue;
                    }

                    for (int i = 0; i < members.size(); i++) {
                        log.info("{}-{}-{}", i, members.get(i).getName(), members.get(i).getIdCardNo());
                    }

                    if (Config.memberName != null) {
                        for (int i = 0; i < members.size(); i++) {
                            if (Config.memberName.equals(members.get(i).getName())) {
                                memberNum = i;
                            }
                        }
                    }
                    if (memberNum == null) {
                        log.info("请输入接种人员序号：");
                        memberNum = Integer.parseInt(sc.nextLine());
                    } else {
                        log.info("自动选择:" +  members.get(memberNum).getName());
                    }

                    Config.memberId = members.get(memberNum).getId();
                    Config.idCard = members.get(memberNum).getIdCardNo();

                    log.info("获取疫苗列表......");

                    List<VaccineList> vaccineList = null;
                    // 可以选其他城市
                    String regionCode = Config.regionCode;
                    try {
                        vaccineList = getVaccineList(regionCode);
                    } catch (SocketTimeoutException e) {
                        log.warn("{} 城市疫苗获取超时重试", regionCode);
                        Thread.sleep(2000);
                        try {
                            vaccineList = getVaccineList(regionCode);
                        } catch (SocketTimeoutException e0) {
                            log.error("{} 城市疫苗获取超时 请检查网络", regionCode);
                            break;
                        }
                    } catch (LoginTimeoutException e) {
                        log.error("", e);
                        System.exit(1);
                    }

                    if (vaccineList == null) {
                        log.error("疫苗为空 请上报bug");
                    }



                    for (int i = 0; i < vaccineList.size(); i++) {
                        VaccineList item = vaccineList.get(i);
                        log.info("{}-{}-{}-{}-{}", i, item.getName(), item.getVaccineName(), item.getAddress(), item.getStartTime());
                    }
                    if (vaccineList.size() > 0) {
                        // 先优先选择最近的一个
                        vaccineNum = 0;
                    } else {
                        log.error("当前无疫苗，请等等再试");
                        System.exit(1);
                    }


                    if (vaccineNum == null) {
                        log.info("请输入疫苗序号：");
                        vaccineNum = Integer.parseInt(sc.nextLine());
                    } else {
                        VaccineList item = vaccineList.get(vaccineNum);
                        log.info("自动选择: {}-{}-{}-{}" ,item.getName(), item.getVaccineName(), item.getAddress(), item.getStartTime());
                    }

                    int code = vaccineList.get(vaccineNum).getId();


                    String startTime = vaccineList.get(vaccineNum).getStartTime();
                    long startDate = convertDateToInt(startTime);
                    long now = System.currentTimeMillis() + Config.timeDiff;
                    if(now + 60000 < startDate){
                        log.info("还未到获取st时间，等待中......");
                        Thread.sleep(55000);
                        continue;
                    }
                    log.info("获取log cookie");
                    httpService.log(String.valueOf(code));
                    now = System.currentTimeMillis() + Config.timeDiff;
                    while (now + 2000 < startDate){
                        log.info("准备抢疫苗，还有" +  (startDate - now ) / 1000+ "秒");
                        Thread.sleep(1000);
                        now = System.currentTimeMillis() + Config.timeDiff;
                    }
                    secKillService.startSecKill(code, startDate);
                }
            } catch (LoginTimeoutException e) {
                log.error(e.getMessage(), e);
                System.exit(1);
            } catch (UnknownException e) {
                log.error("未知错误 code {} 错误信息 {}", e.getCode(), e.getErrMsg(), e);
            }

        }

    }


    /**
     *  将时间字符串转换为时间戳
     * @param dateStr yyyy-mm-dd格式
     * @return
     */
    private long convertDateToInt(String dateStr) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = format.parse(dateStr);
        return date.getTime();
    }

    private List<VaccineList> getVaccineList(String regionCode) throws Exception {
        List<VaccineList> vaccineList = httpService.getVaccineList(regionCode);
        return vaccineList;
    }

    private void queryAllVaccine() throws Exception {
        List<VaccineList> vaccineList = null;
        for (int i = 1000; i <= 9999; i++) {
            try {
                vaccineList = getVaccineList(String.valueOf(i));
            } catch (SocketTimeoutException e) {
                log.info("{} 城市疫苗获取超时 跳过", i);
                Thread.sleep(2000);
                continue;
            }

            if (vaccineList.size() == 0) {
                log.info("当前城市{}疫苗列表为空, 请稍后再试", i);
                Thread.sleep(1000);
            } else {
                log.info(vaccineList);
            }
        }
    }
}
