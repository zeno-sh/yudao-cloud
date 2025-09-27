package cn.iocoder.yudao.module.dm.dto;

import cn.iocoder.yudao.module.dm.enums.FbsPlanformEnum;
import lombok.Data;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/08/27 10:35
 */
@Data
public class FbsPushOrderRequest {

    /**
     * 授权信息
     */
    private AuthInfoDTO authInfo;
    /**
     * 门店ID
     */
    private String clientId;
    /**
     * 平台订单ID
     */
    private String orderId;
    /**
     * 发货编号
     */
    private String postingNumber;
    /**
     * 发货条码
     */
    private String barcode;
    /**
     * 平台
     */
    private FbsPlanformEnum platform;
    /**
     * 订单详情
     */
    private List<FbsPushOrderItemDTO> items;
}
