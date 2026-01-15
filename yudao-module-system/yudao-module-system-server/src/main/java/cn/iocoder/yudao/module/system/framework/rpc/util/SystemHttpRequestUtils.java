package cn.iocoder.yudao.module.system.framework.rpc.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * System 模块发起 HTTP 请求工具类
 * 
 * 复制自 BpmHttpRequestUtils，用于在微服务场景下，通过 HTTP 直接调用其他服务，避免循环依赖问题。
 *
 * @author 芋道源码
 */
@Slf4j
public class SystemHttpRequestUtils {

    /**
     * 执行 HTTP POST 请求（简化版本，用于事件监听器等场景）
     *
     * @param body 请求体
     * @param url  请求 URL
     */
    public static void executeHttpRequest(Object body, String url) {
        // 1.1 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (TenantContextHolder.getTenantId() != null) {
            headers.add(HEADER_TENANT_ID, String.valueOf(TenantContextHolder.getTenantId()));
        }

        // 2. 发起请求
        RestTemplate restTemplate = SpringUtils.getBean(RestTemplate.class);
        sendHttpRequest(url, headers, body, restTemplate);
    }

    public static void executeHttpRequest(String tenantId,
            String url,
            List<KeyValue<String, String>> headerParams,
            List<KeyValue<String, String>> bodyParams,
            Boolean handleResponse,
            List<KeyValue<String, String>> response) {
        // 1.1 设置请求头
        MultiValueMap<String, String> headers = buildHttpHeaders(tenantId, headerParams);
        // 1.2 设置请求体
        MultiValueMap<String, String> body = buildHttpBody(bodyParams);

        // 2. 发起请求
        RestTemplate restTemplate = SpringUtils.getBean(RestTemplate.class);
        ResponseEntity<String> responseEntity = sendHttpRequest(url, headers, body, restTemplate);

        // 3. 处理返回
        if (Boolean.FALSE.equals(handleResponse)) {
            return;
        }
        // 3.1 判断是否需要解析返回值
        if (responseEntity == null
                || StrUtil.isEmpty(responseEntity.getBody())
                || !responseEntity.getStatusCode().is2xxSuccessful()
                || CollUtil.isEmpty(response)) {
            return;
        }
        // 3.2 解析返回值, 返回值必须符合 CommonResult 规范。
        CommonResult<Map<String, Object>> respResult = JsonUtils.parseObjectQuietly(responseEntity.getBody(),
                new TypeReference<CommonResult<Map<String, Object>>>() {
                });
        if (respResult == null || !respResult.isSuccess()) {
            return;
        }
        // 3.3 获取需要更新的变量
        Map<String, Object> updateVariables = getNeedUpdatedVariablesFromResponse(respResult.getData(), response);
        // 3.4 日志记录更新的变量
        if (CollUtil.isNotEmpty(updateVariables)) {
            log.info("[executeHttpRequest] 从响应中解析到变量: {}", updateVariables);
        }
    }

    public static ResponseEntity<String> sendHttpRequest(String url,
            MultiValueMap<String, String> headers,
            Object body,
            RestTemplate restTemplate) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            log.info("[sendHttpRequest][HTTP 请求，请求头：{}，请求体：{}，响应结果：{}]", headers, body, responseEntity);
        } catch (RestClientException e) {
            log.error("[sendHttpRequest][HTTP 请求，请求头：{}，请求体：{}，请求出错：{}]", headers, body, e.getMessage());
            throw new RuntimeException("HTTP 请求失败: " + e.getMessage(), e);
        }
        return responseEntity;
    }

    public static MultiValueMap<String, String> buildHttpHeaders(String tenantId,
            List<KeyValue<String, String>> headerSettings) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HEADER_TENANT_ID, tenantId);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        addHttpRequestParam(headers, headerSettings);
        return headers;
    }

    public static MultiValueMap<String, String> buildHttpBody(List<KeyValue<String, String>> bodySettings) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        addHttpRequestParam(body, bodySettings);
        return body;
    }

    /**
     * 从请求返回值获取需要更新的变量
     *
     * @param result           请求返回结果
     * @param responseSettings 返回设置
     * @return 需要更新的变量
     */
    public static Map<String, Object> getNeedUpdatedVariablesFromResponse(Map<String, Object> result,
            List<KeyValue<String, String>> responseSettings) {
        Map<String, Object> updateVariables = new HashMap<>();
        if (CollUtil.isEmpty(result)) {
            return updateVariables;
        }
        responseSettings.forEach(responseSetting -> {
            if (StrUtil.isNotEmpty(responseSetting.getKey()) && result.containsKey(responseSetting.getValue())) {
                updateVariables.put(responseSetting.getKey(), result.get(responseSetting.getValue()));
            }
        });
        return updateVariables;
    }

    /**
     * 添加 HTTP 请求参数。请求头或者请求体
     *
     * @param params        HTTP 请求参数
     * @param paramSettings HTTP 请求参数设置
     */
    public static void addHttpRequestParam(MultiValueMap<String, String> params,
            List<KeyValue<String, String>> paramSettings) {
        if (CollUtil.isEmpty(paramSettings)) {
            return;
        }
        paramSettings.forEach(item -> {
            params.add(item.getKey(), item.getValue());
        });
    }

}
