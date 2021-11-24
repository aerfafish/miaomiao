package com.github.lyrric.ui;

import com.github.lyrric.conf.Config;
import com.github.lyrric.model.BusinessException;
import com.github.lyrric.model.Member;
import com.github.lyrric.model.VaccineList;
import com.github.lyrric.service.HttpService;
import com.github.lyrric.service.SecKillService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created on 2020-08-14.
 * 控制台模式
 * @author wangxiaodong
 */
public class ConsoleMode {

    private final Logger log = LogManager.getLogger(ConsoleMode.class);

    private ExecutorService service = Executors.newFixedThreadPool(100);

    private HttpService httpService = new HttpService();

    private SecKillService secKillService = new SecKillService();

    public void start() throws Exception {
        while (true) {
            Scanner sc = new Scanner(System.in);
            log.info("请输入tk：");
            Config.tk = sc.nextLine().trim();
            log.info("请输入Cookie：");
            calCookie(sc.nextLine().trim());
            log.info("获取接种人员......");
            List<Member> members = new ArrayList<>();
            while (true) {
                try {
                    members = httpService.getMembers();
                } catch (BusinessException e) {
                    if (e.getErrMsg().contains("用户登录超时,请重新登入")) {
                        log.error("用户登录超时,请重新登入");
                        break;
                    }
                }

                for (int i = 0; i < members.size(); i++) {
                    log.info("{}-{}-{}", i, members.get(i).getName(), members.get(i).getIdCardNo());
                }
                log.info("请输入接种人员序号：");
                int no = Integer.parseInt(sc.nextLine());
                Config.memberId = members.get(no).getId();
                Config.idCard = members.get(no).getIdCardNo();

                log.info("获取疫苗列表......");

                List<VaccineList> vaccineList = null;
                for (int i = 3307; i <= 9999; i++) {
                    try {
                        vaccineList = httpService.getVaccineList(String.valueOf(i));
                    } catch (SocketTimeoutException e) {
                        log.info("{} 获取超时 跳过", i);
                        continue;
                    }

                    if (vaccineList.size() == 0) {
                        log.info("当前城市{}疫苗列表为空, 请稍后再试", i);
                        Thread.sleep(1000);
                    } else {
                        log.info(vaccineList);
                    }
                }

                for (int i = 0; i < vaccineList.size(); i++) {
                    VaccineList item = vaccineList.get(i);
                    log.info("{}-{}-{}-{}-{}", i, item.getName(), item.getVaccineName(), item.getAddress(), item.getStartTime());
                }
                log.info("请输入疫苗序号：");
                no = Integer.parseInt(sc.nextLine());
                int code = vaccineList.get(no).getId();
                String startTime = vaccineList.get(no).getStartTime();
                long startDate = convertDateToInt(startTime);
                long now = System.currentTimeMillis();
                if(now + 60000 < startDate){
                    log.info("还未到获取st时间，等待中......");
                    Thread.sleep(55000);
                    continue;
                }
                log.info("获取log cookie");
                httpService.log(String.valueOf(code));
                while (now + 2000 < startDate){
                    log.info("准备抢疫苗，还有" +  (startDate - now ) / 1000+ "秒");
                    Thread.sleep(1000);
                }
                secKillService.startSecKill(code, startDate, null);
            }
        }

    }
    private void calCookie(String cookie){
        String[] s = cookie.replaceAll(" ", "").split(";");
        for (String s1 : s) {
            Config.cookie.put(s1.split("=")[0], s1);
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
}
