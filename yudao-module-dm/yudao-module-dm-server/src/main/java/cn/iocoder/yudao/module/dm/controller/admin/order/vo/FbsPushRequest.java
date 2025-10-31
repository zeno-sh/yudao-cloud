package cn.iocoder.yudao.module.dm.controller.admin.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/09/01 17:37
 */
@Data
public class FbsPushRequest {

    /**
     * 本地订单号
     */
    private List<Long> orderIds;
}
