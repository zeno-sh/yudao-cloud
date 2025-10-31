package cn.iocoder.yudao.module.dm.dto;

import lombok.Data;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/08/28 20:53
 */
@Data
public class FbsStockRequest {

    /**
     * 海外仓Sku
     */
    private String fbsSku;
    /**
     * 海外仓Sku列表
     */
    private List<String> fbsSkus;
    /**
     * 授权信息
     */
    private AuthInfoDTO authInfo;
}
