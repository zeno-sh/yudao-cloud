package cn.iocoder.yudao.module.dm.dal.redis;

/**
 * @author: Zeno
 * @createTime: 2024/04/26 13:24
 */
public interface RedisKeyConstants {

    /**
     * 序号的缓存
     *
     * KEY 格式：seq_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    String NO = "dm:seq_no:";

    /**
     * ozon广告Token的缓存
     */
    String OZON_AD_TOKEN_KEY = "dm:ozon_ad_token:%s";
}
