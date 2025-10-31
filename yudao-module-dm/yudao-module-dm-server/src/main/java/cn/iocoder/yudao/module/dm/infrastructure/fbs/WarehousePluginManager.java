package cn.iocoder.yudao.module.dm.infrastructure.fbs;

import cn.iocoder.yudao.module.dm.spi.FbsWarehousePlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WarehousePluginManager {

    private final Map<Integer, FbsWarehousePlugin> pluginMap = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        log.info("====开始加载 FBS 插件====");
        // 注意：这里不用SpringFactoriesLoader加载spring.factories文件是因为
        // SpringFactoriesLoader是用反射机制手动创建的实例对象，不受spring管理
        // 因此，会导致实现类中 @Autowired等注解失效，所以需要使用spring的ApplicationContext来加载
        Map<String, FbsWarehousePlugin> beansOfType = applicationContext.getBeansOfType(FbsWarehousePlugin.class);
        for (FbsWarehousePlugin plugin : beansOfType.values()) {
            pluginMap.put(plugin.getCompanyId(), plugin);
            log.info("[FBS 插件加载成功！] 公司ID：{}，插件：{}", plugin.getCompanyId(), plugin.getClass().getName());
        }
    }

    public FbsWarehousePlugin getPluginByCompanyId(Integer companyId) {
        return pluginMap.get(companyId);
    }
}