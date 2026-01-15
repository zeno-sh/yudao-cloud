package cn.iocoder.yudao.module.chrome.infra.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.chrome.infra.dto.CookieResponseDTO;
import cn.iocoder.yudao.module.chrome.service.server.ClientServerService;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientServerDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientCookieDO;
import cn.iocoder.yudao.module.chrome.controller.admin.server.vo.ClientServerPageReqVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Cookie获取服务实现类
 *
 * @author Jax
 */
@Slf4j
@Service
public class CookieQueryService {

    @Autowired
    private ClientServerService clientServerService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 后台Cookie类型
     */
    private static final Integer BACKEND_COOKIE_TYPE = 20;

    /**
     * 前台Cookie类型
     */
    private static final Integer FRONTEND_COOKIE_TYPE = 10;

    /**
     * 后台域名
     */
    private static final String BACKEND_DOMAIN = "wing.coupang.com";

    /**
     * 前台域名
     */
    private static final String FRONTEND_DOMAIN = "coupang.com";

    /**
     * Cookie名称
     */
    private static final String SX_SESSION_ID = "sxSessionId";
    private static final String BM_S = "bm_s";
    private static final String XSRF_TOKEN = "XSRF-TOKEN";
    private static final String OAUTH_TOKEN_REQUEST_STATE = "OAuth_Token_Request_State";

    public String getBackendSxSessionId() {
        log.info("开始获取后台sxSessionId");
        return getCookieValue(BACKEND_COOKIE_TYPE, BACKEND_DOMAIN, SX_SESSION_ID, "/get/houtai");
    }

    public String getFrontendBmS() {
        log.info("开始获取前台bm_s");
        return getCookieValue(FRONTEND_COOKIE_TYPE, FRONTEND_DOMAIN, BM_S, "/get/qiantai");
    }

    /**
     * 通用获取Cookie值的方法
     *
     * @param cookieType Cookie类型
     * @param domain 域名
     * @param cookieName Cookie名称
     * @param path 请求路径
     * @return Cookie值
     */
    private String getCookieValue(Integer cookieType, String domain, String cookieName, String path) {
        // 获取第一个服务器配置（按ID排序）
        ClientServerPageReqVO pageReqVO = new ClientServerPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(1);
        
        List<ClientServerDO> servers = clientServerService.getClientServerPage(pageReqVO).getList();
        if (servers == null || servers.isEmpty()) {
            log.error("未找到可用的服务器配置");
            return null;
        }

        ClientServerDO server = servers.get(0);
        log.info("使用服务器配置: {}:{}", server.getIp(), server.getPort());

        // 获取该服务器的Cookie配置
        List<ClientCookieDO> cookies = clientServerService.getClientCookieListByServerId(server.getId());
        Optional<ClientCookieDO> targetCookie = cookies.stream()
                .filter(cookie -> cookieType.equals(cookie.getType()))
                .findFirst();

        if (!targetCookie.isPresent()) {
            log.error("未找到服务器ID {} 的类型为 {} 的Cookie配置", server.getId(), cookieType);
            return null;
        }

        // 构建请求URL
        String url = String.format("http://%s:%d%s", server.getIp(), server.getPort(), path);
        log.info("请求URL: {}", url);

        // 发送HTTP请求获取Cookie数据
        HttpResponse response = HttpRequest.post(url)
                .form("password", targetCookie.get().getPassword())
                .execute();

        if (!response.isOk()) {
            log.error("获取Cookie失败，HTTP状态码: {}", response.getStatus());
            return null;
        }

        String responseBody = response.body();
        if (StrUtil.isBlank(responseBody)) {
            log.error("获取Cookie响应为空");
            return null;
        }

        // 解析JSON响应
        CookieResponseDTO cookieResponse = JSONUtil.toBean(responseBody, CookieResponseDTO.class);
        if (cookieResponse == null || cookieResponse.getCookieData() == null) {
            log.error("解析Cookie响应失败");
            return null;
        }

        // 获取指定域名的Cookie列表
        Map<String, List<CookieResponseDTO.CookieItem>> cookieData = cookieResponse.getCookieData();
        List<CookieResponseDTO.CookieItem> cookieItems = cookieData.get(domain);
        if (cookieItems == null || cookieItems.isEmpty()) {
            log.warn("域名 {} 下没有找到Cookie", domain);
            return null;
        }

        // 查找指定名称的Cookie
        Optional<CookieResponseDTO.CookieItem> targetCookieItem = cookieItems.stream()
                .filter(item -> cookieName.equals(item.getName()))
                .findFirst();

        if (targetCookieItem.isPresent()) {
            String value = targetCookieItem.get().getValue();
            log.info("成功获取{}: {}", cookieName, value);
            return value;
        }

        log.warn("Cookie中未找到{}", cookieName);
        return null;
    }

