//package cn.iocoder.yudao.module.chrome.service.collect;
//
//import cn.iocoder.yudao.module.chrome.controller.admin.collect.vo.ProductCollectReqVO;
//import cn.iocoder.yudao.module.chrome.controller.admin.collect.vo.ProductCollectRespVO;
//
//import javax.validation.Valid;
//import java.util.List;
//
///**
// * 商品采集 Service 接口
// *
// * @author Jax
// */
//public interface ProductCollectService {
//
//    /**
//     * 采集单个商品信息
//     *
//     * @param userId 用户ID
//     * @param reqVO 采集请求
//     * @return 商品信息
//     */
//    ProductCollectRespVO collectProduct(Long userId, @Valid ProductCollectReqVO reqVO);
//
//    /**
//     * 批量采集商品信息
//     *
//     * @param userId 用户ID
//     * @param reqVOList 采集请求列表
//     * @return 商品信息列表
//     */
//    List<ProductCollectRespVO> batchCollectProduct(Long userId, @Valid List<ProductCollectReqVO> reqVOList);
//
//    /**
//     * 获取用户采集历史
//     *
//     * @param userId 用户ID
//     * @param platform 平台类型（可选）
//     * @param limit 限制数量
//     * @return 采集历史列表
//     */
//    List<ProductCollectRespVO> getCollectHistory(Long userId, String platform, Integer limit);
//
//    /**
//     * 验证商品URL格式
//     *
//     * @param url 商品URL
//     * @param platform 平台类型
//     * @return 是否有效
//     */
//    boolean validateProductUrl(String url, String platform);
//
//    /**
//     * 解析商品ID
//     *
//     * @param url 商品URL
//     * @param platform 平台类型
//     * @return 商品ID
//     */
//    String extractProductId(String url, String platform);
//
//}