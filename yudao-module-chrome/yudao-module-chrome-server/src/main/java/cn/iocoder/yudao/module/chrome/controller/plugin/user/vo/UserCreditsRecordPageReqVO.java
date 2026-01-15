package cn.iocoder.yudao.module.chrome.controller.plugin.user.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "Chrome插件 - 用户积分记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserCreditsRecordPageReqVO extends PageParam {

    @Schema(description = "交易类型（10充值 20消费 30赠送 40重置 50API调用失败 60API调用无数据）", example = "20")
    private Integer transactionType;

    @Schema(description = "业务类型（消费时关联功能类型）", example = "1")
    private Integer businessType;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
