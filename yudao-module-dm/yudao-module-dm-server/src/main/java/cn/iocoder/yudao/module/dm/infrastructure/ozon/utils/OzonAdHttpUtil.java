package cn.iocoder.yudao.module.dm.infrastructure.ozon.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.dal.redis.dao.OzonShopRedisDAO;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.constant.OzonConfig;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.OZON_AD_CONFIG_NOT_EXISTS;
import static cn.iocoder.yudao.module.dm.enums.ErrorCodeConstants.OZON_SHOP_MAPPING_NOT_EXISTS;

/**
 * @Author zeno
 * @Date 2024/2/5
 */
@Service
@Slf4j
public class OzonAdHttpUtil {

    @Resource
    private OzonShopRedisDAO ozonShopRedisDAO;
    @Resource
    private OzonShopMappingService dmShopMappingService;

    private static final int TIME_OUT = 30 * 1000;

    public <R> R get(String clientId, String url, Map<String, Object> params, TypeReference<R> typeReference) {
        try {
            log.info("查询Ozon广告接口，url = {}, request = {}", url, JSON.toJSONString(params));
            HttpResponse httpResponse = HttpRequest.get(url)
                    .header("Authorization", "Bearer " + getToken(clientId))
                    .form(params)
                    .timeout(TIME_OUT)
                    .execute();

            if (httpResponse.getStatus() != 200) {
                log.error("查询Ozon广告接口失败，url = {}, httpResponse.getStatus() = {} , message = {}", url, httpResponse.getStatus(), httpResponse.body());
                return null;
            }
            String body = httpResponse.body();
            log.info("查询Ozon广告接口成功，url = {}, body = {}", url, body);

            return JSON.parseObject(body, typeReference);
        } catch (ServiceException e) {
            log.error("查询Ozon广告接口异常", e);
            throw e;
        } catch (Exception e) {
            log.error("查询Ozon广告接口异常", e);
            throw e;
        }
    }

    public String getByPathVariables(String clientId, String url, String pathVariables) {
        try {
            log.info("查询Ozon广告接口，url = {}, request = {}", url, pathVariables);
            HttpResponse httpResponse = HttpRequest.get(url + pathVariables)
                    .header("Authorization", "Bearer " + getToken(clientId))
                    .timeout(TIME_OUT)
                    .execute();

            if (httpResponse.getStatus() != 200) {
                log.error("查询Ozon广告接口失败，url = {}, httpResponse.getStatus() = {} , message = {}", url, httpResponse.getStatus(), httpResponse.body());
                return null;
            }
            String body = httpResponse.body();
            log.info("查询Ozon广告接口成功，url = {}, body = {}", url, body);

            return body;
        } catch (Exception e) {
            log.error("查询Ozon广告接口异常", e);
        }
        return null;
    }

    public <R> R post(String clientId, String url, String request, TypeReference<R> typeReference) {
        log.info("查询Ozon广告接口，url = {}, request = {}", url, request);
        HttpResponse httpResponse = HttpRequest.post(url)
                .header("Authorization", "Bearer " + getToken(clientId))
                .body(request)
                .timeout(TIME_OUT)
                .execute();

        if (httpResponse.getStatus() != 200) {
            log.error("查询Ozon广告接口失败，url = {}, httpResponse.getStatus() = {} , message = {}", url, httpResponse.getStatus(), httpResponse.body());
            return null;
        }
        String body = httpResponse.body();
        log.info("查询Ozon广告接口成功，url = {}, body = {}", url, body);

        return JSON.parseObject(body, typeReference);
    }


    private String getToken(String clientId) throws ServiceException {

        OzonShopMappingDO ozonShopMapping = dmShopMappingService.getOzonShopMappingByClientId(clientId);
        if (null == ozonShopMapping) {
            throw exception(OZON_SHOP_MAPPING_NOT_EXISTS);
        }
        if (StringUtils.isBlank(ozonShopMapping.getAdClientId())) {
            log.error("获取广告主ID失败, 门店={}, 未设置广告Api配置", clientId);
            throw exception(OZON_AD_CONFIG_NOT_EXISTS);
        }

        String cacheToken = ozonShopRedisDAO.get(clientId);
        if (StringUtils.isBlank(cacheToken)) {
            return refreshToken(clientId, ozonShopMapping.getAdClientId(), ozonShopMapping.getAdClientSecret());
        }
        log.info("缓存中获取广告token, 门店={}, token={}", clientId, cacheToken);
        return cacheToken;
    }


    private String refreshToken(String clientId, String adClientId, String adClientSecret) {

        String url = OzonConfig.OZON_AD_TOKEN;
        Map<String, String> params = new HashMap<>();
        params.put("client_id", adClientId);
        params.put("client_secret", adClientSecret);
        params.put("grant_type", "client_credentials");

        try {
            log.info("查询Ozon广告接口，url = {}, request = {}", url, JSON.toJSONString(params));
            HttpResponse httpResponse = HttpRequest.post(url)
                    .body(JSON.toJSONString(params))
                    .timeout(TIME_OUT)
                    .execute();

            if (httpResponse.getStatus() != 200) {
                log.error("查询Ozon广告接口失败，url = {}, httpResponse.getStatus() = {} , message = {}", url, httpResponse.getStatus(), httpResponse.body());
                throw new RuntimeException("查询Ad Token接口失败");
            }
            String body = httpResponse.body();
            log.info("查询Ozon广告接口成功，url = {}, body = {}", url, body);

            JSONObject jsonObject = JSONObject.parseObject(body);
            String accessToken = (String) jsonObject.get("access_token");
            ozonShopRedisDAO.set(clientId, accessToken);
            return accessToken;
        } catch (Exception e) {
            log.error("查询Ozon广告接口异常", e);
            throw new RuntimeException("获取Token失败");
        }
    }
}
