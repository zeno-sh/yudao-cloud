package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 计算元数据
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 计算元数据")
@Data
@Builder
public class CalculationMetadata {
    
    @Schema(description = "计算任务ID", example = "abc123")
    private String taskId;
    
    @Schema(description = "计算时间", example = "2024-01-01 10:00:00")
    private LocalDateTime calculationTime;
    
    @Schema(description = "计算版本", example = "2.0")
    private String calculationVersion;
    
    @Schema(description = "使用的汇率", example = "{\"RUB_CNY\": \"0.075\"}")
    private Map<String, String> exchangeRates;
    
    @Schema(description = "计算精度", example = "4")
    private Integer precision;
    
    @Schema(description = "数据来源", example = "OZON_API")
    private String dataSource;
    
    @Schema(description = "计算配置标识", example = "config_v2_standard")
    private String configVersion;
    
    @Schema(description = "数据完整性标识", example = "COMPLETE")
    private String dataIntegrity;
    
    @Schema(description = "警告信息")
    private String[] warnings;
    
    @Schema(description = "计算类型", example = "signed_order")
    private String calculationType;
    
    @Schema(description = "订单号", example = "04394143-0055-1")
    private String postingNumber;
    
    @Schema(description = "订单类型", example = "20")
    private Integer orderType;
    
    @Schema(description = "操作类型", example = "ClientReturnAgentOperation")
    private String operationType;
    
    @Schema(description = "原始订单号", example = "04394143-0055")
    private String orderNumber;
    
    @Schema(description = "利润指标")
    private ProfitMetrics profitMetrics;
} 