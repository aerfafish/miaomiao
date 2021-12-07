package com.alpha.seckill;

import com.alibaba.fastjson.JSONObject;
import com.alpha.seckill.conf.Config;
import com.alpha.seckill.ui.ConsoleMode;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
            // create the command line parser
            Options options = new Options();
            options.addOption(new Option("c", "cookie", true, "cookie from wx app"));
            options.addOption(new Option("t", "token", true, "token from wx app"));
            options.addOption(new Option("n", "name", true, "name who will be vaccinated"));
            options.addOption(new Option("r", "region", true, "region code Chengdu is 5101"));
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.getOptionValue("t") != null && !"".equals(cmd.getOptionValue("t"))) {
                Config.tk = cmd.getOptionValue("t");
            }
            if (cmd.getOptionValue("c") != null && !"".equals(cmd.getOptionValue("c"))) {
                Config.cookie = cmd.getOptionValue("c");
            }
            if (cmd.getOptionValue("n") != null && !"".equals(cmd.getOptionValue("n"))) {
                Config.memberName = cmd.getOptionValue("n");
            }
            if (cmd.getOptionValue("r") != null && !"".equals(cmd.getOptionValue("r"))) {
                Config.regionCode = cmd.getOptionValue("r");
            }
            new ConsoleMode().start();
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            // 保存下来
            System.out.println(JSONObject.toJSONString(Config.requestDOList));
        }
        logger.info("=================程序运行结束=================");
    }

}
