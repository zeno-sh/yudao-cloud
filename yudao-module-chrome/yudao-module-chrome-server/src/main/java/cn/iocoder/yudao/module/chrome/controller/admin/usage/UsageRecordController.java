package cn.iocoder.yudao.module.chrome.controller.admin.usage;

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

import cn.iocoder.yudao.module.chrome.controller.admin.usage.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.usage.UsageRecordDO;
import cn.iocoder.yudao.module.chrome.service.usage.UsageRecordService;

@Tag(name = "管理后台 - Chrome使用记录")
@RestController
@RequestMapping("/chrome/usage-record")
@Validated
public class UsageRecordController {

    @Resource
    private UsageRecordService usageRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建Chrome使用记录")
    @PreAuthorize("@ss.hasPermission('chrome:usage-record:create')")
    public CommonResult<Long> createUsageRecord(@Valid @RequestBody UsageRecordSaveReqVO createReqVO) {
        return success(usageRecordService.createUsageRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新Chrome使用记录")
    @PreAuthorize("@ss.hasPermission('chrome:usage-record:update')")
    public CommonResult<Boolean> updateUsageRecord(@Valid @RequestBody UsageRecordSaveReqVO updateReqVO) {
        usageRecordService.updateUsageRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除Chrome使用记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('chrome:usage-record:delete')")
    public CommonResult<Boolean> deleteUsageRecord(@RequestParam("id") Long id) {
        usageRecordService.deleteUsageRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得Chrome使用记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('chrome:usage-record:query')")
    public CommonResult<UsageRecordRespVO> getUsageRecord(@RequestParam("id") Long id) {
        UsageRecordDO usageRecord = usageRecordService.getUsageRecord(id);
        return success(BeanUtils.toBean(usageRecord, UsageRecordRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得Chrome使用记录分页")
    @PreAuthorize("@ss.hasPermission('chrome:usage-record:query')")
    public CommonResult<PageResult<UsageRecordRespVO>> getUsageRecordPage(@Valid UsageRecordPageReqVO pageReqVO) {
        PageResult<UsageRecordDO> pageResult = usageRecordService.getUsageRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UsageRecordRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出Chrome使用记录 Excel")
    @PreAuthorize("@ss.hasPermission('chrome:usage-record:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportUsageRecordExcel(@Valid UsageRecordPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UsageRecordDO> list = usageRecordService.getUsageRecordPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "Chrome使用记录.xls", "数据", UsageRecordRespVO.class,
                        BeanUtils.toBean(list, UsageRecordRespVO.class));
    }

}