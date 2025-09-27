package cn.iocoder.yudao.module.dm.controller.admin.shop.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ozon店铺分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OzonShopMappingPageReqVO extends PageParam {

    @Schema(description = "平台")
    private Integer platform;

    @Schema(description = "门店名称", example = "芋艿")
    private String shopName;

    @Schema(description = "平台门店Id", example = "31283")
    private String clientId;

    @Schema(description = "授权状态", example = "10")
    private Integer authStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}