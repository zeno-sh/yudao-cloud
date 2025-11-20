package cn.iocoder.yudao.module.dm.controller.admin.calculation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "利润计算导入结果 VO")
@Data
@Builder
public class ProfitCalculationImportResultVO {
    
    @Schema(description = "成功导入并计算的记录数量")
    private Integer successCount;
    
    @Schema(description = "失败的记录数量")
    private Integer failureCount;
    
    @Schema(description = "成功创建的利润计算记录ID列表")
    private List<Long> successIds;
    
    @Schema(description = "失败记录详情，key为行号，value为失败原因")
    private Map<String, String> failureDetails;
    
    @Schema(description = "成功计算的结果列表")
    private List<ProfitCalculationResultVO> calculationResults;
} 