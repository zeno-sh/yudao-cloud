package cn.iocoder.yudao.module.chrome.infra.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Cookie响应数据传输对象
 *
 * @author Jax
 */
@Data
public class CookieResponseDTO {

    /**
     * Cookie数据
     */
    @JsonProperty("cookie_data")
    private Map<String, List<CookieItem>> cookieData;

    /**
     * 本地存储数据
     */
    @JsonProperty("local_storage_data")
    private Map<String, Object> localStorageData;

    /**
     * 更新时间
     */
    @JsonProperty("update_time")
    private String updateTime;

    /**
     * Cookie项
     */
    @Data
    public static class CookieItem {
        /**
         * 域名
         */
        private String domain;

        /**
         * 过期时间
         */
        private Double expirationDate;

        /**
         * 是否仅限主机
         */
        private Boolean hostOnly;

        /**
         * 是否HTTP Only
         */
        private Boolean httpOnly;

        /**
         * Cookie名称
         */
        private String name;

        /**
         * 路径
         */
        private String path;

        /**
         * SameSite属性
         */
        private String sameSite;

        /**
         * 是否安全
         */
        private Boolean secure;

        /**
         * 是否会话Cookie
         */
        private Boolean session;

        /**
         * 存储ID
         */
        private String storeId;

        /**
         * Cookie值
         */
        private String value;
    }
}
