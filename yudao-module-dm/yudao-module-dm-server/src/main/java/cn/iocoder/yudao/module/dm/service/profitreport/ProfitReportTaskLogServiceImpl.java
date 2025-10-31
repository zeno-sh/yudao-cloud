package cn.iocoder.yudao.module.dm.service.profitreport;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.ProfitReportTaskLogPageReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.profitreport.ProfitReportTaskLogDO;
import cn.iocoder.yudao.module.dm.dal.mysql.profitreport.ProfitReportTaskLogMapper;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.ProfitReportTaskStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 财务账单报告计算任务日志 Service 实现类
 *
 * @author zeno
 */
@Service
@Validated
public class ProfitReportTaskLogServiceImpl implements ProfitReportTaskLogService {

    @Resource
    private ProfitReportTaskLogMapper profitReportTaskLogMapper;
    
    // 使用ConcurrentHashMap来缓存每个任务的错误信息汇总
    private final Map<String, ErrorSummary> taskErrorSummaries = new ConcurrentHashMap<>();
    
    // 错误信息最大长度，增加到30000以便能够容纳全量错误信息
    private static final int MAX_ERROR_INFO_LENGTH = 30000;
    
    // 错误信息前缀模式，用于去重
    private static final Pattern ERROR_PREFIX_PATTERN = Pattern.compile("^\\[\\d{2}:\\d{2}:\\d{2}\\].*");
    
    // 订单号的正则表达式
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("订单号：([^，,：:]+)");
    
    // 在线商品未映射的正则表达式
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("在线商品未映射：(\\d+)");

    @Override
    public String createTaskLog(String[] clientIds, String dimension, String timeType, 
                              LocalDate startDate, LocalDate endDate) {
        // 生成10位的随机任务ID
        String taskId = RandomUtil.randomString(10);
        
        ProfitReportTaskLogDO taskLog = new ProfitReportTaskLogDO();
        taskLog.setTaskId(taskId);
        taskLog.setClientIds(clientIds != null ? String.join(",", clientIds) : null);
        taskLog.setDimension(dimension);
        taskLog.setTimeType(timeType);
        taskLog.setStartDate(startDate);
        taskLog.setEndDate(endDate);
        taskLog.setExecuteStartTime(LocalDateTime.now());
        taskLog.setStatus(ProfitReportTaskStatusEnum.RUNNING.getStatus());
        taskLog.setExecuteLog("任务已创建，准备执行计算...");
        
        profitReportTaskLogMapper.insert(taskLog);
        
        // 为新任务创建错误汇总对象
        taskErrorSummaries.put(taskId, new ErrorSummary());
        
        return taskId;
    }

    @Override
    public void updateTaskStatus(String taskId, ProfitReportTaskStatusEnum status, 
                              String errorInfo, String executeLog, Integer affectedRecords) {
        ProfitReportTaskLogDO taskLog = profitReportTaskLogMapper.selectByTaskId(taskId);
        if (taskLog == null) {
            return;
        }
        
        // 如果任务已经是终态，则不再更新
        if (Objects.equals(taskLog.getStatus(), ProfitReportTaskStatusEnum.SUCCESS.getStatus()) 
                || Objects.equals(taskLog.getStatus(), ProfitReportTaskStatusEnum.FAILED.getStatus())) {
            return;
        }
        
        ProfitReportTaskLogDO updateObj = new ProfitReportTaskLogDO();
        updateObj.setId(taskLog.getId());
        updateObj.setStatus(status.getStatus());
        
        // 任务完成时，汇总并记录错误信息
        if (status == ProfitReportTaskStatusEnum.SUCCESS || status == ProfitReportTaskStatusEnum.FAILED) {
            // 获取汇总的错误信息
            String summaryErrorInfo = getErrorSummary(taskId);
            if (StrUtil.isNotBlank(summaryErrorInfo)) {
                updateObj.setErrorInfo(summaryErrorInfo);
            } else if (StrUtil.isNotBlank(errorInfo)) {
                updateObj.setErrorInfo(errorInfo);
            } else if (StrUtil.isNotBlank(taskLog.getErrorInfo())) {
                updateObj.setErrorInfo(taskLog.getErrorInfo());
            }
            
            // 清理缓存中的错误汇总对象
            taskErrorSummaries.remove(taskId);
        } else if (StrUtil.isNotBlank(errorInfo)) {
            updateObj.setErrorInfo(errorInfo);
        }
        
        if (StrUtil.isNotBlank(executeLog)) {
            updateObj.setExecuteLog(executeLog);
        }
        
        updateObj.setAffectedRecords(affectedRecords);
        
        // 如果是终态，设置结束时间
        if (status == ProfitReportTaskStatusEnum.SUCCESS || status == ProfitReportTaskStatusEnum.FAILED) {
            updateObj.setExecuteEndTime(LocalDateTime.now());
        }
        
        profitReportTaskLogMapper.updateById(updateObj);
    }

