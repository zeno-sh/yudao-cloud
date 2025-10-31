package cn.iocoder.yudao.module.dm.controller.admin.logistics.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.logistics.FbsFeeDetailDO;

@Schema(description = "管理后台 - 收费项目新增/修改 Request VO")
@Data
public class FbsFeeServicesSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23711")
    private Long id;

    @Schema(description = "海外仓ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25540")
    @NotNull(message = "海外仓ID不能为空")
    private Long warehouseId;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "项目名称不能为空")
    private String name;

    @Schema(description = "项目标签")
    private Integer tag;

    @Schema(description = "收费明细列表")
    private List<FbsFeeDetailDO> fbsFeeDetails;

}