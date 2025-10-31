package cn.iocoder.yudao.module.dm.dal.redis.dao;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.dm.dal.redis.RedisKeyConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author: Zeno
 * @createTime: 2024/04/26 13:28
 */
@Repository
public class DmNoRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 采购计划组号前缀
     */
    public static final String PURCHASE_PLAN_GROUP_NO_PREFIX = "PPG";

    /**
     * 采购计划号前缀
     */
    public static final String PURCHASE_PLAN_NO_PREFIX = "PP";

    /**
     * 采购计划号前缀
     */
    public static final String PURCHASE_PO_NO_PREFIX = "PO";
    /**
     * 付款单前缀
     */
    public static final String FINANCE_PAYMENT_NO_PREFIX = "PV";
    /**
     * 发货计划前缀
     */
    public static final String TRANSPORT_PLAN_NO_PREFIX = "TP";
    /**
     * 供应商代码前缀
     */
    public static final String SUPPLIER_NO_PREFIX = "SU";

    /**
     * 生成序号，使用当前日期，格式为 {PREFIX} + yyyyMMdd + 4 位自增
     * 例如说：QTRK 202109 0001 （没有中间空格）
     *
     * @param prefix 前缀
     * @return 序号
     */
    public String generate(String prefix) {
        // 递增序号
        String noPrefix = prefix + DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        String key = RedisKeyConstants.NO + noPrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        // 设置过期时间
        stringRedisTemplate.expire(key, Duration.ofDays(1L));
        return noPrefix + String.format("%04d", no);
    }
}
