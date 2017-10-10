//package com.golive.cinema.util;
//
//import org.apache.log4j.Level;
//import org.slf4j.Logger;
//
//import de.mindpipe.android.logging.log4j.LogConfigurator;
//
///**
// * Created by Wangzj on 2016/11/24.
// */
//
//public class LoggerHelper {
//
//    /**
//     * 配置Log
//     *
//     * @param filePath    Log文件位置
//     * @param maxFileSize 最大文件大小
//     */
//    public static void configure(String filePath, long maxFileSize) {
//
//        if (maxFileSize <= 0) {
//            maxFileSize = 5L << 20; // 默认5MB
//        }
//
//        final LogConfigurator logConfigurator = new LogConfigurator();
//        logConfigurator.setUseLogCatAppender(true);
//        logConfigurator.setUseFileAppender(true);
//        logConfigurator.setFileName(filePath);
//        logConfigurator.setMaxFileSize(maxFileSize);
//        logConfigurator.setRootLevel(Level.DEBUG);
//        // Set log level of a specific logger
//        logConfigurator.setLevel("org.apache", Level.ERROR);
//        /*
//         * http://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/PatternLayout.html
//         */
//        logConfigurator.setFilePattern("%d - [%p::%c{1}] - %m%n");
//
//        logConfigurator.configure();
//    }
//
//    public static void info(Logger logger, String msg) {
//        logger.info(msg);
//    }
//
//    public static void debug(Logger logger, String msg) {
//        logger.debug(msg);
//    }
//
//    public static void warn(Logger logger, String msg) {
//        logger.warn(msg);
//    }
//
//    public static void error(Logger logger, String msg) {
//        logger.error(msg);
//    }
//
//}
