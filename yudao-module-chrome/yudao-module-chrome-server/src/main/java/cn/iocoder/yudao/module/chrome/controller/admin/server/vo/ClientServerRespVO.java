package cn.iocoder.yudao.module.chrome.controller.admin.server.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - Chrome 插件cookie服务器 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ClientServerRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "9840")
    @ExcelProperty("主键")
    private Integer id;

    @Schema(description = "服务器ip")
    @ExcelProperty("服务器ip")
    private String ip;

    @Schema(description = "端口")
    @ExcelProperty("端口")
    private Integer port;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}