package cn.iocoder.yudao.module.chrome.controller.admin.transaction;

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

import cn.iocoder.yudao.module.chrome.controller.admin.transaction.vo.*;
import cn.iocoder.yudao.module.chrome.dal.dataobject.transaction.CreditsTransactionDO;
import cn.iocoder.yudao.module.chrome.dal.dataobject.user.UserDO;
import cn.iocoder.yudao.module.chrome.dal.mysql.user.ChromeUserMapper;
import cn.iocoder.yudao.module.chrome.service.transaction.CreditsTransactionService;
import cn.iocoder.yudao.module.chrome.service.user.UserService;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

@Tag(name = "管理后台 - 积分交易记录")
@RestController
@RequestMapping("/chrome/credits-transaction")
@Validated
public class CreditsTransactionController {

    @Resource
    private CreditsTransactionService creditsTransactionService;
    @Resource
    private UserService userService;
    @Resource
    private ChromeUserMapper chromeUserMapper;

    @PostMapping("/create")
    @Operation(summary = "创建积分交易记录")
    @PreAuthorize("@ss.hasPermission('chrome:credits-transaction:create')")
    public CommonResult<Long> createCreditsTransaction(@Valid @RequestBody CreditsTransactionSaveReqVO createReqVO) {
        return success(creditsTransactionService.createCreditsTransaction(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新积分交易记录")
    @PreAuthorize("@ss.hasPermission('chrome:credits-transaction:update')")
    public CommonResult<Boolean> updateCreditsTransaction(@Valid @RequestBody CreditsTransactionSaveReqVO updateReqVO) {
        creditsTransactionService.updateCreditsTransaction(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除积分交易记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('chrome:credits-transaction:delete')")
    public CommonResult<Boolean> deleteCreditsTransaction(@RequestParam("id") Long id) {
        creditsTransactionService.deleteCreditsTransaction(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得积分交易记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('chrome:credits-transaction:query')")
    public CommonResult<CreditsTransactionRespVO> getCreditsTransaction(@RequestParam("id") Long id) {
        CreditsTransactionDO creditsTransaction = creditsTransactionService.getCreditsTransaction(id);
        CreditsTransactionRespVO respVO = BeanUtils.toBean(creditsTransaction, CreditsTransactionRespVO.class);
        
        // 填充用户邮箱
        if (creditsTransaction.getUserId() != null) {
            UserDO user = userService.getUser(creditsTransaction.getUserId());
            if (user != null) {
                respVO.setUserEmail(user.getEmail());
            }
        }
        
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得积分交易记录分页")
    @PreAuthorize("@ss.hasPermission('chrome:credits-transaction:query')")
    public CommonResult<PageResult<CreditsTransactionRespVO>> getCreditsTransactionPage(@Valid CreditsTransactionPageReqVO pageReqVO) {
        PageResult<CreditsTransactionDO> pageResult = creditsTransactionService.getCreditsTransactionPage(pageReqVO);
        List<CreditsTransactionRespVO> respList = BeanUtils.toBean(pageResult.getList(), CreditsTransactionRespVO.class);
        
        // 批量填充用户邮箱
        Set<Long> userIds = respList.stream().map(CreditsTransactionRespVO::getUserId).collect(java.util.stream.Collectors.toSet());
        Map<Long, UserDO> userMap = convertMap(chromeUserMapper.selectBatchIds(userIds), UserDO::getId);
        
        respList.forEach(respVO -> {
            UserDO user = userMap.get(respVO.getUserId());
            if (user != null) {
                respVO.setUserEmail(user.getEmail());
            }
        });
        
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出积分交易记录 Excel")
    @PreAuthorize("@ss.hasPermission('chrome:credits-transaction:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCreditsTransactionExcel(@Valid CreditsTransactionPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CreditsTransactionDO> list = creditsTransactionService.getCreditsTransactionPage(pageReqVO).getList();
        List<CreditsTransactionRespVO> respList = BeanUtils.toBean(list, CreditsTransactionRespVO.class);
        
        // 批量填充用户邮箱
        Set<Long> userIds = respList.stream().map(CreditsTransactionRespVO::getUserId).collect(java.util.stream.Collectors.toSet());
        Map<Long, UserDO> userMap = convertMap(chromeUserMapper.selectBatchIds(userIds), UserDO::getId);
        
        respList.forEach(respVO -> {
            UserDO user = userMap.get(respVO.getUserId());
            if (user != null) {
                respVO.setUserEmail(user.getEmail());
            }
        });
        
        // 导出 Excel
        ExcelUtils.write(response, "积分交易记录.xls", "数据", CreditsTransactionRespVO.class, respList);
    }

}