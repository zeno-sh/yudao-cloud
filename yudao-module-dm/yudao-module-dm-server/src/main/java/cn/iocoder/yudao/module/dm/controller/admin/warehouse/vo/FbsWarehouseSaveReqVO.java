package cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseAuthDO;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsWarehouseMappingDO;

@Schema(description = "管理后台 - 海外仓仓库新增/修改 Request VO")
@Data
public class FbsWarehouseSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30848")
    private Long id;

    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "仓库名称不能为空")
    private String name;

    @Schema(description = "城市", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "城市不能为空")
    private String city;

    @Schema(description = "地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "地址不能为空")
    private String address;

    @Schema(description = "联系人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "联系人不能为空")
    private String contactPerson;

    @Schema(description = "电话", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "电话不能为空")
    private String phone;

    @Schema(description = "体积重系数")
    private Integer volumetricWeightFactor;

    @Schema(description = "备注", example = "22942")
    private String remark;

    @Schema(description = "海外仓授权信息")
    private FbsWarehouseAuthDO fbsWarehouseAuth;

    @Schema(description = "海外仓平台仓映射列表")
    private List<FbsWarehouseMappingDO> fbsWarehouseMappings;

}