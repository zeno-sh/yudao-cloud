package cn.iocoder.yudao.module.dm.controller.admin.transaction;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.module.dm.controller.admin.order.vo.OzonOrderSyncReqVO;
import cn.iocoder.yudao.module.dm.dal.dataobject.ozonshopmapping.OzonShopMappingDO;
import cn.iocoder.yudao.module.dm.infrastructure.AuthShopMappingService;
import cn.iocoder.yudao.module.dm.infrastructure.ozon.FinanceManagerService;
import cn.iocoder.yudao.module.dm.service.ozonshopmapping.OzonShopMappingService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.apache.commons.lang3.StringUtils;
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
import java.math.BigDecimal;
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
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

import cn.iocoder.yudao.module.dm.controller.admin.transaction.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.transaction.OzonFinanceTransactionDO;
import cn.iocoder.yudao.module.dm.service.transaction.OzonFinanceTransactionService;

@Tag(name = "管理后台 - 交易记录")
@RestController
@RequestMapping("/dm/ozon-finance-transaction")
@Validated
public class OzonFinanceTransactionController {

    @Resource
    private OzonFinanceTransactionService ozonFinanceTransactionService;
    @Resource
    private FinanceManagerService financeManagerService;
    @Resource
    private AuthShopMappingService authShopMappingService;
    @Resource
    private OzonShopMappingService ozonShopMappingService;

    @PostMapping("/create")
    @Operation(summary = "创建交易记录")
    @PreAuthorize("@ss.hasPermission('dm:ozon-finance-transaction:create')")
    public CommonResult<Long> createOzonFinanceTransaction(@Valid @RequestBody OzonFinanceTransactionSaveReqVO createReqVO) {
        return success(ozonFinanceTransactionService.createOzonFinanceTransaction(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新交易记录")
    @PreAuthorize("@ss.hasPermission('dm:ozon-finance-transaction:update')")
    public CommonResult<Boolean> updateOzonFinanceTransaction(@Valid @RequestBody OzonFinanceTransactionSaveReqVO updateReqVO) {
        ozonFinanceTransactionService.updateOzonFinanceTransaction(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除交易记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:ozon-finance-transaction:delete')")
    public CommonResult<Boolean> deleteOzonFinanceTransaction(@RequestParam("id") Long id) {
        ozonFinanceTransactionService.deleteOzonFinanceTransaction(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得交易记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:ozon-finance-transaction:query')")
    public CommonResult<OzonFinanceTransactionRespVO> getOzonFinanceTransaction(@RequestParam("id") Long id) {
        OzonFinanceTransactionDO ozonFinanceTransaction = ozonFinanceTransactionService.getOzonFinanceTransaction(id);
        return success(BeanUtils.toBean(ozonFinanceTransaction, OzonFinanceTransactionRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得交易记录分页")
    @PreAuthorize("@ss.hasPermission('dm:ozon-finance-transaction:query')")
    public CommonResult<PageResult<OzonFinanceTransactionRespVO>> getOzonFinanceTransactionPage(@Valid OzonFinanceTransactionPageReqVO pageReqVO) {

        String[] clientIds = getClientIds(pageReqVO.getClientIds());
        pageReqVO.setClientIds(clientIds);

        List<OzonShopMappingDO> ozonShopMappingDOList = ozonShopMappingService.batchShopListByClientIds(Arrays.asList(clientIds));
        Map<String, OzonShopMappingDO> shopMappingDOMap = convertMap(ozonShopMappingDOList, OzonShopMappingDO::getClientId);

        PageResult<OzonFinanceTransactionDO> pageResult = ozonFinanceTransactionService.getOzonFinanceTransactionPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, OzonFinanceTransactionRespVO.class, vo -> {
            String services = vo.getServices();
            if (vo.getOperationType().equals("OperationAgentDeliveredToCustomer") && StringUtils.isNotBlank(services)) {
                TypeReference<List<OzonServicesVO>> typeReference = new TypeReference<List<OzonServicesVO>>() {
                };
                List<OzonServicesVO> servicesList = JSON.parseObject(services, typeReference.getType());

                BigDecimal deliverAmount = servicesList.stream().map(OzonServicesVO::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
                vo.setDeliverAmount(deliverAmount);
            }
            MapUtils.findAndThen(shopMappingDOMap, vo.getClientId(), ozonShopMappingDO -> vo.setShopName(ozonShopMappingDO.getShopName()));
            MapUtils.findAndThen(shopMappingDOMap, vo.getClientId(), ozonShopMappingDO -> vo.setPlatform(ozonShopMappingDO.getPlatform()));
        }));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出交易记录 Excel")
    @PreAuthorize("@ss.hasPermission('dm:ozon-finance-transaction:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOzonFinanceTransactionExcel(@Valid OzonFinanceTransactionPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OzonFinanceTransactionDO> list = ozonFinanceTransactionService.getOzonFinanceTransactionPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "交易记录.xls", "数据", OzonFinanceTransactionRespVO.class,
                        BeanUtils.toBean(list, OzonFinanceTransactionRespVO.class));
    }

    @PostMapping("/sync")
    @Operation(summary = "同步交易记录")
    @DataPermission(enable = false)
    public CommonResult<String> syncOzonFinanceTransaction(@Valid @RequestBody OzonTransactionSyncReqVO syncReqVO) {

        String begin = syncReqVO.getOperationDate()[0];
        String end = syncReqVO.getOperationDate()[1];

        long limitDay = DateUtil.betweenDay(DateUtil.parseDate(begin), DateUtil.parseDate(end), true);
        if (limitDay > 31) {
            throw ServiceExceptionUtil.invalidParamException("同步时间不能超过31天");
        }


        String beginDate = DateUtil.format(DateUtil.parseDate(syncReqVO.getOperationDate()[0]), DatePattern.UTC_PATTERN);
        String endDate = DateUtil.format(DateUtil.parseDate(syncReqVO.getOperationDate()[1]), DatePattern.UTC_PATTERN);

        String[] clientIds = getClientIds(syncReqVO.getClientIds());
        for (String clientId : clientIds) {
            financeManagerService.doSync(clientId, beginDate, endDate);
        }
        return success("同步成功");
    }

    private String[] getClientIds(String[] clientIds) {
        if (null == clientIds || clientIds.length == 0) {
            List<OzonShopMappingDO> authShopList = authShopMappingService.getAuthShopList();
            return convertList(authShopList, OzonShopMappingDO::getClientId).toArray(new String[0]);
        }
        return clientIds;
    }

}