    /**
     * 获取前台Cookie字符串（从cookie_data格式解析，拼接所有Cookie）
     *
     * @return Cookie字符串，格式如："name1=value1; name2=value2"
     */
    public String getFrontendCookieString() {
        log.info("开始获取前台Cookie字符串（拼接所有Cookie）");
        return getCookieString(FRONTEND_COOKIE_TYPE, FRONTEND_DOMAIN, "/get/qiantai", null);
    }

    /**
     * 获取后台Cookie字符串（从cookie_data格式解析，只过滤sxSessionId）
     *
     * @return Cookie字符串，格式如："sxSessionId=value"
     */
    public String getBackendCookieString() {
        log.info("开始获取后台Cookie字符串");
        return getCookieString(BACKEND_COOKIE_TYPE, BACKEND_DOMAIN, "/get/houtai", new HashSet<>(Arrays.asList(SX_SESSION_ID, OAUTH_TOKEN_REQUEST_STATE,XSRF_TOKEN)));
    }

    public String getBackendAllCookieString() {
        log.info("开始获取后台Cookie字符串");
        return getCookieString(BACKEND_COOKIE_TYPE, BACKEND_DOMAIN, "/get/houtai", null);
    }

    /**
     * 获取后台XSRF-TOKEN值
     *
     * @return XSRF-TOKEN值
     */
    public String getBackendXsrfToken() {
        log.info("开始获取后台XSRF-TOKEN");
        return getCookieValue(BACKEND_COOKIE_TYPE, BACKEND_DOMAIN, XSRF_TOKEN, "/get/houtai");
    }

    /**
     * 获取Cookie字符串（通用方法，支持自定义过滤）
     *
     * @param cookieType Cookie类型
     * @param domain 域名
     * @param path 请求路径
     * @param filterNames 需要过滤的Cookie名称集合，null表示获取所有Cookie
     * @return Cookie字符串
     */
    public String getCookieString(Integer cookieType, String domain, String path, Set<String> filterNames) {
        return getCookieStringInternal(cookieType, domain, path, filterNames);
    }

    /**
     * 内部获取Cookie字符串的方法
     *
     * @param cookieType Cookie类型
     * @param domain 域名
     * @param path 请求路径
     * @param filterNames 需要过滤的Cookie名称集合，null表示获取所有Cookie
     * @return Cookie字符串
     */
    private String getCookieStringInternal(Integer cookieType, String domain, String path, Set<String> filterNames) {
        // 获取第一个服务器配置（按ID排序）
        ClientServerPageReqVO pageReqVO = new ClientServerPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(1);
        
        List<ClientServerDO> servers = clientServerService.getClientServerPage(pageReqVO).getList();
        if (servers == null || servers.isEmpty()) {
            log.error("未找到可用的服务器配置");
            return null;
        }

        ClientServerDO server = servers.get(0);
        log.info("使用服务器配置: {}:{}", server.getIp(), server.getPort());

        // 获取该服务器的Cookie配置
        List<ClientCookieDO> cookies = clientServerService.getClientCookieListByServerId(server.getId());
        Optional<ClientCookieDO> targetCookie = cookies.stream()
                .filter(cookie -> cookieType.equals(cookie.getType()))
                .findFirst();

        if (!targetCookie.isPresent()) {
            log.error("未找到服务器ID {} 的类型为 {} 的Cookie配置", server.getId(), cookieType);
            return null;
        }

        // 构建请求URL
        String url = String.format("http://%s:%d%s", server.getIp(), server.getPort(), path);
        log.info("请求URL: {}", url);

        // 发送HTTP请求获取Cookie数据
        HttpResponse response = HttpRequest.post(url)
                .form("password", targetCookie.get().getPassword())
                .execute();

        if (!response.isOk()) {
            log.error("获取Cookie失败，HTTP状态码: {}", response.getStatus());
            return null;
        }

        String responseBody = response.body();
        if (StrUtil.isBlank(responseBody)) {
            log.error("获取Cookie响应为空");
            return null;
        }

        // 解析JSON响应并提取cookie字符串
        return parseCookieDataToString(responseBody, domain, filterNames);
    }

