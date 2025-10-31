package cn.iocoder.yudao.module.dm.controller.admin.warehouse;

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

import cn.iocoder.yudao.module.dm.controller.admin.warehouse.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.warehouse.FbsPushOrderLogDO;
import cn.iocoder.yudao.module.dm.service.warehouse.FbsPushOrderLogService;

@Tag(name = "管理后台 - 海外仓推单记录")
@RestController
@RequestMapping("/dm/fbs-push-order-log")
@Validated
public class FbsPushOrderLogController {

    @Resource
    private FbsPushOrderLogService fbsPushOrderLogService;

    @PostMapping("/create")
    @Operation(summary = "创建海外仓推单记录")
    @PreAuthorize("@ss.hasPermission('dm:fbs-push-order-log:create')")
    public CommonResult<Long> createFbsPushOrderLog(@Valid @RequestBody FbsPushOrderLogSaveReqVO createReqVO) {
        return success(fbsPushOrderLogService.createFbsPushOrderLog(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新海外仓推单记录")
    @PreAuthorize("@ss.hasPermission('dm:fbs-push-order-log:update')")
    public CommonResult<Boolean> updateFbsPushOrderLog(@Valid @RequestBody FbsPushOrderLogSaveReqVO updateReqVO) {
        fbsPushOrderLogService.updateFbsPushOrderLog(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除海外仓推单记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:fbs-push-order-log:delete')")
    public CommonResult<Boolean> deleteFbsPushOrderLog(@RequestParam("id") Long id) {
        fbsPushOrderLogService.deleteFbsPushOrderLog(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得海外仓推单记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:fbs-push-order-log:query')")
    public CommonResult<FbsPushOrderLogRespVO> getFbsPushOrderLog(@RequestParam("id") Long id) {
        FbsPushOrderLogDO fbsPushOrderLog = fbsPushOrderLogService.getFbsPushOrderLog(id);
        return success(BeanUtils.toBean(fbsPushOrderLog, FbsPushOrderLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得海外仓推单记录分页")
    @PreAuthorize("@ss.hasPermission('dm:fbs-push-order-log:query')")
    public CommonResult<PageResult<FbsPushOrderLogRespVO>> getFbsPushOrderLogPage(@Valid FbsPushOrderLogPageReqVO pageReqVO) {
        PageResult<FbsPushOrderLogDO> pageResult = fbsPushOrderLogService.getFbsPushOrderLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, FbsPushOrderLogRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出海外仓推单记录 Excel")
    @PreAuthorize("@ss.hasPermission('dm:fbs-push-order-log:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFbsPushOrderLogExcel(@Valid FbsPushOrderLogPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<FbsPushOrderLogDO> list = fbsPushOrderLogService.getFbsPushOrderLogPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "海外仓推单记录.xls", "数据", FbsPushOrderLogRespVO.class,
                        BeanUtils.toBean(list, FbsPushOrderLogRespVO.class));
    }

}