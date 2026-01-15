package cn.iocoder.yudao.module.chrome.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Chrome 功能类型枚举
 * 对应数据库中 chrome_usage_record 表的 feature_type 字段
 *
 * @author Jax
 */
@AllArgsConstructor
@Getter
public enum FeatureTypeEnum {

    /**
     * 商品采集 - 对应数据库值 10
     */
    PRODUCT_COLLECT(10, "商品采集"),

    /**
     * 排名采集 - 对应数据库值 20
     */
    RANKING_COLLECT(20, "排名采集"),
    
    /**
     * 评论采集 - 对应数据库值 30
     */
    COMMENT_COLLECT(30, "评论采集"),
    
    /**
     * 销量采集 - 对应数据库值 40
     */
    SALES_COLLECT(40, "销量采集"),
    
    /**
     * 趋势采集 - 对应数据库值 50
     */
    TREND_COLLECT(50, "趋势采集"),
    
    /**
     * 类目分析 - 对应数据库值 60
     */
    CATEGORY_ANALYSIS(60, "类目分析"),
    
    /**
     * 飞书导出 - 对应数据库值 70
     */
    FEISHU_EXPORT(70, "飞书导出"),
    
    /**
     * Excel导出 - 对应数据库值 80
     */
    EXCEL_EXPORT(80, "Excel导出");

    /**
     * 功能类型代码（对应数据库值）
     */
    private final Integer type;
    
    /**
     * 功能名称
     */
    private final String name;

    /**
     * 根据类型代码获取枚举
     *
     * @param type 类型代码
     * @return 功能类型枚举
     */
    public static FeatureTypeEnum valueOf(Integer type) {
        if (type == null) {
            return null;
        }
        for (FeatureTypeEnum featureType : values()) {
            if (featureType.getType().equals(type)) {
                return featureType;
            }
        }
        throw new IllegalArgumentException("未知的功能类型代码: " + type);
    }

}