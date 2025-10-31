package cn.iocoder.yudao.module.dm.framework.datapermission;

import cn.iocoder.yudao.framework.datapermission.core.rule.dept.DeptDataPermissionRuleCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Jax
 * @Date: Created in 11:57 2025/9/28
 */
@Configuration(proxyBeanMethods = false)
public class DataPermissionConfiguration {

    @Bean
    public DeptDataPermissionRuleCustomizer sysDeptDataPermissionRuleCustomizer() {
        return rule -> {
            // dept
            rule.addDeptColumn("dm_purchase_plan", "dept_id");

            // user
            rule.addUserColumn("dm_purchase_plan", "creator");
        };
    }
}
