package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

/**
 * @author: Zeno
 * @createTime: 2024/10/07 16:24
 */
@Schema(description = "管理后台 - Ozon订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProfitPageReqVO extends PageParam {

    @Schema(description = "平台门店id", example = "22208")
    private String[] clientIds;

    @Schema(description = "货号")
    private String offerId;

    @Schema(description = "接单时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private String[] inProcessAt;
}
