package cn.iocoder.yudao.module.chrome.controller.plugin.trends;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.chrome.enums.FeatureTypeEnum;
import cn.iocoder.yudao.module.chrome.framework.annotation.ConsumeCredits;
import cn.iocoder.yudao.module.chrome.framework.annotation.RequireSubscription;
import cn.iocoder.yudao.module.chrome.infra.dto.CoupangTrendsResponseDTO;
import cn.iocoder.yudao.module.chrome.infra.service.TrendsQueryService;
import cn.iocoder.yudao.module.chrome.controller.plugin.trends.vo.CategoryTrendsQueryReqVO;
import cn.iocoder.yudao.module.chrome.controller.plugin.trends.vo.KeywordTrendsQueryReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 趋势查询控制器
 *
 * @author Jax
 */
@Tag(name = "管理后台 - 趋势查询")
@RestController
@RequestMapping("/chrome/trends")
@Validated
@Slf4j
@RequireSubscription(feature = "类目查询验证")
public class TrendsController {

    @Resource
    private TrendsQueryService trendsQueryService;

    @PostMapping("/category/query")
    @Operation(summary = "查询类目趋势")
    @ConsumeCredits(featureType = FeatureTypeEnum.CATEGORY_ANALYSIS, credits = 5, description = "类目分析功能", consumeBeforeExecution = false, checkReturnValue = true)
    public CommonResult<CoupangTrendsResponseDTO> queryCategoryTrends(
            @RequestBody @Valid CategoryTrendsQueryReqVO reqVO) {

        log.info("开始查询类目趋势，关键词: {}, 类目ID: {}", reqVO.getQuery(), reqVO.getCategoryId());
        
        CoupangTrendsResponseDTO trendsResponse = trendsQueryService.queryCategoryTrends(
            reqVO.getQuery(), reqVO.getCategoryId(), reqVO.getStart(), reqVO.getLimit(), reqVO.getCookie());
        
        return success(trendsResponse);
    }

    @PostMapping("/keyword/query")
    @Operation(summary = "查询关键词趋势")
    @ConsumeCredits(featureType = FeatureTypeEnum.TREND_COLLECT, credits = 5, description = "趋势采集功能", consumeBeforeExecution = false, checkReturnValue = true)
    public CommonResult<CoupangTrendsResponseDTO> queryKeywordTrends(
            @RequestBody @Valid KeywordTrendsQueryReqVO reqVO) {

        log.info("开始查询关键词趋势，关键词: {}", reqVO.getQuery());
        
        CoupangTrendsResponseDTO trendsResponse = trendsQueryService.queryKeywordTrends(
            reqVO.getQuery(), reqVO.getStart(), reqVO.getLimit(), reqVO.getCookie());
        
        return success(trendsResponse);
    }
}
