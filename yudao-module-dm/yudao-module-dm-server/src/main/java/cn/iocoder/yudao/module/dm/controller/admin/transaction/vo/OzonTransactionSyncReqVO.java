package cn.iocoder.yudao.module.dm.controller.admin.transaction.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author: Zeno
 * @createTime: 2024/07/05 11:50
 */
@Data
public class OzonTransactionSyncReqVO {

    @Schema(description = "平台门店id", example = "22208")
    private String[] clientIds;

    @Schema(description = "接单时间")
    @NotNull(message = "接单时间不能为空")
    private String[] operationDate;
}
