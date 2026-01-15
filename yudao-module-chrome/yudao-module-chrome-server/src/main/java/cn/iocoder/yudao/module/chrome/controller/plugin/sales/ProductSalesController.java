package cn.iocoder.yudao.module.chrome.controller.plugin.sales;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.chrome.enums.FeatureTypeEnum;
import cn.iocoder.yudao.module.chrome.framework.annotation.ConsumeCredits;
import cn.iocoder.yudao.module.chrome.framework.annotation.RequireSubscription;
import cn.iocoder.yudao.module.chrome.infra.dto.CoupangSalesResponseDTO;
import cn.iocoder.yudao.module.chrome.infra.service.ProductSalesQueryService;
import cn.iocoder.yudao.module.chrome.controller.plugin.sales.vo.ProductSalesQueryReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 产品销量查询控制器
 *
 * @author Jax
 */
@Tag(name = "管理后台 - 产品销量查询")
@RestController
@RequestMapping("/chrome/product-sales")
@Validated
@Slf4j
@RequireSubscription(checkExpiration = true, checkCredits = true, minCredits = 2, feature = "销量采集功能")
public class ProductSalesController {

    @Resource
    private ProductSalesQueryService productSalesQueryService;

    @PostMapping("/query")
    @Operation(summary = "查询产品销量")
    @ConsumeCredits(featureType = FeatureTypeEnum.SALES_COLLECT, credits = 2, description = "销量采集功能", consumeBeforeExecution = false, checkReturnValue = true)
    public CommonResult<CoupangSalesResponseDTO.ProductSalesInfo> querySales(
            @RequestBody @Valid ProductSalesQueryReqVO reqVO) {

        log.info("开始查询产品销量，关键词: {}", reqVO.getKeyword());

        CoupangSalesResponseDTO.ProductSalesInfo salesInfo = productSalesQueryService.queryProductSales(
                reqVO.getKeyword(), reqVO.getCookie());

        return success(salesInfo);
    }
}
