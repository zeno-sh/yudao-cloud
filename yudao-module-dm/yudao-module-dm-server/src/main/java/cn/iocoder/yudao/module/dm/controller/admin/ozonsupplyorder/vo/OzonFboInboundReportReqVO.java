package cn.iocoder.yudao.module.dm.controller.admin.ozonsupplyorder.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - Ozon FBO进仓报表分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OzonFboInboundReportReqVO extends PageParam {

    @Schema(description = "月份，格式为yyyy-MM")
    private String month;

    @Schema(description = "本地商品ID")
    private Long productId;

    @Schema(description = "客户端编号数组")
    private String[] clientIds;
} 