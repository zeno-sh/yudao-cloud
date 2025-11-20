package cn.iocoder.yudao.module.dm.controller.admin.calculation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 利润预测 Excel 导入 Response VO")
@Data
@Builder
public class ProfitCalculationImportRespVO {

    @Schema(description = "创建成功的利润预测编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> createProfitCalculationIds;

    @Schema(description = "更新成功的利润预测编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> updateProfitCalculationIds;

    @Schema(description = "导入失败的数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureList;

} 