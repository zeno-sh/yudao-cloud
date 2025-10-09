package cn.iocoder.yudao.module.dm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 产品简单信息 DTO
 * 用于RPC接口数据传输
 *
 * @author Jax
 */
@Data
public class ProductSimpleInfoDTO implements Serializable {

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 图片
     */
    private String image;

    /**
     * SKU ID
     */
    private String skuId;

    /**
     * SKU名称
     */
    private String skuName;
}