    /**
     * 解析cookie_data格式的JSON响应为cookie字符串
     *
     * @param responseBody JSON响应体
     * @param targetDomain 目标域名
     * @param filterNames 需要过滤的Cookie名称集合，null表示获取所有Cookie
     * @return Cookie字符串，格式如："name1=value1; name2=value2"
     */
    private String parseCookieDataToString(String responseBody, String targetDomain, Set<String> filterNames) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode cookieDataNode = rootNode.get("cookie_data");
            
            if (cookieDataNode == null) {
                log.warn("响应中未找到cookie_data字段");
                return null;
            }

            StringBuilder cookieString = new StringBuilder();
            
            // 遍历所有域名
            cookieDataNode.fieldNames().forEachRemaining(domain -> {
                // 检查域名是否匹配（支持coupang.com匹配.coupang.com等）
                if (isDomainMatch(domain, targetDomain)) {
                    JsonNode cookiesArray = cookieDataNode.get(domain);
                    if (cookiesArray.isArray()) {
                        for (JsonNode cookieNode : cookiesArray) {
                            String name = cookieNode.get("name").asText();
                            String value = cookieNode.get("value").asText();
                            
                            // 根据过滤条件决定是否添加Cookie
                            boolean shouldAdd = false;
                            if (filterNames == null) {
                                // 获取所有Cookie
                                shouldAdd = true;
                            } else {
                                // 只获取指定名称的Cookie
                                shouldAdd = filterNames.contains(name);
                            }
                            
                            if (shouldAdd) {
                                if (cookieString.length() > 0) {
                                    cookieString.append("; ");
                                }
                                cookieString.append(name).append("=").append(value);
                            }
                        }
                    }
                }
            });

            String result = cookieString.toString();
            if (filterNames == null) {
                log.info("成功解析Cookie字符串（所有Cookie），长度: {} 字符", result.length());
            } else {
                log.info("成功解析Cookie字符串（过滤: {}），长度: {} 字符", filterNames, result.length());
            }
            log.debug("Cookie字符串: {}", result.length() > 200 ? result.substring(0, 200) + "..." : result);
            
            return result.isEmpty() ? null : result;
            
        } catch (Exception e) {
            log.error("解析cookie_data失败", e);
            return null;
        }
    }

    /**
     * 检查域名是否匹配
     *
     * @param cookieDomain Cookie中的域名
     * @param targetDomain 目标域名
     * @return 是否匹配
     */
    private boolean isDomainMatch(String cookieDomain, String targetDomain) {
        if (cookieDomain == null || targetDomain == null) {
            return false;
        }
        
        // 完全匹配
        if (cookieDomain.equals(targetDomain)) {
            return true;
        }
        
        // 检查是否为子域名匹配（如.coupang.com匹配coupang.com）
        if (cookieDomain.startsWith(".") && targetDomain.endsWith(cookieDomain.substring(1))) {
            return true;
        }
        
        // 检查是否为父域名匹配（如coupang.com匹配.coupang.com）
        if (targetDomain.startsWith(".") && cookieDomain.endsWith(targetDomain.substring(1))) {
            return true;
        }
        
        return false;
    }
}
