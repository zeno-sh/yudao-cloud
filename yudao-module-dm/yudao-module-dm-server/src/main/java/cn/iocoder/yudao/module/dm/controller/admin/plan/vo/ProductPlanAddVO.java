package cn.iocoder.yudao.module.dm.controller.admin.plan.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author: Zeno
 * @createTime: 2024/04/23 17:49
 */
@Data
public class ProductPlanAddVO {

    private String planName;

    @NotEmpty(message = "SKU不能为空")
    private String productId;

    @NotEmpty(message = "货代头程报价不能为空")
    private String forwarderPrice;

    private String adRate;

    private String lostRate;
}
