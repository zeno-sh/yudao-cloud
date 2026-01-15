package cn.iocoder.yudao.module.chrome.infra.service;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.chrome.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.chrome.infra.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import okhttp3.*;
import java.util.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 产品评论查询服务
 *
 * @author Jax
 */
@Slf4j
@Service
public class ProductReviewQueryService {

    @Autowired
    private CookieQueryService cookieQueryService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient httpClient;

    // Coupang评论API基础URL - 修正为正确的next-api路径
    private static final String REVIEW_API_BASE_URL = "https://www.coupang.com/next-api/review";
    private static final String BASE_URL = "https://www.coupang.com";
    
    // 用户代理字符串列表，模拟不同的Chrome浏览器
    private static final List<String> USER_AGENTS = Arrays.asList(
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    );

    public ProductReviewQueryService() {
        this.httpClient = createHttpClient();
    }

    /**
     * 创建配置了TLS的OkHttpClient，用于对抗检测
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
     * 生成对抗检测的请求头，基于成功的Postman代码
     */
    private Map<String, String> generateAntiDetectionHeaders(String referer) {
        Map<String, String> headers = new HashMap<>();
        
        // 基于成功的Postman代码的请求头配置
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,ru;q=0.7");
        headers.put("priority", "u=1, i");
        headers.put("sec-ch-ua", "\"Not;A=Brand\";v=\"99\", \"Google Chrome\";v=\"139\", \"Chromium\";v=\"139\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "\"macOS\"");
        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "cors");
        headers.put("sec-fetch-site", "same-origin");
        headers.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
        
        if (referer != null && !referer.isEmpty()) {
            headers.put("referer", referer);
        } else {
            headers.put("referer", "https://www.coupang.com/");
        }

