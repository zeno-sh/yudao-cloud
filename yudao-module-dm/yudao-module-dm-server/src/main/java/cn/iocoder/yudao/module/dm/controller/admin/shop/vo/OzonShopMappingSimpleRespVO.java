package cn.iocoder.yudao.module.dm.controller.admin.shop.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - ozon店铺 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OzonShopMappingSimpleRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4508")
    @ExcelProperty("主键")
    private Integer id;

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "平台", converter = DictConvert.class)
    @DictFormat("dm_platform") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer platform;

    @Schema(description = "平台名称")
    private String platformName;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("门店名称")
    private String shopName;

    @Schema(description = "平台门店Id", requiredMode = Schema.RequiredMode.REQUIRED, example = "31283")
    @ExcelProperty("平台门店Id")
    private String clientId;

    @Schema(description = "分组")
    private List<OzonShopMappingSimpleRespVO> childrenList;
}