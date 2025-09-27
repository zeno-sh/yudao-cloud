package cn.iocoder.yudao.module.dm.controller.admin.transport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 头程计划明细查询 Request VO")
@Data
public class TransportPlanDetailReqVO {

    @Schema(description = "产品ID列表")
    private List<Long> productIds;

    @Schema(description = "运输状态列表")
    private List<String> transportStatus;

    @Schema(description = "海外仓订单号列表")
    private List<String> overseaLocationCheckinIds;
} 