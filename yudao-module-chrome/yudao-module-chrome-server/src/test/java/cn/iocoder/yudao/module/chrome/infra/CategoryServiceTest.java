package cn.iocoder.yudao.module.chrome.infra;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CategoryServiceTest {
    
    @Test
    public void testTrendsSearch() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"searchCondition\":{\"filter\":{\"INTERNAL_CATEGORY\":{\"generalFilters\":[{\"field\":\"INTERNAL_CATEGORY\",\"values\":[\"111637\"],\"exclude\":false,\"generalFilterType\":\"DefaultFilter\",\"operator\":\"AND\"}],\"generalFilterType\":\"Filters\",\"operator\":\"AND\"}},\"query\":\"골프의류\",\"start\":0,\"limit\":100,\"context\":{\"appVersion\":\"1.0.0\",\"filteredAbTests\":null,\"abTests\":null,\"ip\":\"127.0.0.1\",\"userNo\":0,\"swapSet\":null,\"bundleId\":62,\"channel\":\"unknown\",\"uuid\":\"\",\"sourcePage\":\"Srp\",\"engineParams\":{},\"viewType\":\"WEB\",\"osType\":\"PC\",\"pcid\":\"unknown\"},\"sort\":[\"BEST_SELLING\"]}}");
        Request request = new Request.Builder()
                .url("https://wing.coupang.com/tenants/rfm-ss/api/trends/search")
                .method("POST", body)
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("accept-language", "zh-HK,zh-TW;q=0.9,zh;q=0.8")
                .addHeader("content-type", "application/json")
                .addHeader("origin", "https://wing.coupang.com")
                .addHeader("priority", "u=1, i")
                .addHeader("referer", "https://wing.coupang.com/")
                .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"137\", \"Chromium\";v=\"137\", \"Not/A)Brand\";v=\"24\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"macOS\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36")
                .addHeader("withcredentials", "true")
                .addHeader("x-cp-pt-locale", "zh_CN")
                .addHeader("x-xsrf-token", "6bb48b48-11e7-49d9-a4b8-19692bc5ab9a")
                .addHeader("Cookie", "XSRF-TOKEN=6bb48b48-11e7-49d9-a4b8-19692bc5ab9a; sxSessionId=NzIyYzE2N2EtNWU2NC00MDU2LTljZTAtYjc0YzQ1MzM1ODVm;")
                .build();
        
        Response response = client.newCall(request).execute();
        
        // 打印响应结果
        System.out.println("Response Code: " + response.code());
        System.out.println("Response Body: " + response.body().string());
        
        response.close();
    }
}
