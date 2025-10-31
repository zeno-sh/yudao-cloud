package cn.iocoder.yudao.module.dm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 组合产品关系 DTO
 * 用于RPC接口数据传输
 *
 * @author zeno
 */
@Data
public class ProductBundleRelationDTO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 组合产品ID
     */
    private Long bundleProductId;

    /**
     * 子产品ID
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
     * 子产品SKU ID
     */
    private String subSkuId;

    /**
     * 子产品SKU名称
     */
    private String subSkuName;

    /**
     * 备注
     */
    private String remark;
}

