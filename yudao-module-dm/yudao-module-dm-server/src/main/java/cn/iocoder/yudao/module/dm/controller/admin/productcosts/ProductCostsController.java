package cn.iocoder.yudao.module.dm.controller.admin.productcosts;

import cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo.PurchasePlanImportVO;
import io.swagger.v3.oas.annotations.Parameters;
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

import cn.iocoder.yudao.module.dm.controller.admin.productcosts.vo.*;
import cn.iocoder.yudao.module.dm.dal.dataobject.productcosts.ProductCostsDO;
import cn.iocoder.yudao.module.dm.service.productcosts.ProductCostsService;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "管理后台 - 产品成本结构")
@RestController
@RequestMapping("/dm/product-costs")
@Validated
public class ProductCostsController {

    @Resource
    private ProductCostsService productCostsService;

    @PostMapping("/create")
    @Operation(summary = "创建产品成本结构")
    @PreAuthorize("@ss.hasPermission('dm:product-costs:create')")
    public CommonResult<Long> createProductCosts(@Valid @RequestBody ProductCostsSaveReqVO createReqVO) {
        return success(productCostsService.createProductCosts(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新产品成本结构")
    @PreAuthorize("@ss.hasPermission('dm:product-costs:update')")
    public CommonResult<Boolean> updateProductCosts(@Valid @RequestBody ProductCostsSaveReqVO updateReqVO) {
        productCostsService.updateProductCosts(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除产品成本结构")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('dm:product-costs:delete')")
    public CommonResult<Boolean> deleteProductCosts(@RequestParam("id") Long id) {
        productCostsService.deleteProductCosts(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得产品成本结构")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('dm:product-costs:query')")
    public CommonResult<ProductCostsRespVO> getProductCosts(@RequestParam("id") Long id) {
        ProductCostsDO productCosts = productCostsService.getProductCosts(id);
        return success(BeanUtils.toBean(productCosts, ProductCostsRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得产品成本结构分页")
    @PreAuthorize("@ss.hasPermission('dm:product-costs:query')")
    public CommonResult<PageResult<ProductCostsRespVO>> getProductCostsPage(@Valid ProductCostsPageReqVO pageReqVO) {
        PageResult<ProductCostsDO> pageResult = productCostsService.getProductCostsPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ProductCostsRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出产品成本结构 Excel")
    @PreAuthorize("@ss.hasPermission('dm:product-costs:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportProductCostsExcel(@Valid ProductCostsPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ProductCostsDO> list = productCostsService.getProductCostsPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "产品成本结构.xls", "数据", ProductCostsRespVO.class,
                BeanUtils.toBean(list, ProductCostsRespVO.class));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得导入模板")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 输出
        ExcelUtils.write(response, "成本结构导入模板2.xlsx", "成本结构", ProductCostsExcelVO.class, new ArrayList<>());
    }


    @PostMapping("/import")
    @Operation(summary = "导入成本结构")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true)
    })
    @PreAuthorize("@ss.hasPermission('dm:product-costs:import')")
    public CommonResult<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        // 使用 FastExcel 读取 Excel 文件
        List<ProductCostsExcelVO> dataList = ExcelUtils.read(file, ProductCostsExcelVO.class);
        
        // 创建监听器并处理数据
        ProductCostsExcelListener listener = new ProductCostsExcelListener();
        List<String> errorMessages = listener.processData(dataList);
        
        Map<String, Object> response = new HashMap<>();
        if (errorMessages.isEmpty()) {
            response.put("status", 0);
            response.put("message", "导入成功");
        } else {
            response.put("status", 500);
            response.put("errors", errorMessages);
        }
        return success(response);
    }

}