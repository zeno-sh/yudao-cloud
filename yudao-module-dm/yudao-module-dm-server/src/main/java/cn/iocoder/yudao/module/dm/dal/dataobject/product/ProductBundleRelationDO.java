package cn.iocoder.yudao.module.dm.dal.dataobject.product;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

/**
 * 产品组合关系 DO
 * 
 * 设计原则：只存储关联关系，不冗余存储子产品信息
 * 子产品的详细信息（SKU、名称、单价等）通过 JOIN dm_product_info 表获取
 *
 * @author zeno
 */
@TableName("dm_product_bundle_relation")
@KeySequence("dm_product_bundle_relation_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBundleRelationDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    
    /**
     * 组合产品ID（dm_product_info.id）
     */
    private Long bundleProductId;
    
    /**
     * 子产品ID（dm_product_info.id）
     */
    private Long subProductId;
    
    /**
     * 数量
     */
    private Integer quantity;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 备注
     */
    private String remark;
    
    // 注意：子产品的 SKU、名称、单价等信息不在此表存储
    // 查询时通过 JOIN dm_product_info 表获取实时数据
}

