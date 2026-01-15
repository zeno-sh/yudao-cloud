package cn.iocoder.yudao.module.chrome.framework.interceptor;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.chrome.framework.annotation.RequireSubscription;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.chrome.service.subscription.SubscriptionService;
import static cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 订阅验证拦截器
 * 在请求进入Controller之前验证用户订阅状态（到期时间）
 * 
 * 注意：积分验证和消费请使用 @ConsumeCredits 注解配合 CreditsConsumptionAspect
 *
 * @author Jax
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionValidationInterceptor implements HandlerInterceptor {

    private final SubscriptionService subscriptionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理Controller方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        
        // 检查方法或类上是否有@RequireSubscription注解
        RequireSubscription annotation = getRequireSubscriptionAnnotation(method, handlerMethod.getBeanType());
        if (annotation == null) {
            return true; // 没有注解，直接通过
        }

        // 获取当前用户ID
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            log.warn("[SubscriptionValidationInterceptor][未获取到用户ID，跳过订阅验证] method: {}", method.getName());
            return true; // 未登录用户，交给Security处理
        }

        String methodName = method.getName();
        String className = handlerMethod.getBeanType().getSimpleName();
        String feature = annotation.feature().isEmpty() ? 
            String.format("%s.%s", className, methodName) : annotation.feature();

        log.info("[SubscriptionValidationInterceptor][开始验证用户({})订阅状态] method: {}, feature: {}", 
            userId, methodName, feature);

        try {
            // 验证订阅到期
            validateSubscriptionExpiration(userId, feature);

            log.info("[SubscriptionValidationInterceptor][用户({})订阅验证通过] method: {}, feature: {}", 
                userId, methodName, feature);
            return true;

        } catch (ServiceException e) {
            log.warn("[SubscriptionValidationInterceptor][用户({})订阅验证失败] method: {}, feature: {}, error: {}", 
                userId, methodName, feature, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[SubscriptionValidationInterceptor][用户({})订阅验证异常] method: {}, feature: {}", 
                userId, methodName, feature, e);
            throw new ServiceException(SUBSCRIPTION_NOT_FOUND);
        }
    }

    /**
     * 获取RequireSubscription注解，优先从方法上获取，然后从类上获取
     */
    private RequireSubscription getRequireSubscriptionAnnotation(Method method, Class<?> targetClass) {
        // 先检查方法上的注解
        RequireSubscription annotation = AnnotationUtils.findAnnotation(method, RequireSubscription.class);
        if (annotation != null) {
            return annotation;
        }
        
        // 再检查类上的注解
        return AnnotationUtils.findAnnotation(targetClass, RequireSubscription.class);
    }

    /**
     * 验证订阅到期
     */
    private void validateSubscriptionExpiration(Long userId, String feature) {
        if (!subscriptionService.validateSubscriptionStatus(userId)) {
            log.warn("[SubscriptionValidationInterceptor][用户({})订阅已过期] feature: {}", userId, feature);
            throw new ServiceException(SUBSCRIPTION_EXPIRED);
        }
    }
}

