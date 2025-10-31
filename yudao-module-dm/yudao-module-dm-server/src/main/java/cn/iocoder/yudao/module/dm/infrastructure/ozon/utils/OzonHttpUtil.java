package cn.iocoder.yudao.module.dm.infrastructure.ozon.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.request.HttpBaseRequest;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.OzonHttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author zeno
 * @Date 2024/1/30
 */
@Service
@Slf4j
public class OzonHttpUtil<Q extends HttpBaseRequest> {

    public <R> OzonHttpResponse<R> post(String url, Q request, TypeReference<OzonHttpResponse<R>> typeReference) {

        try {
            log.info("查询Ozon接口，url = {}, request = {}", url, JSON.toJSONString(request));
            HttpResponse httpResponse = HttpRequest.post(url)
                    .header("Client-Id", request.getClientId())
                    .header("Api-Key", request.getApiKey())
                    .body(JSON.toJSONString(request))
                    .execute();

            if (httpResponse.getStatus() != 200) {
                log.error("查询Ozon接口失败，url = {}, httpResponse.getStatus() = {} , message = {}", url, httpResponse.getStatus(), httpResponse.body());
                return null;
            }
            String body = httpResponse.body();
            log.info("查询Ozon接口成功，url = {}, body = {}", url, body);

            return JSON.parseObject(body, typeReference.getType());
        } catch (Exception e) {
            log.error("查询Ozon接口异常", e);
        }
        return null;
    }

    public <R> R postDirect(String url, Q request, TypeReference<R> typeReference) {
        try {
            log.info("查询Ozon接口，url = {}, request = {}", url, JSON.toJSONString(request));
            HttpResponse httpResponse = HttpRequest.post(url)
                    .header("Client-Id", request.getClientId())
                    .header("Api-Key", request.getApiKey())
                    .body(JSON.toJSONString(request))
                    .execute();

            if (httpResponse.getStatus() != 200) {
                log.error("查询Ozon接口失败，url = {}, httpResponse.getStatus() = {} , message = {}", url, httpResponse.getStatus(), httpResponse.body());
                return null;
            }
            String body = httpResponse.body();
            log.info("查询Ozon接口成功，url = {}, body = {}", url, body);

            return JSON.parseObject(body, typeReference.getType());
        } catch (Exception e) {
            log.error("查询Ozon接口异常", e);
        }
        return null;
    }
}
