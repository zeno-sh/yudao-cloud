package cn.iocoder.yudao.module.chrome.controller.plugin.review;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.chrome.enums.FeatureTypeEnum;
import cn.iocoder.yudao.module.chrome.framework.annotation.ConsumeCredits;
import cn.iocoder.yudao.module.chrome.framework.annotation.RequireSubscription;
import cn.iocoder.yudao.module.chrome.infra.dto.CoupangReviewResponseDTO;
import cn.iocoder.yudao.module.chrome.infra.dto.ReviewQueryParamsDTO;
import cn.iocoder.yudao.module.chrome.infra.service.ProductReviewQueryService;
import cn.iocoder.yudao.module.chrome.controller.plugin.review.vo.ProductReviewQueryReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 产品评论查询控制器
 *
 * @author Jax
 */
@Tag(name = "管理后台 - 产品评论查询")
@RestController
@RequestMapping("/chrome/product-review")
@Validated
@Slf4j
@RequireSubscription(checkExpiration = true, checkCredits = true, minCredits = 1, feature = "产品评论验证")
public class ProductReviewController {

    @Resource
    private ProductReviewQueryService productReviewQueryService;

    @PostMapping("/query")
    @Operation(summary = "查询产品评论")
    @ConsumeCredits(featureType = FeatureTypeEnum.COMMENT_COLLECT, credits = 1, consumeBeforeExecution=false, checkReturnValue=true, description = "评论采集功能")
    public CommonResult<CoupangReviewResponseDTO> queryReviews(
            @RequestBody @Valid ProductReviewQueryReqVO reqVO) {

        // 构建查询参数
        ReviewQueryParamsDTO params = buildQueryParams(reqVO);
//        CoupangReviewResponseDTO coupangReviewResponseDTO = productReviewQueryService.queryProductReviews(params, reqVO.getCookie());
        CoupangReviewResponseDTO coupangReviewResponseDTO = new CoupangReviewResponseDTO();
        return success(coupangReviewResponseDTO);
    }

    /**
     * 构建查询参数
     */
    private ReviewQueryParamsDTO buildQueryParams(ProductReviewQueryReqVO reqVO) {
        // 验证时间范围
        if ("TIME_RANGE".equals(reqVO.getQueryMode())) {
            if (reqVO.getStartTime() == null || reqVO.getEndTime() == null) {
                throw new IllegalArgumentException("时间范围查询必须提供开始时间和结束时间");
            }
            if (reqVO.getStartTime() >= reqVO.getEndTime()) {
                throw new IllegalArgumentException("开始时间必须小于结束时间");
            }
        }

        ReviewQueryParamsDTO.QueryMode queryMode;
        try {
            queryMode = ReviewQueryParamsDTO.QueryMode.valueOf(reqVO.getQueryMode());
        } catch (IllegalArgumentException e) {
            queryMode = ReviewQueryParamsDTO.QueryMode.DEFAULT; // 默认全量查询
        }

        ReviewQueryParamsDTO.ReviewQueryParamsDTOBuilder builder = ReviewQueryParamsDTO.builder()
                .productId(reqVO.getProductId())
                .queryMode(queryMode)
                .size(10)
                .sortBy(reqVO.getSortBy());

        // 根据查询模式设置特定参数
        switch (queryMode) {
            case DEFAULT:
                // 默认查询：固定1页，每页10条，不支持分页
                builder.maxPages(1).page(1);
                break;
            case TIME_RANGE:
                // 时间范围查询：支持分页
                builder.startTime(reqVO.getStartTime())
                       .endTime(reqVO.getEndTime())
                       .page(reqVO.getPage() != null ? reqVO.getPage() : 1);
                break;
            case ALL:
                // 全量查询：支持分页
                builder.page(reqVO.getPage() != null ? reqVO.getPage() : 1);
                if (reqVO.getMaxPages() != null) {
                    builder.maxPages(reqVO.getMaxPages());
                }
                break;
            case EARLIEST:
                // 最早评论查询：固定1页，不支持分页
                builder.maxPages(1).page(1);
                break;
        }

        return builder.build();
    }

}