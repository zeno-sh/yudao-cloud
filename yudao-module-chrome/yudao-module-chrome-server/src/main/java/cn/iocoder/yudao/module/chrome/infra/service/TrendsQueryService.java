package cn.iocoder.yudao.module.chrome.infra.service;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.chrome.infra.dto.CoupangTrendsResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 趋势查询服务
 *
 * @author Jax
 */
@Slf4j
@Service
public class TrendsQueryService {

    @Autowired
    private CookieQueryService cookieQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient httpClient;

    // Coupang趋势查询API URL
    private static final String TRENDS_API_URL = "https://wing.coupang.com/tenants/rfm-ss/api/trends/search";

    public TrendsQueryService() {
        this.httpClient = createHttpClient();
    }

    /**
     * 创建配置了TLS的OkHttpClient
     */
    private OkHttpClient createHttpClient() {
        try {
            // 创建信任所有证书的TrustManager
            final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                    
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
                    
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
            };
            
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .build();
        } catch (Exception e) {
            log.error("创建OkHttpClient失败", e);
            return new OkHttpClient();
        }
    }

    /**
     * 生成请求头
     */
    private Map<String, String> generateHeaders(String cookieString) {
        Map<String, String> headers = new HashMap<>();
        
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("accept-language", "zh-HK,zh-TW;q=0.9,zh;q=0.8");
        headers.put("content-type", "application/json");
        headers.put("origin", "https://wing.coupang.com");
        headers.put("priority", "u=1, i");
        headers.put("referer", "https://wing.coupang.com/");
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "same-origin");
        headers.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36");
        headers.put("withcredentials", "true");
        headers.put("x-cp-pt-locale", "zh_CN");
        
        // 获取并设置XSRF-TOKEN
        String xsrfToken = cookieQueryService.getBackendXsrfToken();
        if (xsrfToken != null && !xsrfToken.isEmpty()) {
            headers.put("x-xsrf-token", xsrfToken);
            log.info("已设置x-xsrf-token请求头: {}", xsrfToken);
        } else {
            log.warn("未获取到XSRF-TOKEN，跳过x-xsrf-token请求头设置");
        }
        
        if (cookieString != null && !cookieString.isEmpty()) {
            headers.put("Cookie", cookieString);
        }

        return headers;
    }

    /**
     * 构建类目查询请求体
     */
    private String buildCategoryRequestBody(String query, String categoryId, Integer start, Integer limit) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // 搜索条件
        Map<String, Object> searchCondition = new HashMap<>();
        searchCondition.put("start", start);
        searchCondition.put("limit", limit);
        searchCondition.put("query", query);
        searchCondition.put("sort", new String[]{"BEST_SELLING"});
        
        // 过滤条件
        Map<String, Object> filter = new HashMap<>();
        Map<String, Object> internalCategory = new HashMap<>();
        internalCategory.put("generalFilterType", "Filters");
        internalCategory.put("operator", "AND");
        
        // 通用过滤器
        Map<String, Object> generalFilter = new HashMap<>();
        generalFilter.put("generalFilterType", "DefaultFilter");
        generalFilter.put("field", "INTERNAL_CATEGORY");
        generalFilter.put("values", new String[]{categoryId});
        generalFilter.put("operator", "AND");
        generalFilter.put("exclude", false);
        
        internalCategory.put("generalFilters", new Map[]{generalFilter});
        filter.put("INTERNAL_CATEGORY", internalCategory);
        searchCondition.put("filter", filter);
        
        // 上下文
        Map<String, Object> context = new HashMap<>();
        context.put("bundleId", 62);
        context.put("ip", "127.0.0.1");
        context.put("viewType", "WEB");
        context.put("sourcePage", "Srp");
        context.put("pcid", "unknown");
        context.put("channel", "unknown");
        context.put("userNo", 0);
        context.put("uuid", "");
        context.put("osType", "PC");
        context.put("appVersion", "1.0.0");
        context.put("abTests", null);
        context.put("engineParams", new HashMap<>());
        context.put("filteredAbTests", null);
        context.put("swapSet", null);
        
        searchCondition.put("context", context);
        requestBody.put("searchCondition", searchCondition);
        
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            log.error("构建类目查询请求体失败", e);
            throw new RuntimeException("构建类目查询请求体失败", e);
        }
    }

    /**
     * 构建关键词查询请求体
     */
    private String buildKeywordRequestBody(String query, Integer start, Integer limit) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // 搜索条件
        Map<String, Object> searchCondition = new HashMap<>();
        searchCondition.put("start", start);
        searchCondition.put("limit", limit);
        searchCondition.put("query", query);
        searchCondition.put("sort", new String[]{"BEST_SELLING"});
        searchCondition.put("filter", new HashMap<>());
        
        // 上下文
        Map<String, Object> context = new HashMap<>();
        context.put("bundleId", 62);
        context.put("ip", "127.0.0.1");
        context.put("viewType", "WEB");
        context.put("sourcePage", "Srp");
        context.put("pcid", "unknown");
        context.put("channel", "unknown");
        context.put("userNo", 0);
        context.put("uuid", "");
        context.put("osType", "PC");
        context.put("appVersion", "1.0.0");
        context.put("abTests", null);
        context.put("engineParams", new HashMap<>());
        context.put("filteredAbTests", null);
        context.put("swapSet", null);
        
        searchCondition.put("context", context);
        requestBody.put("searchCondition", searchCondition);
        
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            log.error("构建关键词查询请求体失败", e);
            throw new RuntimeException("构建关键词查询请求体失败", e);
        }
    }

    /**
     * 查询类目趋势
     * 
     * @param query 查询关键词
     * @param categoryId 类目ID
     * @param start 起始位置
     * @param limit 查询数量限制
     * @param cookieString Cookie字符串，可为空
     * @return 趋势查询结果
     */
    public CoupangTrendsResponseDTO queryCategoryTrends(String query, String categoryId, Integer start, Integer limit, String cookieString) {
        long startTime = System.currentTimeMillis();
        log.info("开始查询类目趋势，关键词: {}, 类目ID: {}", query, categoryId);

        // 准备Cookie字符串
        String finalCookieString = prepareCookieString(cookieString);

        try {
            // 构建请求体
            String requestBodyJson = buildCategoryRequestBody(query, categoryId, start, limit);
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, requestBodyJson);
            
            // 构建请求
            Request.Builder requestBuilder = new Request.Builder()
                    .url(TRENDS_API_URL)
                    .post(body);
            
            // 设置请求头
            Map<String, String> headers = generateHeaders(finalCookieString);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
            
            Request request = requestBuilder.build();
            
            log.info("发送类目趋势查询请求，URL: {}", TRENDS_API_URL);
            log.info("请求体: {}", requestBodyJson);
            log.info("cookie:{}", finalCookieString);
            
            // 执行请求
            try (Response response = httpClient.newCall(request).execute()) {
                CoupangTrendsResponseDTO trendsResponse = handleHttpResponse(response);
                
                long queryDuration = System.currentTimeMillis() - startTime;
                if (trendsResponse != null && trendsResponse.getSearchItems() != null) {
                    log.info("类目趋势查询成功，返回{}个商品，耗时{}ms", 
                        trendsResponse.getSearchItems().size(), queryDuration);
                } else {
                    log.warn("类目趋势查询完成，但未获取到有效数据，耗时{}ms", queryDuration);
                }
                
                return trendsResponse;
            }
        } catch (Exception e) {
            log.error("类目趋势查询请求失败: {}", e.getMessage(), e);
            throw new RuntimeException("类目趋势查询请求失败", e);
        }
    }

    /**
     * 查询关键词趋势
     * 
     * @param query 查询关键词
     * @param start 起始位置
     * @param limit 查询数量限制
     * @param cookieString Cookie字符串，可为空
     * @return 趋势查询结果
     */
    public CoupangTrendsResponseDTO queryKeywordTrends(String query, Integer start, Integer limit, String cookieString) {
        long startTime = System.currentTimeMillis();
        log.info("开始查询关键词趋势，关键词: {}", query);

        // 准备Cookie字符串
        String finalCookieString = prepareCookieString(cookieString);

        try {
            // 构建请求体
            String requestBodyJson = buildKeywordRequestBody(query, start, limit);
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, requestBodyJson);
            
            // 构建请求
            Request.Builder requestBuilder = new Request.Builder()
                    .url(TRENDS_API_URL)
                    .post(body);
            
            // 设置请求头
            Map<String, String> headers = generateHeaders(finalCookieString);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
            
            Request request = requestBuilder.build();
            
            log.info("发送关键词趋势查询请求，URL: {}", TRENDS_API_URL);
            log.debug("请求体: {}", requestBodyJson);
            
            // 执行请求
            try (Response response = httpClient.newCall(request).execute()) {
                CoupangTrendsResponseDTO trendsResponse = handleHttpResponse(response);
                
                long queryDuration = System.currentTimeMillis() - startTime;
                if (trendsResponse != null && trendsResponse.getSearchItems() != null) {
                    log.info("关键词趋势查询成功，返回{}个商品，耗时{}ms", 
                        trendsResponse.getSearchItems().size(), queryDuration);
                } else {
                    log.warn("关键词趋势查询完成，但未获取到有效数据，耗时{}ms", queryDuration);
                }
                
                return trendsResponse;
            }
        } catch (Exception e) {
            log.error("关键词趋势查询请求失败: {}", e.getMessage(), e);
            throw new RuntimeException("关键词趋势查询请求失败", e);
        }
    }

    /**
     * 准备Cookie字符串
     */
    private String prepareCookieString(String cookieString) {
        if (cookieString != null && !cookieString.trim().isEmpty()) {
            log.debug("使用传入的Cookie字符串");
            return cookieString.trim();
        } else {
            // 尝试获取后台Cookie
            String backendCookie = cookieQueryService.getBackendCookieString();
            if (backendCookie != null && !backendCookie.isEmpty()) {
                log.debug("自动获取后台Cookie成功");
                return backendCookie;
            } else {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.PRODUCT_SALES_QUERY_COOKIE_ERROR);
            }
        }
    }

    /**
     * 处理HTTP响应
     */
    private CoupangTrendsResponseDTO handleHttpResponse(Response response) {
        try {
            int statusCode = response.code();
            
            if (statusCode == 403) {
                log.warn("被反爬虫拦截，状态码: {}", statusCode);
                return null;
            }
            
            if (statusCode == 429) {
                log.warn("请求被限流，状态码: {}", statusCode);
                return null;
            }
            
            if (statusCode != 200) {
                String responseBody = safeGetResponseBody(response);
                log.error("HTTP请求失败，状态码: {}, 响应: {}", statusCode, 
                    responseBody != null ? responseBody.substring(0, Math.min(responseBody.length(), 200)) : "无法获取响应内容");
                return null;
            }
            
            String responseBody = safeGetResponseBody(response);
            if (responseBody == null) {
                log.error("无法获取响应内容");
                return null;
            }
            
            log.debug("API响应成功，响应长度: {} bytes", responseBody.length());
            
            return parseJsonResponse(responseBody);
        } catch (Exception e) {
            log.error("处理HTTP响应时发生异常", e);
            return null;
        }
    }

    /**
     * 安全获取响应内容
     */
    private String safeGetResponseBody(Response response) {
        try {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                return responseBody.string();
            }
            return null;
        } catch (Exception e) {
            log.warn("获取响应内容失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 安全解析JSON响应
     */
    private CoupangTrendsResponseDTO parseJsonResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, CoupangTrendsResponseDTO.class);
        } catch (Exception e) {
            log.error("解析JSON响应失败: {}", e.getMessage());
            return null;
        }
    }
}
