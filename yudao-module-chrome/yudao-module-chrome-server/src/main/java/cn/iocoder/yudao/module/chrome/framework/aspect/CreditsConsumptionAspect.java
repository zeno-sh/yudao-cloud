package cn.iocoder.yudao.module.chrome.framework.aspect;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.chrome.framework.annotation.ConsumeCredits;
import cn.iocoder.yudao.module.chrome.service.credits.UserCreditsService;
import cn.iocoder.yudao.module.chrome.service.usage.UsageRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 积分消费AOP切面
 * 统一处理积分消费和使用记录
 *
 * @author Jax
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class CreditsConsumptionAspect {

    private final UserCreditsService userCreditsService;
    private final UsageRecordService usageRecordService;

    @Around("@annotation(consumeCredits)")
    public Object around(ProceedingJoinPoint joinPoint, ConsumeCredits consumeCredits) throws Throwable {
        // 获取当前用户ID
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            log.warn("[CreditsConsumptionAspect][未获取到用户ID，跳过积分消费]");
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        log.info("[CreditsConsumptionAspect][用户({})调用方法 {}.{}，功能类型: {}，消费积分: {}]", 
            userId, className, methodName, consumeCredits.featureType().getName(), consumeCredits.credits());

        if (consumeCredits.consumeBeforeExecution()) {
            // 方法执行前消费积分
            return consumeCreditsBeforeExecution(joinPoint, consumeCredits, userId, methodName, className);
        } else {
            // 方法执行后消费积分
            return consumeCreditsAfterExecution(joinPoint, consumeCredits, userId, methodName, className);
        }
    }

    /**
     * 方法执行前消费积分
     */
    private Object consumeCreditsBeforeExecution(ProceedingJoinPoint joinPoint, ConsumeCredits consumeCredits, 
                                               Long userId, String methodName, String className) throws Throwable {

        // 消费积分
        String businessId = generateBusinessId(className, methodName);
        String description = consumeCredits.description().isEmpty() ? 
            String.format("使用%s功能", consumeCredits.featureType().getName()) : consumeCredits.description();
            
        boolean consumeSuccess;
        try {
            consumeSuccess = userCreditsService.consumeCredits(
                userId, 
                consumeCredits.credits(), 
                consumeCredits.featureType().getType(), 
                businessId
            );
        } catch (Exception e) {
            log.error("[CreditsConsumptionAspect][用户({})积分消费过程异常，方法 {}.{}]", 
                userId, className, methodName, e);
            throw new RuntimeException("积分消费异常，请稍后重试", e);
        }

        if (!consumeSuccess) {
            log.error("[CreditsConsumptionAspect][用户({})积分消费失败，方法 {}.{}，积分: {}]", 
                userId, className, methodName, consumeCredits.credits());
            throw new RuntimeException("积分消费失败，请稍后重试");
        }

        // 3. 执行原方法
        try {
            Object result = joinPoint.proceed();
            
            // 4. 记录使用记录
            try {
                usageRecordService.recordUsage(userId, consumeCredits.featureType().getType(), 
                    consumeCredits.credits(), null);
                log.info("[CreditsConsumptionAspect][用户({})方法 {}.{} 执行成功，已消费积分: {}，使用记录已保存]", 
                    userId, className, methodName, consumeCredits.credits());
            } catch (Exception recordException) {
                log.error("[CreditsConsumptionAspect][用户({})使用记录保存失败，但不影响主流程]", userId, recordException);
            }
            
            return result;
        } catch (Exception e) {
            // 方法执行失败，积分已消费，记录错误日志
            log.error("[CreditsConsumptionAspect][用户({})方法 {}.{} 执行失败，已消费积分: {}，业务ID: {}]", 
                userId, className, methodName, consumeCredits.credits(), businessId, e);
            // 注意：这里积分已经被消费，如果需要退还积分，需要调用退还接口
            // 根据业务需求决定是否实现积分退还机制
            throw e;
        }
    }

    /**
     * 方法执行后消费积分
     */
    private Object consumeCreditsAfterExecution(ProceedingJoinPoint joinPoint, ConsumeCredits consumeCredits, 
                                              Long userId, String methodName, String className) throws Throwable {
        Object result;
        boolean methodExecutionSuccess = false;
        String businessId = generateBusinessId(className, methodName);
        
        try {
            // 1. 先执行方法
            result = joinPoint.proceed();
            methodExecutionSuccess = true;
        } catch (Exception e) {
            // 方法执行失败，记录API调用但不消费积分
            log.info("[CreditsConsumptionAspect][用户({})方法 {}.{} 执行失败，记录调用但不消费积分]", 
                userId, className, methodName);
            
            // 记录使用记录（积分消费为0）
            try {
                usageRecordService.recordUsage(userId, consumeCredits.featureType().getType(), 0, null);
            } catch (Exception recordException) {
                log.error("[CreditsConsumptionAspect][用户({})使用记录保存失败]", userId, recordException);
            }
            
            // 记录API调用失败
            try {
                String description = String.format("调用%s功能失败: %s", consumeCredits.featureType().getName(), e.getMessage());
                userCreditsService.recordApiCall(userId, consumeCredits.featureType().getType(), 
                    businessId, "failed", description);
            } catch (Exception recordException) {
                log.error("[CreditsConsumptionAspect][用户({})API调用记录保存失败]", userId, recordException);
            }
            
            throw e;
        }

        // 2. 只有在方法执行成功后才处理积分逻辑
        if (methodExecutionSuccess) {
            boolean shouldConsumeCredits = true;
            String noConsumeReason = null;
            
            // 检查是否需要验证返回值
            if (consumeCredits.checkReturnValue()) {
                if (!isValidReturnValue(result)) {
                    shouldConsumeCredits = false;
                    noConsumeReason = "返回值无效";
                    log.info("[CreditsConsumptionAspect][用户({})方法 {}.{} 执行成功但返回值无效，不消费积分]", 
                        userId, className, methodName);
                }
            }
            
            // 先记录使用记录（无论是否消费积分都要记录）
            int actualCreditsConsumed = shouldConsumeCredits ? consumeCredits.credits() : 0;
            try {
                usageRecordService.recordUsage(userId, consumeCredits.featureType().getType(), 
                    actualCreditsConsumed, null);
            } catch (Exception recordException) {
                log.error("[CreditsConsumptionAspect][用户({})使用记录保存失败]", userId, recordException);
            }
            
            if (!shouldConsumeCredits) {
                // 不消费积分，但要记录API调用
                try {
                    String description = String.format("调用%s功能成功但%s，未消费积分", 
                        consumeCredits.featureType().getName(), noConsumeReason);
                    userCreditsService.recordApiCall(userId, consumeCredits.featureType().getType(), 
                        businessId, "no_data", description);
                } catch (Exception recordException) {
                    log.error("[CreditsConsumptionAspect][用户({})API调用记录保存失败]", userId, recordException);
                }
                return result;
            }
            
            // 需要消费积分的情况
            try {
                // 先检查积分余额
                if (!userCreditsService.hasEnoughCredits(userId, consumeCredits.credits())) {
                    log.warn("[CreditsConsumptionAspect][用户({})方法执行成功但积分不足，方法 {}.{}，需要积分: {}]", 
                        userId, className, methodName, consumeCredits.credits());
                    
                    // 积分不足，记录API调用但标记为失败
                    try {
                        String description = String.format("调用%s功能成功但积分不足，未消费积分", 
                            consumeCredits.featureType().getName());
                        userCreditsService.recordApiCall(userId, consumeCredits.featureType().getType(), 
                            businessId, "no_data", description);
                    } catch (Exception recordException) {
                        log.error("[CreditsConsumptionAspect][用户({})API调用记录保存失败]", userId, recordException);
                    }
                    
                    return result;
                }

                String description = consumeCredits.description().isEmpty() ? 
                    String.format("使用%s功能消费积分", consumeCredits.featureType().getName()) : consumeCredits.description();
                    
                boolean consumeSuccess = userCreditsService.consumeCredits(
                    userId, 
                    consumeCredits.credits(), 
                    consumeCredits.featureType().getType(), 
                    businessId
                );

                if (!consumeSuccess) {
                    log.error("[CreditsConsumptionAspect][用户({})方法执行成功但积分消费失败，方法 {}.{}，积分: {}]", 
                        userId, className, methodName, consumeCredits.credits());
                    
                    // 积分消费失败，记录API调用失败
                    try {
                        String failDescription = String.format("调用%s功能成功但积分消费失败", 
                            consumeCredits.featureType().getName());
                        userCreditsService.recordApiCall(userId, consumeCredits.featureType().getType(), 
                            businessId, "failed", failDescription);
                    } catch (Exception recordException) {
                        log.error("[CreditsConsumptionAspect][用户({})API调用记录保存失败]", userId, recordException);
                    }
                    
                    return result;
                }

                log.info("[CreditsConsumptionAspect][用户({})方法 {}.{} 执行完成，成功消费积分: {}，使用记录和积分交易记录已保存]", 
                    userId, className, methodName, consumeCredits.credits());
                    
            } catch (Exception e) {
                // 积分消费过程中出现异常，记录错误但不影响业务结果
                log.error("[CreditsConsumptionAspect][用户({})积分消费过程异常，方法 {}.{}]", 
                    userId, className, methodName, e);
                
                // 记录API调用异常
                try {
                    String errorDescription = String.format("调用%s功能时积分消费异常: %s", 
                        consumeCredits.featureType().getName(), e.getMessage());
                    userCreditsService.recordApiCall(userId, consumeCredits.featureType().getType(), 
                        businessId, "failed", errorDescription);
                } catch (Exception recordException) {
                    log.error("[CreditsConsumptionAspect][用户({})API调用记录保存失败]", userId, recordException);
                }
            }
        }
        
        return result;
    }

    /**
     * 生成业务ID
     */
    private String generateBusinessId(String className, String methodName) {
        return String.format("%s_%s_%d", className, methodName, System.currentTimeMillis());
    }

    /**
     * 检查返回值是否有效
     * 针对不同类型的返回值进行有效性检查
     */
    private boolean isValidReturnValue(Object result) {
        if (result == null) {
            return false;
        }
        
        // 检查CommonResult包装类型
        if (result.getClass().getName().contains("CommonResult")) {
            try {
                // 使用反射获取data字段
                java.lang.reflect.Field dataField = result.getClass().getDeclaredField("data");
                dataField.setAccessible(true);
                Object data = dataField.get(result);
                
                if (data == null) {
                    return false;
                }
                
                // 检查CoupangReviewResponseDTO类型
                if (data.getClass().getName().contains("CoupangReviewResponseDTO")) {
                    return isValidCoupangResponse(data);
                }
                
                // 检查CoupangSalesResponseDTO.ProductSalesInfo类型
                if (data.getClass().getName().contains("ProductSalesInfo")) {
                    return isValidSalesResponse(data);
                }
                
                // 检查CoupangTrendsResponseDTO类型
                if (data.getClass().getName().contains("CoupangTrendsResponseDTO")) {
                    return isValidTrendsResponse(data);
                }
                
                return true;
            } catch (Exception e) {
                log.warn("[CreditsConsumptionAspect][检查CommonResult返回值时发生异常]", e);
                return false;
            }
        }
        
        // 直接检查CoupangReviewResponseDTO类型
        if (result.getClass().getName().contains("CoupangReviewResponseDTO")) {
            return isValidCoupangResponse(result);
        }
        
        // 直接检查ProductSalesInfo类型
        if (result.getClass().getName().contains("ProductSalesInfo")) {
            return isValidSalesResponse(result);
        }
        
        // 直接检查CoupangTrendsResponseDTO类型
        if (result.getClass().getName().contains("CoupangTrendsResponseDTO")) {
            return isValidTrendsResponse(result);
        }
        
        // 其他类型默认认为有效
        return true;
    }
    
    /**
     * 检查CoupangReviewResponseDTO是否包含有效数据
     */
    private boolean isValidCoupangResponse(Object coupangResponse) {
        try {
            // 检查rCode字段
            java.lang.reflect.Field rCodeField = coupangResponse.getClass().getDeclaredField("rCode");
            rCodeField.setAccessible(true);
            String rCode = (String) rCodeField.get(coupangResponse);
            
            if (!"RET0000".equals(rCode)) {
                return false;
            }
            
            // 检查rData字段
            java.lang.reflect.Field rDataField = coupangResponse.getClass().getDeclaredField("rData");
            rDataField.setAccessible(true);
            Object rData = rDataField.get(coupangResponse);
            
            if (rData == null) {
                return false;
            }
            
            // 检查paging字段
            java.lang.reflect.Field pagingField = rData.getClass().getDeclaredField("paging");
            pagingField.setAccessible(true);
            Object paging = pagingField.get(rData);
            
            if (paging == null) {
                return false;
            }
            
            // 检查contents字段
            java.lang.reflect.Field contentsField = paging.getClass().getDeclaredField("contents");
            contentsField.setAccessible(true);
            Object contents = contentsField.get(paging);
            
            if (contents == null) {
                return false;
            }
            
            // 检查是否为空列表
            if (contents instanceof java.util.List) {
                java.util.List<?> contentsList = (java.util.List<?>) contents;
                return !contentsList.isEmpty();
            }
            
            return true;
        } catch (Exception e) {
            log.warn("[CreditsConsumptionAspect][检查CoupangReviewResponseDTO时发生异常]", e);
            return false;
        }
    }
    
    /**
     * 检查ProductSalesInfo是否包含有效数据
     */
    private boolean isValidSalesResponse(Object salesResponse) {
        if (salesResponse == null) {
            return false;
        }
        
        try {
            // 检查是否有销量数据，通过反射检查关键字段
            // ProductSalesInfo通常包含productName、salesCount等字段
            java.lang.reflect.Field[] fields = salesResponse.getClass().getDeclaredFields();
            
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(salesResponse);
                
                // 如果有任何非null的有效数据，认为是有效的
                if (value != null) {
                    if (value instanceof String && !((String) value).trim().isEmpty()) {
                        return true;
                    } else if (value instanceof Number && ((Number) value).intValue() > 0) {
                        return true;
                    } else if (!(value instanceof String) && !(value instanceof Number)) {
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            log.warn("[CreditsConsumptionAspect][检查ProductSalesInfo时发生异常]", e);
            return false;
        }
    }
    
    /**
     * 检查CoupangTrendsResponseDTO是否包含有效数据
     */
    private boolean isValidTrendsResponse(Object trendsResponse) {
        try {
            // CoupangTrendsResponseDTO的结构：包含relatedKeywords和searchItems两个列表字段
            
            // 检查relatedKeywords字段
            java.lang.reflect.Field relatedKeywordsField = trendsResponse.getClass().getDeclaredField("relatedKeywords");
            relatedKeywordsField.setAccessible(true);
            Object relatedKeywords = relatedKeywordsField.get(trendsResponse);
            
            // 检查searchItems字段
            java.lang.reflect.Field searchItemsField = trendsResponse.getClass().getDeclaredField("searchItems");
            searchItemsField.setAccessible(true);
            Object searchItems = searchItemsField.get(trendsResponse);
            
            // 只要有一个列表不为空且包含数据，就认为是有效的
            boolean hasValidRelatedKeywords = false;
            boolean hasValidSearchItems = false;
            
            if (relatedKeywords instanceof java.util.List) {
                java.util.List<?> keywordsList = (java.util.List<?>) relatedKeywords;
                hasValidRelatedKeywords = !keywordsList.isEmpty();
            }
            
            if (searchItems instanceof java.util.List) {
                java.util.List<?> itemsList = (java.util.List<?>) searchItems;
                hasValidSearchItems = !itemsList.isEmpty();
            }
            
            return hasValidRelatedKeywords || hasValidSearchItems;
        } catch (Exception e) {
            log.warn("[CreditsConsumptionAspect][检查CoupangTrendsResponseDTO时发生异常]", e);
            return false;
        }
    }
}
