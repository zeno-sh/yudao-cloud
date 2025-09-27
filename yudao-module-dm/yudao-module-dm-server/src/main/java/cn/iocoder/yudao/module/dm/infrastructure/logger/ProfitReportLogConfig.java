package cn.iocoder.yudao.module.dm.infrastructure.logger;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 财务账单报告日志配置
 * 注册日志拦截器到日志系统中
 *
 * @author zeno
 */
@Configuration
public class ProfitReportLogConfig implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        
        // 设置应用上下文到日志拦截器
        ProfitReportLogAppender.setApplicationContext(applicationContext);
        
        // 注册日志拦截器到日志系统
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        
        // 创建拦截器实例
        ProfitReportLogAppender appender = new ProfitReportLogAppender();
        appender.setContext(loggerContext);
        appender.setName("PROFIT_REPORT_APPENDER");
        appender.start();
        
        // 添加到根日志记录器
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);
        
        // 还可以添加到特定的日志记录器
        Logger ozonLogger = loggerContext.getLogger("cn.iocoder.yudao.module.dm.infrastructure.ozon");
        ozonLogger.addAppender(appender);
        
        Logger serviceLogger = loggerContext.getLogger("cn.iocoder.yudao.module.dm.infrastructure.service");
        serviceLogger.addAppender(appender);
    }
} 