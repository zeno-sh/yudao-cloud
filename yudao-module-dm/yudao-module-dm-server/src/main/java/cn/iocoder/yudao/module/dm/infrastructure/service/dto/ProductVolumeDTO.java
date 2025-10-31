package cn.iocoder.yudao.module.dm.infrastructure.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author: Zeno
 * @createTime: 2024/08/05 16:30
 */
@Data
public class ProductVolumeDTO {

    /**
     * 货号
     */
    private String offerId;
    /**
     * 平台SkuId
     */
    private String platformSkuId;
    /**
     * 本地SkuId
     */
    private String skuId;

    /**
     * dm_product_id - 在 SKU 查询模式下使用
     */
    private Long dmProductId;

    /**
     * 小计
     */
    private Integer total;
    /**
     * 均值
     */
    private Integer avg;
    /**
     * 销量统计
     * key：日期范围
     * value：销量
     */
    private Map<String, Integer> volumeMap;
    /**
     * 在线商品图片
     */
    private String image;
    /**
     * 门店 ID (保留用于 SHOP_TYPE 查询)
     */
    private String clientId;
    /**
     * 门店名称 (保留用于 SHOP_TYPE 查询)
     */
    private String shopName;

    /**
     * 关联的门店列表 (用于 SKU_TYPE 查询)
     */
    private List<ShopInfo> shops;

    /**
     * 平台
     */
    private Integer platform; // 注意：如果一个产品关联多个门店，这个平台字段可能需要调整或移除，或者放在 ShopInfo 里

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopInfo {
        private String clientId;
        private String shopName;
        private Integer platform; // 将平台信息移到这里更合适
    }
}
