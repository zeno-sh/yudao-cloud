package cn.iocoder.yudao.module.dm.controller.admin.statistics.vo;

import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author: Zeno
 * @createTime: 2024/12/27 20:26
 */
@Data
public class ProductStockVO {

    private Long productId;

    private ProductSimpleInfoVO productSimpleInfoVO;

    /**
     * 统计月份 yyyy-MM
     */
    private String date;

    /**
     * 上期结余库存
     */
    private Integer preTotal;
    /**
     * 本期采购
     */
    private Integer currentTotal;
    /**
     * 本期取消
     */
    private Integer currentTotalCancelled;
    /**
     * 本期发货
     */
    private Integer currentTotalDeliver;
    /**
     * 本期结余
     */
    private Integer currentTotalBalance;
    /**
     * 库存货值(不含税)
     */
    private BigDecimal currentTotalPrice;
    /**
     * 库存货值(含税)
     */
    private BigDecimal currentTotalTaxPrice;
}
