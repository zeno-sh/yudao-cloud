package cn.iocoder.yudao.module.chrome.infra.service;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.chrome.infra.dto.CoupangSalesResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import okhttp3.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 产品销量查询服务
 *
 * @author Jax
 */
@Slf4j
@Service
public class ProductSalesQueryService {

    @Autowired
    private CookieQueryService cookieQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient httpClient;

    // Coupang销量查询API URL
    private static final String SALES_API_URL = "https://wing.coupang.com/tenants/seller-web/pre-matching/search";

    public ProductSalesQueryService() {
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
        headers.put("sec-fetch-storage-access", "active");
        headers.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36");
        headers.put("x-rfm-portal2-request-id", "GtPkj3b_Rpr0WcudXOJiU");
        headers.put("x-xsrf-token", "6bb48b48-11e7-49d9-a4b8-19692bc5ab9a");
        
        if (cookieString != null && !cookieString.isEmpty()) {
            cookieString = cookieString + ";locale=zh_CN;wing-locale=zh_CN;";
            headers.put("Cookie", cookieString);
        }

        return headers;
    }

    /**
     * 构建请求体
     */
    private String buildRequestBody(String keyword) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("keyword", keyword);
        requestBody.put("excludedProductIds", new String[]{});
        requestBody.put("searchPage", 0);
        requestBody.put("searchOrder", "DEFAULT");
        requestBody.put("sortType", "BEST_SELLING");
        
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            log.error("构建请求体失败", e);
            throw new RuntimeException("构建请求体失败", e);
        }
    }

    /**
     * 查询产品销量
     * 
     * @param keyword 关键词(sellerProductId)
     * @param cookieString Cookie字符串，可为空
     * @return 匹配的产品销量信息
     */
    public CoupangSalesResponseDTO.ProductSalesInfo queryProductSales(String keyword, String cookieString) {
        long startTime = System.currentTimeMillis();
        log.info("开始查询产品销量，关键词: {}", keyword);

        // 准备Cookie字符串
        String finalCookieString = prepareCookieString(cookieString);

        try {
            // 构建请求体
            String requestBodyJson = buildRequestBody(keyword);
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, requestBodyJson);
            
            // 构建请求
            Request.Builder requestBuilder = new Request.Builder()
                    .url(SALES_API_URL)
                    .post(body);
            
            // 设置请求头
            Map<String, String> headers = generateHeaders(finalCookieString);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
            
            Request request = requestBuilder.build();
            
            log.info("发送销量查询请求，URL: {}, 请求体: {}", SALES_API_URL, requestBodyJson);
            
            // 执行请求
            try (Response response = httpClient.newCall(request).execute()) {
                CoupangSalesResponseDTO salesResponse = handleHttpResponse(response);
                
                if (salesResponse != null && salesResponse.getResult() != null) {
                    // 查找匹配的产品
                    CoupangSalesResponseDTO.ProductSalesInfo matchedProduct = findMatchingProduct(
                        salesResponse.getResult(), keyword);
                    
                    long queryDuration = System.currentTimeMillis() - startTime;
                    if (matchedProduct != null) {
                        log.info("销量查询成功，找到匹配产品ID: {}, 销量: {}, 耗时{}ms", 
                            matchedProduct.getProductId(), matchedProduct.getSalesLast28d(), queryDuration);
                    } else {
                        log.warn("销量查询完成，但未找到匹配的产品，关键词: {}, 耗时{}ms", keyword, queryDuration);
                    }
                    
                    return matchedProduct;
                }
                
                return null;
            }
        } catch (Exception e) {
            log.error("销量查询请求失败: {}", e.getMessage(), e);
            throw new RuntimeException("销量查询请求失败", e);
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
    private CoupangSalesResponseDTO handleHttpResponse(Response response) {
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
    private CoupangSalesResponseDTO parseJsonResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, CoupangSalesResponseDTO.class);
        } catch (Exception e) {
            log.error("解析JSON响应失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 查找匹配的产品
     */
    private CoupangSalesResponseDTO.ProductSalesInfo findMatchingProduct(
            List<CoupangSalesResponseDTO.ProductSalesInfo> products, String keyword) {
        
        if (products == null || products.isEmpty()) {
            return null;
        }
        
        // 如果只有一个结果，直接返回
        if (products.size() == 1) {
            return products.get(0);
        }
        
        // 如果有多个结果，尝试匹配sellerProductId（即productId）
        try {
            Long targetProductId = Long.parseLong(keyword);
            for (CoupangSalesResponseDTO.ProductSalesInfo product : products) {
                if (product.getProductId() != null && product.getProductId().equals(targetProductId)) {
                    return product;
                }
            }
        } catch (NumberFormatException e) {
            log.warn("关键词不是有效的数字ID: {}", keyword);
        }
        
        // 如果没有精确匹配，返回第一个结果
        return products.get(0);
    }
}
