package cn.iocoder.yudao.module.chrome.controller.admin.user;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

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

import cn.iocoder.yudao.module.chrome.controller.admin.user.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.module.chrome.service.user.UserService;
import cn.iocoder.yudao.module.chrome.service.auth.ChromeAuthService;

@Tag(name = "管理后台 - 用户")
@RestController
@RequestMapping("/chrome/user")
@Validated
public class ChromeUserController {

    @Resource
    private UserService userService;
    
    @Resource
    private ChromeAuthService chromeAuthService;

    @PostMapping("/create")
    @Operation(summary = "创建用户")
    @PreAuthorize("@ss.hasPermission('chrome:user:create')")
    public CommonResult<Long> createUser(@Valid @RequestBody UserSaveReqVO createReqVO) {
        return success(userService.createUser(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户")
    @PreAuthorize("@ss.hasPermission('chrome:user:update')")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody UserSaveReqVO updateReqVO) {
        userService.updateUser(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('chrome:user:delete')")
    public CommonResult<Boolean> deleteUser(@RequestParam("id") Long id) {
        // 剔除用户的登录态
        chromeAuthService.removeUserTokens(id);
        // 删除用户（包含作废订阅和积分）
        userService.deleteUser(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('chrome:user:query')")
    public CommonResult<UserRespVO> getUser(@RequestParam("id") Long id) {
        UserDO user = userService.getUser(id);
        return success(BeanUtils.toBean(user, UserRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户分页")
    @PreAuthorize("@ss.hasPermission('chrome:user:query')")
    public CommonResult<PageResult<UserRespVO>> getUserPage(@Valid UserPageReqVO pageReqVO) {
        PageResult<UserDO> pageResult = userService.getUserPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UserRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户 Excel")
    @PreAuthorize("@ss.hasPermission('chrome:user:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportUserExcel(@Valid UserPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UserDO> list = userService.getUserPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "用户.xls", "数据", UserRespVO.class,
                        BeanUtils.toBean(list, UserRespVO.class));
    }

    @GetMapping("/get-by-email")
    @Operation(summary = "根据邮箱获取用户信息")
    @Parameter(name = "email", description = "邮箱", required = true, example = "user@example.com")
    @PreAuthorize("@ss.hasPermission('chrome:user:query')")
    public CommonResult<UserRespVO> getUserByEmail(@RequestParam("email") String email) {
        UserDO user = userService.getUserByEmail(email);
        return success(BeanUtils.toBean(user, UserRespVO.class));
    }

}