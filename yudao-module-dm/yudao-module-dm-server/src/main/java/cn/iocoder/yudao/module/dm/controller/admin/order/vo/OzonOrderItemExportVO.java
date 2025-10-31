package cn.iocoder.yudao.module.dm.controller.admin.order.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项导出VO
 */
@Data
public class OzonOrderItemExportVO {
    
    // ========== 订单信息 ==========
    
    @ExcelProperty("门店名称")
    private String shopName;

    @ExcelProperty(value = "平台", converter = DictConvert.class)
    @DictFormat("dm_platform")
    private Integer platform;
    
    @ExcelProperty("发货编号")
    private String postingNumber;
    
    @ExcelProperty(value = "订单状态", converter = DictConvert.class)
    @DictFormat("dm_order_status")
    private String status;
    
    @ExcelProperty("接单时间")
    private LocalDateTime inProcessAt;
    
    @ExcelProperty(value = "订单类型", converter = DictConvert.class)
    @DictFormat("dm_order_type")
    private Integer orderType;
    
    // ========== 订单项信息 ==========
    
    @ExcelProperty("平台门店ID")
    private String clientId;

    @ExcelProperty("平台订单ID")
    private String orderId;

    @ExcelProperty("货号")
    private String offerId;

    @ExcelProperty("数量")
    private Integer quantity;

    @ExcelProperty("价格")
    private BigDecimal price;

    @ExcelProperty("产品图片")
    private String image;
} 