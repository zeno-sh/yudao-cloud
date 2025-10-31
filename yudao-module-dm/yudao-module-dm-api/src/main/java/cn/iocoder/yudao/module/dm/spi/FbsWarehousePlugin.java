package cn.iocoder.yudao.module.dm.spi;

import cn.iocoder.yudao.module.dm.dto.*;

/**
 * 海外仓插件接口
 * 定义接口规范，实现类由外部实现
 *
 * @author: Zeno
 * @createTime: 2024/08/29 15:22
 */
public interface FbsWarehousePlugin {

    /**
     * 海外仓公司Id
     *
     * @return
     */
    Integer getCompanyId();

    /**
     * 同步库存
     *
     * @param request
     * @return
     */
    FbsStockResponse syncStock(FbsStockRequest request);

    /**
     * 推送订单
     *
     * @param request
     * @return
     */
    FbsPushOrderResponse pushOrder(FbsPushOrderRequest request);
}
