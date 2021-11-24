package com.github.lyrric;

import com.alibaba.fastjson.JSONObject;
import com.github.lyrric.conf.Config;
import com.github.lyrric.ui.ConsoleMode;
import com.github.lyrric.ui.MainFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created on 2020-07-21.
 *
 * @author wangxiaodong
 */
public class Main {

    private  static  final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        logger.info("=================程序开始运行=================");
        try {
            new ConsoleMode().start();
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            System.out.println(JSONObject.toJSONString(Config.requestDOList));
        }
//        if(args.length > 0 && "-c".equals(args[0].toLowerCase())){
//            new ConsoleMode().start();
//        }else{
//            new MainFrame();
//        }
        logger.info("=================程序运行结束=================");
    }

}
