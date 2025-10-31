package cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author: Zeno
 * @createTime: 2024/06/04 14:22
 */
@Schema(description = "管理后台 - 进度条状态")
@Data
@ToString(callSuper = true)
public class PurchasePlanItemProcessVO {

    /**
     * 进度标题
     */
    private String stepTitle;
    /**
     * 状态：success/error/wait/finish/process
     */
    private String status;
    /**
     * 子项
     */
    private List<PurchasePlanItemProcessItemVO> items;
}
