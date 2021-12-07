package com.alpha.seckill.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alpha.seckill.conf.Config;
import com.alpha.seckill.model.exception.BusinessException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wangxiaodong
 */
public class SecKillRunnable implements Runnable{

    private final Logger logger = LogManager.getLogger(SecKillService.class);
    /**
     * 是否刷新st
     */
    private boolean resetSt;
    /**
     * httpService
     */
    private SeckillHttpService httpService;
    /**
     * 疫苗id
     */
    private Integer vaccineId;
    /**
     * 开始时间
     */
    private long startDate;

    public SecKillRunnable(boolean resetSt, SeckillHttpService httpService, Integer vaccineId, long startDate) {
        this.resetSt = resetSt;
        this.httpService = httpService;
        this.vaccineId = vaccineId;
        this.startDate = startDate;
    }

    @Override
    public void run() {
        do {
            long id = Thread.currentThread().getId();
            try {
                //获取加密参数st
                if(resetSt){
                    logger.info("Thread ID：{}，请求获取加密参数st", id);
                    Config.st = httpService.getSt(vaccineId.toString());
                    logger.info("Thread ID：{}，成功获取加密参数st", id);
                }
                logger.info("Thread ID：{}，秒杀请求", id);
                if (Config.orderId == null) {
                    String orderId = httpService.secKill(vaccineId.toString(), "1", Config.memberId.toString(),
                            Config.idCard, Config.st);
                    if (Config.orderId != null) {
                        logger.warn("Thread ID：{}: orderId {} is exist", id, orderId);
                    }
                    Config.orderId = orderId;
                }
                logger.info("Thread ID：{}，抢购成功 orderId: " + Config.orderId, id);
//                for (int i = 0; i <= 10; i ++) {
//                    if (orderTime()) {
//                        break;
//                    }
//                }
                break;
            } catch (BusinessException e) {
                logger.info("Thread ID: {}, 抢购失败: {}", id, e.getMessage());
                if(e.getMessage().contains("没抢到")){
                    Config.success = false;
                    break;
                }
            } catch (ConnectTimeoutException | SocketTimeoutException socketTimeoutException ){
                logger.error("Thread ID: {},抢购失败: 超时了", Thread.currentThread().getId());
            }catch (Exception e) {
                logger.warn("Thread ID: {}，未知异常", Thread.currentThread().getId());
            }finally {
                //如果离开始时间10分钟后，或者已经成功抢到则不再继续
                if (System.currentTimeMillis()+Config.timeDiff > startDate + 1000 * 60 *10) {
                    break;
                }
            }
        } while (true);
    }

    /**
     * 预定疫苗接种时间
     */
    private Boolean orderTime() throws Exception {
        long id = Thread.currentThread().getId();
        logger.info("Thread ID：{}，orderId: {} 开始预定疫苗时间", id, Config.orderId);

        String vaccineDate = httpService.getVaccineDay(vaccineId.toString(), Config.orderId);
        try {
            JSONArray workDays = JSONArray.parseArray(vaccineDate);
            if (workDays.size() == 0) {
                logger.error("Thread ID：{}，orderId: {} 未获取到可用时间 {}", id, Config.orderId, vaccineDate);
                return false;
            }
            for (int i = 0; i < workDays.size(); i++) {
                JSONObject daysObj = workDays.getJSONObject(i);
                if (daysObj.getInteger("total") != null && daysObj.getInteger("total") > 0) {
                    if (Config.success) {
                        break;
                    }
                    String dayString = daysObj.getString("day");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = sdf.parse(dayString);
                    sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String day = sdf.format(date);
                    logger.info("Thread ID：{}，orderId: {} 开始预定日期{}的可用时间", id, Config.orderId, day);
                    String vaccineDayTime = httpService.getVaccineDayTime(vaccineId.toString(), Config.orderId, day);
                    JSONArray dayTimeArray = JSONArray.parseArray(vaccineDayTime);
                    if (dayTimeArray.size() == 0) {
                        logger.info(dayString + " 无可用时间段");
                        continue;
                    }
                    for (int j = 0; j < dayTimeArray.size(); j++) {
                        JSONObject dayTimeObj = dayTimeArray.getJSONObject(j);
                        logger.info("Thread ID：{}，orderId: {} 开始预定Time{}", id, Config.orderId, dayTimeObj.toJSONString());
                        Integer maxSub = dayTimeObj.getInteger("maxSub");
                        if (maxSub != null && maxSub > 0) {
                            String wid = dayTimeObj.getString("wid");
                            if (wid == null) {
                                logger.info(dayTimeObj.toJSONString() + " wid is null");
                                continue;
                            }
                            httpService.submitDayTime(vaccineId.toString(), Config.orderId, day, wid);
                            logger.info("预定成功!! 日期为 " + dayString + " " + dayTimeObj.toJSONString());
                            Config.success = true;
                            return true;
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("", e);
        }
        return false;
    }
}
