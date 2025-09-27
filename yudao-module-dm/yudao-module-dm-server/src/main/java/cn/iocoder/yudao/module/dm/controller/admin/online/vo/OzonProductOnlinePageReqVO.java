package cn.iocoder.yudao.module.dm.controller.admin.online.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 在线商品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OzonProductOnlinePageReqVO extends PageParam {

    @Schema(description = "门店", example = "10269")
    private String[] clientIds;

    @Schema(description = "平台货号", example = "22077")
    private String offerId;

    @Schema(description = "本地产品ID", example = "17933")
    private Long dmProductId;

    @Schema(description = "状态")
    private Integer status;

}