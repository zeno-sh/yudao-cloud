package cn.iocoder.yudao.module.chrome.controller.admin.credits;

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

import cn.iocoder.yudao.module.chrome.controller.admin.credits.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.credits.UserCreditsDO;
import cn.iocoder.yudao.module.chrome.service.credits.UserCreditsService;

@Tag(name = "管理后台 - 用户积分账户")
@RestController
@RequestMapping("/chrome/user-credits")
@Validated
public class UserCreditsController {

    @Resource
    private UserCreditsService userCreditsService;

    @PostMapping("/create")
    @Operation(summary = "创建用户积分账户")
    @PreAuthorize("@ss.hasPermission('chrome:user-credits:create')")
    public CommonResult<Long> createUserCredits(@Valid @RequestBody UserCreditsSaveReqVO createReqVO) {
        return success(userCreditsService.createUserCredits(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户积分账户")
    @PreAuthorize("@ss.hasPermission('chrome:user-credits:update')")
    public CommonResult<Boolean> updateUserCredits(@Valid @RequestBody UserCreditsSaveReqVO updateReqVO) {
        userCreditsService.updateUserCredits(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户积分账户")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('chrome:user-credits:delete')")
    public CommonResult<Boolean> deleteUserCredits(@RequestParam("id") Long id) {
        userCreditsService.deleteUserCredits(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户积分账户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('chrome:user-credits:query')")
    public CommonResult<UserCreditsRespVO> getUserCredits(@RequestParam("id") Long id) {
        UserCreditsDO userCredits = userCreditsService.getUserCredits(id);
        return success(BeanUtils.toBean(userCredits, UserCreditsRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户积分账户分页")
    @PreAuthorize("@ss.hasPermission('chrome:user-credits:query')")
    public CommonResult<PageResult<UserCreditsRespVO>> getUserCreditsPage(@Valid UserCreditsPageReqVO pageReqVO) {
        PageResult<UserCreditsDO> pageResult = userCreditsService.getUserCreditsPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UserCreditsRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户积分账户 Excel")
    @PreAuthorize("@ss.hasPermission('chrome:user-credits:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportUserCreditsExcel(@Valid UserCreditsPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UserCreditsDO> list = userCreditsService.getUserCreditsPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "用户积分账户.xls", "数据", UserCreditsRespVO.class,
                        BeanUtils.toBean(list, UserCreditsRespVO.class));
    }

}