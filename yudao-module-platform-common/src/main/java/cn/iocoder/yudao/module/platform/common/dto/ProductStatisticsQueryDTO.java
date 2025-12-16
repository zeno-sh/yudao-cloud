package cn.iocoder.yudao.module.platform.common.dto;

import cn.iocoder.yudao.module.platform.common.enums.ProductOrderByEnum;
import cn.iocoder.yudao.module.platform.common.enums.ProductSearchTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 产品统计查询条件 DTO
 * <p>
 * 多平台产品维度统计数据查询的统一入参
 * </p>
 * <p>
 * 注意：searchType 和 orderBy 使用 String 类型传递，符合行业最佳实践：
 * 前端传 String，后端用枚举处理业务逻辑
 * </p>
 *
 * @author Jax
 */
@Data
@Accessors(chain = true)
@Schema(description = "产品统计查询条件")
public class ProductStatisticsQueryDTO implements Serializable {

    private static final long serialVersionUID = -5652730437564882900L;

    // ==================== 平台筛选条件 ====================

    @Schema(description = "平台ID列表，为空则查询所有平台", example = "[50, 60]")
    private List<Integer> platformIds;

    @Schema(description = "店铺ID列表", example = "[\"123456\"]")
    private List<String> shopIds;

    @Schema(description = "站点ID列表", example = "[\"ATVPDKIKX0DER\"]")
    private List<String> marketplaceIds;

    // ==================== 时间范围 ====================

    @Schema(description = "开始日期", example = "2024-01-01")
    private String startDate;

    @Schema(description = "结束日期", example = "2024-01-31")
    private String endDate;

    // ==================== 搜索条件 ====================

    @Schema(description = "搜索类型: asin/parentAsin/msku/sku", example = "asin")
    private String searchType;

    @Schema(description = "搜索内容列表", example = "[\"B08N5WRWNW\"]")
    private List<String> searchContentList;

    // ==================== 排序条件 ====================

    @Schema(description = "排序字段: saleNum/salePrice/profit/profitRate/adTotalCost/adSalesPrice/acos/refundNum/refundRate", example = "saleNum")
    private String orderBy;

    @Schema(description = "是否降序，默认true", example = "true")
    private Boolean desc = Boolean.TRUE;

    // ==================== 分页参数 ====================

    @Schema(description = "页码，从1开始", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页条数", example = "20")
    private Integer pageSize = 20;

    // ==================== 便捷方法 ====================

    /**
     * 获取搜索类型枚举
     */
    public ProductSearchTypeEnum getSearchTypeEnum() {
        return ProductSearchTypeEnum.getByCode(searchType);
    }

    /**
     * 获取排序字段枚举
     */
    public ProductOrderByEnum getOrderByEnum() {
        return ProductOrderByEnum.getByCode(orderBy);
    }

    /**
     * 获取排序方向字符串
     */
    public String getDescString() {
        return Boolean.TRUE.equals(desc) ? "true" : "false";
    }

}
