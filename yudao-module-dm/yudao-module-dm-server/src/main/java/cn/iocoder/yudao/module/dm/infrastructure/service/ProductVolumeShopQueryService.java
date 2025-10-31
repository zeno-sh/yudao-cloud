package cn.iocoder.yudao.module.dm.infrastructure.service;

import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.ProductVolumeReqVO;
import cn.iocoder.yudao.module.dm.controller.admin.statistics.vo.ProductVolumeRespVO;

import java.util.List;

/**
 * 店铺维度的产品销量统计 Service 接口
 *
 * @author: Zeno
 * @createTime: 2024/08/17 16:30
 */
public interface ProductVolumeShopQueryService {

    /**
     * 按店铺统计商品销量
     * 
     * @param request 查询请求参数
     * @return 店铺商品销量统计列表
     */
    List<ProductVolumeRespVO> queryProductVolumeByShop(ProductVolumeReqVO request);
} 