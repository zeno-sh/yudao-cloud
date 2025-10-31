package cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

/**
 * @author: Zeno
 * @createTime: 2024/06/04 14:22
 */
@Schema(description = "管理后台 - 进度明细")
@Data
@ToString(callSuper = true)
public class PurchasePlanItemProcessItemVO {

    /**
     * 干系人
     */
    private String name;
    /**
     * 时间
     */
    private String dateTime;
    /**
     * 文案
     */
    private String content;
}
