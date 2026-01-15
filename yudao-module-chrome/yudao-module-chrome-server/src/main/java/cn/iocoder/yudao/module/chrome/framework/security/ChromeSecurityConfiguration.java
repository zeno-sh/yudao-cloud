package cn.iocoder.yudao.module.chrome.framework.security;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Chrome 模块的 Security 配置
 */
@Configuration(proxyBeanMethods = false, value = "chromeSecurityConfiguration")
public class ChromeSecurityConfiguration {

    /**
     * 自定义的权限规则,实现对 plugin API 的自定义权限控制
     * 
     * 权限控制逻辑：
     * 1. 带 @PermitAll 注解的方法：允许匿名访问（已在全局配置中自动处理）
     * 2. 带 @PreAuthorize 注解的方法：需要相应权限
     * 3. 没有注解的方法：需要登录认证（已在全局兜底规则中处理）
     * 
     * 注意：无需在此处配置 authenticated() 规则，因为全局配置已经有兜底规则 anyRequest().authenticated()
     * 此处仅配置需要特殊处理的 permitAll 路径（如果有的话）
     */
    @Bean("chromeAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
                // Chrome 模块暂无需要特殊配置的路径
                // 所有路径的权限控制通过 @PermitAll、@PreAuthorize 注解或全局兜底规则处理
            }
        };
    }
}
