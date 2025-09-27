package cn.iocoder.yudao.module.dm.dal.dataobject.product;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 产品信息 DO
 *
 * @author zeno
 */
@TableName("dm_product_info")
@KeySequence("dm_product_info_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfoDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * skuId
     */
    private String skuId;
    /**
     * 产品名称
     */
    private String skuName;
    /**
     * 规格说明
     */
    private String specification;
    /**
     * 合并的标志
     */
    private String modelNumber;
    /**
     * 单位
     *
     * 枚举 {@link TODO dm_unit_type 对应的类}
     */
    private String unit;
    /**
     * 售卖状态
     *
     * 枚举 {@link TODO dm_sale_status 对应的类}
     */
    private Integer saleStatus;
    /**
     * 类目ID
     */
    private Long categoryId;
    /**
     * 品牌ID
     */
    private Long brandId;
    /**
     * 标签
     *
     * 枚举 {@link TODO dm_product_flag 对应的类}
     */
    private Integer flagId;
    /**
     * 图片
     */
    private String pictureUrl;
    /**
     * 预估成本价
     */
    private BigDecimal costPrice;
    /**
     * 产品描述
     */
    private String description;
    /**
     * 类目佣金
     */
    private BigDecimal categoryCommission;
    /**
     * 类目佣金Id
     */
    private Long categoryCommissionId;
    /**
     * 目标平台
     *
     * 枚举 {@link TODO dm_platform 对应的类}
     */
    private Integer platform;

}