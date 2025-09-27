package cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

@Schema(description = "管理后台 - 海外仓仓库 Response VO")
@Data
@ExcelIgnoreUnannotated
public class FbsWarehouseRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30848")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("仓库名称")
    private String name;

    @Schema(description = "城市", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("城市")
    private String city;

    @Schema(description = "地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("地址")
    private String address;

    @Schema(description = "联系人", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("联系人")
    private String contactPerson;

    @Schema(description = "电话", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("电话")
    private String phone;

    @Schema(description = "体积重系数")
    @ExcelProperty("体积重系数")
    private Integer volumetricWeightFactor;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "备注", example = "22942")
    @ExcelProperty("备注")
    private String remark;

}