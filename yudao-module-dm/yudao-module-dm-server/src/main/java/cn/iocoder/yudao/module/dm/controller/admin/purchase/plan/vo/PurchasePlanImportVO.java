package cn.iocoder.yudao.module.dm.controller.admin.purchase.plan.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.format.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * @author: Zeno
 * @createTime: 2024/06/02 17:55
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class PurchasePlanImportVO {

    @ExcelProperty("Sku")
    private String skuId;

    @ExcelProperty("产品名称")
    private String skuName;

    @ExcelProperty("采购数量")
    private String quantity;

    @ExcelProperty("期望到货时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private String arrivedDate;
}
