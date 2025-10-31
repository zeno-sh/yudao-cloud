package cn.iocoder.yudao.module.dm.controller.admin.plan.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author: Zeno
 * @createTime: 2024/04/19 19:04
 */
@Schema(description = "管理后台 - 选品计划 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProductCostVO {

    /**
     * 海外仓成本：卸货+上架+订单操作+送货
     */
    private String fbsTotalPrice;
    /**
     * 海外仓卸货费（15元/立方米）
     */
    private String fbsUnloadPrice;

    /**
     * 海外仓上架费（0.8元/KG）
     */
    private String fbsShelfPrice;

    /**
     * 海外仓订单操作费（产品实重与体积重较大者）
     */
    private String fbsOrderPrice;

    /**
     * 海外仓送货费（5元/个）
     */
    private String fbsDeliveryPrice;

    /**
     * 头程费用
     */
    private String firstLegPrice;

    /**
     * ozon转运费
     */
    private String ozonDeliveryPrice;

    /**
     * ozon最后一公里
     */
    private String lastMilePrice;

    /**
     * 采购费用
     */
    private String purchasePrice;

    /**
     * 广告费用
     */
    private String adPrice;

    /**
     * 货损费用
     */
    private String lossPrice;

    /**
     * 店铺税率
     */
    private String storeRate;

    /**
     * 银行提现税率
     */
    private String bankRate;

    /**
     * 最后一公里税率
     */
    private String lastMileRate;

    /**
     * 平台类目佣金
     */
    private String categoryRate;

    private String categoryCommission;

    /**
     * 广告费率
     */
    private String adRate;

    /**
     * 货损
     */
    private String lossRate;
}