    @Override
    public void appendExecuteLog(String taskId, String logInfo) {
        if (StrUtil.isBlank(logInfo)) {
            return;
        }
        
        ProfitReportTaskLogDO taskLog = profitReportTaskLogMapper.selectByTaskId(taskId);
        if (taskLog == null) {
            return;
        }
        
        String currentLog = taskLog.getExecuteLog();
        String newLog = currentLog == null ? logInfo : currentLog + "\n" + logInfo;
        
        // 防止日志过长，优化截断逻辑
        if (newLog.length() > MAX_ERROR_INFO_LENGTH) {
            // 找到适当的换行符位置，尽量保留完整的日志条目
            int startPos = newLog.length() - MAX_ERROR_INFO_LENGTH + 500; // 多截取500字符，以便找到合适的换行位置
            if (startPos < 0) {
                startPos = 0;
            }
            
            // 从startPos位置查找最近的换行符
            int newLinePos = newLog.indexOf('\n', startPos);
            if (newLinePos > 0) {
                newLog = "...(前面的日志已省略)\n" + newLog.substring(newLinePos + 1);
            } else {
                // 如果找不到合适的换行符，直接截断
                newLog = "...(前面的日志已省略)\n" + newLog.substring(newLog.length() - MAX_ERROR_INFO_LENGTH);
            }
        }
        
        ProfitReportTaskLogDO updateObj = new ProfitReportTaskLogDO();
        updateObj.setId(taskLog.getId());
        updateObj.setExecuteLog(newLog);
        
        profitReportTaskLogMapper.updateById(updateObj);
    }

    @Override
    public void appendErrorInfo(String taskId, String errorInfo) {
        if (StrUtil.isBlank(errorInfo)) {
            return;
        }
        
        // 添加错误到汇总对象
        ErrorSummary errorSummary = taskErrorSummaries.computeIfAbsent(taskId, k -> new ErrorSummary());
        
        // 处理错误信息，提取关键部分
        String processedError = processErrorInfo(errorInfo);
        errorSummary.addError(processedError);
        
        // 立即更新数据库中的错误信息
        updateErrorInfoToDatabase(taskId, errorSummary.getFormattedErrorInfo());
    }
    
    /**
     * 处理错误信息，提取关键部分，去除时间戳等重复信息
     */
    private String processErrorInfo(String errorInfo) {
        if (StrUtil.isBlank(errorInfo)) {
            return errorInfo;
        }
        
        // 去除可能的时间戳前缀
        Matcher matcher = ERROR_PREFIX_PATTERN.matcher(errorInfo);
        if (matcher.find()) {
            // 提取时间戳后的实际错误消息
            int timestampEnd = errorInfo.indexOf("]") + 1;
            if (timestampEnd > 0 && timestampEnd < errorInfo.length()) {
                // 跳过时间戳后的空格
                while (timestampEnd < errorInfo.length() && Character.isWhitespace(errorInfo.charAt(timestampEnd))) {
                    timestampEnd++;
                }
                return errorInfo.substring(timestampEnd);
            }
        }
        
        return errorInfo;
    }
    
    /**
     * 更新错误信息到数据库
     */
    private void updateErrorInfoToDatabase(String taskId, String errorInfo) {
        ProfitReportTaskLogDO taskLog = profitReportTaskLogMapper.selectByTaskId(taskId);
        if (taskLog == null) {
            return;
        }
        
        ProfitReportTaskLogDO updateObj = new ProfitReportTaskLogDO();
        updateObj.setId(taskLog.getId());
        updateObj.setErrorInfo(errorInfo);
        
        profitReportTaskLogMapper.updateById(updateObj);
    }
    
