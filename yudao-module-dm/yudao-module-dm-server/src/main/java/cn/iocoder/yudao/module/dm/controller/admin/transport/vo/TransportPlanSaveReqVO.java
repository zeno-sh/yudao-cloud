package cn.iocoder.yudao.module.dm.controller.admin.transport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.iocoder.yudao.module.dm.dal.dataobject.transport.TransportPlanItemDO;

@Schema(description = "管理后台 - 头程计划新增/修改 Request VO")
@Data
public class TransportPlanSaveReqVO {

    @Schema(description = "发货计划", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "运输状态", example = "2")
    private Integer transportStatus;

    @Schema(description = "海外仓入库单号", example = "7794")
    private List<String> overseaLocationCheckinId;

    @Schema(description = "货代公司")
    private String forwarder;

    @Schema(description = "报价", example = "25376")
    private BigDecimal offerPrice;

    @Schema(description = "币种")
    private Integer currency;

    @Schema(description = "结算状态", example = "2")
    private String settleStatus;

    @Schema(description = "账单（人民币）", example = "8462")
    private BigDecimal billPrice;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "发运日期")
    private LocalDateTime despatchDate;

    @Schema(description = "预计抵达日期")
    private LocalDateTime arrivalDate;

    @Schema(description = "实际抵达日期")
    private LocalDateTime finishedDate;

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "28027")
    private Long id;

    @Schema(description = "文件")
    private List<String> fileUrls;

    @Schema(description = "头程计划明细列表")
    @Size(min = 1, message = "头程计划明细列表不能为空")
    private List<TransportPlanItemDO> transportPlanItems;

}