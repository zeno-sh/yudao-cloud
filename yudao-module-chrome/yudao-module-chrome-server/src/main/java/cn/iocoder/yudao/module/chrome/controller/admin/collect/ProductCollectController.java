//package cn.iocoder.yudao.module.chrome.controller.admin.collect;
//
//import cn.iocoder.yudao.framework.common.pojo.CommonResult;
//import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
//import cn.iocoder.yudao.module.chrome.controller.admin.collect.vo.ProductCollectReqVO;
//import cn.iocoder.yudao.module.chrome.controller.admin.collect.vo.ProductCollectRespVO;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import javax.annotation.Resource;
//import javax.validation.Valid;
//
//import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
//import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
//
///**
// * 商品采集 Controller
// *
// * @author Jax
// */
//@Tag(name = "管理后台 - 商品采集")
//@RestController
//@RequestMapping("/chrome/collect/product")
//@Validated
//@Slf4j
//public class ProductCollectController {
//
//    @Resource
//    private ProductCollectService productCollectService;
//
//    @PostMapping("/collect")
//    @Operation(summary = "采集商品信息")
//    @PreAuthenticated
//    public CommonResult<ProductCollectRespVO> collectProduct(@Valid @RequestBody ProductCollectReqVO reqVO) {
//        Long userId = getLoginUserId();
//        ProductCollectRespVO result = productCollectService.collectProduct(userId, reqVO);
//        return success(result);
//    }
//
//    @PostMapping("/batch-collect")
//    @Operation(summary = "批量采集商品信息")
//    @PreAuthenticated
//    public CommonResult<java.util.List<ProductCollectRespVO>> batchCollectProduct(
//            @Valid @RequestBody java.util.List<ProductCollectReqVO> reqVOList) {
//        Long userId = getLoginUserId();
//        java.util.List<ProductCollectRespVO> result = productCollectService.batchCollectProduct(userId, reqVOList);
//        return success(result);
//    }
//
//    @GetMapping("/history")
//    @Operation(summary = "获取采集历史")
//    @PreAuthenticated
//    public CommonResult<java.util.List<ProductCollectRespVO>> getCollectHistory(
//            @RequestParam(value = "platform", required = false) String platform,
//            @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
//        Long userId = getLoginUserId();
//        java.util.List<ProductCollectRespVO> result = productCollectService.getCollectHistory(userId, platform, limit);
//        return success(result);
//    }
//
//}