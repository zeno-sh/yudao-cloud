package cn.iocoder.yudao.module.chrome.service.config;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Chrome模块配置服务
 * 管理Chrome模块的各种配置项
 *
 * @author Jax
 */
@Slf4j
@Service
public class ChromeConfigService {

    /**
     * 配置缓存
     */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    /**
     * 配置键常量
     */
    public static final String SIMPLE_MODE_KEY = "chrome.subscription.simple_mode";
    public static final String CREDITS_PACK_ENABLED_KEY = "chrome.credits_pack.enabled";

    /**
     * 初始化默认配置
     */
    public ChromeConfigService() {
        // 设置默认配置
        configCache.put(SIMPLE_MODE_KEY, "true"); // 默认启用简化模式
        configCache.put(CREDITS_PACK_ENABLED_KEY, "true"); // 默认启用积分包
        
        log.info("[ChromeConfigService][配置服务初始化完成，默认启用简化模式]");
    }

    /**
     * 获取布尔类型配置
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public boolean getBooleanValue(String key, boolean defaultValue) {
        String value = configCache.get(key);
        if (value == null) {
            log.debug("[ChromeConfigService][配置项({})不存在，使用默认值: {}]", key, defaultValue);
            return defaultValue;
        }
        
        boolean result = Boolean.parseBoolean(value);
        log.debug("[ChromeConfigService][获取配置] key: {}, value: {}", key, result);
        return result;
    }

    /**
     * 获取字符串类型配置
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getStringValue(String key, String defaultValue) {
        String value = configCache.get(key);
        if (value == null) {
            log.debug("[ChromeConfigService][配置项({})不存在，使用默认值: {}]", key, defaultValue);
            return defaultValue;
        }
        
        log.debug("[ChromeConfigService][获取配置] key: {}, value: {}", key, value);
        return value;
    }

    /**
     * 设置配置值
     *
     * @param key 配置键
     * @param value 配置值
     */
    public void setConfigValue(String key, String value) {
        String oldValue = configCache.put(key, value);
        log.info("[ChromeConfigService][更新配置] key: {}, oldValue: {}, newValue: {}", key, oldValue, value);
    }

    /**
     * 是否启用简化模式
     */
    public boolean isSimpleModeEnabled() {
        return getBooleanValue(SIMPLE_MODE_KEY, true);
    }

    /**
     * 是否启用积分包功能
     */
    public boolean isCreditsPackEnabled() {
        return getBooleanValue(CREDITS_PACK_ENABLED_KEY, true);
    }

    /**
     * 启用简化模式
     */
    public void enableSimpleMode() {
        setConfigValue(SIMPLE_MODE_KEY, "true");
        log.info("[ChromeConfigService][已启用简化模式]");
    }

    /**
     * 禁用简化模式
     */
    public void disableSimpleMode() {
        setConfigValue(SIMPLE_MODE_KEY, "false");
        log.info("[ChromeConfigService][已禁用简化模式，切换到完整套餐模式]");
    }

    /**
     * 获取所有配置
     */
    public Map<String, String> getAllConfigs() {
        return new ConcurrentHashMap<>(configCache);
    }

    /**
     * 重置为默认配置
     */
    public void resetToDefault() {
        configCache.clear();
        configCache.put(SIMPLE_MODE_KEY, "true");
        configCache.put(CREDITS_PACK_ENABLED_KEY, "true");
        log.info("[ChromeConfigService][配置已重置为默认值]");
    }
}
