package cn.iocoder.yudao.module.dm.infrastructure.ozon.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author zeno
 * @Date 2024/2/9
 */
@Data
public class ProductOnlineDTO {
    @JSONField(name = "id")
    private Long id;

    @JSONField(name = "name")
    private String name;

    @JSONField(name = "offer_id")
    private String offerId;

    @JSONField(name = "created_at")
    private Date createdAt;

    @JSONField(name = "marketing_price")
    private String marketingPrice;

    @JSONField(name = "price")
    private String price;

    @JSONField(name = "volume_weight")
    private Double volumeWeight;

    @JSONField(name = "primary_image")
    private List<String> primaryImage;

    @JSONField(name = "images")
    private List<String> images;

    @JSONField(name = "is_kgt")
    private Boolean isKgt;

    @JSONField(name = "sources")
    private List<ProductOnlineSourceDTO> sources;

    @JSONField(name = "is_archived")
    // 手动归档
    private Boolean isArchived;

    @JSONField(name = "is_autoarchived")
    // 自动归档
    private Boolean isAutoArchived;

    @JSONField(name = "statuses")
    // 状态
    private ProductOnlineStatusDTO statuses;

    @Deprecated
    private Boolean visible;
}
