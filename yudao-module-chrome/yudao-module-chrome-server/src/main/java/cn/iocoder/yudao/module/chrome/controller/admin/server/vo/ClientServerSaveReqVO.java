package cn.iocoder.yudao.module.chrome.controller.admin.server.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientCookieDO;

@Schema(description = "管理后台 - Chrome 插件cookie服务器新增/修改 Request VO")
@Data
public class ClientServerSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "9840")
    private Integer id;

    @Schema(description = "服务器ip")
    private String ip;

    @Schema(description = "端口")
    private Integer port;

    @Schema(description = "Chrome 插件cookie配置列表")
    private List<ClientCookieDO> clientCookies;

}