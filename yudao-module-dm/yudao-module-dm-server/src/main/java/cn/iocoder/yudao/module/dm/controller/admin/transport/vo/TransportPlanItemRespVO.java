package cn.iocoder.yudao.module.dm.controller.admin.transport.vo;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.ContentStyle;
import cn.idev.excel.enums.poi.HorizontalAlignmentEnum;
import cn.idev.excel.enums.poi.VerticalAlignmentEnum;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.dm.controller.admin.product.vo.ProductSimpleInfoVO;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.net.URL;

@Schema(description = "管理后台 - 头程计划 Response VO")
@Data
@ExcelIgnoreUnannotated
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER)
@ContentRowHeight(60) // 行高
public class TransportPlanItemRespVO {

    private Integer id;
    /**
     * 头程计划ID
     */
    @ExcelIgnore
    private Long planId;
    /**
     * 采购计划单号
     */
    @ExcelProperty(value = "采购计划单号", index = 0)
    private String planNumber;
    /**
     * 采购单号
     */
    private String poNumber;
    /**
     * 导出使用
     */
    @ExcelProperty(value = "图片", index = 1, converter = UrlImageConverter.class)
    @ColumnWidth(11) // 列宽
    private String productImage;
    /**
     * 导出使用
     */
    @ExcelProperty(value = "Sku", index = 2)
    private String productSkuId;
    /**
     * 导出使用
     */
    @ExcelProperty(value = "产品名称", index = 3)
    private String productSkuName;
    /**
     * 产品Id
     */
    @ExcelProperty(value = "本地产品Id", index = 4)
    private Long productId;
    /**
     * 产品信息
     */
    private ProductSimpleInfoVO productSimpleInfo;
    /**
     * 采购单详情ID
     */
    @ExcelIgnore
    private Long purchaseOrderItemId;
    /**
     * 发运数量
     */
    @ExcelProperty(value = "发运数量", index = 5)
    private Integer quantity;
    /**
     * pcs
     */
    @ExcelProperty(value = "pcs", index = 6)
    @ColumnWidth(5) // 列宽
    private Integer pcs;
    /**
     * 箱数
     */
    @ExcelProperty(value = "箱数", index = 7)
    private Integer numberOfBox;
    /**
     * 体积
     */
    @ExcelProperty(value = "体积", index = 8)
    private BigDecimal volume;
    /**
     * 重量
     */
    @ExcelProperty(value = "重量", index = 9)
    private BigDecimal weight;
    /**
     * 运营
     */
    @ExcelProperty(value = "运营", index = 10)
    private String operatorName;
    /**
     * 总金额
     */
    @ExcelProperty(value = "成本价", index = 11)
    private BigDecimal totalAmount;
    /**
     * 含税总金额
     */
    @ExcelProperty(value = "含税价", index = 12)
    private BigDecimal totalTaxAmount;
    /**
     * 海外仓入库单号
     */
    @ExcelProperty(value = "海外仓入库单号", index = 13)
    private String overseaLocationCheckinId;
    /**
     * 上架状态
     *
     * 枚举 {@link TODO dm_first_choice 对应的类}
     */
    @ExcelProperty(value = "上架状态", index = 14, converter = DictConvert.class)
    @DictFormat("dm_first_choice")
    private String shelfStatus;
}