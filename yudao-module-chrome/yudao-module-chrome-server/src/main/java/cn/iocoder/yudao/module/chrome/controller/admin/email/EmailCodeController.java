package cn.iocoder.yudao.module.chrome.controller.admin.email;

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

import cn.iocoder.yudao.module.chrome.controller.admin.email.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.email.EmailCodeDO;
import cn.iocoder.yudao.module.chrome.service.email.EmailCodeService;

@Tag(name = "管理后台 - Chrome邮箱验证码")
@RestController
@RequestMapping("/chrome/email-code")
@Validated
public class EmailCodeController {

    @Resource
    private EmailCodeService emailCodeService;

    @PostMapping("/create")
    @Operation(summary = "创建Chrome邮箱验证码")
    @PreAuthorize("@ss.hasPermission('chrome:email-code:create')")
    public CommonResult<Long> createEmailCode(@Valid @RequestBody EmailCodeSaveReqVO createReqVO) {
        return success(emailCodeService.createEmailCode(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新Chrome邮箱验证码")
    @PreAuthorize("@ss.hasPermission('chrome:email-code:update')")
    public CommonResult<Boolean> updateEmailCode(@Valid @RequestBody EmailCodeSaveReqVO updateReqVO) {
        emailCodeService.updateEmailCode(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除Chrome邮箱验证码")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('chrome:email-code:delete')")
    public CommonResult<Boolean> deleteEmailCode(@RequestParam("id") Long id) {
        emailCodeService.deleteEmailCode(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得Chrome邮箱验证码")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('chrome:email-code:query')")
    public CommonResult<EmailCodeRespVO> getEmailCode(@RequestParam("id") Long id) {
        EmailCodeDO emailCode = emailCodeService.getEmailCode(id);
        return success(BeanUtils.toBean(emailCode, EmailCodeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得Chrome邮箱验证码分页")
    @PreAuthorize("@ss.hasPermission('chrome:email-code:query')")
    public CommonResult<PageResult<EmailCodeRespVO>> getEmailCodePage(@Valid EmailCodePageReqVO pageReqVO) {
        PageResult<EmailCodeDO> pageResult = emailCodeService.getEmailCodePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EmailCodeRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出Chrome邮箱验证码 Excel")
    @PreAuthorize("@ss.hasPermission('chrome:email-code:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportEmailCodeExcel(@Valid EmailCodePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<EmailCodeDO> list = emailCodeService.getEmailCodePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "Chrome邮箱验证码.xls", "数据", EmailCodeRespVO.class,
                        BeanUtils.toBean(list, EmailCodeRespVO.class));
    }

}