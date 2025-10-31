package cn.iocoder.yudao.module.dm.controller.admin.profitreport.vo.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 日期范围（限制31天内）
 *
 * @author Jax
 */
@Schema(description = "管理后台 - 日期范围")
@Data
public class DateRange {
    
    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-01")
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    
    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-01-31")
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
    
    @AssertTrue(message = "时间跨度不能超过31天")
    public boolean isValidRange() {
        if (startDate == null || endDate == null) {
            return true; // 让@NotNull处理null值
        }
        return ChronoUnit.DAYS.between(startDate, endDate) <= 31;
    }
    
    @AssertTrue(message = "结束日期不能早于开始日期")
    public boolean isValidOrder() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }
} 