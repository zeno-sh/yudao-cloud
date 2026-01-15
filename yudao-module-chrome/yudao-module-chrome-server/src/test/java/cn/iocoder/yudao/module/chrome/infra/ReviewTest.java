package cn.iocoder.yudao.module.chrome.infra;

import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Proxy;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 高级反爬虫对抗测试类
 * 针对 Coupang 等电商平台的反爬虫机制进行绕过
 * 
 * @author Jax
 */
public class ReviewTest {

    private static final List<String> USER_AGENTS = Arrays.asList(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:121.0) Gecko/20100101 Firefox/121.0"
    );

    private static final List<String> ACCEPT_LANGUAGES = Arrays.asList(
        "ko-KR,ko;q=0.9,en;q=0.8",
        "en-US,en;q=0.9,ko;q=0.8",
        "zh-CN,zh;q=0.9,en;q=0.8,ko;q=0.7",
        "ja-JP,ja;q=0.9,en;q=0.8,ko;q=0.7"
    );

    // 代理池 - 实际使用时需要配置真实代理
    private static final List<Proxy> PROXY_POOL = Arrays.asList(
        Proxy.NO_PROXY
        // new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy1.example.com", 8080)),
        // new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy2.example.com", 8080))
    );

    public static void main(String[] args) {
        AdvancedHttpClient client = new AdvancedHttpClient();
        
        String url = "https://www.coupang.com/next-api/review?productId=8905122229&page=1&size=10&sortBy=ORDER_SCORE_ASC&ratingSummary=true&ratings=&market=";
        
        try {
            String response = client.executeWithRetry(url, 3);
            System.out.println("✅ 请求成功！");
            System.out.println("响应长度: " + response.length());
            System.out.println("响应内容: " + response.substring(0, Math.min(500, response.length())));
        } catch (Exception e) {
            System.err.println("❌ 请求失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 高级 HTTP 客户端，集成多重反爬虫对抗策略
     */
    static class AdvancedHttpClient {
        private final Random random = new Random();
        private int requestCount = 0;

        /**
         * 创建高级配置的 OkHttpClient
         */
        private OkHttpClient createClient() {
            return new OkHttpClient.Builder()
                    // 连接配置
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .callTimeout(120, TimeUnit.SECONDS)
                    
                    // 连接池配置
                    .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
                    
                    // 协议配置 - 强制使用 HTTP/1.1 避免 HTTP/2 问题
                    .protocols(Arrays.asList(Protocol.HTTP_1_1))
                    
                    // SSL 配置 - 信任所有证书（仅测试用）
                    .sslSocketFactory(createTrustAllSSLSocketFactory(), createTrustAllTrustManager())
                    .hostnameVerifier((hostname, session) -> true)
                    
                    // 代理配置
                    .proxy(getRandomProxy())
                    
                    // 拦截器
                    .addInterceptor(new HeaderRandomizerInterceptor())
                    .addInterceptor(new RetryInterceptor(3))
                    .addInterceptor(new DelayInterceptor())
                    // 重定向配置
                    .followRedirects(true)
                    .followSslRedirects(true)
                    
                    .build();
        }

        /**
         * 带重试的请求执行
         */
        public String executeWithRetry(String url, int maxRetries) throws IOException {
            IOException lastException = null;
            
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    System.out.println("🚀 第 " + attempt + " 次尝试请求...");
                    
                    // 每次重试都创建新的客户端，避免连接复用被检测
                    OkHttpClient client = createClient();
                    Request request = buildRequest(url);
                    
                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            String body = response.body().string();
                            System.out.println("✅ 第 " + attempt + " 次尝试成功！状态码: " + response.code());
                            return body;
                        } else {
                            throw new IOException("HTTP " + response.code() + ": " + response.message());
                        }
                    }
                    
                } catch (IOException e) {
                    lastException = e;
                    System.err.println("❌ 第 " + attempt + " 次尝试失败: " + e.getMessage());
                    
                    if (attempt < maxRetries) {
                        // 指数退避延时
                        int delay = (int) (Math.pow(2, attempt) * 1000) + random.nextInt(2000);
                        System.out.println("⏰ 等待 " + delay + "ms 后重试...");
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new IOException("请求被中断", ie);
                        }
                    }
                }
            }
            
            throw new IOException("所有重试都失败了", lastException);
        }

        /**
         * 构建请求
         */
        private Request buildRequest(String url) {
            requestCount++;
            
            return new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("Accept-Language", getRandomAcceptLanguage())
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Pragma", "no-cache")
                    .addHeader("Referer", "https://www.coupang.com/")
                    .addHeader("Origin", "https://www.coupang.com")
                    .addHeader("Sec-Fetch-Dest", "empty")
                    .addHeader("Sec-Fetch-Mode", "cors")
                    .addHeader("Sec-Fetch-Site", "same-origin")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("DNT", "1")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Upgrade-Insecure-Requests", "1")
                    // 动态生成的请求头
                    .addHeader("X-Request-ID", generateRequestId())
                    .addHeader("X-Client-Version", generateClientVersion())
                    .build();
        }

        private String getRandomAcceptLanguage() {
            return ACCEPT_LANGUAGES.get(random.nextInt(ACCEPT_LANGUAGES.size()));
        }

        private Proxy getRandomProxy() {
            return PROXY_POOL.get(random.nextInt(PROXY_POOL.size()));
        }

        private String generateRequestId() {
            return "req_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
        }

        private String generateClientVersion() {
            return "1." + random.nextInt(10) + "." + random.nextInt(100);
        }

        /**
         * 请求头随机化拦截器
         */
        class HeaderRandomizerInterceptor implements Interceptor {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                
                // 随机化 User-Agent
                String userAgent = USER_AGENTS.get(random.nextInt(USER_AGENTS.size()));
                
                // 生成随机的浏览器指纹
                String secChUa = generateSecChUa(userAgent);
                String secChUaPlatform = generateSecChUaPlatform();
                
                Request.Builder builder = original.newBuilder()
                        .header("User-Agent", userAgent)
                        .header("Sec-Ch-Ua", secChUa)
                        .header("Sec-Ch-Ua-Mobile", "?0")
                        .header("Sec-Ch-Ua-Platform", secChUaPlatform);
                
                // 随机添加一些可选头
                if (random.nextBoolean()) {
                    builder.header("Priority", "u=1, i");
                }
                
                if (random.nextBoolean()) {
                    builder.header("Sec-Ch-Ua-Arch", "\"x86\"");
                }
                
                return chain.proceed(builder.build());
            }
            
            private String generateSecChUa(String userAgent) {
                if (userAgent.contains("Chrome")) {
                    int version = 120 + random.nextInt(5);
                    return "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"" + version + "\", \"Google Chrome\";v=\"" + version + "\"";
                } else if (userAgent.contains("Firefox")) {
                    int version = 121 + random.nextInt(3);
                    return "\"Not_A Brand\";v=\"99\", \"Firefox\";v=\"" + version + "\"";
                }
                return "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\"";
            }
            
            private String generateSecChUaPlatform() {
                String[] platforms = {"\"Windows\"", "\"macOS\"", "\"Linux\""};
                return platforms[random.nextInt(platforms.length)];
            }
        }

        /**
         * 重试拦截器
         */
        class RetryInterceptor implements Interceptor {
            private final int maxRetries;
            
            public RetryInterceptor(int maxRetries) {
                this.maxRetries = maxRetries;
            }
            
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                IOException lastException = null;
                
                for (int i = 0; i <= maxRetries; i++) {
                    try {
                        Response response = chain.proceed(request);
                        
                        // 检查是否需要重试
                        if (response.isSuccessful() || !shouldRetry(response.code())) {
                            return response;
                        }
                        
                        response.close();
                        
                        if (i < maxRetries) {
                            System.out.println("🔄 状态码 " + response.code() + "，准备重试...");
                            Thread.sleep(1000 + random.nextInt(2000));
                        }
                        
                    } catch (IOException e) {
                        lastException = e;
                        
                        if (i < maxRetries && shouldRetryOnException(e)) {
                            System.out.println("🔄 网络异常，准备重试: " + e.getMessage());
                            try {
                                Thread.sleep(1000 + random.nextInt(2000));
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                throw new IOException("重试被中断", ie);
                            }
                        } else {
                            throw e;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("请求被中断", e);
                    }
                }
                
                throw lastException != null ? lastException : new IOException("重试次数耗尽");
            }
            
            private boolean shouldRetry(int code) {
                return code == 429 || code == 503 || code == 502 || code == 504;
            }
            
            private boolean shouldRetryOnException(IOException e) {
                String message = e.getMessage().toLowerCase();
                return message.contains("reset") || 
                       message.contains("timeout") || 
                       message.contains("connection") ||
                       message.contains("internal_error");
            }
        }

        /**
         * 延时拦截器 - 模拟人类行为
         */
        class DelayInterceptor implements Interceptor {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // 随机延时 1-3 秒，模拟人类行为
                int delay = 1000 + random.nextInt(2000);
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("延时被中断", e);
                }
                
                return chain.proceed(chain.request());
            }
        }

        /**
         * 创建信任所有证书的 SSL Socket Factory
         */
        private SSLSocketFactory createTrustAllSSLSocketFactory() {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{createTrustAllTrustManager()}, new java.security.SecureRandom());
                return sslContext.getSocketFactory();
            } catch (Exception e) {
                throw new RuntimeException("创建 SSL Socket Factory 失败", e);
            }
        }

        /**
         * 创建信任所有证书的 Trust Manager
         */
        private X509TrustManager createTrustAllTrustManager() {
            return new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
        }

    }
}
