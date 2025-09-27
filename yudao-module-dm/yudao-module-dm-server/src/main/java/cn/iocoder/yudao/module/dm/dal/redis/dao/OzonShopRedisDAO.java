package cn.iocoder.yudao.module.dm.dal.redis.dao;

import cn.iocoder.yudao.module.dm.dal.redis.RedisKeyConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author: Zeno
 * @createTime: 2024/06/24 12:03
 */
@Repository
public class OzonShopRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public String get(String clientId) {
        String redisKey = formatKey(clientId);
        return stringRedisTemplate.opsForValue().get(redisKey);
    }

    public void set(String clientId, String token) {
        String redisKey = formatKey(clientId);
        stringRedisTemplate.opsForValue().set(redisKey, token, 1600L, TimeUnit.SECONDS);
    }

    private static String formatKey(String clientId) {
        return String.format(RedisKeyConstants.OZON_AD_TOKEN_KEY, clientId);
    }
}
