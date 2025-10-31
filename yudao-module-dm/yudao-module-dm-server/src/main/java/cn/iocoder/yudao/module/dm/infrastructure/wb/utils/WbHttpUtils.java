package cn.iocoder.yudao.module.dm.infrastructure.wb.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.response.OzonHttpResponse;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.request.WbHttpBaseRequest;
import cn.iocoder.yudao.module.dm.infrastructure.wb.dto.response.WbHttpBaseResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: Zeno
 * @createTime: 2024/07/10 23:18
 */
@Component
@Slf4j
public class WbHttpUtils {

    public <Q extends WbHttpBaseRequest, R extends WbHttpBaseResponse> R post(String url, Q request, TypeReference<R> typeReference) {
        log.info("查询 WB 接口，url = {}, request = {}", url, JSON.toJSONString(request));

        try {
            HttpResponse httpResponse = HttpRequest.post(url)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", request.getToken())
                    .body(JSON.toJSONString(request))
                    .execute();

            if (httpResponse.getStatus() != 200) {
                log.error("查询 WB 接口失败，url = {}, httpResponse.getStatus() = {} , message = {}", url, httpResponse.getStatus(), httpResponse.body());
                return null;
            }
            String body = httpResponse.body();
            log.info("查询 WB 接口成功，url = {}, body = {}", url, body);
            return JSON.parseObject(body, typeReference.getType());
        } catch (Exception e) {
            log.error("查询 WB 接口异常", e);
        }
        return null;
    }

    public <R extends WbHttpBaseResponse> R get(String token, String url, Map<String, Object> params, TypeReference<R> typeReference) {
        try {
            log.info("查询 WB 接口，url = {}, request = {}", url, JSON.toJSONString(params));
            HttpResponse httpResponse = HttpRequest.get(url)
                    .header("accept", "application/json")
                    .header("Authorization", token)
                    .form(params)
                    .execute();


            if (httpResponse.getStatus() != 200) {
                log.error("查询 WB 接口失败，url = {}, httpResponse.getStatus() = {} , message = {}", url, httpResponse.getStatus(), httpResponse.body());
                return null;
            }
            String body = httpResponse.body();
            log.info("查询 WB 接口成功，url = {}, body = {}", url, body);

            return JSON.parseObject(body, typeReference);
        } catch (Exception e) {
            log.error("查询 WB 接口异常", e);
        }
        return null;
    }

    public <R> R get2(String token, String url, Map<String, Object> params, TypeReference<R> typeReference) {
        try {
            log.info("查询 WB 接口，url = {}, request = {}", url, JSON.toJSONString(params));
            HttpRequest httpRequest = HttpRequest.get(url)
                    .header("accept", "application/json")
                    .header("Authorization", token);

            if (MapUtils.isNotEmpty(params)) {
                httpRequest =  httpRequest.form(params);
            }

            HttpResponse httpResponse = httpRequest.execute();

            if (httpResponse.getStatus() != 200) {
                log.error("查询 WB 接口失败，url = {}, httpResponse.getStatus() = {} , message = {}", url, httpResponse.getStatus(), httpResponse.body());
                return null;
            }
            String body = httpResponse.body();
            log.info("查询 WB 接口成功，url = {}, body = {}", url, body);

            return JSON.parseObject(body, typeReference);
        } catch (Exception e) {
            log.error("查询 WB 接口异常", e);
        }
        return null;
    }

}
