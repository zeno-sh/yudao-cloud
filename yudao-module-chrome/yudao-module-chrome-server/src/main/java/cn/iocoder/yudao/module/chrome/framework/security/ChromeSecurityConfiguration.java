package cn.iocoder.yudao.module.chrome.framework.security;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import cn.iocoder.yudao.module.chrome.enums.ApiConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Chrome 模块的 Security 配置
 */
@Configuration(proxyBeanMethods = false, value = "chromeSecurityConfiguration")
public class ChromeSecurityConfiguration {

    @Bean("chromeAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
                // 插件 API 的认证相关接口，允许匿名访问
                // 注意：由于 RequestMappingHandlerMapping 的 pathPrefix 不会存储在 RequestMappingInfo 中，
                // 导致 @PermitAll 注解扫描时获取的路径不包含 /plugin-api 前缀，需要在此处显式配置
                registry.requestMatchers("/plugin-api/chrome/auth/**").permitAll();
                
                // Swagger 接口文档
                registry.requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/swagger-ui").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll();
                // Druid 监控
                registry.requestMatchers("/druid/**").permitAll();
                // Spring Boot Actuator 的安全配置
                registry.requestMatchers("/actuator").permitAll()
                        .requestMatchers("/actuator/**").permitAll();
                // RPC 服务的安全配置
                registry.requestMatchers(ApiConstants.PREFIX + "/**").permitAll();
            }
        };
    }
}