    /**
     * 获取汇总的错误信息
     */
    private String getErrorSummary(String taskId) {
        ErrorSummary errorSummary = taskErrorSummaries.get(taskId);
        if (errorSummary == null) {
            return null;
        }
        return errorSummary.getFormattedErrorInfo();
    }

    @Override
    public ProfitReportTaskLogDO getTaskLog(String taskId) {
        return profitReportTaskLogMapper.selectByTaskId(taskId);
    }

    @Override
    public PageResult<ProfitReportTaskLogDO> getTaskLogPage(ProfitReportTaskLogPageReqVO pageReqVO) {
        return profitReportTaskLogMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ProfitReportTaskLogDO> getLatestTaskLogs(int limit) {
        return profitReportTaskLogMapper.selectLatestTasks(limit);
    }
    
    /**
     * 错误信息汇总类，用于对相似错误进行去重和归类
     */
    private static class ErrorSummary {
        // 存储唯一订单错误
        private final Set<String> uniqueOrderErrors = new TreeSet<>();
        // 存储唯一商品错误
        private final Set<String> uniqueProductErrors = new TreeSet<>();
        // 存储其他唯一错误
        private final Set<String> uniqueOtherErrors = new TreeSet<>();
        
        /**
         * 添加错误信息
         */
        public synchronized void addError(String errorInfo) {
            if (StrUtil.isBlank(errorInfo)) {
                return;
            }
            
            // 检查是否是订单号相关错误
            Matcher orderMatcher = ORDER_ID_PATTERN.matcher(errorInfo);
            if (orderMatcher.find()) {
                String orderId = orderMatcher.group(1);
                if (errorInfo.contains("在线商品不存在")) {
                    // 添加到订单错误集合
                    uniqueOrderErrors.add(orderId);
                    return;
                }
            }
            
            // 检查是否是商品未映射错误
            Matcher productMatcher = PRODUCT_ID_PATTERN.matcher(errorInfo);
            if (productMatcher.find()) {
                String productId = productMatcher.group(1);
                // 添加到商品错误集合
                uniqueProductErrors.add(productId);
                return;
            }
            
            // 处理其他一般错误
            uniqueOtherErrors.add(errorInfo);
        }
        
        /**
         * 获取格式化的错误信息
         */
        public String getFormattedErrorInfo() {
            StringBuilder result = new StringBuilder();
            
            // 如果没有任何错误，返回null
            if (uniqueOrderErrors.isEmpty() && uniqueProductErrors.isEmpty() && uniqueOtherErrors.isEmpty()) {
                return null;
            }
            
            result.append("=== 错误信息汇总 ===\n\n");
            
            // 添加订单相关错误汇总
            if (!uniqueOrderErrors.isEmpty()) {
                result.append("【订单错误】共计 ").append(uniqueOrderErrors.size()).append(" 个订单出现错误:\n");
                // 全量展示订单错误
                uniqueOrderErrors.forEach(orderId -> 
                    result.append("- 订单号：").append(orderId).append(" 在线商品不存在，无法计算收单成本\n"));
                result.append("\n");
            }
            
            // 添加商品相关错误汇总
            if (!uniqueProductErrors.isEmpty()) {
                result.append("【商品错误】共计 ").append(uniqueProductErrors.size()).append(" 个商品未映射:\n");
                // 全量展示商品错误
                uniqueProductErrors.forEach(productId -> 
                    result.append("- 在线商品未映射：").append(productId).append("\n"));
                result.append("\n");
            }
            
            // 添加一般错误汇总
            if (!uniqueOtherErrors.isEmpty()) {
                result.append("【其他错误】共计 ").append(uniqueOtherErrors.size()).append(" 种错误:\n");
                
                // 按字母顺序排序，全量显示
                uniqueOtherErrors.stream()
                        .sorted()
                        .forEach(error -> result.append("- ").append(error).append("\n"));
            }
            
            return result.toString();
        }
    }
} 