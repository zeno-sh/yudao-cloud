package cn.iocoder.yudao.module.dm.service.profitreport.v2.util;

import cn.iocoder.yudao.module.dm.service.profitreport.ProfitReportTaskLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 利润计算结构化日志记录工具
 * 参考V1版本的日志记录模式，提供统一的日志记录接口
 *
 * @author Jax
 */
@Component
@Slf4j
public class ProfitCalculationLogger {

    @Resource
    private ProfitReportTaskLogService profitReportTaskLogService;

    /**
     * 记录结构化警告日志
     * 同时输出到控制台和任务日志
     *
     * @param taskId 任务ID
     * @param message 警告消息
     * @param args 参数
     */
    public void logStructuredWarning(String taskId, String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        // 输出到控制台
        log.warn("[WARNING] [{}] {}", taskId, formattedMessage);
        // 记录到任务日志
        profitReportTaskLogService.appendExecuteLog(taskId, "[WARNING] " + formattedMessage);
    }

    /**
     * 记录结构化错误日志
     * 同时输出到控制台和任务日志
     *
     * @param taskId 任务ID
     * @param message 错误消息
     * @param args 参数
     */
    public void logStructuredError(String taskId, String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        // 输出到控制台
        log.error("[ERROR] [{}] {}", taskId, formattedMessage);
        // 记录到任务日志
        profitReportTaskLogService.appendErrorInfo(taskId, formattedMessage);
        profitReportTaskLogService.appendExecuteLog(taskId, "[ERROR] " + formattedMessage);
    }

    /**
     * 记录结构化异常日志
     * 同时输出到控制台和任务日志
     *
     * @param taskId 任务ID
     * @param message 异常消息
     * @param exception 异常对象
     * @param args 参数
     */
    public void logStructuredException(String taskId, String message, Exception exception, Object... args) {
        String formattedMessage = formatMessage(message, args);
        String fullMessage = formattedMessage + ": " + exception.getMessage();
        // 输出到控制台
        log.error("[EXCEPTION] [{}] {}", taskId, fullMessage, exception);
        // 记录到任务日志
        profitReportTaskLogService.appendErrorInfo(taskId, fullMessage);
        profitReportTaskLogService.appendExecuteLog(taskId, "[EXCEPTION] " + fullMessage);
    }

    /**
     * 记录重要信息日志
     * 用于记录关键的处理步骤
     *
     * @param taskId 任务ID
     * @param message 信息消息
     * @param args 参数
     */
    public void logImportantInfo(String taskId, String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        // 输出到控制台
        log.info("[INFO] [{}] {}", taskId, formattedMessage);
        // 记录到任务日志
        profitReportTaskLogService.appendExecuteLog(taskId, "[INFO] " + formattedMessage);
    }

    /**
     * 记录进度信息
     * 用于记录处理进度
     *
     * @param taskId 任务ID
     * @param message 进度消息
     * @param args 参数
     */
    public void logProgress(String taskId, String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        // 输出到控制台
        log.info("[PROGRESS] [{}] {}", taskId, formattedMessage);
        // 记录到任务日志
        profitReportTaskLogService.appendExecuteLog(taskId, "[PROGRESS] " + formattedMessage);
    }

    /**
     * 记录成功信息
     * 用于记录成功完成的操作
     *
     * @param taskId 任务ID
     * @param message 成功消息
     * @param args 参数
     */
    public void logSuccess(String taskId, String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        // 输出到控制台
        log.info("[SUCCESS] [{}] {}", taskId, formattedMessage);
        // 记录到任务日志
        profitReportTaskLogService.appendExecuteLog(taskId, "[SUCCESS] " + formattedMessage);
    }

    // ==================== 业务特定的日志记录方法 ====================

    /**
     * 记录商品未映射的警告日志
     *
     * @param taskId 任务ID
     * @param skuId 商品ID
     */
    public void logProductNotMapped(String taskId, String skuId) {
        logStructuredWarning(taskId, "在线商品未映射: %s", skuId);
    }

    /**
     * 记录订单商品不存在的警告日志
     *
     * @param taskId 任务ID
     * @param orderNumber 订单号
     */
    public void logOrderProductNotFound(String taskId, String orderNumber) {
        logStructuredWarning(taskId, "订单号: %s 不存在，无法计算收单成本", orderNumber);
    }

    /**
     * 记录产品成本信息缺失的警告日志
     *
     * @param taskId 任务ID
     * @param skuId 商品ID
     * @param costType 成本类型
     */
    public void logProductCostMissing(String taskId, String skuId, String costType) {
        logStructuredWarning(taskId, "产品 %s 缺失 %s 成本信息", skuId, costType);
    }

    /**
     * 记录汇率数据缺失的警告日志
     *
     * @param taskId 任务ID
     * @param currency 货币类型
     * @param date 日期
     */
    public void logExchangeRateMissing(String taskId, String currency, String date) {
        logStructuredWarning(taskId, "缺失汇率数据: %s 在 %s，将使用默认汇率", currency, date);
    }

    /**
     * 记录数据验证失败的警告日志
     *
     * @param taskId 任务ID
     * @param dataType 数据类型
     * @param reason 失败原因
     */
    public void logDataValidationFailed(String taskId, String dataType, String reason) {
        logStructuredWarning(taskId, "数据验证失败: %s - %s", dataType, reason);
    }

    /**
     * 记录计算异常的警告日志
     *
     * @param taskId 任务ID
     * @param calculationType 计算类型
     * @param identifier 标识符（如订单号、商品ID等）
     * @param reason 异常原因
     */
    public void logCalculationException(String taskId, String calculationType, String identifier, String reason) {
        logStructuredWarning(taskId, "%s 计算异常: %s - %s", calculationType, identifier, reason);
    }

    /**
     * 记录数据收集统计信息
     *
     * @param taskId 任务ID
     * @param dataType 数据类型
     * @param expectedCount 期望数量
     * @param actualCount 实际数量
     */
    public void logDataCollectionStats(String taskId, String dataType, int expectedCount, int actualCount) {
        if (expectedCount != actualCount) {
            logStructuredWarning(taskId, "%s 数据收集统计异常: 期望 %d 条，实际 %d 条", 
                    dataType, expectedCount, actualCount);
        } else {
            logImportantInfo(taskId, "%s 数据收集完成: %d 条", dataType, actualCount);
        }
    }

    /**
     * 记录批量处理统计信息
     *
     * @param taskId 任务ID
     * @param operation 操作类型
     * @param totalCount 总数
     * @param successCount 成功数
     * @param failureCount 失败数
     */
    public void logBatchProcessingStats(String taskId, String operation, int totalCount, int successCount, int failureCount) {
        if (failureCount > 0) {
            logStructuredWarning(taskId, "%s 批量处理完成: 总数 %d，成功 %d，失败 %d", 
                    operation, totalCount, successCount, failureCount);
        } else {
            logSuccess(taskId, "%s 批量处理完成: 总数 %d，全部成功", operation, totalCount);
        }
    }

    /**
     * 格式化消息
     *
     * @param message 消息模板
     * @param args 参数
     * @return 格式化后的消息
     */
    private String formatMessage(String message, Object... args) {
        if (args.length > 0) {
            try {
                return String.format(message, args);
            } catch (Exception e) {
                log.warn("格式化日志消息失败: {}", message, e);
                return message + " " + String.join(", ", java.util.Arrays.toString(args));
            }
        }
        return message;
    }
} 