        return headers;
    }

    /**
     * 构建评论API请求URL，按照Python示例代码的参数格式
     */
    private String buildReviewApiUrl(Long productId, Integer page, Integer size, String sortBy) {
        StringBuilder url = new StringBuilder(REVIEW_API_BASE_URL);
        url.append("?productId=").append(productId);
        url.append("&page=").append(page);
        url.append("&size=").append(size);
        url.append("&sortBy=").append(sortBy != null ? sortBy : "ORDER_SCORE_ASC");
        url.append("&ratingSummary=true");
        url.append("&ratings="); // 所有评分
        url.append("&market="); // 市场为空
        
        return url.toString();
    }

    /**
     * 请求单页评论数据（统一的HTTP请求方法）
     */
    private CoupangReviewResponseDTO requestSinglePage(Long productId, int page, int size, String sortBy, String cookieString) {
        String url = buildReviewApiUrl(productId, page, size, sortBy);
        log.info("请求评论数据 - 产品ID: {}, 页码: {}, URL: {}", productId, page, url);
        
        try {
            // 直接构建请求，参考测试类的成功写法
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("accept", "application/json, text/plain, */*")
                    .addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,ru;q=0.7")
                    .addHeader("priority", "u=1, i")
                    .addHeader("referer", "https://www.coupang.com/")
                    .addHeader("sec-ch-ua", "\"Not;A=Brand\";v=\"99\", \"Google Chrome\";v=\"139\", \"Chromium\";v=\"139\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("sec-ch-ua-platform", "\"macOS\"")
                    .addHeader("sec-fetch-dest", "empty")
                    .addHeader("sec-fetch-mode", "cors")
                    .addHeader("sec-fetch-site", "same-origin")
                    .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36")
                    .addHeader("Cookie", cookieString != null ? cookieString : "")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return handleHttpResponse(response);
            }
        } catch (Exception e) {
            log.error("HTTP请求执行失败: {}", e.getMessage(), e);
            return null;
        }
    }
    

    
    /**
     * 处理HTTP响应，统一异常处理逻辑
     */
    private CoupangReviewResponseDTO handleHttpResponse(Response response) {
        try {
            int statusCode = response.code();
            
            // 处理特殊状态码
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
            
            // 解析成功响应
            String responseBody = safeGetResponseBody(response);
            if (responseBody == null) {
                log.error("无法获取响应内容");
                return null;
            }
            
            log.debug("API响应成功，响应长度: {} bytes", responseBody.length());
            
            CoupangReviewResponseDTO result = parseJsonResponse(responseBody);
            if (result == null) {
                return null;
            }
            
            // 检查API响应码
            if (!"RET0000".equals(result.getRCode())) {
                log.warn("API返回错误码: {}, 消息: {}", result.getRCode(), result.getRMessage());
                return null;
            }
            
            return result;
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
    private CoupangReviewResponseDTO parseJsonResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, CoupangReviewResponseDTO.class);
        } catch (Exception e) {
            log.error("解析JSON响应失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 查询产品评论
     * 
     * @param queryParams 查询参数
     * @param cookieString Cookie字符串，可为空
     * @return 查询结果
     */
    public CoupangReviewResponseDTO queryProductReviews(ReviewQueryParamsDTO queryParams, String cookieString) {
        long startTime = System.currentTimeMillis();
        log.info("开始查询产品评论，产品ID: {}, 查询模式: {}", queryParams.getProductId(), queryParams.getQueryMode());

        // 参数验证
        if (queryParams == null || queryParams.getProductId() == null) {
            log.error("查询参数不能为空");
            return null;
        }

        // 准备Cookie字符串
        String finalCookieString = prepareCookieString(cookieString);

        // 根据查询模式执行相应的查询逻辑
        switch (queryParams.getQueryMode()) {
            case DEFAULT:
            case ALL:
                return queryReviewsDirectly(queryParams, finalCookieString, startTime);
            case TIME_RANGE:
                return queryReviewsWithTimeFilter(queryParams, finalCookieString, startTime);
            case EARLIEST:
                return queryEarliestReviews(queryParams, finalCookieString, startTime);
            default:
                log.error("不支持的查询模式: {}", queryParams.getQueryMode());
                return null;
        }
    }

    /**
     * 准备Cookie字符串，如果传入为空则使用成功的Cookie示例
     */
    private String prepareCookieString(String cookieString) {
        // 如果传入了Cookie字符串，直接使用
        if (cookieString != null && !cookieString.trim().isEmpty()) {
            log.debug("使用传入的Cookie字符串");
            return cookieString.trim();
        } else {
            // 使用成功的Cookie示例（从Postman代码中提取）
            String successfulCookie = "x-coupang-target-market=KR; x-coupang-accept-language=ko-KR; PCID=17549616453407215840903; sid=766c6862cffe448da56f94a7c7968be58ed24fd1; MARKETID=17549616453407215840903; AK_COOKIE_REQ=https://www.coupang.com/; AK_COOKIE_UIP=65.20.82.220; AK_COOKIE_REF=18.1e092e17.1755701184.f743f24; bm_ss=ab8e18ef4e; bm_so=55188DBBCCCB8580DA0CB73811ED5DFAC09BC5FF149700B9DF42285280676162~YAAQBfAgF/dvfuuYAQAArQo19gSeNL3qG323sTrO3z4sMVKtGTA3mZRxvfrbbDJxZnuCW9ITv2Or9XhyxFid3zVygDzxv+rqbut03jSbWeT9vB5L34f/K/9MAkLwJwIkw4du5bowFs5T+qmMrXitdF4cnEEIxqiBx+NVuUB93cVbo5Mc+QMCP/F0X4LTmgWpK1ciLDgy0VLrjHaktvNgdRjRzRvqM1DnI8OxVxnBq+EdsT2Sl2ksC0Xtlg3Xa3a3IFRv5WDmI6N2yfLxElzlpJ2PK4cQalGmJJTC6akBTDOZC3qYRSaXJC+TbIKvEKTbj4E4XT1BopdIdJVpftKKSWXCTzBMeqs1T4prSnv/aSI2y1h1qkEzM4WGkIR6ocbtnUc0Nq/R173x7cveZ3NaEqmbrJ2m3Xf+Ol4dHerQFsJo6608VGQHhR948cizHBaQPrK9fmWgFwJzy3aPU3E1fzw=; bm_lso=55188DBBCCCB8580DA0CB73811ED5DFAC09BC5FF149700B9DF42285280676162~YAAQBfAgF/dvfuuYAQAArQo19gSeNL3qG323sTrO3z4sMVKtGTA3mZRxvfrbbDJxZnuCW9ITv2Or9XhyxFid3zVygDzxv+rqbut03jSbWeT9vB5L34f/K/9MAkLwJwIkw4du5bowFs5T+qmMrXitdF4cnEEIxqiBx+NVuUB93cVbo5Mc+QMCP/F0X4LTmgWpK1ciLDgy0VLrjHaktvNgdRjRzRvqM1DnI8OxVxnBq+EdsT2Sl2ksC0Xtlg3Xa3a3IFRv5WDmI6N2yfLxElzlpJ2PK4cQalGmJJTC6akBTDOZC3qYRSaXJC+TbIKvEKTbj4E4XT1BopdIdJVpftKKSWXCTzBMeqs1T4prSnv/aSI2y1h1qkEzM4WGkIR6ocbtnUc0Nq/R173x7cveZ3NaEqmbrJ2m3Xf+Ol4dHerQFsJo6608VGQHhR948cizHBaQPrK9fmWgFwJzy3aPU3E1fzw=^1756477329478; bm_mi=3D7B53EEE2A321F5FE614F6DEE7FE4BF~YAAQBfAgFxFzfuuYAQAAQhk19hwcSNh2lLYjpI/UrnEH+0ey1KKvTwgKbrtyBikl3Foislppq5uHuf/jPqzwmUliKpwdcR26+x+Ev1+5EDGxO+FedhKPS0FegVkv9Rk/Ru1FeQMZLPeKP8vUehrvJKn3oxsPApbxx0UcYPk9ul9SFCqDz8YDVreQv2yvIx+4hleAocNwMArcapjQgZMhfqo5fYwr4FEtbxjDlzlGMLTjsGJzntX4gV5aMd4pd8YJb3+9WyVeUVE5T3wjtkVh10ZAhmKe/NQDjckHQAEFGfcwd6pXLyflQLh7iw1Jz2AlUB2Sp+ECLsk=~1; bm_sc=4~1~108197086~YAAQBfAgFxNzfuuYAQAAQhk19gWDzhqXqjvo6rkdBdtR5vE+bjk9DqG+zJVg7Dvs3KonT7P+KJPtnNYofsv45doyyBrccDnxkKFdx5E15HivKflnK/IBTsH/rjN+XzhhKsZn2rJk3Z4ucXEU69X/Jpn5cO7KeaH";
            log.debug("使用成功的Cookie示例");
            return successfulCookie;
        }
    }
    
    /**
     * 直接查询：DEFAULT和ALL模式，直接透传API数据
     */
    private CoupangReviewResponseDTO queryReviewsDirectly(ReviewQueryParamsDTO queryParams, String cookieString, long startTime) {
        int page = queryParams.getPage() != null ? queryParams.getPage() : 1;
        log.info("执行直接查询，产品ID: {}, 页码: {}", queryParams.getProductId(), page);
        
        CoupangReviewResponseDTO response = requestSinglePage(queryParams.getProductId(), page, 
            queryParams.getSize(), queryParams.getSortBy(), cookieString);
        
        if (response != null && response.getRData() != null && response.getRData().getPaging() != null) {
            List<CoupangReviewResponseDTO.ReviewContent> reviews = response.getRData().getPaging().getContents();
            long queryDuration = System.currentTimeMillis() - startTime;
            log.info("直接查询完成，第{}页获取{}条评论，耗时{}ms", 
                page, reviews != null ? reviews.size() : 0, queryDuration);
        }
        
        return response;
    }
    
    /**
     * 最早评论查询：获取最后一页最早数据
     */
    private CoupangReviewResponseDTO queryEarliestReviews(ReviewQueryParamsDTO queryParams, String cookieString, long startTime) {
        log.info("执行最早评论查询，产品ID: {}", queryParams.getProductId());
        
        // 第一步：获取第一页数据，了解总页数
        CoupangReviewResponseDTO firstResponse = requestSinglePage(queryParams.getProductId(), 1, 
            queryParams.getSize(), "DATE_DESC", cookieString);
        
        if (firstResponse == null || firstResponse.getRData() == null) {
            return null;
        }
        
        int totalPages = firstResponse.getRData().getPaging().getTotalPage();
        log.info("总页数: {}, 准备获取最后一页的最早评论", totalPages);
        
        // 第二步：获取最后一页的评论（最早的评论）
        CoupangReviewResponseDTO lastResponse = requestSinglePage(queryParams.getProductId(), totalPages, 
            queryParams.getSize(), "DATE_DESC", cookieString);
        
        if (lastResponse == null || lastResponse.getRData() == null) {
            return null;
        }
        
        List<CoupangReviewResponseDTO.ReviewContent> lastPageReviews = 
            lastResponse.getRData().getPaging().getContents();
        
        if (lastPageReviews != null && !lastPageReviews.isEmpty()) {
            // 按创建时间排序，获取最早的评论
            CoupangReviewResponseDTO.ReviewContent earliestReview = lastPageReviews.stream()
                .min(Comparator.comparingLong(r -> r.getCreatedAt() != null ? r.getCreatedAt() : 0L))
                .orElse(lastPageReviews.get(0));
            
            // 只保留最早的一条评论
            List<CoupangReviewResponseDTO.ReviewContent> earliestReviews = new ArrayList<>();
            earliestReviews.add(earliestReview);
            lastResponse.getRData().getPaging().setContents(earliestReviews);
            
            long queryDuration = System.currentTimeMillis() - startTime;
            log.info("最早评论查询完成，获取最早评论，创建时间: {}, 耗时{}ms", 
                earliestReview.getCreatedAt(), queryDuration);
        }
        
        return lastResponse;
    }

    /**
     * 时间范围查询：过滤数据并修改hasNext字段
     */
    private CoupangReviewResponseDTO queryReviewsWithTimeFilter(ReviewQueryParamsDTO queryParams, String cookieString, long startTime) {
        int currentPage = queryParams.getPage() != null ? queryParams.getPage() : 1;
        
        log.info("执行时间范围查询，产品ID: {}, 页码: {}, 时间范围: {} - {}", 
            queryParams.getProductId(), currentPage, queryParams.getStartTime(), queryParams.getEndTime());
        
        CoupangReviewResponseDTO response = requestSinglePage(queryParams.getProductId(), currentPage, 
            queryParams.getSize(), queryParams.getSortBy(), cookieString);
        
        if (response == null || response.getRData() == null) {
            return null;
        }
        
        List<CoupangReviewResponseDTO.ReviewContent> pageReviews = response.getRData().getPaging().getContents();
        if (pageReviews == null) {
            pageReviews = new ArrayList<>();
        }
        
        // 时间范围过滤
        List<CoupangReviewResponseDTO.ReviewContent> filteredReviews = new ArrayList<>();
        for (CoupangReviewResponseDTO.ReviewContent review : pageReviews) {
            Long reviewTime = review.getCreatedAt() != null ? review.getCreatedAt() : review.getReviewAt();
            if (reviewTime != null && 
                reviewTime >= queryParams.getStartTime() && 
                reviewTime <= queryParams.getEndTime()) {
                filteredReviews.add(review);
            }
        }
        
        // 更新过滤后的评论列表
        response.getRData().getPaging().setContents(filteredReviews);
        
        // 如果当前页没有匹配数据，设置hasNext为false
        if (filteredReviews.isEmpty()) {
            response.getRData().getPaging().setIsNext(false);
        }
        
        long queryDuration = System.currentTimeMillis() - startTime;
        log.info("时间范围查询完成，第{}页匹配{}条评论，耗时{}ms", 
            currentPage, filteredReviews.size(), queryDuration);
        
        return response;
    }

}
