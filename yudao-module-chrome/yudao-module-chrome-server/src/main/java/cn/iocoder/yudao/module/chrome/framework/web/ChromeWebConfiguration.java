package cn.iocoder.yudao.module.chrome.framework.web;

import cn.iocoder.yudao.module.chrome.framework.interceptor.SubscriptionValidationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Chrome模块的Web配置
 * 注册Chrome模块特有的拦截器
 *
 * @author Jax
 */
@Configuration
@RequiredArgsConstructor
public class ChromeWebConfiguration implements WebMvcConfigurer {

    private final SubscriptionValidationInterceptor subscriptionValidationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册订阅验证拦截器
        registry.addInterceptor(subscriptionValidationInterceptor)
                .addPathPatterns("/plugin-api/chrome/**") // 只拦截Chrome插件API
                .excludePathPatterns(
                    "/plugin-api/chrome/auth/**",  // 排除认证相关接口
                    "/plugin-api/chrome/health/**", // 排除健康检查接口
                    "/plugin-api/chrome/public/**"  // 排除公开接口
                )
                .order(1); // 设置拦截器优先级，数字越小优先级越高
    }
}
