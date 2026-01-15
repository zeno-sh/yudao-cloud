package cn.iocoder.yudao.module.chrome.controller.admin.server;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.chrome.controller.admin.server.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientServerDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.server.ClientCookieDO;
import cn.iocoder.yudao.module.chrome.service.server.ClientServerService;
import cn.iocoder.yudao.module.chrome.service.plugin.PluginSessionService;
import cn.iocoder.yudao.framework.websocket.core.plugin.service.PluginCommandService;

@Tag(name = "管理后台 - Chrome 插件cookie服务器")
@RestController
@RequestMapping("/chrome/client-server")
@Validated
public class ClientServerController {

    @Resource
    private ClientServerService clientServerService;

    @Resource
    private PluginSessionService pluginSessionService;

    @Resource
    private PluginCommandService pluginCommandService;

    @PostMapping("/create")
    @Operation(summary = "创建Chrome 插件cookie服务器")
    @PreAuthorize("@ss.hasPermission('chrome:client-server:create')")
    public CommonResult<Integer> createClientServer(@Valid @RequestBody ClientServerSaveReqVO createReqVO) {
        return success(clientServerService.createClientServer(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新Chrome 插件cookie服务器")
    @PreAuthorize("@ss.hasPermission('chrome:client-server:update')")
    public CommonResult<Boolean> updateClientServer(@Valid @RequestBody ClientServerSaveReqVO updateReqVO) {
        clientServerService.updateClientServer(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除Chrome 插件cookie服务器")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('chrome:client-server:delete')")
    public CommonResult<Boolean> deleteClientServer(@RequestParam("id") Integer id) {
        clientServerService.deleteClientServer(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得Chrome 插件cookie服务器")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('chrome:client-server:query')")
    public CommonResult<ClientServerRespVO> getClientServer(@RequestParam("id") Integer id) {
        ClientServerDO clientServer = clientServerService.getClientServer(id);
        return success(BeanUtils.toBean(clientServer, ClientServerRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得Chrome 插件cookie服务器分页")
    @PreAuthorize("@ss.hasPermission('chrome:client-server:query')")
    public CommonResult<PageResult<ClientServerRespVO>> getClientServerPage(@Valid ClientServerPageReqVO pageReqVO) {
        PageResult<ClientServerDO> pageResult = clientServerService.getClientServerPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ClientServerRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出Chrome 插件cookie服务器 Excel")
    @PreAuthorize("@ss.hasPermission('chrome:client-server:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportClientServerExcel(@Valid ClientServerPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ClientServerDO> list = clientServerService.getClientServerPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "Chrome 插件cookie服务器.xls", "数据", ClientServerRespVO.class,
                        BeanUtils.toBean(list, ClientServerRespVO.class));
    }

    // ==================== 子表（Chrome 插件cookie配置） ====================

    @GetMapping("/client-cookie/list-by-server-id")
    @Operation(summary = "获得Chrome 插件cookie配置列表")
    @Parameter(name = "serverId", description = "服务器id")
    @PreAuthorize("@ss.hasPermission('chrome:client-server:query')")
    public CommonResult<List<ClientCookieDO>> getClientCookieListByServerId(@RequestParam("serverId") Integer serverId) {
        return success(clientServerService.getClientCookieListByServerId(serverId));
    }

    @GetMapping("/client-cookie/page")
    @Operation(summary = "获得Chrome 插件cookie配置分页(含WebSocket在线状态)")
    @PreAuthorize("@ss.hasPermission('chrome:client-server:query')")
    public CommonResult<PageResult<ClientCookieDO>> getClientCookiePage(@RequestParam(value = "serverId", required = false) Integer serverId,
                                                                         @RequestParam(value = "account", required = false) String account,
                                                                         @RequestParam(value = "online", required = false) Boolean online,
                                                                         @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                                         @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return success(pluginSessionService.getPluginSessionPage(serverId, account, online, pageNo, pageSize));
    }

    // ==================== WebSocket 插件会话管理 ====================

    @PostMapping("/client-cookie/send-command")
    @Operation(summary = "向指定账号发送命令")
    @PreAuthorize("@ss.hasPermission('chrome:client-server:update')")
    public CommonResult<Boolean> sendCommandToPlugin(@RequestParam("account") @NotEmpty(message = "账号不能为空") String account,
                                                      @RequestParam("commandType") @NotEmpty(message = "命令类型不能为空") String commandType,
                                                      @RequestParam(value = "commandData", required = false) String commandData) {
        return success(pluginCommandService.sendCommand(account, commandType, commandData));
    }

    @PostMapping("/client-cookie/broadcast-command")
    @Operation(summary = "广播命令到所有插件")
    @PreAuthorize("@ss.hasPermission('chrome:client-server:update')")
    public CommonResult<Boolean> broadcastCommandToPlugins(@RequestParam("commandType") @NotEmpty(message = "命令类型不能为空") String commandType,
                                                            @RequestParam(value = "commandData", required = false) String commandData) {
        pluginCommandService.broadcastCommand(commandType, commandData);
        return success(true);
    }

}