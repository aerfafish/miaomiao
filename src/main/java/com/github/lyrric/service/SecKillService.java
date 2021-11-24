package com.github.lyrric.service;

import com.github.lyrric.conf.Config;
import com.github.lyrric.model.BusinessException;
import com.github.lyrric.model.VaccineList;
import com.github.lyrric.ui.MainFrame;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2020-07-22.
 *
 * @author wangxiaodong
 */
public class SecKillService {

    private HttpService httpService;

    private final Logger logger = LogManager.getLogger(SecKillService.class);

    ExecutorService service = Executors.newFixedThreadPool(4);

    public SecKillService() {
        httpService = new HttpService();
    }

    /**
     * 多线程秒杀开启
     */
    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public void startSecKill(Integer vaccineId, Long startDate, MainFrame mainFrame) throws ParseException, InterruptedException {
        while (true){
            //提前五秒钟获取服务器时间戳接口，计算加密用
            try {
                logger.info("Thread ID：main，请求获取加密参数st");
                Config.st = httpService.getSt(vaccineId.toString());
                logger.info("Thread ID：main，成功获取加密参数st：{}", Config.st);
                break;
            }catch (ConnectTimeoutException  | SocketTimeoutException socketTimeoutException ){
                logger.error("Thread ID：main,获取st失败: 超时");
            }catch (BusinessException e){
                logger.error("Thread ID：main,获取st失败: {}", e.getMessage());
            }catch (Exception e) {
                logger.error("Thread ID：main,获取st失败，大概率是约苗问题:{}", e.getMessage());
            }
        }
        Long now = System.currentTimeMillis();
        while (now + 30 < startDate){
            logger.info("获取st参数成功，还未到秒杀开始时间，还有 " + (startDate - now) + "ms");
            Thread.sleep(10);
        }

        service.submit(new SecKillRunnable(false, httpService, vaccineId, startDate));
        Thread.sleep(20);
        service.submit(new SecKillRunnable(true, httpService, vaccineId, startDate));
        Thread.sleep(20);
        service.submit(new SecKillRunnable(true, httpService, vaccineId, startDate));
        Thread.sleep(20);
        service.submit(new SecKillRunnable(false, httpService, vaccineId, startDate));
        service.shutdown();
        //等待线程结束
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            if (Config.success) {
                if (mainFrame != null) {
                    mainFrame.appendMsg("抢购成功，请登录约苗小程序查看");
                }
                logger.info("抢购成功，请登录约苗小程序查看");
            } else {
                if (mainFrame != null) {
                    mainFrame.appendMsg("抢购失败");
                }
                logger.info("抢购失败");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (mainFrame != null) {
                mainFrame.setStartBtnEnable();
            }
        }

    }
    public List<VaccineList> getVaccines() throws Exception {
        return httpService.getVaccineList("5101");
    }



}
